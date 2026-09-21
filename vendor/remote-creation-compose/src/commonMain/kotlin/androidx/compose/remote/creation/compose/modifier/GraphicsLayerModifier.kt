/*
 * Copyright (C) 2024 The Android Open Source Project
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteStateScope
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.common.RemoteLayerAttribute
import androidx.compose.remote.creation.common.RemoteModifierOperation
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.CompositingStrategy.Companion.Auto
import androidx.compose.ui.graphics.layer.CompositingStrategy.Companion.ModulateAlpha
import androidx.compose.ui.graphics.layer.CompositingStrategy.Companion.Offscreen

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class GraphicsLayerModifier(
    public val scaleX: RemoteFloat,
    public val scaleY: RemoteFloat,
    public val rotationX: RemoteFloat,
    public val rotationY: RemoteFloat,
    public val rotationZ: RemoteFloat,
    public val shadowElevation: RemoteFloat,
    public val transformOriginX: RemoteFloat,
    public val transformOriginY: RemoteFloat,
    public val translationX: RemoteFloat,
    public val translationY: RemoteFloat,
    public val shape: Shape,
    public val compositingStrategy: Int,
    public val alpha: RemoteFloat,
    public val cameraDistance: RemoteFloat,
    public val renderEffect: RenderEffect?,
) : RemoteModifier.Element {

    override fun RemoteStateScope.toRemoteModifierOperation(): RemoteModifierOperation {
        val attributes = mutableListOf<RemoteLayerAttribute>()
        fun float(id: Int, value: RemoteFloat, default: Float) {
            if (value.floatId != default) {
                attributes += RemoteLayerAttribute.FloatValue(id, value.floatId)
            }
        }
        fun int(id: Int, value: Int, default: Int) {
            if (value != default) attributes += RemoteLayerAttribute.IntValue(id, value)
        }
        float(Layer.SCALE_X, scaleX, 1f)
        float(Layer.SCALE_Y, scaleY, 1f)
        float(Layer.ROTATION_X, rotationX, 0f)
        float(Layer.ROTATION_Y, rotationY, 0f)
        float(Layer.ROTATION_Z, rotationZ, 0f)
        float(Layer.TRANSFORM_ORIGIN_X, transformOriginX, 0f)
        float(Layer.TRANSFORM_ORIGIN_Y, transformOriginY, 0f)
        float(Layer.TRANSLATION_X, translationX, 0f)
        float(Layer.TRANSLATION_Y, translationY, 0f)
        float(Layer.SHADOW_ELEVATION, shadowElevation, 0f)
        float(Layer.ALPHA, alpha, 1f)
        float(Layer.CAMERA_DISTANCE, cameraDistance, 8f)
        int(Layer.COMPOSITING_STRATEGY, compositingStrategy, 0)
        if (renderEffect is BlurEffect) {
            attributes +=
                RemoteLayerAttribute.FloatValue(
                    Layer.BLUR_RADIUS_X,
                    renderEffect.radiusX,
                )
            attributes +=
                RemoteLayerAttribute.FloatValue(
                    Layer.BLUR_RADIUS_Y,
                    renderEffect.radiusY,
                )
            val tileMode =
                when (renderEffect.edgeTreatment) {
                    TileMode.Clamp -> Layer.TILE_MODE_CLAMP
                    TileMode.Repeated -> Layer.TILE_MODE_REPEATED
                    TileMode.Mirror -> Layer.TILE_MODE_MIRROR
                    TileMode.Decal -> Layer.TILE_MODE_DECAL
                    else -> Layer.TILE_MODE_CLAMP
                }
            attributes +=
                RemoteLayerAttribute.IntValue(
                    Layer.BLUR_TILE_MODE,
                    tileMode,
                )
        }
        when (shape) {
            RectangleShape ->
                attributes +=
                    RemoteLayerAttribute.IntValue(
                        Layer.SHAPE,
                        Layer.SHAPE_RECT,
                    )
            is RoundedCornerShape -> {
                attributes +=
                    RemoteLayerAttribute.IntValue(
                        Layer.SHAPE,
                        Layer.SHAPE_ROUND_RECT,
                    )
                attributes +=
                    RemoteLayerAttribute.FloatValue(Layer.SHAPE_RADIUS, 40f)
            }
            CircleShape ->
                attributes +=
                    RemoteLayerAttribute.IntValue(
                        Layer.SHAPE,
                        Layer.SHAPE_CIRCLE,
                    )
            else -> Unit
        }
        return RemoteModifierOperation.GraphicsLayer(attributes)
    }
}

/**
 * Applies a graphics layer modifier to the [RemoteModifier].
 *
 * A graphics layer modifier can be used to apply effects such as scaling, rotation, translation,
 * shadow, clipping, alpha, and render effects (like blur) to the drawing content.
 *
 * This overload accepts [RemoteFloat] values, which allow binding properties to dynamic expressions
 * or state.
 *
 * @param scaleX The horizontal scale factor.
 * @param scaleY The vertical scale factor.
 * @param rotationX The rotation of the layer around the X axis, in degrees.
 * @param rotationY The rotation of the layer around the Y axis, in degrees.
 * @param rotationZ The rotation of the layer around the Z axis, in degrees.
 * @param shadowElevation The shadow elevation of the layer.
 * @param transformOriginX The horizontal center of the transform, as a fraction of the layer width.
 * @param transformOriginY The vertical center of the transform, as a fraction of the layer height.
 * @param translationX The horizontal translation of the layer.
 * @param translationY The vertical translation of the layer.
 * @param alpha The alpha (opacity) of the layer.
 * @param shape The shape of the layer.
 * @param compositingStrategy The compositing strategy to use for the layer.
 * @param cameraDistance The camera distance for 3D transforms.
 * @param renderEffect The [RenderEffect] to apply, or null.
 */
