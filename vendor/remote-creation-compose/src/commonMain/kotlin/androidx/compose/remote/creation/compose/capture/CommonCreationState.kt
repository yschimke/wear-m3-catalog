/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@file:kotlin.jvm.JvmName("RemoteComposeCreationStateKt")
@file:kotlin.jvm.JvmMultifileClass

package androidx.compose.remote.creation.compose.capture

import androidx.annotation.RestrictTo
import androidx.collection.MutableIntObjectMap
import androidx.collection.MutableObjectIntMap
import androidx.compose.remote.creation.common.RemoteDocumentWriter
import androidx.compose.remote.creation.common.RemoteWriter
import androidx.compose.remote.creation.compose.state.BaseRemoteState
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteInt
import androidx.compose.remote.creation.compose.state.RemoteStateCacheKey
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.LayoutDirection

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public open class RemoteComposeCreationState(
    public val creationDisplayInfo: RemoteCreationDisplayInfo,
    public final override val writer: RemoteWriter,
    public override var remoteDensity: RemoteDensity = RemoteDensity.from(creationDisplayInfo),
    public override var layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    public val supportedOperations: Set<Int> = emptySet(),
    private val platformImageProvider: PlatformImageProvider = NoOpPlatformImageProvider,
    internal var platformState: Any? = null,
) : RemoteComposeCreationContext {
    override val parentScope: RemoteComposeCreationState
        get() = this

    public final override val densityBehavior: RemoteDensityBehavior =
        creationDisplayInfo.densityBehavior
    public override val expressionCache: MutableIntObjectMap<RemoteFloat> = MutableIntObjectMap()
    public override val intExpressionCache: MutableIntObjectMap<RemoteInt> = MutableIntObjectMap()
    public var ready: Boolean = true
    internal val remoteVariableToId: MutableObjectIntMap<RemoteStateCacheKey> =
        MutableObjectIntMap()
    internal val floatArrayCache: HashMap<RemoteStateCacheKey, FloatArray> = HashMap()
    internal val longArrayCache: HashMap<RemoteStateCacheKey, LongArray> = HashMap()
    private val globalDeclarations = LinkedHashMap<RemoteStateCacheKey, BaseRemoteState<*>>()
    private var nextComponentId = -1_000_000
    internal var componentCacheKey: Int = 0
        private set

    internal fun allocateComponentId(): Int = nextComponentId--

    internal fun enterComponentScope(componentId: Int): Int {
        val previous = componentCacheKey
        componentCacheKey = componentId
        return previous
    }

    internal fun restoreComponentScope(previous: Int) {
        componentCacheKey = previous
    }

    public override fun enqueueGlobalDeclaration(state: BaseRemoteState<*>) {
        globalDeclarations[state.cacheKey] = state
    }

    internal fun drainGlobalDeclarations(record: (() -> Unit) -> Unit) {
        globalDeclarations.values.forEach { state ->
            record { state.getIdForCreationState(this) }
        }
        globalDeclarations.clear()
    }

    public override fun getOrPutFloatArray(key: Any, compute: () -> FloatArray): FloatArray =
        floatArrayCache.getOrPut(key as RemoteStateCacheKey) { compute() }

    public override fun getOrPutLongArray(key: Any, compute: () -> LongArray): LongArray =
        longArrayCache.getOrPut(key as RemoteStateCacheKey) { compute() }

    public override fun getOrPutVariableId(key: Any, compute: () -> Int): Int {
        key as RemoteStateCacheKey
        val id = remoteVariableToId.getOrDefault(key, -1)
        if (id != -1) return id
        return compute().also { remoteVariableToId.put(key, it) }
    }

    public override fun hasVariableId(key: Any): Boolean =
        remoteVariableToId.contains(key as RemoteStateCacheKey)

    public val time: MutableState<Long> = mutableLongStateOf(0L)

    public override fun addBitmap(image: ImageBitmap): Int = platformImageProvider.addBitmap(image)

    public override fun addNamedBitmap(name: String, image: ImageBitmap): Int =
        platformImageProvider.addNamedBitmap(name, image)
}

private object NoOpPlatformImageProvider : PlatformImageProvider {
    override fun addBitmap(image: ImageBitmap): Int = -1

    override fun addNamedBitmap(name: String, image: ImageBitmap): Int = -1
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class NoRemoteCompose :
    RemoteComposeCreationState(
        creationDisplayInfo = RemoteCreationDisplayInfo(1, 1, 160, 1.0f),
        writer = RemoteDocumentWriter(1, 1),
    )

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface PlatformImageProvider {
    public fun addBitmap(image: ImageBitmap): Int

    public fun addNamedBitmap(name: String, image: ImageBitmap): Int
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public val LocalRemoteComposeCreationState: ProvidableCompositionLocal<RemoteComposeCreationState> =
    compositionLocalOf { NoRemoteCompose() }
