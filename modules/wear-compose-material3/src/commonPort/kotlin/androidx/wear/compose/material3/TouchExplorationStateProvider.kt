/*
 * Copyright 2026 The Android Open Source Project
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

package androidx.wear.compose.material3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import ee.schimke.wearcmp.port.platformTouchExplorationEnabled

/*
 * Replaces the generated `TouchExplorationStateProvider.kt`, which is excluded in
 * `transform-rules.json`.
 *
 * A patch would have been the wrong tool: the upstream file is 138 lines of `AccessibilityManager`
 * listener plumbing wrapped around a one-method interface, and a patch that deletes 120 of 138
 * lines has to be re-cut every time any of them changes. The INTERFACE is the contract the rest of
 * the module compiles against — `LocalTouchExplorationStateProvider` and `touchExplorationState()`
 * — and it is stable, so it is restated here in full and left alone.
 *
 * Behaviour off-Android: always `false`. See `platformTouchExplorationEnabled` for why that is the
 * honest answer rather than a stub, and how a host that knows better says so.
 */

/**
 * A functional interface for providing the state of touch exploration services. It is strongly
 * discouraged to make logic conditional based on state of accessibility services. Please consult
 * with accessibility experts before making such change.
 */
internal fun interface TouchExplorationStateProvider {

    /**
     * Returns the touch exploration service state wrapped in a [State] to allow composables to
     * attach the state to itself.
     */
    @Composable public fun touchExplorationState(): State<Boolean>
}

/** The default implementation, which asks the platform once. */
internal class DefaultTouchExplorationStateProvider : TouchExplorationStateProvider {
    @Composable
    override fun touchExplorationState(): State<Boolean> = remember {
        mutableStateOf(platformTouchExplorationEnabled())
    }
}

/** CompositionLocal to provide a means to override TouchExplorationStateProvider during testing */
internal val LocalTouchExplorationStateProvider:
    ProvidableCompositionLocal<TouchExplorationStateProvider> =
    staticCompositionLocalOf {
        DefaultTouchExplorationStateProvider()
    }
