@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.remote.material3.RemoteButtonColors
import androidx.wear.compose.remote.material3.RemoteButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme

// THE SNAPSHOT LANE'S BUTTON PALETTE — the same four emphases, named by the library.
//
// The counterpart of this file under `src/released` transcribes three theme-role pairs because
// `remote-material3` 1.0.0-alpha10 publishes only the generic `buttonColors()`. Since that release
// the library has grown `filledTonalButtonColors()`, `filledVariantButtonColors()` and
// `outlinedButtonColors()` — the same three `RemoteIconButtonDefaults` grew for the icon-button
// family, which this sheet has been calling since `RemoteIconButtonPalette.kt` split. The plain
// button family was simply never moved across with it.
//
// THE ENABLED COLOURS ARE UNCHANGED. `filled` is `primary` / `onPrimary`, `filledVariant` is
// `primaryContainer` / `onPrimaryContainer`, `tonal` is `surfaceContainer` / `onSurface`,
// `outlined` is transparent over `onSurface` — the pairs the released lane already names. So every
// `Icon=No, Disabled=No` base cell renders the same on both lanes, and a difference in one of them
// would be a regression rather than the point.
//
// THE OTHER SIX COLOURS ARE THE POINT, and they are what the released lane cannot reach:
//
//   * `disabledContainerColor` / `disabledContentColor`. Written out through
//     `buttonColors(containerColor = …, contentColor = …)` these stay at the FILLED style's
//     defaults, so the disabled cell of every non-filled style was the disabled filled button to
//     the byte. `RemoteRenderTest.knownDuplicate` recorded that for `CompactRemoteButton` as
//     though it were a fact about the library; it was a fact about the call site.
//   * `secondaryContentColor`. Every `Icon=Yes` cell in the kit's `Button` set draws
//     `Secondary label` under the primary one, and the generic factory colours it for a `primary`
//     container. On the outlined style — no container at all — that put a dark subtitle on
//     transparency, which is what
//     [#323](https://github.com/yschimke/wear-m3-catalog/issues/323) reported and what these
//     factories fix.
//
// So the `disabled` and `icon*` cells of `Button/Tonal`, `/FilledVariant`, `/Outlined` and
// `Button/Compact` are expected to CHANGE on this lane, and to change towards their kit nodes.
// That is the render difference this file is here to produce.
//
// When the factories ship in a release: delete the `src/released` sibling, move this file into
// `src/main`, and the lane split for buttons is over.

/** The kit's `Filled` emphasis, and the library's own default. */
@Composable
internal fun remoteFilledButtonColors(): RemoteButtonColors = RemoteButtonDefaults.buttonColors()

/** The kit's `Filled Variant` emphasis — the higher-chroma container pair. */
@Composable
internal fun remoteFilledVariantButtonColors(): RemoteButtonColors =
  RemoteButtonDefaults.filledVariantButtonColors()

/** The kit's `Tonal` emphasis: the muted surface container. */
@Composable
internal fun remoteTonalButtonColors(): RemoteButtonColors =
  RemoteButtonDefaults.filledTonalButtonColors()

/**
 * The kit's `Child (No background)` column — a label on whatever is behind it.
 *
 * The ONE function this file does not delegate, and deliberately identical to its released
 * sibling: the snapshot line grew `filledTonalButtonColors`, `filledVariantButtonColors` and
 * `outlinedButtonColors` and no child factory, so there is nothing to call. Wear builds
 * `childButtonColors()` from exactly these four, which is what makes the transcription safe.
 */
@Composable
internal fun remoteChildButtonColors(): RemoteButtonColors =
  RemoteButtonDefaults.buttonColors(
    containerColor = RemoteColor(Color.Transparent),
    contentColor = RemoteMaterialTheme.colorScheme.onSurface,
    secondaryContentColor = RemoteMaterialTheme.colorScheme.onSurfaceVariant,
    iconColor = RemoteMaterialTheme.colorScheme.primary,
  )

/**
 * The kit's `Outlined` emphasis — no container, and the border is drawn by the call site rather
 * than by the colours (see [KitOutlinedBorderWidth]).
 */
@Composable
internal fun remoteOutlinedButtonColors(): RemoteButtonColors =
  RemoteButtonDefaults.outlinedButtonColors()
