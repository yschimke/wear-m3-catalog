package ee.schimke.wearm3catalog.uibuilder

import ee.schimke.composeai.uibuilder.UiBuilderSemanticActionController
import ee.schimke.composeai.uibuilder.UiBuilderSurface
import ee.schimke.composeai.uibuilder.startCatalogRenderer

/**
 * Transitional combined-build entrypoint.
 *
 * There is still exactly one document interpreter: [UiBuilderSurface] from the checked-out builder.
 * This module establishes catalog-owned packaging without copying that interpreter. The callback is
 * replaced by the Wear adapter registry after traversal moves into the renderer SDK.
 */
fun main() {
  val actions = UiBuilderSemanticActionController()
  startCatalogRenderer(actions) { document, renderSessionId, onInspectionSnapshot ->
    UiBuilderSurface(
      document = document,
      editorOverlay = false,
      runtimeActionController = actions,
      renderSessionId = renderSessionId,
      onInspectionSnapshot = onInspectionSnapshot,
      canvasAdapterRegistry = wearCanvasAdapters,
    )
  }
}
