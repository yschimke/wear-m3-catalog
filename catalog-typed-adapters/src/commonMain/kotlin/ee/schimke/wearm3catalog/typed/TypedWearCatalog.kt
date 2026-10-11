package ee.schimke.wearm3catalog.typed

import ee.schimke.composeai.discovery.ComponentRecord
import ee.schimke.composeai.discovery.ComponentRecordFile
import ee.schimke.composeai.discovery.GeneratedAdapterCatalog
import ee.schimke.composeai.discovery.TypedAdapterCatalog
import ee.schimke.composeai.discovery.UiBuilderCatalogFile
import ee.schimke.composeai.discovery.UiBuilderCatalogs
import ee.schimke.composeai.discovery.UiBuilderPolicyFile
import kotlinx.serialization.json.jsonObject

/** The parallel pilot's catalog id. Production keeps `wear-m3` and its committed JSON policy. */
const val TYPED_WEAR_CATALOG_ID = "wear-m3-typed-pilot"

/**
 * The typed pilot's adapters, each built against its freshly discovered compiled record. Ids are
 * the production ids, so saved designs and the production structure adapters (the screen frame and
 * the transforming list) address them unchanged.
 */
class TypedWearCatalog(val discovered: ComponentRecordFile) {
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
    val full = requireNotNull(UiBuilderCatalogs.generate(portable.record, cover, authored))
    check(full.diagnostics.isEmpty()) { full.diagnostics.joinToString { it.message } }
    builtinAdapters.forEach { it.freeze() }
    return TypedWearCatalogOutput(portable, full)
  }
}

typealias TypedComponentAdapterAny = ee.schimke.composeai.discovery.TypedComponentAdapter<*>

class TypedWearCatalogOutput(
  val portable: GeneratedAdapterCatalog,
  val catalog: UiBuilderCatalogFile,
) {
  val record: ComponentRecordFile
    get() = portable.record
}
