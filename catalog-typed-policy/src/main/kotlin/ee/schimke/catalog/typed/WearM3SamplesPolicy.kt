package ee.schimke.catalog.typed

import ee.schimke.composeai.discovery.UiBuilderPolicyFile

// Parallel authored policy. Inventory membership remains annotation-owned.

fun wearM3SamplesPolicy(): UiBuilderPolicyFile =
  UiBuilderPolicyFile.Builder("compose-ui-builder-policy/v1", "wear")
    .apply {
      jsonSchema =
        "https://raw.githubusercontent.com/yschimke/compose-ai-tools/main/scripts/design-artifacts/ui-builder.policy.schema.json"
      catalogId = "wear-m3-samples"
      platformLabel = "Wear"
    }
    .build()
