/*
 * Copyright 2026 Yuri Schimke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ee.schimke.composeai.horologist.lottie

import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.runtime.Composable
import com.google.android.horologist.remotecompose.lottie.LottieAnimation

/**
 * The call the UI builder's Wear `LottiePlayer` element generates, compiled.
 *
 * Not a test — there is nothing to assert at runtime. It is a *compilation*: this is verbatim the
 * shape `RemoteContentEmitter` in `yschimke/compose-preview-server` writes into a generated widget
 * (`ui-builder-export/.../RemoteContentEmitter.kt`, the `remote-m3/lottie` branch), so if the
 * vendored compiler's entry point renames an argument, moves package, or stops taking a
 * `RemoteFloat` progress, this file stops compiling here rather than in the file somebody pasted
 * into their app.
 *
 * Ours rather than upstream's, which is why it is in our package and carries our copyright: the
 * vendored `src/main` is verbatim Horologist and `PROVENANCE.md` says so.
 */
@RemoteComposable
@Composable
private fun ExportedCallShape() {
  LottieAnimation(json = LOTTIE_ANIMATION, modifier = RemoteModifier.fillMaxSize())
  LottieAnimation(json = LOTTIE_ANIMATION, progress = 0.rf)
}

private const val LOTTIE_ANIMATION =
  "{\"v\":\"5.9.6\",\"fr\":30,\"ip\":0,\"op\":30,\"w\":64,\"h\":64,\"layers\":[]}"
