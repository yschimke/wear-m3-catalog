package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.ExportProjection
import ee.schimke.composeai.discovery.ScreenValue
import ee.schimke.composeai.discovery.TypedComponentAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The real-record definitions against the library they export to: each validates against its
 * discovered record, and lowers the properties the Wear fixture designs set to what the builder's
 * hand-written `WearContentEmitter` writes for them.
 */
class RealWearAdaptersTest {
  private val adapters: Map<String, TypedComponentAdapter<*>> =
    RealWearRecords.adapters().associateBy { it.id }

  private fun projection(id: String) = adapters.getValue(id).projection!!

  private fun wrapped(type: String, value: Any): JsonElement = buildJsonObject {
    put("type", type)
    when (value) {
      is String -> put("value", value)
      is Number -> put("value", value)
      is Boolean -> put("value", value)
    }
  }

  private fun node(
    id: String,
    vararg properties: Pair<String, JsonElement>,
    role: String? = null,
  ): ExportProjection.Lowered.Node {
    val lowered = projection(id).lower(properties.toMap(), role)
    return assertIs<ExportProjection.Lowered.Node>(lowered, lowered.toString())
  }

  private fun refusal(id: String, vararg properties: Pair<String, JsonElement>): String =
    assertIs<ExportProjection.Lowered.Refused>(projection(id).lower(properties.toMap()))
      .reasons
      .joinToString("\n")

  @Test
  fun `every definition validates against the record discovery reads`() {
    assertEquals(
      setOf(
        "wear-m3/text",
        "wear-m3/list-header",
        "wear-m3/icon",
        "wear-m3/edge-button",
        "wear-m3/button",
        "wear-m3/transforming-lazy-column",
      ),
      adapters.keys,
    )
    for (adapter in adapters.values) {
      assertEquals(emptyList(), adapter.validateAgainst(adapter.component), adapter.id)
    }
  }

  @Test
  fun `a fixture text writes its style, colour, size and line settings`() {
    val text =
      node(
        "wear-m3/text",
        "text" to wrapped("string", "Steps"),
        "style" to wrapped("typographyToken", "titleMedium"),
        "color" to wrapped("colorToken", "onSurfaceVariant"),
        "fontSizeSp" to wrapped("float", 14.0),
        "maxLines" to wrapped("int", 1),
        "overflow" to wrapped("enum", "ellipsis"),
        "textAlign" to wrapped("enum", "start"),
      )
    val arguments = text.arguments
    assertEquals(ScreenValue.Text("Steps"), arguments["text"])
    assertEquals(
      listOf("typography", "titleMedium"),
      assertIs<ScreenValue.Reference>(arguments["style"]).members,
    )
    assertEquals(
      listOf("colorScheme", "onSurfaceVariant"),
      assertIs<ScreenValue.Reference>(arguments["color"]).members,
    )
    assertEquals(listOf("Ellipsis"), assertIs<ScreenValue.Reference>(arguments["overflow"]).members)
    assertEquals(listOf("Start"), assertIs<ScreenValue.Reference>(arguments["textAlign"]).members)
    assertIs<ScreenValue.Chain>(arguments["fontSize"])
    assertEquals(
      setOf("text", "style", "color", "fontSize", "maxLines", "overflow", "textAlign"),
      arguments.keys,
    )
    assertTrue(text.modifier.isEmpty(), "outside a list a text writes no row treatment")
  }

  @Test
  fun `the seeded defaults write nothing but the text`() {
    val text =
      node(
        "wear-m3/text",
        "text" to wrapped("string", "Hi"),
        "minLines" to wrapped("int", 1),
        "maxLines" to wrapped("int", Int.MAX_VALUE),
        "softWrap" to wrapped("bool", true),
        "fontSizeSp" to wrapped("float", 0),
        "letterSpacingSp" to wrapped("float", 0),
        "textDecoration" to wrapped("string", "none"),
        "style" to wrapped("string", ""),
      )
    assertEquals(setOf("text"), text.arguments.keys)
  }

  @Test
  fun `a text row takes the transformed height and TextDefaults' list padding`() {
    val text = node("wear-m3/text", "text" to wrapped("string", "Hi"), role = "list-item")
    assertEquals(
      listOf(
        "androidx.wear.compose.material3.lazy.transformedHeight",
        "androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope.minimumVerticalContentPadding",
      ),
      text.modifier.map { it.callableFqn },
    )
    assertFalse("transformation" in text.arguments, "a bare Text draws no surface")
  }

  @Test
  fun `what a text cannot export is refused by name`() {
    assertTrue(
      "headlineSmall" in
        refusal("wear-m3/text", "style" to wrapped("typographyToken", "headlineSmall"))
    )
    // Font settings reach the call only through a style copy: off the projection entirely.
    val read = projection("wear-m3/text").properties
    assertFalse("fontVariationSettings" in read)
    assertFalse("fontFeatureSettings" in read)
  }

