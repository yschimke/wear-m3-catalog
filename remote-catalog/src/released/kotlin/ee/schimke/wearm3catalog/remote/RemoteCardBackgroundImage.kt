@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.Action
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteCardDefaults
import androidx.wear.compose.remote.material3.RemoteTitleCard

// THE RELEASED LANE'S CARD STYLES — the kit's `Style` axis minus the one the library cannot draw.
//
// The kit crosses every `Card` layout with `Style = Tonal | Outline | Background Image`. At
// `remote-material3-1.0.0-alpha10` neither `RemoteCardKt` nor `RemoteCardDefaults` exposes a
// painter or container-painter parameter — `RemoteCard`, `RemoteTitleCard` and `RemoteAppCard` take
// colours and shapes only — so `Background Image` has no call site here and its cells are not
// declared. A cell mapped to a node this lane cannot draw would be worse than none
// ([#157](https://github.com/yschimke/wear-m3-catalog/issues/157)).
//
// The sibling of this file under `src/snapshot` has the third style and the three cells it unlocks.
// When the painter overloads ship in a release, delete this file, promote that one into `src/main`,
// and the split for cards is over.

/** The kit's `Style` values this lane can draw. */
internal val KitCardStyles = listOf("tonal", "outlined")

/**
 * No cells. The snapshot sibling declares the three `Background Image` crossings; on this lane the
 * annotation is applied to the same component and contributes nothing, so the cell list is the
 * lane's answer rather than something a reader has to reconcile.
 */
annotation class RemoteCardBackgroundImageCells

/**
 * `RemoteTitleCard` with the kit's `Style` axis resolved — the one call site both lanes share.
 *
 * The wrapper exists so the STYLE decides the call, not the caller: on this lane every style is the
 * plain overload with different colours, and on the snapshot lane `image` is a different function
 * taking a `containerPainter`. Keeping that fork here leaves `TitleCardRemote` reading the same on
 * both lanes.
 */
@Composable
@RemoteComposable
internal fun KitTitleCard(
  onClick: Action,
  style: String,
  modifier: RemoteModifier,
  title: @Composable @RemoteComposable () -> Unit,
  time: (@Composable @RemoteComposable () -> Unit)?,
  subtitle: (@Composable @RemoteComposable () -> Unit)?,
  content: (@Composable @RemoteComposable () -> Unit)?,
) {
  RemoteTitleCard(
    onClick = onClick,
    modifier = modifier,
    colors =
      if (style == "outlined") RemoteCardDefaults.outlinedCardColors()
      else RemoteCardDefaults.cardColors(),
    title = title,
    time = time,
    subtitle = subtitle,
    content = content,
  )
}
