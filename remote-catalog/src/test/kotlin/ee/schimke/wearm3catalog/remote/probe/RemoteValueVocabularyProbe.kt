package ee.schimke.wearm3catalog.remote.probe

import androidx.compose.remote.creation.compose.action.lambdaAction
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.state.asRemoteTextUnit
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText

/**
 * The expressions a generator would have to write to call this catalog's components — compiled, not
 * guessed.
 *
 * A `remote-material3` component takes Remote Compose values rather than Kotlin ones:
 * `RemoteText(text: RemoteString)`, `RemoteButton(onClick: Action)`. That is why the component
 * record refuses a call site for 25 of this module's 27 components, and why `RemoteContentEmitter`
 * writes the eleven it knows by hand. The cost of the rest is six value types, not twenty-five
 * components — measured in `PublishedRemoteM3CatalogEquivalenceTest` over in
 * compose-preview-server.
 *
 * This file is the other half of that measurement: whether the spellings a mapper would emit are
 * real. It has no test method and asserts nothing. It is compiled by `compileDebugUnitTestKotlin`
 * against remote-material3 and the creation DSL, and compiling **is** the assertion — the same
 * trick as the generated widget beside it, one level down.
 *
 * What it establishes, each verified rather than assumed:
 * - `"…".rs`, `true.rb`, `0.5f.rf` build a `RemoteString`, `RemoteBoolean` and `RemoteFloat` —
 *   three of the six types blocking a call site, covering twelve components between them;
 * - **`22.rsp` is a `RemoteTextUnit`**, and the receiver is an `Int` — `.rsp` takes whole sizes
 *   only, which is why the fractional one below exists rather than a second `.rsp`. This is the
 *   type that decides whether a design keeps its authored text size: the preview server derives a
 *   published catalog's properties from the record and drops every parameter it has no JSON type
 *   for, so while `RemoteTextUnit` was unmapped `remote-m3/remote-text` declared `text`, `color`
 *   and `maxLines` and nothing else
 *   ([compose-preview-server#844](https://github.com/yschimke/compose-preview-server/pull/844));
 * - **`RemoteMaterialTheme.typography.<role>` is a `RemoteTextStyle`**, which is how a design names
 *   a type scale rather than a size. `RemoteContentEmitter` already writes this for the borrowed
 *   `m3/text`, and the published component is where it has to keep working;
 * - **`14.5f.sp.asRemoteTextUnit()` is a fractional size**, and it exists because `.rsp` cannot be
 *   one: the extension is `val Int.rsp` and there is no `Float.rsp` anywhere in this library. A
 *   generator that writes `14.5f.rsp` emits source that does not compile, which is what the preview
 *   server did until this spelling was proved here — so a half-size is carried rather than refused;
 * - `Color(0xFF6750A4).rc` is a `RemoteColor`, which is how a design's colour travels;
 * - **`lambdaAction {}` is a legal `Action`**, which is the one that matters most. Nine components
 *   require an `Action` and no design carries one, so "what does a generator write for `onClick`"
 *   looked like a product decision. It has an answer that compiles: a design that says nothing
 *   about behaviour emits an action that does nothing.
 *
 * Left over, and genuinely unknown: `RemotePageIndicatorState` (two components), `ImageVector`
 * (one), and one callable that is not public or internal and so cannot be called from a generated
 * file at all. Four of the twenty-five, against twenty-one this vocabulary reaches.
 *
 * A line removed from here is a claim withdrawn, so remove one only when the emitter stops needing
 * that spelling.
 */
@RemoteComposable
@Composable
fun remoteValueVocabularyProbe() {
  RemoteText(text = "a design's string".rs)
  RemoteText(text = "coloured".rs, color = Color(0xFF6750A4).rc)
  RemoteText(text = "sized".rs, fontSize = 22.rsp)
  RemoteText(text = "scaled".rs, style = RemoteMaterialTheme.typography.bodyMedium)
  RemoteText(text = "half a point".rs, fontSize = 14.5f.sp.asRemoteTextUnit())
  RemoteButton(onClick = lambdaAction {}, enabled = true.rb) { RemoteText(text = "label".rs) }
  @Suppress("UNUSED_EXPRESSION") 0.5f.rf
}
