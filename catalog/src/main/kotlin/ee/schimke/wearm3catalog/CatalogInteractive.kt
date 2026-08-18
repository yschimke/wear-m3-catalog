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

/** A checked state a sticker owns, so a toggle in a live session actually toggles. */
@Composable
fun toggleable(initial: Boolean): Pair<Boolean, (Boolean) -> Unit> {
  if (!catalogInteractive()) return initial to {}
  var checked by remember { mutableStateOf(initial) }
  return checked to { value: Boolean -> checked = value }
}
