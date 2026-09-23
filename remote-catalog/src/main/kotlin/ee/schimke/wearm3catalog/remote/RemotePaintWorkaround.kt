@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.layout.RemoteContentDrawScope
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.drawWithContent
import androidx.compose.remote.creation.compose.state.RemotePaint
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle

/**
 * Keeps the Remote Compose paint tracker and its player in agreement at a content boundary.
 *
 * Some current AndroidX Embedded renders retain a previous paint state across a [drawContent]
 * boundary. Emitting an invisible fill before and after content forces the following drawable to
 * serialise its own paint state. Remove this once the upstream renderer fixes the state leak.
 */
internal fun RemoteModifier.paintStateFence(): RemoteModifier = drawWithContent {
  drawTransparentFill()
  drawContent()
  drawTransparentFill()
}

private fun RemoteContentDrawScope.drawTransparentFill() {
  drawRect(
    RemotePaint {
      style = PaintingStyle.Fill
      color = Color.Transparent.rc
    }
  )
}
