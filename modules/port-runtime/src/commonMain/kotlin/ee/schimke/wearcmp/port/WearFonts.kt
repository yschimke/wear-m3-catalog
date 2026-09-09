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
 * Fonts the host has supplied, by the name the Wear type scale asks for.
 *
 * The Wear type scale is declared in terms of a font installed on the device — `roboto-flex`, the
 * variable font Wear OS ships. Off-Android there is no device font table to look it up in, so
 * something has to put the bytes where [deviceFontFamily] can find them, and only the host knows
 * where its fonts come from: a jar resource, a `@font-face`, a fetch.
 *
 * Register before the first composition; a family resolved once is cached for the life of the
 * process, so a font that arrives late will not be picked up by text already drawn.
 *
 *     WearFonts.register(WearFonts.RobotoFlex, robotoFlexBytes)
 *
 * The `wear-compose-fonts` artifact carries Roboto Flex and registers it for you on the JVM.
 */
public expect object WearFonts {
    /** The family name the Wear type scale asks for. */
    public val RobotoFlex: String

    /** Make [data] — the bytes of a font file — available as [familyName]. */
    public fun register(familyName: String, data: ByteArray)

    /** Whether anything is registered under [familyName]. */
    public fun isRegistered(familyName: String): Boolean
}
