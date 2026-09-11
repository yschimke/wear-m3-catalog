package ee.schimke.wearm3catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureAction
import kotlinx.coroutines.launch

// ACTIVATING A ONE-HANDED GESTURE WHERE THERE IS NO WRIST.
//
// `Modifier.oneHandedGesture` is driven by the watch: the double pinch and the wrist turn are
// sensor events the platform's `GestureInputManager` reports, and the app never sees a pointer for
// them. Everywhere that manager is absent — the preview server, an emulator, a phone — a
// gesture-aware sticker is inert. The hint animation still plays, because `showIndicator()` is
// ordinary public API the sticker can call itself, but the ACTION behind the hint cannot be
// reached at all: a reader watches the glyph promise something and then has no way to take it up.
//
// So where the gesture cannot fire, the sticker offers a button that calls the same `onGesture` the
// framework would. It is scaffolding around the component, not part of it, and it is deliberately
// visible as such.

/**
 * Whether the Wear gesture SDK is on this classpath at all.
 *
 * `androidx.wear.compose:compose-material3` does not depend on `com.google.wear` — the classes come
 * from the watch's system image — so `Class.forName` failing is exactly the condition the library's
 * own `createSdkWearManagerIfNeeded` swallows in a `try`/`catch`, and exactly the condition under
 * which `Modifier.oneHandedGesture` registers nothing and no gesture will ever arrive.
 *
 * Resolved once: the classpath does not change under a running process, and the lookup throws
 * rather than returning null, which is not a thing to do per composition.
 *
 * It is a proxy, and worth being exact about what it does NOT prove. Present does not mean a wearer
 * *can* gesture — the watch may not support the action, or may have it switched off, which is
 * `GestureInputManager.isActionSupported` / `isActionEnabled` and needs an instance this has no
 * business creating. Absent, though, is conclusive: with no SDK there is no gesture source, which
 * is the only direction this decides.
 */
private val wearGestureSdkPresent: Boolean by lazy {
  try {
    Class.forName("com.google.wear.Sdk")
    true
  } catch (_: Throwable) {
    false
  }
}

/**
 * Whether a sticker should offer its own way to fire a gesture.
 *
 * Two conditions, and both are load-bearing:
 *
 * - **[catalogInteractive]** — never in a baked capture. The published PNG is a picture of the
 *   component, and a button the renderer put there would be in every one of them and in every kit
 *   comparison. This is the same live-lane gate `counted` uses.
 * - **[wearGestureSdkPresent] is false** — never on a real watch. There the wrist is the input and
 *   a fake button beside it would be the catalog teaching the wrong gesture.
 *
 * Which leaves exactly the case that needs it: a held Compose session on the preview server, where
 * the hint plays and nothing can answer it.
 */
@Composable
fun gestureActivationAvailable(): Boolean = catalogInteractive() && !wearGestureSdkPresent

/**
 * The stand-in for a wrist: one button per gesture the sticker has registered.
 *
 * Renders nothing at all unless [gestureActivationAvailable], so a baked capture and a real watch
 * are both untouched — the whole composable disappears rather than drawing disabled controls.
 *
 * The label names the GESTURE, not the action: a reader of a Wear catalog is looking up what a
 * double pinch does, and `Primary` is the API's word for it rather than the wearer's. `onGesture`
 * is the component's own suspend callback — the same one the framework invokes — so pressing this
 * runs the real thing and not a preview's imitation of it.
 */
@Composable
fun GestureActivation(
  action: OneHandedGestureAction,
  onGesture: suspend () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (!gestureActivationAvailable()) return
  val scope = rememberCoroutineScope()
  Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    CompactButton(
      onClick = { scope.launch { onGesture() } },
      colors = ButtonDefaults.filledTonalButtonColors(),
    ) {
      Text(if (action == OneHandedGestureAction.Dismiss) "Wrist turn" else "Double pinch")
    }
  }
}
