package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.ComponentRecord
import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.discovery.FactorySignature
import ee.schimke.composeai.discovery.TypedAdapterDiscovery
import ee.schimke.composeai.discovery.TypedAdapterDiscovery.LibraryCallable
import ee.schimke.composeai.discovery.TypedComponentAdapter
import ee.schimke.wearm3catalog.typed.real.RealAlertDialogAdapter
import ee.schimke.wearm3catalog.typed.real.RealButtonAdapter
import ee.schimke.wearm3catalog.typed.real.RealButtonGroupAdapter
import ee.schimke.wearm3catalog.typed.real.RealCardAdapter
import ee.schimke.wearm3catalog.typed.real.RealEdgeButtonAdapter
import ee.schimke.wearm3catalog.typed.real.RealIconAdapter
import ee.schimke.wearm3catalog.typed.real.RealIconButtonAdapter
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

  /** The content overload, not the image one that adds `containerPainter`. */
  private fun card(name: String) =
    LibraryCallable("$MATERIAL3.CardKt", name, excluding = setOf("containerPainter"))

  val callables: List<LibraryCallable> =
    listOf(
      LibraryCallable("$MATERIAL3.TextKt", "Text", types = mapOf("text" to "kotlin.String")),
      button("Button"),
      button("FilledTonalButton"),
      button("OutlinedButton"),
      button("ChildButton"),
      LibraryCallable("$MATERIAL3.ListHeaderKt", "ListHeader"),
      LibraryCallable("$MATERIAL3.EdgeButtonKt", "EdgeButton"),
      LibraryCallable("$MATERIAL3.IconButtonKt", "IconButton"),
      LibraryCallable("$MATERIAL3.IconButtonKt", "FilledIconButton"),
      LibraryCallable("$MATERIAL3.IconButtonKt", "FilledTonalIconButton"),
      LibraryCallable("$MATERIAL3.IconButtonKt", "OutlinedIconButton"),
      LibraryCallable("$MATERIAL3.ButtonGroupKt", "ButtonGroup"),
      LibraryCallable(
        "$MATERIAL3.AlertDialogKt",
        "AlertDialog",
        declaring = setOf("confirmButton"),
        // The overload taking a `transformationSpec` is for a dialog whose content is a list.
        excluding = setOf("transformationSpec"),
      ),
      card("TitleCard"),
      card("AppCard"),
      card("OutlinedCard"),
      card("Card"),
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
      listOf(
          "iconButtonColors",
          "filledIconButtonColors",
          "filledTonalIconButtonColors",
          "outlinedIconButtonColors",
        )
        .map { TypedAdapterDiscovery.Callable("$MATERIAL3.IconButtonDefaults", it) } +
      listOf("cardColors", "outlinedCardColors").map {
        TypedAdapterDiscovery.Callable("$MATERIAL3.CardDefaults", it)
      } +
      TypedAdapterDiscovery.Callable("androidx.compose.foundation.layout.Arrangement", "spacedBy")

  fun discover(): ComponentRecordFile =
    TypedAdapterDiscovery.discoverLibrary("wear-m3", "release", callables)

  fun factories(): List<FactorySignature> =
    TypedAdapterDiscovery.discoverFactories(factoryCallables)

  /** What the pilot pair publishes to export through the real records. */
  fun publication(): RealWearPublication = RealWearPublication(adapters(), layouts())

  /** The builder's shared layouts, by the Compose function each one calls. */
  private val LAYOUTS: Map<String, String> =
    mapOf("layout/column" to "Column", "layout/row" to "Row", "layout/box" to "Box")

  /** Compose's `Column`, `Row` and `Box`, each named by the shared `layout/…` id it is. */
  fun layouts(): List<ComponentRecord> {
    val records =
      TypedAdapterDiscovery.discoverLibrary(
          "wear-m3",
          "release",
          LAYOUTS.values.map { name ->
            LibraryCallable(
              "androidx.compose.foundation.layout.${name}Kt",
              name,
              declaring = setOf("content"),
            )
          },
        )
        .components
        .associateBy { it.symbol.name }
    return LAYOUTS.map { (id, name) ->
      records.getValue(name).newBuilder().also { it.componentIds = listOf(id) }.build()
    }
  }

  /** The real-record definitions, each on the record discovery read for it. */
  fun adapters(): List<TypedComponentAdapter<*>> {
    val records = discover().components.associateBy { it.symbol.name }
    val factories = factories()
    return listOf(
      RealTextAdapter(records.getValue("Text")),
      RealListHeaderAdapter(records.getValue("ListHeader")),
      RealIconAdapter(records.getValue("Icon")),
      RealCardAdapter(
        records.getValue("TitleCard"),
        records.getValue("AppCard"),
        records.getValue("OutlinedCard"),
        records.getValue("Card"),
        factories.filter { it.callableFqn.startsWith("$MATERIAL3.CardDefaults.") },
      ),
      RealIconButtonAdapter(
        records.getValue("IconButton"),
        records.getValue("FilledIconButton"),
        records.getValue("FilledTonalIconButton"),
        records.getValue("OutlinedIconButton"),
        factories.filter { it.callableFqn.startsWith("$MATERIAL3.IconButtonDefaults.") },
      ),
      RealButtonGroupAdapter(records.getValue("ButtonGroup")),
      RealAlertDialogAdapter(records.getValue("AlertDialog")),
      RealEdgeButtonAdapter(
        records.getValue("EdgeButton"),
        factories.filter { it.callableFqn == "$MATERIAL3.ButtonDefaults.buttonColors" },
      ),
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
