package ee.schimke.wearm3catalog.remoteuibuilder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.remote.creation.compose.action.combinedAction
import androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo
import androidx.compose.remote.creation.compose.capture.captureCommonRemoteDocument
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteCollapsibleColumn
import androidx.compose.remote.creation.compose.layout.RemoteCollapsibleColumnScope
import androidx.compose.remote.creation.compose.layout.RemoteCollapsibleRow
import androidx.compose.remote.creation.compose.layout.RemoteCollapsibleRowScope
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteColumnScope
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteFitBox
import androidx.compose.remote.creation.compose.layout.RemoteFlowRow
import androidx.compose.remote.creation.compose.layout.RemoteImage
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.layout.RemoteRowScope
import androidx.compose.remote.creation.compose.layout.RemoteStateLayout
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.alpha
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.border
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxHeight
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.height
import androidx.compose.remote.creation.compose.modifier.heightIn
import androidx.compose.remote.creation.compose.modifier.offset
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.rotate
import androidx.compose.remote.creation.compose.modifier.scale
import androidx.compose.remote.creation.compose.modifier.sharedElement
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.modifier.widthIn
import androidx.compose.remote.creation.compose.modifier.wrapContentSize
import androidx.compose.remote.creation.compose.modifier.zIndex
import androidx.compose.remote.creation.compose.shaders.RemoteBrush
import androidx.compose.remote.creation.compose.shaders.horizontalGradient
import androidx.compose.remote.creation.compose.shaders.verticalGradient
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.asRemoteTextUnit
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteInt
import androidx.compose.remote.creation.compose.state.rf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteCard
import androidx.wear.compose.remote.material3.RemoteCircularProgressIndicator
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import ee.schimke.composeai.rcplayer.compose.RcComposePlayer
import ee.schimke.composeai.rcplayer.compose.RcPlayerTheme
import ee.schimke.composeai.rcplayer.protocol.RcDocument
import ee.schimke.composeai.rcplayer.protocol.RcDocumentCodec
import ee.schimke.composeai.uibuilder.CanvasRenderNode
import ee.schimke.composeai.uibuilder.CanvasRenderTree
import ee.schimke.composeai.uibuilder.SHOW_BY_STATE
import ee.schimke.composeai.uibuilder.UiBuilderDocument
import ee.schimke.composeai.uibuilder.UiBuilderNode
import ee.schimke.composeai.uibuilder.WearWidgetScaffoldSize
import ee.schimke.composeai.uibuilder.hostSpec
import ee.schimke.composeai.uibuilder.stateSelection
import ee.schimke.wearm3catalog.uibuilder.WearWidgetContainerFrame
import ee.schimke.wearm3catalog.uibuilder.wearWidgetHostShape
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull

