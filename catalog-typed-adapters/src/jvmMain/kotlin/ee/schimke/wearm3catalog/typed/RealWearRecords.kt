package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.discovery.FactorySignature
import ee.schimke.composeai.discovery.TypedAdapterDiscovery
import ee.schimke.composeai.discovery.TypedAdapterDiscovery.LibraryCallable

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
      LibraryCallable(
        "$LAZY.TransformingLazyColumnKt",
        "TransformingLazyColumn",
        declaring = setOf("state"),
      ),
    )

  /** The `…Defaults` factories the projections call. */
  val factoryCallables: List<TypedAdapterDiscovery.Callable> =
    listOf("buttonColors", "filledTonalButtonColors", "outlinedButtonColors", "childButtonColors")
      .map { TypedAdapterDiscovery.Callable("$MATERIAL3.ButtonDefaults", it) }

  fun discover(): ComponentRecordFile =
    TypedAdapterDiscovery.discoverLibrary("wear-m3", "release", callables)

  fun factories(): List<FactorySignature> =
    TypedAdapterDiscovery.discoverFactories(factoryCallables)
}
