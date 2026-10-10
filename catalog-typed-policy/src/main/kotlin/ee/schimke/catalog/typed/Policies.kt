package ee.schimke.catalog.typed

import ee.schimke.composeai.discovery.UiBuilderPolicyFile

// Only the baseline tests read these paths. Generation calls the authored functions directly.
data class AuthoredPolicy(
  val outputName: String,
  val baseline: String,
  val policy: UiBuilderPolicyFile,
)

fun authoredPolicies(): List<AuthoredPolicy> =
  listOf(
    AuthoredPolicy("wear-m3", "ui-builder.policy.json", wearM3Policy()),
    AuthoredPolicy(
      "wear-m3-samples",
      "samples-catalog/ui-builder.policy.json",
      wearM3SamplesPolicy(),
    ),
  )