public fun RemoteModifier.graphicsLayer(
    scaleX: RemoteFloat = 1f.rf,
    scaleY: RemoteFloat = 1f.rf,
    rotationX: RemoteFloat = 0f.rf,
    rotationY: RemoteFloat = 0f.rf,
    rotationZ: RemoteFloat = 0f.rf,
    shadowElevation: RemoteFloat = 0f.rf,
    transformOriginX: RemoteFloat = 0.5f.rf,
    transformOriginY: RemoteFloat = 0.5f.rf,
    translationX: RemoteFloat = 0f.rf,
    translationY: RemoteFloat = 0f.rf,
    alpha: RemoteFloat = 1f.rf,
    shape: Shape = RectangleShape,
    compositingStrategy: CompositingStrategy = Auto,
    cameraDistance: RemoteFloat = 8f.rf, // Default Value for Camera Distance
    renderEffect: RenderEffect? = null,
): RemoteModifier {

    val cS =
        when (compositingStrategy) {
            Auto -> 0
            Offscreen -> 1
            ModulateAlpha -> 2
            else -> 0
        }
    return then(
        GraphicsLayerModifier(
            scaleX,
            scaleY,
            rotationX,
            rotationY,
            rotationZ,
            shadowElevation,
            transformOriginX,
            transformOriginY,
            translationX,
            translationY,
            shape,
            cS,
            alpha,
            cameraDistance,
            renderEffect,
        )
    )
}

/** Scope for configuring graphics layer properties in a type-safe builder lambda. */
public interface GraphicsLayerScope {
    /** The horizontal scale factor. */
    public var scaleX: RemoteFloat

    /** The vertical scale factor. */
    public var scaleY: RemoteFloat

    /** The rotation of the layer around the X axis, in degrees. */
    public var rotationX: RemoteFloat

    /** The rotation of the layer around the Y axis, in degrees. */
    public var rotationY: RemoteFloat

    /** The rotation of the layer around the Z axis, in degrees. */
    public var rotationZ: RemoteFloat

    /** The shadow elevation of the layer. */
    public var shadowElevation: RemoteFloat

    /** The horizontal center of the transform, as a fraction of the layer width. */
    public var transformOriginX: RemoteFloat

    /** The vertical center of the transform, as a fraction of the layer height. */
    public var transformOriginY: RemoteFloat

    /** The horizontal translation of the layer. */
    public var translationX: RemoteFloat

    /** The vertical translation of the layer. */
    public var translationY: RemoteFloat

