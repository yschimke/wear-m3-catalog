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

package androidx.wear.compose.material3.internal

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The format-string subset the resources use. Positional arguments are the reason this exists:
 * the date picker's content description is `"%1${'$'}s, %2${'$'}d"`, and translations reorder them.
 */
class FormatResourceTest {
    @Test
    fun `positional arguments are substituted in the order the string asks for`() {
        assertEquals(
            "March, 14",
            Strings.DatePickerContentDescription.textIn(Locale("en-US")).format("March", 14),
        )
    }

    @Test
    fun `sequential arguments are taken in order`() {
        assertEquals("a then b", Strings("t").let { "%s then %s" }.format("a", "b"))
    }

    @Test
    fun `a doubled percent is a literal`() {
        assertEquals("100% sure", "100%% sure".format())
    }

    @Test
    fun `an unknown conversion is passed through untouched`() {
        assertEquals("%q kept", "%q kept".format())
    }

    @Test
    fun `a missing argument yields empty rather than throwing`() {
        assertEquals("x  y", "x %s y".format())
    }

    /** The same entry point the string getters and `DatePicker` use. */
    private fun String.format(vararg args: Any): String = formatTemplate(this, args)
}
