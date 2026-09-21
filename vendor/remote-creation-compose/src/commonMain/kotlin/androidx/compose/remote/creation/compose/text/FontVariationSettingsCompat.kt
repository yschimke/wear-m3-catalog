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

package androidx.compose.remote.creation.compose.text

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontVariation

/**
 * Bridges the `Font.variationSettings` API used by the AndroidX change until it reaches the
 * externally published Compose JVM artifacts. Member properties win over this extension once the
 * API is present, so this file can be deleted without changing callers after that update.
 */
internal val Font.variationSettings: FontVariation.Settings
  get() = platformVariationSettings(this)

internal expect fun platformVariationSettings(font: Font): FontVariation.Settings
