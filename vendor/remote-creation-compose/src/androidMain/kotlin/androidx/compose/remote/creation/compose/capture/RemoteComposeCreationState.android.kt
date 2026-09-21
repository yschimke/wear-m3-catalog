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
import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.RemoteComposeWriterAndroid
import androidx.compose.remote.creation.profile.Profile
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.LayoutDirection

internal class AndroidPlatformImageProvider(private val document: RemoteComposeWriter) :
    PlatformImageProvider {
    override fun addBitmap(image: ImageBitmap): Int = document.addBitmap(image.asAndroidBitmap())

    override fun addNamedBitmap(name: String, image: ImageBitmap): Int =
        document.addNamedBitmap(name, image.asAndroidBitmap())
}

@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemoteComposeCreationState(
    creationDisplayInfo: RemoteCreationDisplayInfo,
    profile: Profile,
    writerEvents: WriterEvents?,
    remoteDensity: RemoteDensity = RemoteDensity.from(creationDisplayInfo),
    layoutDirection: LayoutDirection,
): RemoteComposeCreationState {
    val document = profile.create(creationDisplayInfo.toCreationDisplayInfo(), writerEvents)
    return legacyCreationState(
        creationDisplayInfo,
        profile,
        document,
        remoteDensity,
        layoutDirection,
        AndroidPlatformImageProvider(document),
    )
}

@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemoteComposeCreationState(
    creationDisplayInfo: RemoteCreationDisplayInfo,
    contentDescription: String?,
    profile: Profile,
): RemoteComposeCreationState {
    val document = profile.create(creationDisplayInfo.toCreationDisplayInfo(), null)
    return legacyCreationState(
        creationDisplayInfo,
        profile,
        document,
        platformImageProvider = AndroidPlatformImageProvider(document),
    )
}

@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemoteComposeCreationState(
    creationDisplayInfo: RemoteCreationDisplayInfo,
    profile: Profile,
    writer: RemoteComposeWriter,
): RemoteComposeCreationState =
    legacyCreationState(
        creationDisplayInfo,
        profile,
        writer,
        platformImageProvider = AndroidPlatformImageProvider(writer),
    )

@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemoteComposeCreationState(
    size: Size,
    profile: Profile,
): RemoteComposeCreationState {
    val info = RemoteCreationDisplayInfo(size.width.toInt(), size.height.toInt(), 160, 1.0f)
    val document = profile.create(info.toCreationDisplayInfo(), null)
    return legacyCreationState(
        info,
        profile,
        document,
        platformImageProvider = AndroidPlatformImageProvider(document),
    )
}

@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemoteComposeCreationState(
    platform: RcPlatformServices,
    size: Size,
): RemoteComposeCreationState {
    val profile =
        Profile(
            CoreDocument.DOCUMENT_API_LEVEL,
            0,
            platform,
            { creationDisplayInfo, prof, callback ->
                RemoteComposeWriterAndroid(creationDisplayInfo, null, prof, callback)
            },
        )
    val creationDisplayInfo =
        RemoteCreationDisplayInfo(size.width.toInt(), size.height.toInt(), 160, 1.0f)
    val document = RemoteComposeWriterAndroid(size.width.toInt(), size.height.toInt(), "", platform)
    return legacyCreationState(
        creationDisplayInfo,
        profile,
        document,
        platformImageProvider = AndroidPlatformImageProvider(document),
    )
}

@Suppress("FunctionName")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RemoteComposeCreationState(
    platform: RcPlatformServices,
    size: Size,
    apiLevel: Int,
    profiles: Int,
): RemoteComposeCreationState {
    val profile =
        Profile(
            apiLevel,
            profiles,
            platform,
            { creationDisplayInfo, prof, callback ->
                RemoteComposeWriterAndroid(creationDisplayInfo, null, prof, callback)
            },
        )
    val creationDisplayInfo =
        RemoteCreationDisplayInfo(size.width.toInt(), size.height.toInt(), 160, 1f)
    val document =
        if (apiLevel == CoreDocument.DOCUMENT_API_LEVEL && profiles == 0) {
            RemoteComposeWriterAndroid(size.width.toInt(), size.height.toInt(), "", platform)
        } else {
            RemoteComposeWriterAndroid(
                size.width.toInt(),
                size.height.toInt(),
                "",
                apiLevel,
                profiles,
                platform,
            )
        }
    return legacyCreationState(
        creationDisplayInfo,
        profile,
        document,
        platformImageProvider = AndroidPlatformImageProvider(document),
    )
}
