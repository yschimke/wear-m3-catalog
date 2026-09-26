package ee.schimke.wearm3catalog.uibuilder

import androidx.compose.runtime.staticCompositionLocalOf
import ee.schimke.composeai.uibuilder.export.UiBuilderDocument
import ee.schimke.composeai.uibuilder.export.WearWidgetHostShape
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * The `environment` key the UI builder names a surface's widget host shape under, as the shape's
 * `id` (`rectangular`, `squircle`, `round`).
 *
 * A runtime surface is otherwise told only its size, so every pane was framed as
 * [WearWidgetHostShape.Default]: the Samsung stadium and the Pixel Watch rounded rectangle drew one
 * frame, and whatever of the pane it left uncovered showed the runtime's background.
 */
const val WEAR_WIDGET_HOST_SHAPE_ENVIRONMENT_KEY: String = "wearWidgetHostShape"

/** The host shape this document was sent for, or the default when the editor named none. */
fun UiBuilderDocument.wearWidgetHostShape(): WearWidgetHostShape =
  WearWidgetHostShape.fromId(
    (environment[WEAR_WIDGET_HOST_SHAPE_ENVIRONMENT_KEY] as? JsonPrimitive)?.contentOrNull
  )

/** The host shape the canvas's widget container frames itself in. */
val LocalWearWidgetHostShape = staticCompositionLocalOf { WearWidgetHostShape.Default }
