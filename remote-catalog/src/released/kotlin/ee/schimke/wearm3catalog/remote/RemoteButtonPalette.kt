@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.remote.material3.RemoteButtonColors
import androidx.wear.compose.remote.material3.RemoteButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme

// THE RELEASED LANE'S BUTTON PALETTE — the exact counterpart of `RemoteIconButtonPalette.kt`, one
// component family over, and it exists for the reason that file already records.
//
// `remote-material3` 1.0.0-alpha10 publishes exactly one colour factory for `RemoteButton`,
// `buttonColors()`, whose defaults are the FILLED emphasis. So the kit's other three styles are
// reached by naming the theme roles Wear's own `filledTonalButtonColors()` and friends resolve to,
// and passing them through that one factory.
//
// The transcription is exact for the ENABLED pair on all four, which is the state every base cell
// draws — `filled` is `primary` / `onPrimary`, `filledVariant` is `primaryContainer` /
// `onPrimaryContainer`, `tonal` is `surfaceContainer` / `onSurface`, and `outlined` is a
// transparent container over `onSurface`.
//
// IT IS NOT EXACT FOR THE DISABLED PAIR, OR FOR THE SECONDARY CONTENT, and cannot be made so from
// here — which is the whole reason this file is a lane split rather than the sheet's only answer.
// `buttonColors(containerColor = …, contentColor = …)` leaves the other six colours at the FILLED
// style's defaults, so:
//
//   * the three disabled cells of every non-filled style are the disabled FILLED button to the
//     byte (`RemoteRenderTest.knownDuplicate` records exactly that for `CompactRemoteButton`), and
//   * `secondaryContentColor` stays the colour meant to sit on a `primary` container, so an
//     `Icon=Yes` cell — every one of which draws `Secondary label` under the primary — renders its
//     subtitle in a colour chosen for a container this style does not have. On the outlined style
//     that is a dark subtitle on no container at all, which is what
//     [#323](https://github.com/yschimke/wear-m3-catalog/issues/323) reported.
//
// Passing those six in by hand would mean typing the kit's alphas and roles under our own name,
// which is the invention this repo refuses elsewhere. So on this lane they stay wrong and visible.
//
// The snapshot lane's file is the same four functions calling the library's own named factories,
// which resolve all eight colours properly. When the factories ship in a release, delete this file,
// promote that one into `src/main`, and the difference disappears with it.

/** The kit's `Filled` emphasis, and the library's own default. */
@Composable
internal fun remoteFilledButtonColors(): RemoteButtonColors = RemoteButtonDefaults.buttonColors()

/**
 * The kit's `Filled Variant` emphasis — the higher-chroma container pair.
 *
 * The tokens come from the Wear function this pairs with:
 * `ButtonDefaults.filledVariantButtonColors()` is `primaryContainer` / `onPrimaryContainer`.
 */
@Composable
internal fun remoteFilledVariantButtonColors(): RemoteButtonColors =
  RemoteButtonDefaults.buttonColors(
    containerColor = RemoteMaterialTheme.colorScheme.primaryContainer,
    contentColor = RemoteMaterialTheme.colorScheme.onPrimaryContainer,
    secondaryContentColor = RemoteMaterialTheme.colorScheme.onPrimaryContainer,
    iconColor = RemoteMaterialTheme.colorScheme.onPrimaryContainer,
  )

/**
 * The kit's `Tonal` emphasis: the muted surface container.
 *
 * `surfaceContainer`, NOT `secondaryContainer`. Wear Material 3 is where the token comes from:
 * `ButtonDefaults.filledTonalButtonColors()` is `surfaceContainer` / `onSurface` on this platform,
 * unlike phone M3's secondary-container tonal, and `Button/Tonal` in the sibling catalog is that
 * function. Against the wrong token this drew a blue button beside a neutral-grey kit cell.
 */
@Composable
internal fun remoteTonalButtonColors(): RemoteButtonColors =
  RemoteButtonDefaults.buttonColors(
    containerColor = RemoteMaterialTheme.colorScheme.surfaceContainer,
    contentColor = RemoteMaterialTheme.colorScheme.onSurface,
    secondaryContentColor = RemoteMaterialTheme.colorScheme.onSurfaceVariant,
    iconColor = RemoteMaterialTheme.colorScheme.primary,
  )

/**
 * The kit's `Child (No background)` column — a label on whatever is behind it.
 *
 * Transparent container plus `onSurface` content is how Wear's own `childButtonColors()` is built,
 * and with no container to draw the whole style IS those colours. Hand-written on BOTH lanes: the
 * snapshot line grew `filledTonalButtonColors`, `filledVariantButtonColors` and
 * `outlinedButtonColors` and no child factory, so this one function is identical in the sibling
 * file rather than delegating.
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
 *
 * The same four colours as [remoteChildButtonColors], because the two styles differ by the stroke
 * and nothing else: Wear builds `outlinedButtonColors()` on the same container-less pair. Naming
 * all four is the released lane's half of the [#323] fix — this style used to pass the container
 * and content colours ALONE, leaving `secondaryContentColor` at the filled default, which drew the
 * `Secondary label` of every `Icon=Yes` cell in a colour meant for a `primary` container it does
 * not have.
 */
@Composable
internal fun remoteOutlinedButtonColors(): RemoteButtonColors =
  RemoteButtonDefaults.buttonColors(
    containerColor = RemoteColor(Color.Transparent),
    contentColor = RemoteMaterialTheme.colorScheme.onSurface,
    secondaryContentColor = RemoteMaterialTheme.colorScheme.onSurfaceVariant,
    iconColor = RemoteMaterialTheme.colorScheme.primary,
  )
