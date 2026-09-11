@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.runtime.Composable
import com.google.android.horologist.remotecompose.lottie.LottieAnimation
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// ---------------------------------------------------------------------------
// A Lottie animation, COMPILED INTO the document rather than played from it.
//
// This is the one sticker in the catalog whose point is what the export does, not what the picture
// shows. `LottieAnimation(json = …)` is Horologist's Lottie **compiler**
// (`third_party/horologist-lottie`, vendored — see its PROVENANCE.md): a `@RemoteComposable` that
// parses the animation once, at document-build time, and re-emits every layer, shape and keyframe
// as Remote Compose operations over the document's own animation clock. What ships is a `.rc`
// document that draws the animation — no Lottie runtime on the device, no JSON, no fetch.
//
// So this belongs to the Remote column and to no other. The Wear column's answer to "play a
// Lottie" is `lottie-compose`, a different library with a different API that plays the JSON at
// runtime; the two are not the same component wearing two hats, which is why this sticker declares
// no `parallel`.
//
// WHY THE CATALOG DRAWS ONE AT ALL
//
// The UI builder's shelf has offered `remote-m3/lottie` since the synthesised catalog existed, and
// `RemoteContentEmitter` has always known how to write it. The published catalog could not offer
// it, because a published component needs a component RECORD and a record needs a call site — and
// this catalog had none. It was the last component the published shelf lost to the synthesised one
// (yschimke/compose-preview-server#674). This sticker is that call site.
//
// It is therefore load-bearing twice: it draws a cell on the compare page, and its mere existence
// is what lets a design place a Lottie node against the published catalog.
//
// THE ANIMATION IS OURS
//
// Hand-authored rather than taken from a library, for two reasons. A third-party animation carries
// a licence question into a repository that vendors nothing else by copy-paste; and the emitter
// inlines the JSON into generated Kotlin as a string literal under a 64KiB budget, which most
// real-world animations blow through immediately. [PULSE_DOTS] is 1.4KiB and exercises the paths
// that matter — shape layer, groups, ellipses, fills, keyframed transforms — rather than being a
// single flat colour that would compile through the renderer without touching most of it.
// ---------------------------------------------------------------------------

/**
 * Three dots pulsing in sequence: a glanceable "working" state, the commonest thing a Wear widget
 * animates.
 *
 * 96x96 at 30fps over a 40-frame loop. The three groups differ only in when their scale keyframes
 * start — 0, 6 and 12 — which is what reads as a wave rather than three dots breathing together.
 * Every keyframe list starts at frame 0 and ends at frame 40 on the same value, so the loop does
 * not jump when the clock wraps.
 *
 * Compact JSON with no whitespace, deliberately: this same literal is what
 * `RemoteContentEmitter.lottie` would write into a generated widget, and pretty-printing is most of
 * the bytes of a Lottie file.
 */
