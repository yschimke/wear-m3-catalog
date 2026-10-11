package ee.schimke.wearm3catalog.typed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.uibuilder.ProvideUiBuilderFonts
import ee.schimke.composeai.uibuilder.UiBuilderFontRegistry
import ee.schimke.composeai.uibuilder.parseVendoredFontManifest
import ee.schimke.composeai.uibuilder.protocol.UiBuilderRendererSurfaceModeV2
import ee.schimke.composeai.uibuilder.renderer.sdk.CATALOG_RUNTIME_CAPABILITY_HORIZONTAL_UNROLL
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasDocumentHost
import ee.schimke.composeai.uibuilder.renderer.sdk.CanvasMode
import ee.schimke.composeai.uibuilder.renderer.sdk.RenderCanvasNode
import ee.schimke.composeai.uibuilder.renderer.sdk.UiBuilderSemanticActionController
import ee.schimke.composeai.uibuilder.renderer.sdk.applyCanvasModifier
import ee.schimke.composeai.uibuilder.renderer.sdk.catalogRuntimeFontRegistry
import ee.schimke.composeai.uibuilder.renderer.sdk.startCatalogRenderer
import ee.schimke.wearcmp.port.LocalWearDeviceConfiguration
import ee.schimke.wearcmp.port.WearDeviceConfiguration
import ee.schimke.wearcmp.port.WearFonts
import ee.schimke.wearm3catalog.uibuilder.foundationCanvasAdapters
import ee.schimke.wearm3catalog.uibuilder.resolveWearColor
import ee.schimke.wearm3catalog.uibuilder.wearScreenStructureAdapters
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.wasm.awaitSkiko

// The typed pilot runtime: the production structure adapters (screen frame, scrolling list,
// foundation layouts) and the typed registrations in place of every component they cover. Its ids
// come from the typed definitions themselves, never from a second copy of the policy.
private val typedCatalog by lazy {
  TypedWearCatalog(Json.decodeFromString(ComponentRecordFile.serializer(), typedWearRecordsJson))
}

private val runtimeAdapters by lazy {
  foundationCanvasAdapters + wearScreenStructureAdapters + typedWearCanvasAdapters(typedCatalog)
}

// Every other id is its own adapter id; only the screen builtin draws through the frame.
private val adapterIds: Map<String, String> by lazy {
  mapOf("wear-m3/screen-scaffold" to "frame/round-screen") +
    typedCatalog.runtimeAdapters.associate { it.id to it.id }
}

/** Typed components that pass their own click callback, so the SDK adds no second clickable. */
private val clickHandlers: Set<String> by lazy {
  typedCatalog.runtimeAdapters
    .filter { definition -> definition.component.parameters.any { it.name == "onClick" } }
    .map { it.id }
    .toSet()
}

