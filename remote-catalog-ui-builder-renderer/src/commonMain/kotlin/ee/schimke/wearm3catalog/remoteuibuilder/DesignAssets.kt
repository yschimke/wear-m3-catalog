package ee.schimke.wearm3catalog.remoteuibuilder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import ee.schimke.composeai.uibuilder.UiBuilderDocument
import ee.schimke.composeai.uibuilder.UiBuilderNode
import kotlin.io.encoding.Base64
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * The picture a design carries for [assetKey], when its registry entry is an `embedded` source.
 *
 * The only kind this runtime can draw: it renders in a sandboxed frame that is sent the document
 * and nothing else, so the editor inlines every uploaded picture it has fetched before posting it.
 * Any other source, a missing key, or bytes that are not base64 is null, and the node draws a
 * placeholder rather than failing the frame.
 */
internal fun UiBuilderDocument.embeddedAssetBytes(assetKey: String): ByteArray? {
  val binding = assets[assetKey] as? JsonObject ?: return null
  val source = binding["source"] as? JsonObject ?: return null
  if ((source["type"] as? JsonPrimitive)?.contentOrNull != "embedded") return null
  val encoded = (source["base64"] as? JsonPrimitive)?.contentOrNull ?: return null
  return try {
    Base64.Default.decode(encoded).takeIf { it.isNotEmpty() }
  } catch (_: IllegalArgumentException) {
    null
  }
}

/** Encoded image bytes to pixels, or null when they do not decode. */
internal expect fun decodeDesignAssetBitmap(bytes: ByteArray): ImageBitmap?

/** Pixels to PNG bytes, for a picture written inline into a captured Remote Compose document. */
internal expect fun encodeDesignAssetPng(image: ImageBitmap): ByteArray?

internal fun UiBuilderNode.assetKey(): String = propertyText("assetKey")

internal fun UiBuilderNode.propertyText(name: String): String =
  ((properties[name] as? JsonObject)?.get("value") as? JsonPrimitive)?.contentOrNull.orEmpty()

internal fun UiBuilderNode.assetContentScale(): ContentScale =
  when (propertyText("contentScale")) {
    "fit" -> ContentScale.Fit
    "fillBounds" -> ContentScale.FillBounds
    "inside" -> ContentScale.Inside
    else -> ContentScale.Crop
  }

/** `asset/image` on the authoring canvas: the embedded picture, or a plain placeholder frame. */
@Composable
internal fun DesignAssetImage(
  document: UiBuilderDocument,
  node: UiBuilderNode,
  modifier: Modifier,
) {
  val bytes = document.embeddedAssetBytes(node.assetKey())
  val bitmap = remember(bytes) { bytes?.let(::decodeDesignAssetBitmap) }
  if (bitmap == null) {
    Box(modifier.background(Color(0x33808080)))
    return
  }
  Image(
    bitmap = bitmap,
    contentDescription = node.propertyText("contentDescription").ifEmpty { null },
    modifier = modifier,
    contentScale = node.assetContentScale(),
  )
}

/**
 * `shape/linear-gradient` on the authoring canvas: a draw layer that fills whatever it is placed
 * in, which is what makes it usable as a scrim in a widget container's background slot.
 */
@Composable
internal fun DesignLinearGradient(
  node: UiBuilderNode,
  modifier: Modifier,
  resolveColor: @Composable (String) -> Color,
) {
  val colors =
    listOf(node.propertyText("startColor"), node.propertyText("endColor"))
      .filter(String::isNotEmpty)
      .map { resolveColor(it) }
  if (colors.size < 2) {
    Box(modifier.fillMaxSize())
    return
  }
  val brush =
    if (node.propertyText("direction") == "horizontal") Brush.horizontalGradient(colors)
    else Brush.verticalGradient(colors)
  Box(modifier.fillMaxSize().background(brush))
}
