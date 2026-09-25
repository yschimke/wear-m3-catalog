package ee.schimke.wearm3catalog.remoteuibuilder

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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import ee.schimke.composeai.uibuilder.CanvasDocumentHost
import ee.schimke.composeai.uibuilder.CanvasMode
import ee.schimke.composeai.uibuilder.RenderCanvasNode
import ee.schimke.composeai.uibuilder.UiBuilderInspectionCollector
import ee.schimke.composeai.uibuilder.UiBuilderSemanticActionController
import ee.schimke.composeai.uibuilder.applyCanvasModifier
import ee.schimke.composeai.uibuilder.protocol.CanvasAdapterMappingV1
import ee.schimke.composeai.uibuilder.protocol.UiBuilderRendererSurfaceModeV2
import ee.schimke.composeai.uibuilder.startCatalogRenderer
import ee.schimke.wearcmp.port.LocalWearDeviceConfiguration
import ee.schimke.wearcmp.port.WearDeviceConfiguration
import ee.schimke.wearm3catalog.uibuilder.LocalWearWidgetHostShape
import ee.schimke.wearm3catalog.uibuilder.foundationCanvasAdapters
import ee.schimke.wearm3catalog.uibuilder.materialCanvasAdapters
import ee.schimke.wearm3catalog.uibuilder.resolveWearColor
import ee.schimke.wearm3catalog.uibuilder.wearCanvasAdapters
import ee.schimke.wearm3catalog.uibuilder.wearScreenAdapters
import ee.schimke.wearm3catalog.uibuilder.wearTextAdapters
import ee.schimke.wearm3catalog.uibuilder.wearWidgetCanvasAdapters
import ee.schimke.wearm3catalog.uibuilder.wearWidgetHostShape
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.wasm.awaitSkiko

private val runtimeAdapters =
  foundationCanvasAdapters +
    materialCanvasAdapters +
    wearCanvasAdapters +
    wearScreenAdapters +
    wearTextAdapters +
    wearWidgetCanvasAdapters

private val runtimePolicy by lazy {
  Json { ignoreUnknownKeys = true }.parseToJsonElement(catalogUiBuilderPolicyJson).jsonObject
}

private val adapterIds: Map<String, String> by lazy {
  buildMap {
    listOf("builtins", "components").forEach { section ->
      runtimePolicy[section]?.jsonObject?.forEach { (componentId, element) ->
        element.jsonObject["canvas"]?.jsonPrimitive?.contentOrNull?.let { put(componentId, it) }
      }
    }
  }
}

private val adapterMappings: Map<String, CanvasAdapterMappingV1> by lazy {
  val json = Json { ignoreUnknownKeys = true }
  buildMap {
    runtimePolicy["components"]?.jsonObject?.forEach { (componentId, element) ->
      element.jsonObject["canvasMapping"]?.let { mapping ->
        put(componentId, json.decodeFromJsonElement(CanvasAdapterMappingV1.serializer(), mapping))
      }
    }
  }
}

@OptIn(InternalSkikoApi::class)
fun main() {
  val actions = UiBuilderSemanticActionController()
  awaitSkiko.then(
    onFulfilled = {
      startCatalogRenderer(actions) { document, surface, renderSessionId, onInspectionSnapshot ->
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
          LocalWearWidgetHostShape provides document.wearWidgetHostShape(),
        ) {
          MaterialTheme {
            // The viewport's canvas starts opaque white, and whatever the design leaves uncovered
            // (a widget frame's rounded corners, a pane wider than the frame) showed as a white box
            // in the editor. The editor draws its own backdrop behind this frame, so the runtime
            // clears to transparent and paints only the design.
            Box(
              Modifier.fillMaxSize().drawWithContent {
                drawRect(Color.Transparent, blendMode = BlendMode.Clear)
                drawContent()
              }
            )
            Box(Modifier.requiredSize(surface.widthDp.dp, surface.heightDp.dp)) {
              if (surface.mode == UiBuilderRendererSurfaceModeV2.AUTHORING_UNROLLED) {
                SemanticCanvas(
                  document = document,
                  mode = mode,
                  density = density,
                  renderSessionId = renderSessionId,
                  actions = actions,
                  onInspectionSnapshot = onInspectionSnapshot,
                )
              } else {
                RemoteM3DevicePreview(
                  document = document,
                  widthDp = surface.widthDp,
                  heightDp = surface.heightDp,
                  onReady = {
                    // DEVICE is read-only, so the host only needs document identity to correlate
                    // the frame. Bounds and semantic actions belong to AUTHORING_UNROLLED and must
                    // not be fabricated from the player's pixels.
                    onInspectionSnapshot(UiBuilderInspectionCollector(document).snapshot())
                  },
                )
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

@Composable
private fun SemanticCanvas(
  document: ee.schimke.composeai.uibuilder.UiBuilderDocument,
  mode: CanvasMode,
  density: Density,
  renderSessionId: String,
  actions: UiBuilderSemanticActionController,
  onInspectionSnapshot: (ee.schimke.composeai.uibuilder.UiBuilderInspectionSnapshot) -> Unit,
  modifier: Modifier = Modifier,
) {
  CanvasDocumentHost(
    document = document,
    adapterIds = adapterIds,
    adapterMappings = adapterMappings,
    mode = mode,
    density = density,
    modifier = modifier.fillMaxSize(),
    renderSessionId = renderSessionId,
    runtimeActionController = actions,
    onInspectionSnapshot = onInspectionSnapshot,
    rootModifier = { Modifier.align(Alignment.TopStart) },
  ) { entry, rootModifier ->
    RenderCanvasNode(
      entry = entry,
      registry = runtimeAdapters,
      modifier = rootModifier,
      applyModifier = { current, value ->
        current.applyCanvasModifier(
          value = value,
          mode = mode,
          resolveColor = { resolveWearColor(it) },
          resolveShape = ::resolveWearShape,
        )
      },
      missingComponent = { label, next -> UnsupportedComponent(label, next) },
    ) {
      // A picture is the design's own content rather than a catalog component: drawn from the
      // bytes the editor inlines, since this sandboxed frame cannot fetch the design's assets.
      when (node.componentId) {
        "asset/image" -> DesignAssetImage(document, node, prepared.modifier)
        "shape/linear-gradient" ->
          DesignLinearGradient(node, prepared.modifier) { resolveWearColor(it) }
        else -> UnsupportedComponent(node.componentId, prepared.modifier)
      }
    }
  }
}

@Composable
private fun resolveWearShape(value: String?): Shape =
  RoundedCornerShape(namedShapeRadiusDp(value).dp)

@Composable
private fun UnsupportedComponent(label: String, modifier: Modifier) {
  Box(modifier.background(MaterialTheme.colorScheme.errorContainer).padding(8.dp)) {
    Text(label, color = MaterialTheme.colorScheme.onErrorContainer)
  }
}
