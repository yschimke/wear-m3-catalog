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

package androidx.wear.compose.material3.onehandedgesture

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `Modifier.oneHandedGesture` against a manager that records what it is asked to do.
 *
 * This is the test the port did not have and could not have had: until the modifier was ported,
 * NOTHING in the library called [OneHandedGestureManager], so the interface was exercised only by
 * the fact that it compiled. What is checked here is the pairing — a registration must be handed
 * back when the node detaches, and the handle given back must be the handle that was issued.
 *
 * The View parameter upstream keys these calls on is gone; the registration handle replaces it, so
 * pairing is the thing that could plausibly break.
 */
@OptIn(ExperimentalTestApi::class)
class OneHandedGestureModifierTest {

    /** Records every call, and issues a distinguishable handle per registration. */
    private class RecordingGestureManager : OneHandedGestureManager {
        class Handle(val id: Int) : OneHandedGestureManager.GestureRegistration

        var registrations = 0
        val unregistered = mutableListOf<OneHandedGestureManager.GestureRegistration>()
        val issued = mutableListOf<Handle>()

        override fun registerGesture(
            haptic: HapticFeedback,
            gestureConfiguration: OneHandedGestureConfiguration,
            enabledInAmbient: Boolean,
            onGestureLabel: String?,
            onGestureAvailable: () -> Unit,
            onGesture: suspend (centerOffset: Offset) -> Unit,
            isActive: () -> Boolean,
            size: () -> IntSize,
        ): OneHandedGestureManager.GestureRegistration {
            val handle = Handle(registrations++)
            issued += handle
            return handle
        }

        override fun unregisterGesture(registration: OneHandedGestureManager.GestureRegistration) {
            unregistered += registration
        }

        val live: Int
            get() = issued.size - unregistered.size
    }

    private val configuration =
        OneHandedGestureConfiguration(
            action = OneHandedGestureAction.Primary,
            gestureId = "test-gesture",
        )

    @Composable
    private fun Subject(manager: OneHandedGestureManager) {
        CompositionLocalProvider(LocalOneHandedGestureManager provides manager) {
            Box(
                Modifier.size(48.dp)
                    .oneHandedGesture(
                        gestureConfiguration = configuration,
                        onGestureLabel = "test",
                        onGesture = {},
                    )
            )
        }
    }

    @Test
    fun registersOnceWhileComposed() = runComposeUiTest {
        val manager = RecordingGestureManager()
        setContent { Subject(manager) }
        waitForIdle()

        assertEquals(1, manager.registrations, "expected exactly one registration")
        assertTrue(manager.unregistered.isEmpty(), "nothing should be unregistered yet")
    }

    @Test
    fun handsTheRegistrationBackWhenTheNodeDetaches() = runComposeUiTest {
        val manager = RecordingGestureManager()
        var present by mutableStateOf(true)
        setContent { if (present) Subject(manager) }
        waitForIdle()
        assertEquals(1, manager.live, "one live registration while composed")

        present = false
        waitForIdle()

        assertEquals(0, manager.live, "the registration must be given back on detach")
        assertEquals(
            manager.issued.toList(),
            manager.unregistered.toList(),
            "the handle handed back must be the handle that was issued",
        )
    }

    /**
     * `LocalOneHandedGestureEnabled` is false, so the manager is never asked to register — and, the
     * part worth pinning, `unregisterGesture` is never called either. Upstream had a View to hand
     * over whether or not it had registered anything; the port has null, and calling
     * `unregisterGesture(null)` would be a bug the type system does not catch.
     */
    @Test
    fun neverUnregistersSomethingItDidNotRegister() = runComposeUiTest {
        val manager = RecordingGestureManager()
        var present by mutableStateOf(true)
        setContent {
            CompositionLocalProvider(LocalOneHandedGestureEnabled provides false) {
                if (present) Subject(manager)
            }
        }
        waitForIdle()
        present = false
        waitForIdle()

        assertEquals(0, manager.registrations, "disabled: nothing should register")
        assertTrue(manager.unregistered.isEmpty(), "disabled: nothing should unregister either")
    }

    /** The default manager registers nothing, so a host that provides none is simply inert. */
    @Test
    fun theDefaultManagerIsInert() = runComposeUiTest {
        setContent {
            Box(
                Modifier.size(48.dp)
                    .oneHandedGesture(
                        gestureConfiguration = configuration,
                        onGestureLabel = "test",
                        onGesture = {},
                    )
            )
        }
        waitForIdle()
    }
}
