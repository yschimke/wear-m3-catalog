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

package ee.schimke.wearcmp.fonts

/**
 * Register the bundled Roboto Flex with `WearFonts`, so that Wear's type scale renders in the
 * typeface it was designed for instead of a fallback sans-serif.
 *
 * Call once, before the first composition. Returns whether the font was registered: `false` means
 * this platform has no bundled copy, and the type scale falls back — see the platform note below,
 * because that is the normal answer in a browser.
 */
public expect fun installBundledWearFonts(): Boolean
