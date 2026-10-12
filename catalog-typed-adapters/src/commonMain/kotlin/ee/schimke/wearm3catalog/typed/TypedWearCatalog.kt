package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.ComponentRecord
import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.discovery.ExportProjectionJson
import ee.schimke.composeai.discovery.GeneratedAdapterCatalog
import ee.schimke.composeai.discovery.TypedAdapterCatalog
import ee.schimke.composeai.discovery.UiBuilderAuthoredComponent
import ee.schimke.composeai.discovery.UiBuilderCatalogFile
import ee.schimke.composeai.discovery.UiBuilderCatalogs
import ee.schimke.composeai.discovery.UiBuilderCode
import ee.schimke.composeai.discovery.UiBuilderComposeSourceExport
import ee.schimke.composeai.discovery.UiBuilderPolicyFile
import kotlinx.serialization.json.jsonObject

/** The parallel pilot's catalog id. Production keeps `wear-m3` and its committed JSON policy. */
const val TYPED_WEAR_CATALOG_ID = "wear-m3-typed-pilot"

/**
 * The typed pilot's adapters, each built against its freshly discovered compiled record. Ids are
 * the production ids, so saved designs and the production structure adapters (the screen frame and
 * the transforming list) address them unchanged.
 */
class TypedWearCatalog(
  val discovered: ComponentRecordFile,
  /** The real-record definitions to export through; null publishes the wrappers alone. */
  val real: RealWearPublication? = null,
) {
  private fun record(name: String): ComponentRecord =
    discovered.components.single { it.symbol.name == name }

  val text = TextAdapter(record("WearText"))
  val icon = IconAdapter(record("WearIcon"))
  val listHeader = ListHeaderAdapter(record("WearListHeader"))
  val listSubHeader = ListSubHeaderAdapter(record("WearListSubHeader"))
  val card = CardAdapter(record("WearCard"))
  val edgeButton = EdgeButtonAdapter(record("WearEdgeButton"))
  val button = ButtonAdapter(record("WearButton"))
  val iconButton = IconButtonAdapter(record("WearIconButton"))
  val textButton = TextButtonAdapter(record("WearTextButton"))
  val checkboxButton = CheckboxButtonAdapter(record("WearCheckboxButton"))
  val switchButton = SwitchButtonAdapter(record("WearSwitchButton"))
  val radioButton = RadioButtonAdapter(record("WearRadioButton"))
  val slider = SliderAdapter(record("WearSlider"))
  val stepper = StepperAdapter(record("WearStepper"))
  val buttonGroup = ButtonGroupAdapter(record("WearButtonGroup"))
  val datePicker = DatePickerAdapter(record("WearDatePicker"))
  val timePicker = TimePickerAdapter(record("WearTimePicker"))
  val alertDialog = AlertDialogAdapter(record("WearAlertDialog"))
  val confirmationDialog = ConfirmationDialogAdapter(record("WearConfirmationDialog"))
  val openOnPhoneDialog = OpenOnPhoneDialogAdapter(record("WearOpenOnPhoneDialog"))
  val progressIndicator = ProgressIndicatorAdapter(record("WearProgressIndicator"))
  val variableFontText = VariableFontTextAdapter(record("WearVariableFontText"))

  /** Shelf components: generated into the portable pair, records and all. */
  val adapters: List<TypedComponentAdapterAny> =
    listOf(
      text,
      icon,
      listHeader,
      listSubHeader,
      card,
      edgeButton,
      button,
      iconButton,
      textButton,
      checkboxButton,
      switchButton,
      radioButton,
      slider,
      stepper,
      buttonGroup,
      datePicker,
      timePicker,
    )

  /** Builtins keep their authored policy entries; only their renderers are typed. */
  val builtinAdapters: List<TypedComponentAdapterAny> =
    listOf(alertDialog, confirmationDialog, openOnPhoneDialog, progressIndicator, variableFontText)

  val runtimeAdapters: List<TypedComponentAdapterAny>
    get() = adapters + builtinAdapters

  /**
   * Generate the portable typed pair, then the full pilot catalog: the production policy's
   * builtins, menu, templates and colour tokens, with each typed component's record and canvas
   * taken from its adapter. Existing property and slot capabilities stay authoritative, so the
   * shelf and inspector offer what production offers; a typed parameter production lacks is
   * appended.
   */
  fun generate(base: UiBuilderPolicyFile): TypedWearCatalogOutput {
    val cover = UiBuilderCatalogs.CoverSheet(TYPED_WEAR_CATALOG_ID, "Wear Material 3 — typed pilot")
    val portable =
      TypedAdapterCatalog.generate(
        discovered,
        cover,
        base
          .newBuilder()
          .apply {
            catalogId = cover.system
            components = emptyMap()
          }
          .build(),
        adapters,
      )
    val inferred = portable.catalog.statusSemantics.components
    val authored =
      base
        .newBuilder()
        .apply {
          catalogId = cover.system
          components = adapters.associate { adapter ->
            val old = requireNotNull(base.components[adapter.id]) { "${adapter.id}: not in policy" }
            val current = inferred.getValue(adapter.id)
            adapter.id to
              old
                .newBuilder()
                .apply {
                  record = current.record
                  canvas = current.canvas
                  propertyCapabilities =
                    old.propertyCapabilities.orEmpty() +
                      current.propertyCapabilities.orEmpty().filter { candidate ->
                        old.propertyCapabilities.orEmpty().none {
                          it.jsonObject["name"] == candidate.jsonObject["name"]
                        }
                      }
                  slotCapabilities =
                    old.slotCapabilities.orEmpty() +
                      current.slotCapabilities.orEmpty().filter { candidate ->
                        old.slotCapabilities.orEmpty().none {
                          it.jsonObject["name"] == candidate.jsonObject["name"]
                        }
                      }
                }
                .build()
          }
        }
        .build()
    val published = real?.let { publish(authored, portable.record, it) }
    val record = published?.first ?: portable.record
    val full =
      requireNotNull(UiBuilderCatalogs.generate(record, cover, published?.second ?: authored))
    // An export target kept off the shelf is reported, and is exactly what was asked for.
    val unexpected =
      full.diagnostics.filterNot {
        it.code == UiBuilderCatalogs.Diagnostics.COMPONENT_EXCLUDED &&
          it.subject.startsWith(EXPORT_TARGET_PREFIX)
      }
    check(unexpected.isEmpty()) { unexpected.joinToString { it.message } }
    builtinAdapters.forEach { it.freeze() }
    return TypedWearCatalogOutput(portable, full, record)
  }

  /**
   * [policy] and [record] exporting through [real]: the real records join the record as export
   * targets kept off the shelf, each shelf component a real definition covers carries that
   * definition's projection (its wrapper still draws it), the list builtin names the real
   * `TransformingLazyColumn` as its implementation, and the `code` block states the screen frame
   * around the generated root, the locals that frame declares and the list item's scope. The
   * catalog then selects the component-record export, which reads all of it.
   */
  private fun publish(
    policy: UiBuilderPolicyFile,
    record: ComponentRecordFile,
    real: RealWearPublication,
  ): Pair<ComponentRecordFile, UiBuilderPolicyFile> {
    val byId = real.adapters.associateBy { it.id }
    val targets =
      real.adapters
        .flatMap { adapter -> listOf(adapter.component) + adapter.variantRecords }
        .distinctBy { it.canonicalId }
    val listRecord = byId.getValue(LIST).component.canonicalId
    val components =
      policy.components.mapValues { (id, component) ->
        val adapter = byId[id] ?: return@mapValues component
        component.newBuilder().also { it.exportProjection = projectionJson(adapter) }.build()
      } +
        targets.associate { target ->
          val owner = real.adapters.first { target.canonicalId in it.recordIds() }
          "$EXPORT_TARGET_PREFIX${target.symbol.name.lowercase()}" to
            UiBuilderAuthoredComponent.Builder()
              .also {
                it.record = target.canonicalId
                it.excluded = "an export target of ${owner.id}"
                // The list's call is a builtin's implementation, so it carries the projection.
                if (target.canonicalId == listRecord) it.exportProjection = projectionJson(owner)
              }
              .build()
        }
    val builtins =
      policy.builtins.mapValues { (id, builtin) ->
        if (id == LIST) builtin.newBuilder().also { it.implementation = listRecord }.build()
        else builtin
      }
    val published =
      policy
        .newBuilder()
        .apply {
          this.components = components
          this.builtins = builtins
          code = WEAR_SCREEN_CODE
          composeSourceExport =
            UiBuilderComposeSourceExport.Builder(COMPONENT_RECORDS, COMPONENT_RECORDS_V1).build()
        }
        .build()
    val merged =
      record
        .newBuilder()
        .also { b ->
          b.components =
            record.components +
              targets.filter { t -> record.components.none { it.canonicalId == t.canonicalId } }
        }
        .build()
    return merged to published
  }

  private fun TypedComponentAdapterAny.recordIds(): Set<String> =
    (listOf(component) + variantRecords).map { it.canonicalId }.toSet()

  private fun projectionJson(adapter: TypedComponentAdapterAny) =
    ExportProjectionJson.encode(
      requireNotNull(adapter.projection) { "${adapter.id}: no projection" }
    )

  companion object {
    /** The list builtin, written as the real `TransformingLazyColumn`. */
    const val LIST: String = "wear-m3/transforming-lazy-column"

    /** Policy ids of the real records published only as export targets. */
    const val EXPORT_TARGET_PREFIX: String = "wear-m3/export-"

    /** compose-ui-builder's catalog-neutral component-record export adapter. */
    const val COMPONENT_RECORDS: String = "compose-component-records"
    const val COMPONENT_RECORDS_V1: Int = 1

    private const val FOUNDATION_LAZY = "androidx.wear.compose.foundation.lazy"

    /**
     * The structure around a Wear screen's records: `ScreenScaffold` sharing the list's state, the
     * transformation spec its rows read, and the content padding the scaffold hands its content.
     * The generated root (the list) is `${'$'}{content}`; everything the records read and no record
     * declares is a local here.
     */
    val WEAR_SCREEN_CODE: UiBuilderCode =
      UiBuilderCode.Builder()
        .also {
          it.strategy = "templates"
          it.imports =
            listOf(
              "$FOUNDATION_LAZY.rememberTransformingLazyColumnState",
              "androidx.wear.compose.material3.ScreenScaffold",
              "androidx.wear.compose.material3.lazy.rememberTransformationSpec",
            )
          it.templates =
            mapOf(
              "screen-root" to
                "val listState = rememberTransformingLazyColumnState()\n" +
                  "val spec = rememberTransformationSpec()\n" +
                  "ScreenScaffold(scrollState = listState) { contentPadding ->\n" +
                  "    ${'$'}{content}\n" +
                  "}",
              // The same frame for a screen with an edge button, which the scaffold anchors.
              "screen-root+edgeButton" to
                "val listState = rememberTransformingLazyColumnState()\n" +
                  "val spec = rememberTransformationSpec()\n" +
                  "ScreenScaffold(\n" +
                  "    scrollState = listState,\n" +
                  "    edgeButton = {\n" +
                  "        ${'$'}{edgeButton}\n" +
                  "    },\n" +
                  ") { contentPadding ->\n" +
                  "    ${'$'}{content}\n" +
                  "}",
            )
          it.locals =
            mapOf(
              "listState" to "$FOUNDATION_LAZY.TransformingLazyColumnState",
              "spec" to "androidx.wear.compose.material3.lazy.TransformationSpec",
              "contentPadding" to "androidx.compose.foundation.layout.PaddingValues",
            )
          it.scopes = mapOf("list-item" to "$FOUNDATION_LAZY.TransformingLazyColumnItemScope")
        }
        .build()
  }
}

/** The real-record definitions a pilot pair exports through, read where discovery runs (JVM). */
class RealWearPublication(val adapters: List<TypedComponentAdapterAny>)

typealias TypedComponentAdapterAny = ee.schimke.composeai.discovery.TypedComponentAdapter<*>

class TypedWearCatalogOutput(
  val portable: GeneratedAdapterCatalog,
  val catalog: UiBuilderCatalogFile,
  /** The published record: the portable pair's, plus any real export targets. */
  val record: ComponentRecordFile = portable.record,
)