  @Test
  fun `a list header writes its label as a synthesized text and its row as a surface`() {
    val header =
      node(
        "wear-m3/list-header",
        "text" to wrapped("string", "Activity"),
        "maxLines" to wrapped("int", 1),
        role = "list-item",
      )
    val label = header.synthesized.getValue("content")
    assertEquals("wear-m3/text", label.component)
    assertEquals(setOf("text", "maxLines"), label.properties.keys)
    assertIs<ScreenValue.Construct>(header.arguments["transformation"])
    assertEquals(2, header.modifier.size)
  }

  @Test
  fun `an icon writes a set size as a size modifier, and no modifier unset`() {
    val sized = node("wear-m3/icon", "sizeDp" to wrapped("float", 24))
    assertEquals(
      listOf("androidx.compose.foundation.layout.size"),
      sized.modifier.map { it.callableFqn },
    )
    assertIs<ScreenValue.Chain>(sized.modifier.single().positional.single())
    assertTrue(node("wear-m3/icon").modifier.isEmpty())
  }

  @Test
  fun `a button variant picks the real composable and its colours`() {
    val tonal =
      node(
        "wear-m3/button",
        "variant" to wrapped("enum", "filled-tonal"),
        "contentColor" to wrapped("colorToken", "primary"),
      )
    assertTrue(tonal.record.endsWith(".FilledTonalButton"), tonal.record)
    val colors = assertIs<ScreenValue.Construct>(tonal.arguments["colors"])
    assertTrue(colors.callableFqn.endsWith(".filledTonalButtonColors"))
    // As the canvas draws it: the content colour tints the icon slot's icon too.
    assertEquals(setOf("contentColor", "iconColor"), colors.named.keys)
    assertEquals(colors.named["contentColor"], colors.named["iconColor"])
    assertEquals("onClick", tonal.events["click"])
    // An unstyled, enabled filled button keeps the library's own colours and the short call.
    val plain =
      node(
        "wear-m3/button",
        "variant" to wrapped("enum", "filled"),
        "enabled" to wrapped("bool", true),
      )
    assertTrue(plain.record.endsWith(".ButtonKt.Button"), plain.record)
    assertTrue(plain.arguments.isEmpty(), plain.arguments.toString())
  }

  @Test
  fun `an outlined or child button's container colour is refused, not dropped`() {
    for (variant in listOf("outlined", "child")) {
      assertTrue(
        "containerColor" in
          refusal(
            "wear-m3/button",
            "variant" to wrapped("enum", variant),
            "containerColor" to wrapped("colorToken", "primary"),
          ),
        variant,
      )
    }
  }

  @Test
  fun `a button row is a transformed surface with ButtonDefaults' list padding`() {
    val button = node("wear-m3/button", role = "list-item")
    assertIs<ScreenValue.Construct>(button.arguments["transformation"])
    val padding = button.modifier.last()
    assertEquals(
      listOf("minimumVerticalListContentPadding"),
      assertIs<ScreenValue.Reference>(padding.positional.single()).members,
    )
  }

  @Test
  fun `a transforming list reads the frame's locals and opens the list-item role unless none`() {
    val spec =
      node(
        "wear-m3/transforming-lazy-column",
        "verticalSpacingDp" to wrapped("float", 4),
        "transformation" to wrapped("enum", "spec"),
      )
    assertEquals(mapOf("content" to "item"), spec.items)
    assertEquals(mapOf("content" to "list-item"), spec.itemRoles)
    assertEquals("content", spec.slots["items"])
    assertEquals(
      "listState",
      assertIs<ScreenValue.LocalRead>(spec.arguments["state"]).name,
    )
    val arrangement = assertIs<ScreenValue.Construct>(spec.arguments["verticalArrangement"])
    assertTrue(arrangement.callableFqn.endsWith("Arrangement.spacedBy"), arrangement.callableFqn)
    // Absent is `spec`, the canvas default.
    assertEquals(spec.itemRoles, node("wear-m3/transforming-lazy-column").itemRoles)
    val none = node("wear-m3/transforming-lazy-column", "transformation" to wrapped("enum", "none"))
    assertEquals(mapOf("content" to "item"), none.items)
    assertTrue(none.itemRoles.isEmpty())
  }

  @Test
  fun `every projection survives its published spelling`() {
    for (adapter in adapters.values) {
      val projection = adapter.projection!!
      assertEquals(
        projection,
        ee.schimke.composeai.discovery.ExportProjectionJson.decode(
            ee.schimke.composeai.discovery.ExportProjectionJson.encode(projection)
          )
          .getOrThrow(),
        adapter.id,
      )
    }
  }
}
