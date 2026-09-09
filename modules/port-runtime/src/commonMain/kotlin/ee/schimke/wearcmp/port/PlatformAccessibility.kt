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

package ee.schimke.wearcmp.port

/**
 * The user's reduce-motion preference — upstream `Settings.Global "reduce_motion"`, read through a
 * `ContentResolver` and a `ContentObserver` that neither wasm nor the JVM has.
 *
 * Wear Compose consults it to decide whether to animate at all, so it is not cosmetic: getting it
 * wrong means a watch face that animates for someone who asked it not to. The browser has the same
 * preference under a different name, and answers it honestly; see the wasmJs actual.
 */
public expect fun platformReduceMotion(): Boolean

/**
 * Whether a touch-exploration screen reader is driving the UI — upstream Android's
 * `AccessibilityManager.isTouchExplorationEnabled`.
 *
 * There is no equivalent question in a browser: assistive technology on the web does not announce
 * itself, by design. So the honest answer off-Android is `false`, and a host that knows better —
 * a preview harness portraying TalkBack, say — overrides
 * `LocalTouchExplorationStateProvider` rather than this.
 */
public expect fun platformTouchExplorationEnabled(): Boolean
