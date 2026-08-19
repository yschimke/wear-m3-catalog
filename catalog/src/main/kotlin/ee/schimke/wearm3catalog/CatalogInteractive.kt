package ee.schimke.wearm3catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * True when the sticker is being driven live rather than baked into a PNG.
 *
 * Every sticker is rendered on two surfaces that want opposite things from a pointer. A baked
 * capture must not depend on whether something tapped it; a held Compose session on the preview
 * server must visibly respond. One derived flag serves both, so a single sticker body covers them —
 * never a hard-coded constant.
 */
@Composable fun catalogInteractive(): Boolean = !LocalInspectionMode.current

/**
 * A label and an `onClick` that tally into it, for components that own no state of their own.
 *
 * At `n == 0` this returns the bare label and a no-op handler, so the baked capture is
 * byte-identical either way; in a live session each click appends `(n)`. That is what lets a
 * sticker ship a handler that does something without changing the published picture — the
 * alternative, a literal `{}`, publishes a component that cannot be shown to work.
 */
class Counted(val label: String, val onClick: () -> Unit)

@Composable
fun counted(label: String): Counted {
  if (!catalogInteractive()) return Counted(label, {})
  var clicks by remember { mutableIntStateOf(0) }
  return Counted(if (clicks == 0) label else "$label ($clicks)", { clicks++ })
}

/**
 * A checked state the sticker owns, so a toggle actually toggles.
 *
 * Unlike [counted] this does NOT go inert outside the live lane, and the difference matters twice.
 * A baked still is unaffected either way — nothing taps it, so it captures at [initial] — while an
 * inert setter makes a **motion capture impossible**: the renderer drives a real tap, the state
 * cannot move, the encoder gets one distinct frame and refuses to write a single-frame GIF. Going
 * inert here would buy nothing and cost every interaction recording in `Motion.kt`.
 */
@Composable
fun toggleable(initial: Boolean): Pair<Boolean, (Boolean) -> Unit> {
  var checked by remember { mutableStateOf(initial) }
  return checked to { value: Boolean -> checked = value }
}