    /** The alpha (opacity) of the layer, from 0f (transparent) to 1f (opaque). */
    public var alpha: RemoteFloat

    /** The camera distance for 3D transforms. */
    public var cameraDistance: RemoteFloat

    /** The shape of the layer. Used for clipping and outline shadows. */
    public var shape: Shape

    /** The compositing strategy to use for the layer. */
    public var compositingStrategy: CompositingStrategy

    /** The [RenderEffect] to apply to this layer, or null. */
    public var renderEffect: RenderEffect?
}

internal class GraphicsLayerScopeImpl : GraphicsLayerScope {
    override var scaleX: RemoteFloat = 1f.rf
    override var scaleY: RemoteFloat = 1f.rf
    override var rotationX: RemoteFloat = 0f.rf
    override var rotationY: RemoteFloat = 0f.rf
    override var rotationZ: RemoteFloat = 0f.rf
    override var shadowElevation: RemoteFloat = 0f.rf
    override var transformOriginX: RemoteFloat = 0.5f.rf
    override var transformOriginY: RemoteFloat = 0.5f.rf
    override var translationX: RemoteFloat = 0f.rf
    override var translationY: RemoteFloat = 0f.rf
    override var alpha: RemoteFloat = 1f.rf
    override var cameraDistance: RemoteFloat = 8f.rf
    override var shape: Shape = RectangleShape
    override var compositingStrategy: CompositingStrategy = Auto
    override var renderEffect: RenderEffect? = null
}

/**
 * Applies a graphics layer modifier to the [RemoteModifier] using a type-safe builder lambda.
 *
 * This overload allows configuring layer properties within a [GraphicsLayerScope] block, which is
 * similar to standard Compose's `graphicsLayer { ... }`.
 *
 * Example usage:
 * ```
 * modifier.graphicsLayer {
 *     alpha = 0.5f.rf
 *     rotationZ = 45f.rf
 * }
 * ```
 *
 * @param block The lambda block to configure the [GraphicsLayerScope].
 */
public fun RemoteModifier.graphicsLayer(block: GraphicsLayerScope.() -> Unit): RemoteModifier {
    val scope = GraphicsLayerScopeImpl().apply(block)
    val cS =
        when (scope.compositingStrategy) {
            Auto -> 0
            Offscreen -> 1
            ModulateAlpha -> 2
            else -> 0
        }
    return then(
        GraphicsLayerModifier(
            scaleX = scope.scaleX,
            scaleY = scope.scaleY,
            rotationX = scope.rotationX,
            rotationY = scope.rotationY,
            rotationZ = scope.rotationZ,
            shadowElevation = scope.shadowElevation,
            transformOriginX = scope.transformOriginX,
            transformOriginY = scope.transformOriginY,
            translationX = scope.translationX,
            translationY = scope.translationY,
            shape = scope.shape,
            compositingStrategy = cS,
            alpha = scope.alpha,
            cameraDistance = scope.cameraDistance,
            renderEffect = scope.renderEffect,
        )
    )
}

private object Layer {
    const val SCALE_X = 0
    const val SCALE_Y = 1
    const val ROTATION_X = 2
    const val ROTATION_Y = 3
    const val ROTATION_Z = 4
    const val TRANSFORM_ORIGIN_X = 5
    const val TRANSFORM_ORIGIN_Y = 6
    const val TRANSLATION_X = 7
    const val TRANSLATION_Y = 8
    const val SHADOW_ELEVATION = 10
    const val ALPHA = 11
    const val CAMERA_DISTANCE = 12
    const val COMPOSITING_STRATEGY = 13
    const val BLUR_RADIUS_X = 17
    const val BLUR_RADIUS_Y = 18
    const val BLUR_TILE_MODE = 19
    const val SHAPE = 20
    const val SHAPE_RADIUS = 21
    const val SHAPE_RECT = 0
    const val SHAPE_ROUND_RECT = 1
    const val SHAPE_CIRCLE = 2
    const val TILE_MODE_CLAMP = 0
    const val TILE_MODE_REPEATED = 1
    const val TILE_MODE_MIRROR = 2
    const val TILE_MODE_DECAL = 3
}
