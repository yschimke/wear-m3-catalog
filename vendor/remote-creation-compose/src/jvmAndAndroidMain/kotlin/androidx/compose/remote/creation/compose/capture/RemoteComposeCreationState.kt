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
@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)

@file:JvmName("RemoteComposeCreationStateKt")
@file:JvmMultifileClass

package androidx.compose.remote.creation.compose.capture

import androidx.annotation.RestrictTo
import androidx.collection.MutableIntObjectMap
import androidx.collection.MutableObjectIntMap
import androidx.compose.remote.creation.common.CoreDocument
import androidx.compose.remote.creation.common.RemoteWriter
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteInt
import androidx.compose.remote.creation.compose.state.BaseRemoteState
import androidx.compose.remote.creation.compose.state.RemoteStateCacheKey
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.profile.Profile
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.LayoutDirection

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public open class RemoteComposeCreationState : RemoteStateScope {

    override val parentScope: RemoteComposeCreationState
        get() = this

    public val creationDisplayInfo: RemoteCreationDisplayInfo
    public val profile: Profile
    public override lateinit var remoteDensity: RemoteDensity
    public override lateinit var layoutDirection: LayoutDirection
    public final override val densityBehavior: RemoteDensityBehavior

    public val expressionCache: MutableIntObjectMap<RemoteFloat> = MutableIntObjectMap()
    public val intExpressionCache: MutableIntObjectMap<RemoteInt> = MutableIntObjectMap()
    public var ready: Boolean = true
    public override lateinit var document: RemoteComposeWriter
    internal val writer: RemoteWriter
        get() = LegacyRemoteWriterAdapter(document)
    internal val remoteVariableToId: MutableObjectIntMap<RemoteStateCacheKey> =
        MutableObjectIntMap()
    internal val floatArrayCache: HashMap<RemoteStateCacheKey, FloatArray> = HashMap()
    internal val longArrayCache: HashMap<RemoteStateCacheKey, LongArray> = HashMap()
    private val globalDeclarations = LinkedHashMap<RemoteStateCacheKey, BaseRemoteState<*>>()

    internal fun enqueueGlobalDeclaration(state: BaseRemoteState<*>) {
        globalDeclarations[state.cacheKey] = state
    }

    internal fun drainGlobalDeclarations(program: RemoteDocumentProgram) {
        globalDeclarations.values.forEach { state ->
            program.recordDeclaration { state.getIdForCreationState(this) }
        }
        globalDeclarations.clear()
    }

    internal inline fun getOrPutFloatArray(
        key: RemoteStateCacheKey,
        crossinline compute: () -> FloatArray,
    ): FloatArray = floatArrayCache.getOrPut(key) { compute() }

    internal inline fun getOrPutLongArray(
        key: RemoteStateCacheKey,
        crossinline compute: () -> LongArray,
    ): LongArray = longArrayCache.getOrPut(key) { compute() }

    internal inline fun getOrPutVariableId(
        key: RemoteStateCacheKey,
        crossinline compute: () -> Int,
    ): Int {
        val id = remoteVariableToId.getOrDefault(key, -1)
        if (id == -1) {
            val nextId = compute()
            remoteVariableToId.put(key, nextId)
            return nextId
        }
        return id
    }

    public val time: MutableState<Long> = mutableLongStateOf(0L)

    public val platform: RcPlatformServices
        get() = profile.platform

    internal val platformImageProvider: PlatformImageProvider

    public fun addBitmap(image: ImageBitmap): Int = platformImageProvider.addBitmap(document, image)

    public fun addNamedBitmap(name: String, image: ImageBitmap): Int =
        platformImageProvider.addNamedBitmap(document, name, image)

    public constructor(
        creationDisplayInfo: RemoteCreationDisplayInfo,
        profile: Profile,
        writerEvents: Any?,
        remoteDensity: RemoteDensity = RemoteDensity.from(creationDisplayInfo),
        layoutDirection: LayoutDirection,
        platformImageProvider: PlatformImageProvider,
    ) {
        this.creationDisplayInfo = creationDisplayInfo
        this.profile = profile
        this.document = profile.create(creationDisplayInfo.toCreationDisplayInfo(), writerEvents)
        this.remoteDensity = remoteDensity
        this.layoutDirection = layoutDirection
        this.densityBehavior = creationDisplayInfo.densityBehavior
        this.platformImageProvider = platformImageProvider
    }

    public constructor(
        creationDisplayInfo: RemoteCreationDisplayInfo,
        contentDescription: String?,
        profile: Profile,
        platformImageProvider: PlatformImageProvider,
    ) : this(
        creationDisplayInfo = creationDisplayInfo,
        profile = profile,
        writer = profile.create(creationDisplayInfo.toCreationDisplayInfo(), null),
        platformImageProvider = platformImageProvider,
    )

    public constructor(
        creationDisplayInfo: RemoteCreationDisplayInfo,
        profile: Profile,
        writer: RemoteComposeWriter,
        platformImageProvider: PlatformImageProvider,
    ) {
        this.creationDisplayInfo = creationDisplayInfo
        this.profile = profile
        this.document = writer
        this.remoteDensity = RemoteDensity.from(creationDisplayInfo)
        this.layoutDirection = LayoutDirection.Ltr
        this.densityBehavior = creationDisplayInfo.densityBehavior
        this.platformImageProvider = platformImageProvider
    }

    public constructor(
        size: Size,
        profile: Profile,
        platformImageProvider: PlatformImageProvider,
    ) : this(
        creationDisplayInfo =
            RemoteCreationDisplayInfo(size.width.toInt(), size.height.toInt(), 160, 1.0f),
        profile = profile,
        writer =
            profile.create(
                RemoteCreationDisplayInfo(size.width.toInt(), size.height.toInt(), 160, 1.0f)
                    .toCreationDisplayInfo(),
                null,
            ),
        platformImageProvider = platformImageProvider,
    )
}

private object NoOpPlatformImageProvider : PlatformImageProvider {
    override fun addBitmap(document: RemoteComposeWriter, image: ImageBitmap): Int = -1

    override fun addNamedBitmap(
        document: RemoteComposeWriter,
        name: String,
        image: ImageBitmap,
    ): Int = -1
}

// Density and Size should be taken from Compose in this mode
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class NoRemoteCompose :
    RemoteComposeCreationState(
        RemoteCreationDisplayInfo(1, 1, 160, 1.0f),
        null,
        Profile(
            CoreDocument.DOCUMENT_API_LEVEL,
            0,
            RcPlatformServices.None,
            { creationDisplayInfo, profile, callback ->
                RemoteComposeWriter(creationDisplayInfo, null, profile, callback)
            },
        ),
        NoOpPlatformImageProvider,
    )

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface PlatformImageProvider {
    public fun addBitmap(document: RemoteComposeWriter, image: ImageBitmap): Int

    public fun addNamedBitmap(document: RemoteComposeWriter, name: String, image: ImageBitmap): Int
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public val LocalRemoteComposeCreationState: ProvidableCompositionLocal<RemoteComposeCreationState> =
    compositionLocalOf {
        NoRemoteCompose()
    }
