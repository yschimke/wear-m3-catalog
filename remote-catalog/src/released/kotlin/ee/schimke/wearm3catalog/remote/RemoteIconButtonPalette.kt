@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.remote.material3.RemoteIconButtonColors
import androidx.wear.compose.remote.material3.RemoteIconButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme

// THE RELEASED LANE'S ICON-BUTTON PALETTE — four emphases the library does not name yet.
//
// `remote-material3` 1.0.0-alpha10 publishes exactly one colour factory for `RemoteIconButton`,
// `iconButtonColors()`, and its defaults are the STANDARD (transparent) emphasis. So the kit's four
// contained styles are reached by naming the theme roles Wear's own `filledIconButtonColors()` and
// friends resolve to, and passing them through that one factory.
//
// The transcription is exact for the ENABLED pair on all four, which is the state every base cell
// draws — `filled` is `primary` / `onPrimary`, `filledVariant` is `primaryContainer` /
// `onPrimaryContainer`, `filledTonal` is `surfaceContainer` / `onSurface`, and `outlined` is a
// transparent container over `onSurface`.
//
// IT IS NOT EXACT FOR THE DISABLED PAIR, and cannot be made so from here. `iconButtonColors` on
// this line declares `disabledContainerColor: RemoteColor = RemoteColor(Color.Transparent)` as a
// hard default rather than deriving it, so the three CONTAINED styles lose their disabled container
// entirely: the kit (and Wear, and the snapshot line — see the sibling of this file under
// `src/snapshot`) draws `onSurface` at 12%, and these cells draw nothing. Passing the scrim in by
// hand would mean typing `0.12f` under the kit's name, which is the invention this repo refuses
// elsewhere for the same reason. The `disabled` cells of `IconButton/Filled`, `/FilledVariant` and
// `/Tonal` therefore diverge from their kit nodes on this lane, on purpose and visibly.
//
// The snapshot lane's file is the same four functions calling the library's own named factories,
// which resolve those disabled colours properly. When the factories ship in a release, delete this
// file, promote that one into `src/main`, and the difference disappears with it.

/** The kit's `Filled` emphasis: a `primary` container under a contrasting glyph. */
@Composable
internal fun remoteFilledIconButtonColors(): RemoteIconButtonColors =
  RemoteIconButtonDefaults.iconButtonColors(
    containerColor = RemoteMaterialTheme.colorScheme.primary,
    contentColor = RemoteMaterialTheme.colorScheme.onPrimary,
  )

/** The kit's `Filled Variant` emphasis — the higher-chroma container pair. */
@Composable
internal fun remoteFilledVariantIconButtonColors(): RemoteIconButtonColors =
  RemoteIconButtonDefaults.iconButtonColors(
    containerColor = RemoteMaterialTheme.colorScheme.primaryContainer,
    contentColor = RemoteMaterialTheme.colorScheme.onPrimaryContainer,
  )

/** The kit's `Filled Tonal` emphasis: the muted surface container. */
@Composable
internal fun remoteFilledTonalIconButtonColors(): RemoteIconButtonColors =
  RemoteIconButtonDefaults.iconButtonColors(
    containerColor = RemoteMaterialTheme.colorScheme.surfaceContainer,
    contentColor = RemoteMaterialTheme.colorScheme.onSurface,
  )

/**
 * The kit's `Outlined` emphasis — no container, and the border is drawn by the call site rather
 * than by the colours (see [RemoteKitIconButton]'s `border` / `borderColor`).
 */
@Composable
internal fun remoteOutlinedIconButtonColors(): RemoteIconButtonColors =
  RemoteIconButtonDefaults.iconButtonColors(containerColor = RemoteColor(Color.Transparent))