internal const val REMOTE_M3_DEVICE_PREVIEW_TEST_TAG = "remote-m3-device-preview"
internal const val REMOTE_M3_WIDGET_HOST_TEST_TAG = "remote-m3-widget-host"
internal const val REMOTE_M3_WIDGET_BACKGROUND_TEST_TAG = "remote-m3-widget-background"
internal const val REMOTE_M3_WIDGET_CONTENT_TEST_TAG = "remote-m3-widget-content"

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
  val hostSpec = widgetSize?.hostSpec(document.wearWidgetHostShape())
  // The frame is the host's: the launcher hands a widget its padding and corner radius, so a
  // design never authors them and a document that still carries them from before is not read.
  val horizontalPadding = hostSpec?.horizontalPaddingDp ?: 0f
  val verticalPadding = hostSpec?.verticalPaddingDp ?: 0f
  val cornerRadius = hostSpec?.cornerRadiusDp ?: 0f
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
    captured = next
    onReady()
  }

  val result = captured
  val state =
    when {
      result == null -> "Building"
      result.isSuccess -> "Ready"
      else -> "Failed"
    }
  Box(
    Modifier.fillMaxSize().testTag(REMOTE_M3_DEVICE_PREVIEW_TEST_TAG).semantics {
      stateDescription = state
    }
  ) {
    when (result) {
      null -> Text("Building Remote M3 preview…")
      else ->
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
                modifier = Modifier.testTag(REMOTE_M3_WIDGET_HOST_TEST_TAG),
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
                      modifier =
                        Modifier.fillMaxSize()
                          .clip(shape)
                          .testTag(REMOTE_M3_WIDGET_BACKGROUND_TEST_TAG),
                    )
                  }
                },
              ) {
                RcComposePlayer(
                  document = documents.content,
                  theme = document.playerTheme(),
                  modifier = Modifier.fillMaxSize().testTag(REMOTE_M3_WIDGET_CONTENT_TEST_TAG),
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
    captureCommonRemoteDocument(
      RemoteCreationDisplayInfo(widthDp.toInt(), heightDp.toInt(), 160),
      // Pictures travel inside the document: without an encoder the common writer drops them.
      encodePng = ::encodeDesignAssetPng,
    ) {
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
    // Stacked in one full-frame box, the way the host layers a widget's background: a picture
    // and the scrim over it both fill the frame, rather than each claiming the document's root.
    RemoteBox(modifier = RemoteModifier.fillMaxSize()) {
      document.roots.singleOrNull()?.let(tree::root)?.slot(slotName)?.forEach { RenderNode(it) }
    }
  }

  /**
   * "Show by state" on a box, played as the `RemoteStateLayout` the export writes.
   *
   * Every branch is recorded — one per case, in case order, then the fallback — and the layout
   * starts on the branch the design's initial state selects. Recording them all, rather than only
   * the selected child, is what lets a `sharedElement` pair across branches the way it does in the
   * exported widget, and what stops the preview stacking every branch on top of the others.
   */
  @Composable
  @RemoteComposable
  private fun StateSwitch(entry: CanvasRenderNode, modifier: RemoteModifier) {
    val node = entry.node
    val children = entry.slot("children").associateBy { it.node.id }
    val selection = node.stateSelection()
    val branches: List<String?> =
      selection?.let { it.cases.keys.toList() + it.fallback } ?: listOf(null)
    val selected =
      selection?.selectedNode(document.initialState(), document.stateVariables)?.let {
        branches.indexOf(it)
      }
    val current = rememberMutableRemoteInt(selected?.takeIf { it >= 0 } ?: branches.lastIndex)
    RemoteBox(modifier = modifier, contentAlignment = node.boxAlignment()) {
      RemoteStateLayout(current, *IntArray(branches.size) { it }) { branch ->
        RemoteBox { branches.getOrNull(branch)?.let(children::get)?.let { RenderNode(it) } }
      }
    }
  }

  /**
   * One node, entered from the container it sits in.
   *
   * [row] and [column] are that container's scope, because `weight` is a member of it rather than a
   * plain modifier — exactly as in the Kotlin this design exports to.
   */
  @Composable
  @RemoteComposable
  private fun RenderNode(
    entry: CanvasRenderNode,
    row: RemoteRowScope? = null,
    column: RemoteColumnScope? = null,
    collapsibleColumn: RemoteCollapsibleColumnScope? = null,
    collapsibleRow: RemoteCollapsibleRowScope? = null,
  ) {
    val node = entry.node
    val modifier = node.remoteModifier(row, column, collapsibleColumn, collapsibleRow)
    if (node.componentId == "layout/box" && SHOW_BY_STATE in node.properties) {
      StateSwitch(entry, modifier)
      return
    }
    when (node.componentId) {
      "remote-m3/widget-container-small",
      "remote-m3/widget-container-large" -> entry.slot("content").forEach { RenderNode(it) }
      "layout/box" -> {
        val children = entry.slot("children")
        RemoteBox(
          modifier = modifier,
          contentAlignment =
            children.sharedAlignment("align")?.boxAlignment() ?: node.boxAlignment(),
        ) {
          children.forEach { RenderNode(it) }
        }
      }
      "layout/column" -> {
        val children = entry.slot("children")
        RemoteColumn(
          modifier = modifier,
          verticalArrangement = node.verticalArrangement(),
          horizontalAlignment =
            children.sharedAlignment("alignHorizontal")?.horizontal() ?: node.horizontalAlignment(),
        ) {
          children.forEach { RenderNode(it, column = this) }
        }
      }
      "layout/row" -> {
        val children = entry.slot("children")
        RemoteRow(
          modifier = modifier,
          horizontalArrangement = node.horizontalArrangement(),
          verticalAlignment =
            children.sharedAlignment("alignVertical")?.vertical() ?: node.verticalAlignment(),
        ) {
          children.forEach { RenderNode(it, row = this) }
        }
      }
      // Remote Compose's own layouts. The CMP writer records all four; only the fit box is in the
      // Glance Wear widget profile, so a widget using the other three plays here and fails on
      // Native / Live, which is the authoritative lane.
      "layout/flow-row" ->
        RemoteFlowRow(
          modifier = modifier,
          horizontalArrangement = node.horizontalArrangement(),
          verticalArrangement = node.verticalArrangement(),
          maxItemsInEachRow = node.integer("maxItemsInEachRow")?.takeIf { it > 0 } ?: Int.MAX_VALUE,
        ) {
          entry.slot("children").forEach { RenderNode(it) }
        }
      "layout/collapsible-column" -> {
        val children = entry.slot("children")
        RemoteCollapsibleColumn(
          modifier = modifier,
          verticalArrangement = node.verticalArrangement(),
          horizontalAlignment =
            children.sharedAlignment("alignHorizontal")?.horizontal() ?: node.horizontalAlignment(),
        ) {
          children.forEach { RenderNode(it, collapsibleColumn = this) }
        }
      }
      "layout/collapsible-row" -> {
        val children = entry.slot("children")
        RemoteCollapsibleRow(
          modifier = modifier,
          horizontalArrangement = node.horizontalArrangement(),
          verticalAlignment =
            children.sharedAlignment("alignVertical")?.vertical() ?: node.verticalAlignment(),
        ) {
          children.forEach { RenderNode(it, collapsibleRow = this) }
        }
      }
      "layout/fit-box" ->
        RemoteFitBox(
          modifier = modifier,
          horizontalAlignment =
            when (node.string("horizontalAlignment")) {
              "start" -> RemoteAlignment.Start
              "end" -> RemoteAlignment.End
              else -> RemoteAlignment.CenterHorizontally
            },
          verticalArrangement =
            when (node.string("verticalArrangement")) {
              "top" -> RemoteArrangement.Top
              "bottom" -> RemoteArrangement.Bottom
              else -> RemoteArrangement.Center
            },
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
          fontWeight = node.fontWeight(),
          textAlign = node.textAlign(),
          overflow = node.textOverflow(),
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
      "remote-m3/remote-circular-progress-indicator" ->
        RemoteCircularProgressIndicator(
          progress = (node.number("progress") ?: 0f).rf,
          modifier = modifier,
          startAngle = (node.number("startAngle") ?: 0f).rf,
          endAngle = (node.number("endAngle") ?: node.number("startAngle") ?: 0f).rf,
        )
      "asset/image" -> {
        // The editor inlines the uploaded pictures it has fetched; until it has, a plain frame.
        val bytes = document.embeddedAssetBytes(node.assetKey())
        val bitmap = remember(bytes) { bytes?.let(::decodeDesignAssetBitmap) }
        if (bitmap == null) RemoteBox(modifier = modifier.background(Color(0x33808080).rc))
        else
          RemoteImage(
            remoteBitmap = bitmap.rb,
            contentDescription = node.string("contentDescription").ifEmpty { null }?.rs,
            modifier = modifier,
            contentScale = node.assetContentScale(),
          )
      }
      "shape/linear-gradient" -> {
        val colors = listOfNotNull(node.remoteColor("startColor"), node.remoteColor("endColor"))
        val brush =
          if (node.string("direction") == "horizontal") RemoteBrush.horizontalGradient(colors)
          else RemoteBrush.verticalGradient(colors)
        // A draw layer declares no size modifiers: it fills whatever it is placed in.
        RemoteBox(modifier = modifier.fillMaxSize().background(brush))
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

/**
 * The alignment every child asks its container for with a [modifierType] modifier, when they agree.
 *
 * `RemoteBox`, `RemoteRow` and `RemoteColumn` align their content as a group, so a child's own
 * alignment becomes the container's argument, which is how the exported Kotlin writes it too.
 */
private fun List<CanvasRenderNode>.sharedAlignment(modifierType: String): String? = map { child ->
  child.node.modifiers
    .mapNotNull { it as? JsonObject }
    .firstOrNull { (it["type"] as? JsonPrimitive)?.contentOrNull == modifierType }
    ?.let { (it["alignment"] as? JsonPrimitive)?.contentOrNull }
}
  .distinct()
  .singleOrNull()

private fun String.horizontal(): RemoteAlignment.Horizontal =
  when (this) {
    "center" -> RemoteAlignment.CenterHorizontally
    "end" -> RemoteAlignment.End
    else -> RemoteAlignment.Start
  }

private fun String.vertical(): RemoteAlignment.Vertical =
  when (this) {
    "top" -> RemoteAlignment.Top
    "bottom" -> RemoteAlignment.Bottom
    else -> RemoteAlignment.CenterVertically
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
private fun UiBuilderNode.remoteModifier(
  row: RemoteRowScope? = null,
  column: RemoteColumnScope? = null,
  collapsibleColumn: RemoteCollapsibleColumnScope? = null,
  collapsibleRow: RemoteCollapsibleRowScope? = null,
): RemoteModifier {
  var result: RemoteModifier = RemoteModifier
  modifiers.forEach { element ->
    val modifier = element as? JsonObject ?: return@forEach
    val type = (modifier["type"] as? JsonPrimitive)?.contentOrNull ?: return@forEach
    fun number(vararg names: String): Float? = names.firstNotNullOfOrNull {
      (modifier[it] as? JsonPrimitive)?.floatOrNull
    }
    // The shape a `clip` names, or a `background` draws in.
    fun shape(): RemoteRoundedCornerShape? =
      (modifier["shape"] as? JsonPrimitive)?.contentOrNull?.let {
        RemoteRoundedCornerShape(namedShapeRadiusDp(it).rdp)
      }
    result =
      when (type) {
        "fillMaxSize" -> result.fillMaxSize()
        "fillMaxWidth" -> result.fillMaxWidth()
        "fillMaxHeight" -> result.fillMaxHeight()
        "width" -> number("widthDp", "value")?.let { result.width(it.rdp) } ?: result
        "height" -> number("heightDp", "value")?.let { result.height(it.rdp) } ?: result
        "size" -> {
          val both = number("sizeDp", "value")
          val width = number("widthDp") ?: both
          val height = number("heightDp") ?: both
          var sized = result
          if (width != null) sized = sized.width(width.rdp)
          if (height != null) sized = sized.height(height.rdp)
          sized
        }
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
        // A member of the container's scope, as in the exported Kotlin; outside a row or column
        // it means nothing and is dropped rather than failing the preview.
        "weight" -> {
          val weight = number("weight", "value") ?: 1f
          row?.run { result.weight(weight.rf) }
            ?: column?.run { result.weight(weight.rf) }
            ?: collapsibleColumn?.run { result.weight(weight.rf) }
            ?: collapsibleRow?.run { result.weight(weight.rf) }
            ?: result
        }
        // Members of the collapsible scopes, like `weight`; meaningless anywhere else.
        "collapsiblePriority" -> {
          val priority = number("priority") ?: 0f
          collapsibleColumn?.run { result.collapsiblePriority(priority) }
            ?: collapsibleRow?.run { result.collapsiblePriority(priority) }
            ?: result
        }
        // The export writes `animationSpec(Int, Boolean)` because the native lane's
        // alpha19 predates `sharedElement`; the port this preview records with has both, and they
        // lower to the same AnimationSpec operation with the same default motion.
        "sharedElement" ->
          number("key")?.toInt()?.takeIf { it >= 1 }?.let { result.sharedElement(key = it) }
            ?: result
        "alpha" -> result.alpha((number("alpha") ?: 1f).rf)
        "rotate" -> result.rotate((number("degrees") ?: 0f).rf)
        "scale" -> result.scale((number("scaleX") ?: 1f).rf, (number("scaleY") ?: 1f).rf)
        "zIndex" -> result.zIndex((number("zIndex") ?: 0f).rf)
        "offset" -> result.offset((number("xDp") ?: 0f).rdp, (number("yDp") ?: 0f).rdp)
        "widthIn" -> result.widthIn(number("minDp")?.rdp, number("maxDp")?.rdp)
        "heightIn" -> result.heightIn(number("minDp")?.rdp, number("maxDp")?.rdp)
        "wrapContentSize" -> result.wrapContentSize()
        "border" -> {
          val color = modifier["color"].modifierColor()?.remoteColor() ?: Color.Transparent.rc
          result.border(
            (number("widthDp") ?: 1f).rdp,
            color,
            shape() ?: RemoteRoundedCornerShape(0f.rdp),
          )
        }
        "clip" -> result.clip(shape() ?: RemoteRoundedCornerShape(0f.rdp))
        "background" -> {
          val color = modifier["color"].modifierColor()?.remoteColor()
          val clipped = shape()?.let { result.clip(it) } ?: result
          color?.let { clipped.background(it) } ?: clipped
        }
        // Read by the parent: a remote container aligns its content as a group (`sharedAlignment`).
        "align",
        "alignVertical",
        "alignHorizontal" -> result
        else -> error("Unsupported Remote Compose modifier '$type' on ${componentId}")
      }
  }
  return result
}

/**
 * The corner radius a stored shape names: a number of dp, or one of the size words.
 *
 * The words resolve to what the exported Kotlin writes for them (`RemoteContentEmitter`), because
 * that is the widget that ships; the authoring canvas uses the same table so switching between it
 * and a device preview never changes a component's geometry.
 */
internal fun namedShapeRadiusDp(declared: String?): Float =
  when (declared) {
    "large" -> 16f
    "medium" -> 12f
    "small" -> 8f
    else -> declared?.toFloatOrNull() ?: 0f
  }

/** A modifier's colour, written either bare or as the `{"type":"color","value":…}` wrapper. */
private fun kotlinx.serialization.json.JsonElement?.modifierColor(): String? =
  when (this) {
    is JsonPrimitive -> contentOrNull
    is JsonObject -> (this["value"] as? JsonPrimitive)?.contentOrNull
    else -> null
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
  string("contentAlignment").boxAlignment()

private fun String.boxAlignment(): RemoteAlignment =
  when (this) {
    "center" -> RemoteAlignment.Center
    "topCenter" -> RemoteAlignment.TopCenter
    "topEnd" -> RemoteAlignment.TopEnd
    "centerStart" -> RemoteAlignment.CenterStart
    "centerEnd" -> RemoteAlignment.CenterEnd
    "bottomStart" -> RemoteAlignment.BottomStart
    "bottomCenter" -> RemoteAlignment.BottomCenter
    "bottomEnd" -> RemoteAlignment.BottomEnd
    else -> RemoteAlignment.TopStart
  }

private fun UiBuilderNode.fontWeight(): FontWeight? =
  when (string("fontWeight")) {
    "thin" -> FontWeight.Thin
    "light" -> FontWeight.Light
    "normal" -> FontWeight.Normal
    "medium" -> FontWeight.Medium
    "semiBold" -> FontWeight.SemiBold
    "bold" -> FontWeight.Bold
    "black" -> FontWeight.Black
    else -> string("fontWeight").removePrefix("w").toIntOrNull()?.let { FontWeight(it) }
  }

private fun UiBuilderNode.textOverflow(): TextOverflow =
  when (string("overflow")) {
    "ellipsis" -> TextOverflow.Ellipsis
    "visible" -> TextOverflow.Visible
    else -> TextOverflow.Clip
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
