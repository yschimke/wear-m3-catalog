package ee.schimke.wearm3catalog.remoteuibuilder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.remote.creation.compose.action.combinedAction
import androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo
import androidx.compose.remote.creation.compose.capture.captureCommonRemoteDocument
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.fillMaxHeight
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.height
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.asRemoteTextUnit
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.text.RemoteTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteCard
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.rcplayer.compose.RcComposePlayer
import ee.schimke.composeai.rcplayer.compose.RcPlayerTheme
import ee.schimke.composeai.rcplayer.protocol.RcDocument
import ee.schimke.composeai.rcplayer.protocol.RcDocumentCodec
import ee.schimke.composeai.uibuilder.CanvasRenderNode
import ee.schimke.composeai.uibuilder.CanvasRenderTree
import ee.schimke.composeai.uibuilder.UiBuilderDocument
import ee.schimke.composeai.uibuilder.UiBuilderNode
import ee.schimke.composeai.uibuilder.WearWidgetHostShape
import ee.schimke.composeai.uibuilder.WearWidgetScaffoldSize
import ee.schimke.composeai.uibuilder.hostSpec
import ee.schimke.wearm3catalog.uibuilder.WearWidgetContainerFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull

/** Real Remote M3 creation and playback for the read-only browser device surface. */
@Composable
internal fun RemoteM3DevicePreview(
  document: UiBuilderDocument,
  widthDp: Float,
  heightDp: Float,
  onReady: () -> Unit,
) {
  val root = document.roots.singleOrNull()?.let(document.nodes::get)
  val widgetSize = root?.widgetSize()
  val hostSpec = widgetSize?.hostSpec(WearWidgetHostShape.Default)
  val horizontalPadding = root?.number("horizontalPaddingDp") ?: hostSpec?.horizontalPaddingDp ?: 0f
  val verticalPadding = root?.number("verticalPaddingDp") ?: hostSpec?.verticalPaddingDp ?: 0f
  val cornerRadius = root?.number("cornerRadiusDp") ?: hostSpec?.cornerRadiusDp ?: 0f
  val contentWidth =
    hostSpec?.let { (it.frameWidthDp - 2f * horizontalPadding).coerceAtLeast(0f) } ?: widthDp
  val contentHeight =
    hostSpec?.let { (it.frameHeightDp - 2f * verticalPadding).coerceAtLeast(0f) } ?: heightDp
  var captured by
    remember(document, contentWidth, contentHeight) {
      mutableStateOf<Result<CapturedRemoteDocuments>?>(null)
    }
  LaunchedEffect(document, contentWidth, contentHeight) {
    val next = runCatching {
      val content =
        captureDocument(contentWidth, contentHeight) {
          RemoteDocumentTree(document).Render(widgetSize != null)
        }
      val background = hostSpec?.let { spec ->
        root
          ?.takeIf { it.slots["background"].orEmpty().isNotEmpty() }
          ?.let {
            captureDocument(spec.frameWidthDp.toFloat(), spec.frameHeightDp.toFloat()) {
              RemoteDocumentTree(document).RenderRootSlot("background")
            }
          }
      }
      CapturedRemoteDocuments(content = content, background = background)
    }
    withContext(Dispatchers.Main) {
      captured = next
      onReady()
    }
  }

  when (val result = captured) {
    null -> Box(Modifier.fillMaxSize()) { Text("Building Remote M3 preview…") }
    else -> {
      result.fold(
        onSuccess = { documents ->
          if (hostSpec == null || root == null) {
            RcComposePlayer(
              document = documents.content,
              theme = document.playerTheme(),
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            val background = root.color("background") ?: Color(39, 36, 48)
            WearWidgetContainerFrame(
              contentWidthDp = contentWidth,
              contentHeightDp = contentHeight,
              horizontalPaddingDp = horizontalPadding,
              verticalPaddingDp = verticalPadding,
              cornerRadiusDp = cornerRadius,
              background = background,
              backgroundContent = { shape ->
                documents.background?.let {
                  RcComposePlayer(
                    document = it,
                    theme = document.playerTheme(),
                    modifier = Modifier.fillMaxSize().clip(shape),
                  )
                }
              },
            ) {
              RcComposePlayer(
                document = documents.content,
                theme = document.playerTheme(),
                modifier = Modifier.fillMaxSize(),
              )
            }
          }
        },
        onFailure = { Text("Remote M3 preview failed: ${it.message ?: it::class.simpleName}") },
      )
    }
  }
}

private data class CapturedRemoteDocuments(
  val content: RcDocument,
  val background: RcDocument?,
)

private suspend fun captureDocument(
  widthDp: Float,
  heightDp: Float,
  content: @Composable @RemoteComposable () -> Unit,
): RcDocument {
  val bytes =
    captureCommonRemoteDocument(RemoteCreationDisplayInfo(widthDp.toInt(), heightDp.toInt(), 160)) {
      RemoteMaterialTheme { content() }
    }
  return RcDocumentCodec.decode(bytes)
}

private class RemoteDocumentTree(private val document: UiBuilderDocument) {
  private val tree =
    CanvasRenderTree(
      document = document,
      state = document.initialState(),
      adapterIds = emptyMap(),
      adapterMappings = emptyMap(),
    )

  @Composable
  @RemoteComposable
  fun Render(skipWidgetFrame: Boolean) {
    document.roots.forEach { id ->
      tree.root(id)?.let { root ->
        if (skipWidgetFrame && root.node.widgetSize() != null) {
          root.slot("content").forEach { RenderNode(it) }
        } else {
          RenderNode(root)
        }
      }
    }
  }

  @Composable
  @RemoteComposable
  fun RenderRootSlot(slotName: String) {
    document.roots.singleOrNull()?.let(tree::root)?.slot(slotName)?.forEach { RenderNode(it) }
  }

  @Composable
  @RemoteComposable
  private fun RenderNode(entry: CanvasRenderNode) {
    val node = entry.node
    val modifier = node.remoteModifier()
    when (node.componentId) {
      "remote-m3/widget-container-small",
      "remote-m3/widget-container-large" -> entry.slot("content").forEach { RenderNode(it) }
      "layout/box" ->
        RemoteBox(modifier = modifier, contentAlignment = node.boxAlignment()) {
          entry.slot("children").forEach { RenderNode(it) }
        }
      "layout/column" ->
        RemoteColumn(
          modifier = modifier,
          verticalArrangement = node.verticalArrangement(),
          horizontalAlignment = node.horizontalAlignment(),
        ) {
          entry.slot("children").forEach { RenderNode(it) }
        }
      "layout/row" ->
        RemoteRow(
          modifier = modifier,
          horizontalArrangement = node.horizontalArrangement(),
          verticalAlignment = node.verticalAlignment(),
        ) {
          entry.slot("children").forEach { RenderNode(it) }
        }
      "m3/text",
      "remote-m3/remote-text" ->
        RemoteText(
          text = node.string("text").rs,
          modifier = modifier,
          color = node.remoteColor("color"),
          fontSize =
            node.number("fontSize")?.sp?.asRemoteTextUnit()
              ?: node.number("fontSizeSp")?.sp?.asRemoteTextUnit(),
          textAlign = node.textAlign(),
          maxLines = node.integer("maxLines") ?: Int.MAX_VALUE,
          style = node.textStyle(),
        )
      "remote-m3/remote-button" ->
        RemoteButton(
          onClick = combinedAction(),
          modifier = modifier,
          enabled =
            node.boolean("enabled", true).let {
              androidx.compose.remote.creation.compose.state.RemoteBoolean(it)
            },
        ) {
          entry.slot("content").forEach { RenderNode(it) }
        }
      "remote-m3/remote-card" ->
        RemoteCard(onClick = combinedAction(), modifier = modifier) {
          entry.slot("content").forEach { RenderNode(it) }
        }
      else ->
        RemoteText(
          text = "Unsupported: ${node.componentId}".rs,
          modifier = modifier,
          color = Color.Red.rc,
          fontSize = 10.sp.asRemoteTextUnit(),
        )
    }
  }
}

private fun UiBuilderDocument.initialState(): Map<String, String?> =
  stateVariables.mapValues { (_, declaration) ->
    ((declaration as? JsonObject)?.get("initialValue") as? JsonPrimitive)?.contentOrNull
  }

private fun UiBuilderNode.widgetSize(): WearWidgetScaffoldSize? =
  WearWidgetScaffoldSize.entries.firstOrNull { it.componentId == componentId }

private fun UiBuilderDocument.playerTheme(): RcPlayerTheme =
  when ((environment["theme"] as? JsonPrimitive)?.contentOrNull) {
    "light" -> RcPlayerTheme.Light
    "dark" -> RcPlayerTheme.Dark
    else -> RcPlayerTheme.System
  }

private fun UiBuilderNode.value(name: String): JsonPrimitive? =
  ((properties[name] as? JsonObject)?.get("value") as? JsonPrimitive)

private fun UiBuilderNode.string(name: String): String = value(name)?.contentOrNull.orEmpty()

private fun UiBuilderNode.number(name: String): Float? = value(name)?.floatOrNull

private fun UiBuilderNode.integer(name: String): Int? = value(name)?.intOrNull

private fun UiBuilderNode.boolean(name: String, fallback: Boolean): Boolean =
  value(name)?.booleanOrNull ?: fallback

@Composable
private fun UiBuilderNode.remoteModifier(): RemoteModifier {
  var result: RemoteModifier = RemoteModifier
  modifiers.forEach { element ->
    val modifier = element as? JsonObject ?: return@forEach
    val type = (modifier["type"] as? JsonPrimitive)?.contentOrNull ?: return@forEach
    fun number(vararg names: String): Float? = names.firstNotNullOfOrNull {
      (modifier[it] as? JsonPrimitive)?.floatOrNull
    }
    result =
      when (type) {
        "fillMaxSize" -> result.fillMaxSize()
        "fillMaxWidth" -> result.fillMaxWidth()
        "fillMaxHeight" -> result.fillMaxHeight()
        "width" -> number("widthDp", "value")?.let { result.width(it.rdp) } ?: result
        "height" -> number("heightDp", "value")?.let { result.height(it.rdp) } ?: result
        "size" -> number("sizeDp", "value")?.let { result.size(it.rdp) } ?: result
        "padding" -> {
          val all = number("allDp", "value")
          if (all != null) result.padding(all.rdp)
          else
            result.padding(
              start = (number("startDp", "horizontalDp") ?: 0f).rdp,
              top = (number("topDp", "verticalDp") ?: 0f).rdp,
              end = (number("endDp", "horizontalDp") ?: 0f).rdp,
              bottom = (number("bottomDp", "verticalDp") ?: 0f).rdp,
            )
        }
        "background" ->
          (modifier["color"] as? JsonPrimitive)?.contentOrNull?.remoteColor()?.let {
            result.background(it)
          } ?: result
        else -> error("Unsupported Remote Compose modifier '$type' on ${componentId}")
      }
  }
  return result
}

@Composable
private fun UiBuilderNode.remoteColor(name: String): RemoteColor? =
  value(name)?.contentOrNull?.remoteColor()

@Composable
private fun String.remoteColor(): RemoteColor? =
  parseColor()?.rc
    ?: when (this) {
      "background" -> RemoteMaterialTheme.colorScheme.background
      "surface" -> RemoteMaterialTheme.colorScheme.surfaceContainer
      "surfaceContainer" -> RemoteMaterialTheme.colorScheme.surfaceContainer
      "surfaceContainerLow" -> RemoteMaterialTheme.colorScheme.surfaceContainerLow
      "surfaceContainerHigh" -> RemoteMaterialTheme.colorScheme.surfaceContainerHigh
      "surfaceContainerHighest" -> RemoteMaterialTheme.colorScheme.surfaceContainerHigh
      "primary" -> RemoteMaterialTheme.colorScheme.primary
      "onPrimary" -> RemoteMaterialTheme.colorScheme.onPrimary
      "tertiary" -> RemoteMaterialTheme.colorScheme.tertiary
      "onTertiary" -> RemoteMaterialTheme.colorScheme.onTertiary
      "onSurface" -> RemoteMaterialTheme.colorScheme.onSurface
      "onSurfaceVariant" -> RemoteMaterialTheme.colorScheme.onSurfaceVariant
      "outlineVariant" -> RemoteMaterialTheme.colorScheme.outlineVariant
      "transparent" -> Color.Transparent.rc
      else -> null
    }

private fun UiBuilderNode.color(name: String): Color? =
  value(name)?.contentOrNull?.let { word ->
    word.parseColor()
      ?: when (word) {
        "background" -> Color(0xFF1A1110)
        "surface" -> Color(0xFF1A1110)
        "surfaceContainer" -> Color(0xFF2A2220)
        "primary" -> Color(0xFFFFB4A8)
        "onPrimary" -> Color(0xFF561E18)
        "onSurface" -> Color(0xFFF1DFDB)
        "onSurfaceVariant" -> Color(0xFFD8C2BD)
        "tertiary" -> Color(0xFFE7C089)
        "onTertiary" -> Color(0xFF442B03)
        "transparent" -> Color.Transparent
        else -> null
      }
  }

private fun String.parseColor(): Color? = takeIf {
  it.startsWith("#")
}
  ?.removePrefix("#")
  ?.let { hex ->
    runCatching {
      when (hex.length) {
        6 -> (0xFF000000u or hex.toUInt(16)).toLong()
        8 -> hex.toUInt(16).toLong()
        else -> return null
      }
    }
      .getOrNull()
  }
  ?.let(::Color)

private fun UiBuilderNode.textAlign(): TextAlign? =
  when (string("textAlign").ifEmpty { string("alignment") }) {
    "center" -> TextAlign.Center
    "end" -> TextAlign.End
    "justify" -> TextAlign.Justify
    "start" -> TextAlign.Start
    else -> null
  }

@Composable
private fun UiBuilderNode.textStyle(): RemoteTextStyle =
  when (string("style")) {
    "displayLarge" -> RemoteMaterialTheme.typography.displayLarge
    "displayMedium" -> RemoteMaterialTheme.typography.displayMedium
    "displaySmall" -> RemoteMaterialTheme.typography.displaySmall
    "titleLarge" -> RemoteMaterialTheme.typography.titleLarge
    "titleMedium" -> RemoteMaterialTheme.typography.titleMedium
    "titleSmall" -> RemoteMaterialTheme.typography.titleSmall
    "labelLarge" -> RemoteMaterialTheme.typography.labelLarge
    "labelMedium" -> RemoteMaterialTheme.typography.labelMedium
    "labelSmall" -> RemoteMaterialTheme.typography.labelSmall
    "bodyLarge" -> RemoteMaterialTheme.typography.bodyLarge
    "bodySmall" -> RemoteMaterialTheme.typography.bodySmall
    "bodyExtraSmall" -> RemoteMaterialTheme.typography.bodyExtraSmall
    "numeralExtraLarge" -> RemoteMaterialTheme.typography.numeralExtraLarge
    "numeralLarge" -> RemoteMaterialTheme.typography.numeralLarge
    "numeralMedium" -> RemoteMaterialTheme.typography.numeralMedium
    "numeralSmall" -> RemoteMaterialTheme.typography.numeralSmall
    "numeralExtraSmall" -> RemoteMaterialTheme.typography.numeralExtraSmall
    else -> RemoteMaterialTheme.typography.bodyMedium
  }

private fun UiBuilderNode.boxAlignment(): RemoteAlignment =
  when (string("contentAlignment")) {
    "center" -> RemoteAlignment.Center
    "topEnd" -> RemoteAlignment.TopEnd
    "bottomStart" -> RemoteAlignment.BottomStart
    "bottomEnd" -> RemoteAlignment.BottomEnd
    else -> RemoteAlignment.TopStart
  }

private fun UiBuilderNode.horizontalAlignment(): RemoteAlignment.Horizontal =
  when (string("horizontalAlignment")) {
    "center" -> RemoteAlignment.CenterHorizontally
    "end" -> RemoteAlignment.End
    else -> RemoteAlignment.Start
  }

private fun UiBuilderNode.verticalAlignment(): RemoteAlignment.Vertical =
  when (string("verticalAlignment")) {
    "top" -> RemoteAlignment.Top
    "bottom" -> RemoteAlignment.Bottom
    else -> RemoteAlignment.CenterVertically
  }

private fun UiBuilderNode.verticalArrangement(): RemoteArrangement.Vertical =
  when (string("verticalArrangement")) {
    "center" ->
      RemoteArrangement.spacedBy(
        (number("verticalSpacingDp") ?: 0f).rdp,
        RemoteAlignment.CenterVertically,
      )
    "bottom" ->
      RemoteArrangement.spacedBy(
        (number("verticalSpacingDp") ?: 0f).rdp,
        RemoteAlignment.Bottom,
      )
    "spaceBetween" -> RemoteArrangement.SpaceBetween
    "spaceAround" -> RemoteArrangement.SpaceAround
    "spaceEvenly" -> RemoteArrangement.SpaceEvenly
    else ->
      RemoteArrangement.spacedBy(
        (number("verticalSpacingDp") ?: 0f).rdp,
        RemoteAlignment.Top,
      )
  }

private fun UiBuilderNode.horizontalArrangement(): RemoteArrangement.Horizontal =
  when (string("horizontalArrangement")) {
    "center" ->
      RemoteArrangement.spacedBy(
        (number("horizontalSpacingDp") ?: 0f).rdp,
        RemoteAlignment.CenterHorizontally,
      )
    "end" ->
      RemoteArrangement.spacedBy(
        (number("horizontalSpacingDp") ?: 0f).rdp,
        RemoteAlignment.End,
      )
    "spaceBetween" -> RemoteArrangement.SpaceBetween
    "spaceAround" -> RemoteArrangement.SpaceAround
    "spaceEvenly" -> RemoteArrangement.SpaceEvenly
    else ->
      RemoteArrangement.spacedBy(
        (number("horizontalSpacingDp") ?: 0f).rdp,
        RemoteAlignment.Start,
      )
  }