@OptIn(InternalSkikoApi::class)
fun main() {
  val actions = UiBuilderSemanticActionController()
  // One registry for the page: a design's theme typefaces, from the runtime's own `fonts/` and,
  // for anything it does not ship, the host's Google Fonts route.
  val fonts = catalogRuntimeFontRegistry()
  awaitSkiko.then(
    onFulfilled = {
      MainScope().launch {
        fonts.registerWearDeviceFace()
        startCatalogRenderer(
          actions,
          capabilities = setOf(CATALOG_RUNTIME_CAPABILITY_HORIZONTAL_UNROLL),
        ) { document, surface, renderSessionId, onInspectionSnapshot ->
          val hostDensity = LocalDensity.current
          val density =
            Density(
              density = surface.density,
              fontScale =
                document.environment["fontScale"]
                  ?.let { it as? JsonPrimitive }
                  ?.contentOrNull
                  ?.toFloatOrNull()
                  ?.takeIf { it.isFinite() && it > 0f } ?: hostDensity.fontScale,
            )
          val mode =
            if (surface.mode == UiBuilderRendererSurfaceModeV2.AUTHORING_UNROLLED)
              CanvasMode.AuthoringUnrolled
            else CanvasMode.Device
          val layoutDirection =
            if (document.environment["layoutDirection"]?.jsonPrimitive?.contentOrNull == "rtl")
              LayoutDirection.Rtl
            else LayoutDirection.Ltr
          CompositionLocalProvider(
            LocalDensity provides density,
            LocalLayoutDirection provides layoutDirection,
            LocalWearDeviceConfiguration provides
              WearDeviceConfiguration(
                isScreenRound = true,
                screenWidthDp = surface.widthDp.toInt(),
                screenHeightDp = surface.heightDp.toInt(),
              ),
          ) {
            ProvideUiBuilderFonts(fonts) {
              MaterialTheme {
                Box(Modifier.requiredSize(surface.widthDp.dp, surface.heightDp.dp)) {
                  CanvasDocumentHost(
                    document = document,
                    adapterIds = adapterIds,
                    adapterMappings = emptyMap(),
                    mode = mode,
                    density = density,
                    modifier = Modifier.fillMaxSize(),
                    renderSessionId = renderSessionId,
                    runtimeActionController = actions,
                    onInspectionSnapshot = onInspectionSnapshot,
                    rootModifier = { entry ->
                      if (entry.adapterId == "frame/round-screen")
                        Modifier.align(Alignment.TopCenter)
                      else Modifier
                    },
                  ) { entry, rootModifier ->
                    RenderCanvasNode(
                      entry = entry,
                      registry = runtimeAdapters,
                      handlesClick = { it.componentId in clickHandlers },
                      modifier = rootModifier,
                      applyModifier = { current, value ->
                        current.applyCanvasModifier(
                          value = value,
                          mode = mode,
                          unrolledHorizontally = unrolledHorizontally,
                          resolveColor = { resolveWearColor(it) },
                          resolveShape = ::resolveWearShape,
                        )
                      },
                      missingComponent = { label, next ->
                        UnsupportedComponent("Typed pilot: $label", next)
                      },
                    ) {
                      UnsupportedComponent(node.componentId, prepared.modifier)
                    }
                  }
                }
              }
            }
          }
        }
      }
      null
    },
    onRejected = { error("Skiko initialization failed: $it") },
  )
}

/**
 * Hand the Wear port the watch's system face, `roboto-flex`, from this runtime's own `fonts/`.
 *
 * Wear Material 3 sets every type role in `DeviceFontFamilyName("roboto-flex")`, which the port
 * resolves through [WearFonts] once, when the type scale is first read. No browser has a font by
 * that name, so unless it is registered before anything composes, every screen this runtime draws
 * is set in the browser's fallback sans — wider than Roboto Flex, enough to wrap labels that fit on
 * a watch. The editor's own renderer does the same (`registerWearDeviceFonts`).
 */
private suspend fun UiBuilderFontRegistry.registerWearDeviceFace() {
  if (WearFonts.isRegistered(WearFonts.RobotoFlex)) return
  runCatching {
    val manifest = parseVendoredFontManifest(readManifestText())
    val file =
      manifest.families.firstOrNull { it.name == "Roboto Flex" }?.fonts?.firstOrNull() ?: return
    WearFonts.register(WearFonts.RobotoFlex, readFontBytes(file.file))
  }
}

@Composable
private fun resolveWearShape(value: String?): Shape =
  when (value) {
    "large" -> RoundedCornerShape(26.dp)
    "medium" -> RoundedCornerShape(16.dp)
    "small" -> RoundedCornerShape(8.dp)
    else -> RoundedCornerShape(value?.toFloatOrNull()?.dp ?: 0.dp)
  }

@Composable
private fun UnsupportedComponent(label: String, modifier: Modifier) {
  Box(modifier.background(MaterialTheme.colorScheme.errorContainer).padding(8.dp)) {
    Text(label, color = MaterialTheme.colorScheme.onErrorContainer)
  }
}