private const val PULSE_DOTS =
  """{"v":"5.9.6","fr":30,"ip":0,"op":40,"w":96,"h":96,"nm":"pulse-dots","layers":[{"ty":4,""" +
    """"nm":"dots","ind":1,"ip":0,"op":40,"st":0,"ks":{"a":{"a":0,"k":[0,0]},"p":{"a":0,""" +
    """"k":[0,0]},"r":{"a":0,"k":0},"s":{"a":0,"k":[100,100]},"o":{"a":0,"k":100}},"shapes":[""" +
    """{"ty":"gr","nm":"dot-left","it":[{"ty":"el","nm":"ellipse","p":{"a":0,"k":[0,0]},""" +
    """"s":{"a":0,"k":[18,18]}},{"ty":"fl","nm":"fill","c":{"a":0,""" +
    """"k":[0.647,0.549,0.937,1]},"o":{"a":0,"k":100}},{"ty":"tr","a":{"a":0,"k":[0,0]},""" +
    """"p":{"a":0,"k":[26,48]},"r":{"a":0,"k":0},"s":{"a":1,"k":[{"t":0,"s":[55,55]},""" +
    """{"t":8,"s":[100,100]},{"t":16,"s":[55,55]},{"t":40,"s":[55,55]}]},""" +
    """"o":{"a":0,"k":100}}]},{"ty":"gr","nm":"dot-centre","it":[{"ty":"el","nm":"ellipse",""" +
    """"p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[18,18]}},{"ty":"fl","nm":"fill","c":{"a":0,""" +
    """"k":[0.647,0.549,0.937,1]},"o":{"a":0,"k":100}},{"ty":"tr","a":{"a":0,"k":[0,0]},""" +
    """"p":{"a":0,"k":[48,48]},"r":{"a":0,"k":0},"s":{"a":1,"k":[{"t":0,"s":[55,55]},""" +
    """{"t":6,"s":[55,55]},{"t":14,"s":[100,100]},{"t":22,"s":[55,55]},""" +
    """{"t":40,"s":[55,55]}]},"o":{"a":0,"k":100}}]},{"ty":"gr","nm":"dot-right","it":[""" +
    """{"ty":"el","nm":"ellipse","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[18,18]}},{"ty":"fl",""" +
    """"nm":"fill","c":{"a":0,"k":[0.647,0.549,0.937,1]},"o":{"a":0,"k":100}},{"ty":"tr",""" +
    """"a":{"a":0,"k":[0,0]},"p":{"a":0,"k":[70,48]},"r":{"a":0,"k":0},"s":{"a":1,""" +
    """"k":[{"t":0,"s":[55,55]},{"t":12,"s":[55,55]},{"t":20,"s":[100,100]},""" +
    """{"t":28,"s":[55,55]},{"t":40,"s":[55,55]}]},"o":{"a":0,"k":100}}]}]}]}"""

/** The drawn side, matching the animation's own 96x96 so nothing is scaled on the way out. */
private const val LOTTIE_SIDE_DP = 96

/**
 * Which frame a still render freezes on, keyed by the knob's value.
 *
 * `run` is the one that is not a frame at all. Leaving `progress` out is what makes the compiled
 * document drive itself from `ANIMATION_TIME` — the animation loops forever on a device — and it is
 * the default because a widget that animates is the point of the component. A still PNG of that is
 * whatever frame the renderer sampled, which is why the pinned variants exist beside it: they are
 * the ones a reader can compare between two renders.
 *
 * The three pinned values are the loop's own shape rather than arbitrary: the left dot peaks at
 * frame 8 (0.2), the centre at 14 (0.35), the right at 20 (0.5).
 */
private val LOTTIE_FRAMES: Map<String, Float?> =
  linkedMapOf("run" to null, "left-peak" to 0.2f, "centre-peak" to 0.35f, "right-peak" to 0.5f)

/** The variant a render with no seeded override draws. */
private const val BASE_FRAME = "run"

@CatalogComponent(
  id = "Lottie/PulseDots",
  group = "Content",
  caption =
    "A Lottie animation compiled into the RemoteDocument by Horologist's Lottie compiler, so the " +
      "animation plays with no Lottie runtime on the device.",
)
@CatalogRemoteModes
@OverrideVariant(name = "left-peak", strings = ["frame=left-peak"])
@OverrideVariant(name = "centre-peak", strings = ["frame=centre-peak"])
@OverrideVariant(name = "right-peak", strings = ["frame=right-peak"])
@Composable
fun LottiePulseDotsRemote() = RemoteSticker {
  val choice = previewOverrideChoice("frame", BASE_FRAME, LOTTIE_FRAMES.keys.toList())
  // `progress` is a `RemoteFloat?` and the null branch is not a fallback — it is the running
  // animation, which is a different document rather than a missing argument. Written as two calls
  // rather than one with a nullable argument so each shape is the one the exporter writes.
  when (val frame = LOTTIE_FRAMES[choice]) {
    null ->
      LottieAnimation(
        json = PULSE_DOTS,
        modifier = RemoteModifier.size(LOTTIE_SIDE_DP.rdp, LOTTIE_SIDE_DP.rdp),
      )
    else ->
      LottieAnimation(
        json = PULSE_DOTS,
        modifier = RemoteModifier.size(LOTTIE_SIDE_DP.rdp, LOTTIE_SIDE_DP.rdp),
        progress = frame.rf,
      )
  }
}
