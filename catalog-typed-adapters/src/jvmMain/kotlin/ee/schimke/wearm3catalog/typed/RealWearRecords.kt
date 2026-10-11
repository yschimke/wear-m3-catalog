package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.discovery.FactorySignature
import ee.schimke.composeai.discovery.TypedAdapterDiscovery
import ee.schimke.composeai.discovery.TypedAdapterDiscovery.LibraryCallable
import ee.schimke.composeai.discovery.TypedComponentAdapter
import ee.schimke.wearm3catalog.typed.real.RealButtonAdapter
import ee.schimke.wearm3catalog.typed.real.RealIconAdapter
import ee.schimke.wearm3catalog.typed.real.RealListAdapter
import ee.schimke.wearm3catalog.typed.real.RealListHeaderAdapter
import ee.schimke.wearm3catalog.typed.real.RealTextAdapter

/**
 * The real Wear Material 3 composables the typed definitions export to, read from the library's
 * metadata on this module's JVM classpath (the multiplatform port publishes the same
 * `androidx.wear.compose` API the Android export calls). Nothing is loaded or run.
 */
object RealWearRecords {
  private const val MATERIAL3 = "androidx.wear.compose.material3"
  private const val LAZY = "androidx.wear.compose.foundation.lazy"

  /** The label overload, not the image one that adds `containerPainter`. */
  private fun button(name: String) =
    LibraryCallable(
      "$MATERIAL3.ButtonKt",
      name,
      declaring = setOf("label"),
      excluding = setOf("containerPainter"),
    )

  val callables: List<LibraryCallable> =
    listOf(
      LibraryCallable("$MATERIAL3.TextKt", "Text", types = mapOf("text" to "kotlin.String")),
      button("Button"),
      button("FilledTonalButton"),
      button("OutlinedButton"),
      button("ChildButton"),
      LibraryCallable("$MATERIAL3.ListHeaderKt", "ListHeader"),
      // The vector overload; `painter` is the image one.
      LibraryCallable(
        "$MATERIAL3.IconKt",
        "Icon",
        types = mapOf("imageVector" to "androidx.compose.ui.graphics.vector.ImageVector"),
      ),
      LibraryCallable(
        "$LAZY.TransformingLazyColumnKt",
        "TransformingLazyColumn",
        declaring = setOf("state"),
      ),
    )

  /** The `…Defaults` factories the projections call, and the list's `Arrangement.spacedBy`. */
  val factoryCallables: List<TypedAdapterDiscovery.Callable> =
    listOf("buttonColors", "filledTonalButtonColors", "outlinedButtonColors", "childButtonColors")
      .map { TypedAdapterDiscovery.Callable("$MATERIAL3.ButtonDefaults", it) } +
      TypedAdapterDiscovery.Callable("androidx.compose.foundation.layout.Arrangement", "spacedBy")

  fun discover(): ComponentRecordFile =
    TypedAdapterDiscovery.discoverLibrary("wear-m3", "release", callables)

  fun factories(): List<FactorySignature> =
    TypedAdapterDiscovery.discoverFactories(factoryCallables)

  /** What the pilot pair publishes to export through the real records. */
  fun publication(): RealWearPublication = RealWearPublication(adapters())

  /** The real-record definitions, each on the record discovery read for it. */
  fun adapters(): List<TypedComponentAdapter<*>> {
    val records = discover().components.associateBy { it.symbol.name }
    val factories = factories()
    return listOf(
      RealTextAdapter(records.getValue("Text")),
      RealListHeaderAdapter(records.getValue("ListHeader")),
      RealIconAdapter(records.getValue("Icon")),
      RealButtonAdapter(
        records.getValue("Button"),
        records.getValue("FilledTonalButton"),
        records.getValue("OutlinedButton"),
        records.getValue("ChildButton"),
        factories.filter { it.callableFqn.startsWith("$MATERIAL3.ButtonDefaults.") },
      ),
      // `spacedBy(space: Dp)` alone: the aligned overloads return a one-axis arrangement.
      RealListAdapter(
        records.getValue("TransformingLazyColumn"),
        factories.filter { it.callableFqn.endsWith(".spacedBy") && it.parameters.size == 1 },
      ),
    )
  }
}
