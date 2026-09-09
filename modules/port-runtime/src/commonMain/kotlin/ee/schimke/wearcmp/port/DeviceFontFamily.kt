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

import androidx.compose.ui.text.font.FontFamily

/**
 * A font family named the way the platform names it — Android's `DeviceFontFamilyName`, which asks
 * the system font provider for a font installed on the device.
 *
 * The Wear type scale is declared in terms of one: `roboto-flex`, the variable font Wear ships. So
 * this is not a nicety — without it every ported text style silently falls back to a generic
 * sans-serif, and the catalog draws in the wrong typeface at the right size.
 *
 * Returns [FontFamily.SansSerif] when the host has no font by that name, which is the honest
 * fallback and what a browser without the font will do anyway.
 */
public expect fun deviceFontFamily(name: String): FontFamily
