/*
 * Copyright 2025 The Android Open Source Project
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

package androidx.compose.remote.creation.compose.modifier

import androidx.annotation.RestrictTo
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.common.RemoteModifierOperation
import androidx.compose.ui.semantics.Role

/**
 * SemanticsPropertyKey is the infrastructure for setting key/value pairs inside semantics block in
 * a type-safe way. Each key has one particular statically defined value type T.
 */
public class SemanticsPropertyKey<T>
internal constructor(
    /** The name of the property. Should be the same as the constant from which it is accessed. */
    public val name: String
) {
    override fun toString(): String {
        return "SemanticsPropertyKey: $name"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SemanticsPropertyKey<*>) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

/**
 * SemanticsPropertyReceiver is the scope provided by semantics {} blocks, letting you set key/value
 * pairs primarily via extension functions.
 */
public interface SemanticsPropertyReceiver {
    public operator fun <T> set(key: SemanticsPropertyKey<T>, value: T?)

    @Suppress("HiddenAbstractMethodInInterface")
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public operator fun <T> get(key: SemanticsPropertyKey<T>): T?
}

/** General semantics properties, mainly used for accessibility and testing. */
public object SemanticsProperties {
    public val ContentDescription: SemanticsPropertyKey<RemoteString> =
        SemanticsPropertyKey("ContentDescription")
    public val Role: SemanticsPropertyKey<Role> = SemanticsPropertyKey("Role")
    public val Text: SemanticsPropertyKey<RemoteString> = SemanticsPropertyKey("Text")
    public val StateDescription: SemanticsPropertyKey<RemoteString> =
        SemanticsPropertyKey("StateDescription")
    public val Enabled: SemanticsPropertyKey<Boolean> = SemanticsPropertyKey("Enabled")
}

/**
 * Developer-set content description of the semantics node, for use in testing, accessibility and
 * similar use cases.
 */
public var SemanticsPropertyReceiver.contentDescription: RemoteString?
    get() = get(SemanticsProperties.ContentDescription)
    set(value) {
        set(SemanticsProperties.ContentDescription, value)
    }

/** The type of user interface element. Accessibility services can use this to describe the node. */
public var SemanticsPropertyReceiver.role: Role?
    get() = get(SemanticsProperties.Role)
    set(value) {
        set(SemanticsProperties.Role, value)
    }

/** Text content for the semantics node. */
public var SemanticsPropertyReceiver.text: RemoteString?
    get() = get(SemanticsProperties.Text)
    set(value) {
        set(SemanticsProperties.Text, value)
    }

/** Description of the state for the semantics node. */
public var SemanticsPropertyReceiver.stateDescription: RemoteString?
    get() = get(SemanticsProperties.StateDescription)
    set(value) {
        set(SemanticsProperties.StateDescription, value)
    }

/** Whether the component is enabled. */
public var SemanticsPropertyReceiver.enabled: Boolean
    get() = get(SemanticsProperties.Enabled) ?: true
    set(value) {
        set(SemanticsProperties.Enabled, value)
    }

internal data class SemanticsModifier(
    val mergeMode: Int,
    val properties: Map<SemanticsPropertyKey<*>, Any?>,
) : RemoteModifier.Element {
    override fun RemoteStateScope.toRemoteModifierOperation(): RemoteModifierOperation =
        RemoteModifierOperation.Semantics(
            contentDescriptionId =
                (properties[SemanticsProperties.ContentDescription] as? RemoteString)?.id ?: 0,
            role = roleValue(properties[SemanticsProperties.Role] as? Role),
            textId = (properties[SemanticsProperties.Text] as? RemoteString)?.id ?: 0,
            stateDescriptionId =
                (properties[SemanticsProperties.StateDescription] as? RemoteString)?.id ?: 0,
            mode = mergeMode,
            enabled = properties[SemanticsProperties.Enabled] as? Boolean ?: true,
            clickable = false,
        )
}

private fun roleValue(role: Role?): Int {
    return when (role) {
        Role.Button -> 0
        Role.Checkbox -> 1
        Role.Switch -> 2
        Role.RadioButton -> 3
        Role.Tab -> 4
        Role.Image -> 5
        Role.DropdownList -> 6
        null -> -1
        else -> 9
    }
}

/**
 * Scope provided by semantics {} blocks, letting you set key/value pairs primarily via extension
 * functions.
 */
internal class AccessibilitySemantics : SemanticsPropertyReceiver {
    internal val props: MutableMap<SemanticsPropertyKey<*>, Any?> = mutableMapOf()

    override fun <T> set(key: SemanticsPropertyKey<T>, value: T?) {
        props[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: SemanticsPropertyKey<T>): T? {
        return props[key] as T?
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccessibilitySemantics) return false
        return props == other.props
    }

    override fun hashCode(): Int {
        return props.hashCode()
    }
}

/**
 * Clears the semantics of all descendants and sets new semantics.
 *
 * @param properties A lambda to configure the semantics.
 */
public fun RemoteModifier.clearAndSetSemantics(
    properties: SemanticsPropertyReceiver.() -> Unit
): RemoteModifier =
    then(SemanticsModifier(1, AccessibilitySemantics().apply(properties).props.toMap()))

/**
 * Adds semantics to the node.
 *
 * @param mergeDescendants Whether to merge the semantics of all descendants into this node.
 * @param properties A lambda to configure the semantics.
 */
public fun RemoteModifier.semantics(
    mergeDescendants: Boolean = false,
    properties: SemanticsPropertyReceiver.() -> Unit,
): RemoteModifier =
    then(
        SemanticsModifier(
            if (mergeDescendants) 2 else 0,
            AccessibilitySemantics().apply(properties).props.toMap(),
        )
    )
