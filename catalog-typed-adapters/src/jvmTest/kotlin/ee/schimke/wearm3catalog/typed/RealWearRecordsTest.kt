package ee.schimke.wearm3catalog.typed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What [RealWearRecords] reads from the library: the real composables, each with every overload,
 * and the colour factories the projections call. A library bump that moves any of these fails here.
 */
class RealWearRecordsTest {
  private val records = RealWearRecords.discover().components.associateBy { it.symbol.name }

  @Test
  fun `every real composable is discovered as a library record`() {
    assertEquals(RealWearRecords.callables.map { it.name }.toSet(), records.keys)
    assertTrue(records.values.all { it.signatureKnown })
  }

  @Test
  fun `the button is the label overload, with every overload published`() {
    val button = records.getValue("Button")
    val parameters = button.parameters.map { it.name }
    assertTrue("label" in parameters && "secondaryLabel" in parameters && "icon" in parameters)
    assertTrue("containerPainter" !in parameters)
    // `label` and `content`, each with and without `containerPainter`.
    assertEquals(4, button.overloads.size)
    assertTrue(button.overloads.any { o -> o.parameters.any { it.name == "content" } })
  }

  @Test
  fun `text is the String overload`() {
    val text = records.getValue("Text").parameters.single { it.name == "text" }
    assertEquals("kotlin.String", text.typeFqn)
  }

  @Test
  fun `the list declares its rows through the item scope DSL`() {
    val content =
      records.getValue("TransformingLazyColumn").parameters.single { it.name == "content" }
    assertEquals(
      "androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope",
      content.scopeDslReceiver,
    )
    assertEquals("kotlin.Unit", content.lambdaReturnTypeFqn)
  }

  @Test
  fun `the list's spacing is Arrangement's one-argument spacedBy`() {
    val spacedBy =
      RealWearRecords.factories().filter {
        it.callableFqn == "androidx.compose.foundation.layout.Arrangement.spacedBy"
      }
    val single = spacedBy.single { it.parameters.map { p -> p.name } == listOf("space") }
    // A `HorizontalOrVertical`, so it is passed to `verticalArrangement: Arrangement.Vertical`.
    assertTrue(
      "androidx.compose.foundation.layout.Arrangement.Vertical" in single.returnSupertypes,
      single.toString(),
    )
  }

  @Test
  fun `the colour factories each return ButtonColors`() {
    val factories =
      RealWearRecords.factories().filter { it.callableFqn.contains(".ButtonDefaults.") }
    assertEquals(
      setOf("buttonColors", "filledTonalButtonColors", "outlinedButtonColors", "childButtonColors"),
      factories.map { it.callableFqn.substringAfterLast('.') }.toSet(),
    )
    assertTrue(factories.all { it.returnTypeFqn == "androidx.wear.compose.material3.ButtonColors" })
  }
}
