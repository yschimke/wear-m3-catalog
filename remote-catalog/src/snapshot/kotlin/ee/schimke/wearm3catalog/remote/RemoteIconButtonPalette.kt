@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteIconButtonColors
import androidx.wear.compose.remote.material3.RemoteIconButtonDefaults

// THE SNAPSHOT LANE'S ICON-BUTTON PALETTE — the same four emphases, named by the library.
//
// The counterpart of this file under `src/released` transcribes four theme-role pairs because
// `remote-material3` 1.0.0-alpha10 publishes only the generic `iconButtonColors()`. Since that
// release the library has grown `filledIconButtonColors()`, `filledVariantIconButtonColors()`,
// `filledTonalIconButtonColors()` and `outlinedIconButtonColors()`, each backed by a
// `RemoteColorScheme.default*IconButtonColors` — so the emphasis is a name the library owns again
// and this sheet stops holding a private copy of it.
//
// THE ENABLED COLOURS ARE UNCHANGED. `filled` is `primary` / `onPrimary`, `filledVariant` is
// `primaryContainer` / `onPrimaryContainer`, `filledTonal` is `surfaceContainer` / `onSurface`,
// `outlined` is transparent over `onSurface` — the four pairs the released lane already names. So
// every base cell of `IconButton/Filled`, `/FilledVariant`, `/Tonal` and `/Outlined` renders the
// same on both lanes, and a difference in one of them would be a regression rather than the point.
//
// THE DISABLED COLOURS ARE THE POINT. The released `iconButtonColors(containerColor = …)` declares
// `disabledContainerColor` as a hard `Transparent` default rather than deriving it, so the three
// contained styles drew NO disabled container at all — where the kit, Wear Compose Material 3, and
// these factories all draw `onSurface` at 12%. The `disabled` cells of the three contained icon
// buttons are therefore expected to CHANGE on this lane, and to change towards their kit nodes.
// That is the one render difference this file is here to produce.
//
// When the factories ship in a release: delete the `src/released` sibling, move this file into
// `src/main`, and the lane split for icon buttons is over.

/** The kit's `Filled` emphasis: a `primary` container under a contrasting glyph. */
@Composable
internal fun remoteFilledIconButtonColors(): RemoteIconButtonColors =
  RemoteIconButtonDefaults.filledIconButtonColors()

/** The kit's `Filled Variant` emphasis — the higher-chroma container pair. */
@Composable
internal fun remoteFilledVariantIconButtonColors(): RemoteIconButtonColors =
  RemoteIconButtonDefaults.filledVariantIconButtonColors()

/** The kit's `Filled Tonal` emphasis: the muted surface container. */
@Composable
internal fun remoteFilledTonalIconButtonColors(): RemoteIconButtonColors =
  RemoteIconButtonDefaults.filledTonalIconButtonColors()

/**
 * The kit's `Outlined` emphasis — no container, and the border is drawn by the call site rather
 * than by the colours (see [RemoteKitIconButton]'s `border` / `borderColor`).
 */
@Composable
internal fun remoteOutlinedIconButtonColors(): RemoteIconButtonColors =
  RemoteIconButtonDefaults.outlinedIconButtonColors()
