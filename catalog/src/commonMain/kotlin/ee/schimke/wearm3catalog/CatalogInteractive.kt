package ee.schimke.wearm3catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import ee.schimke.composeai.overrides.previewOverrideBoolean

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
 * A label and a real `onClick`, for components that own no state of their own.
 *
 * The handler is live-lane only, so a baked capture cannot depend on whether something tapped it.
 * What a live click *shows* is the component's own press feedback — the ripple, the state layer,
 * the pressed shape — and that is deliberately all it shows by default.
 *
 * ### The tally is a knob now, not the behaviour
 *
 * This used to append `(n)` to the label on every click, so a sticker could be *seen* to respond.
 * It answered the wrong question twice over. A growing label is not what the component does when
 * you press it, and reading it as proof that clicks work hid the fact that the ripple was missing
 * on the live lane at all ([#32](https://github.com/yschimke/wear-m3-catalog/issues/32) — the
 * preview server drove clicks through the node's `OnClick` semantics action, which invokes the
 * handler and emits no press interaction, so nothing ever rippled). Fixing the ripple upstream left
 * the tally competing with it: two answers, the louder one wrong.
 *
 * So it is `clickCount`, a knob every sticker exposes and nothing turns on by default. Set it and
 * the label counts again — useful when the question really is "did the handler run?", e.g. on a
 * component whose press feedback is subtle or suppressed. Leave it and the component speaks for
 * itself. The default is what every published PNG renders, so the knob costs the baked lane
 * nothing.
 */
class Counted(val label: String, val onClick: () -> Unit)

@Composable
fun counted(label: String): Counted {
  if (!catalogInteractive()) return Counted(label, {})
  var clicks by remember { mutableIntStateOf(0) }
  val tally = previewOverrideBoolean("clickCount", false)
  return Counted(if (tally && clicks > 0) "$label ($clicks)" else label, { clicks++ })
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
