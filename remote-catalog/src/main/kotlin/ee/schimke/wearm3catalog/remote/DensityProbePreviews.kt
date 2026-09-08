package ee.schimke.wearm3catalog.remote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberNamedRemoteDp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.wear.compose.remote.material3.RemoteIcon
import ee.schimke.composeai.daemon.rememberOverridableRemoteDp

/**
 * Which dp path `composePreview.rcDensity=host` breaks — a literal one or a *named* one.
 *
 * Under `host`, `IconRemote` collapses from a 112x112 ink box to 28x28 at dpi 320, exactly 4x
 * linear, which is density squared at this density. Everything in the deferred conversion looked
 * correct on inspection: `RemoteDensity.Host` is `density = FLOAT_DENSITY`, and `RemoteDp.toPx`
 * multiplies by that density on both its constant-folding and expression branches. So inspection
 * was not going to find it, and the two probes below split the remaining candidates by rendering
 * them side by side rather than by reading more bytecode.
 *
 * [DensityProbeLiteralDp] sizes the icon with a literal `48.rdp` — the plain AndroidX path, no
 * document variable involved.
 *
 * [DensityProbeNamedDp] sizes it with [rememberOverridableRemoteDp], which is what the real
 * `IconRemote` uses. That routes through `rememberNamedRemoteDp`, so the size becomes a *named
 * document variable* rather than a literal, and it is resolved at replay rather than at capture.
 *
 * Both draw the same glyph at the same nominal 48dp, so at dpi 320 both should ink identically at
 * both settings. Whichever of the two moves under `host` names the layer that is wrong:
 *
 * * both move -> the fault is in AndroidX's deferred dp conversion, and belongs upstream;
 * * only named -> the fault is in the named-value path, which is this repo's plus the alpha
 *   `rememberNamedRemoteDp`, and belongs here.
 *
 * Delete these once the cause is fixed; they exist to answer that one question.
 */
@CatalogRemoteModes
@Composable
fun DensityProbeLiteralDp() = RemoteSticker {
  RemoteIcon(Icons.Filled.Add, "Add".rs, modifier = RemoteModifier.size(48.rdp))
}

/** The named-variable twin of [DensityProbeLiteralDp]. Same glyph, same nominal 48dp. */
@CatalogRemoteModes
@Composable
fun DensityProbeNamedDp() = RemoteSticker {
  val size = rememberOverridableRemoteDp("probeSize", 48.dp)
  RemoteIcon(Icons.Filled.Add, "Add".rs, modifier = RemoteModifier.size(size))
}

/**
 * The same named dp, bound through AndroidX's [rememberNamedRemoteDp] **directly** rather than
 * through this repo's [rememberOverridableRemoteDp] wrapper.
 *
 * [DensityProbeNamedDp] inks 112x112 under `fixed` where [DensityProbeLiteralDp] inks 56x56 for the
 * same nominal 48dp, so the named path applies density twice. The wrapper's only contribution is a
 * `recordDeclaration` side effect and `{ default.value.rdp }` as the value lambda — it passes a
 * `RemoteDp` of 48, exactly as this probe does. So if this one also inks 112x112, the wrapper is
 * not implicated and the double application is upstream in `rememberNamedRemoteDp`; if it inks
 * 56x56, the fault is the wrapper's.
 */
@CatalogRemoteModes
@Composable
fun DensityProbeRawNamedDp() = RemoteSticker {
  val size = rememberNamedRemoteDp("rawProbeSize") { 48.rdp }
  RemoteIcon(Icons.Filled.Add, "Add".rs, modifier = RemoteModifier.size(size))
}
