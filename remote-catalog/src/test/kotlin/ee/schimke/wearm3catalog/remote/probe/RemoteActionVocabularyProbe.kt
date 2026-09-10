package ee.schimke.wearm3catalog.remote.probe

import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteBoolean
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteFloat
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteInt
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteString
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.ri
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable

/**
 * What a design's ACTIONS have to become, compiled rather than guessed.
 *
 * Every action a UI-builder document can carry is a state write — `set`, `select` and `setText`
 * assign, `toggle` negates, `selectOrClear` assigns or clears — and each names a variable declared
 * in the document's `stateVariables`. Nothing in the model calls out to the host, so `hostAction`
 * maps to nothing that exists and every one of them is a `valueChange`.
 *
 * Sibling of `RemoteValueVocabularyProbe`, and the same trick: no test method, no assertion. It is
 * compiled by `compileDebugUnitTestKotlin` against the creation DSL, and compiling is the
 * assertion.
 *
 * | Document action              | Remote                      |
 * |------------------------------|-----------------------------|
 * | `set` / `select` / `setText` | `valueChange(v, <literal>)` |
 * | `toggle` on a boolean        | `valueChange(b, !b)`        |
 * | `selectOrClear`              | **nothing** — see below     |
 *
 * All four state types have a mutable remote form, which is what a generated widget declares for
 * each of the document's variables, and `valueChange` takes both a literal and an expression
 * reference — `(i + 1).createReference()` is how the catalog's own counter sticker writes one.
 *
 * `selectOrClear` is the exception and it is a refusal rather than a gap in this file:
 * `valueChange(s, null)` does not compile, because the second parameter is a non-null
 * `RemoteState<T>`. A design that clears a selection and one that sets it to a sentinel are
 * different designs, so the emitter refuses it by name instead of picking one.
 */
@RemoteComposable
@Composable
fun remoteActionVocabularyProbe() {
  val i = rememberMutableRemoteInt(0)
  val f = rememberMutableRemoteFloat(0f)
  val b = rememberMutableRemoteBoolean(false)
  val s = rememberMutableRemoteString("")

  // `set` / `select` / `setText`, one per state type.
  valueChange(i, 5.ri)
  valueChange(f, 0.5f.rf)
  valueChange(b, true.rb)
  valueChange(s, "x".rs)

  // `toggle`, and the expression form the catalog's own stickers use for a counted or animated
  // value — both are `valueChange`, so a document that grows arithmetic needs no new mechanism.
  valueChange(b, !b)
  valueChange(f, (1f.rf - f).createReference())
  valueChange(i, (i + 1).createReference())
}
