package ee.schimke.wearm3catalog.remoteuibuilder

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

internal actual fun decodeDesignAssetBitmap(bytes: ByteArray): ImageBitmap? =
  try {
    Image.makeFromEncoded(bytes).toComposeImageBitmap()
  } catch (_: Throwable) {
    null
  }

internal actual fun encodeDesignAssetPng(image: ImageBitmap): ByteArray? =
  try {
    Image.makeFromBitmap(image.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
  } catch (_: Throwable) {
    null
  }
