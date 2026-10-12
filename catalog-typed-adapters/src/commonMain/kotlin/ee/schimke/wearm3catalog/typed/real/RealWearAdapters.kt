package ee.schimke.wearm3catalog.typed.real

import ee.schimke.composeai.discovery.AdapterValueCodec
import ee.schimke.composeai.discovery.AdapterValueCodecs
import ee.schimke.composeai.discovery.ChainLink
import ee.schimke.composeai.discovery.ComponentRecord
import ee.schimke.composeai.discovery.ExportProjection
import ee.schimke.composeai.discovery.FactorySignature
import ee.schimke.composeai.discovery.ScreenValue
import ee.schimke.composeai.discovery.TypedComponentAdapter
import ee.schimke.composeai.discovery.WireColor
import ee.schimke.composeai.discovery.named
import kotlinx.serialization.json.JsonPrimitive

// The typed Wear definitions on the real Wear Material 3 composables (`RealWearRecords`), each
// with the `ExportProjection` the builder lowers a design through. They mirror what the builder's
// hand-written `WearContentEmitter` writes for the same components; see
// docs/design/TYPED_WEAR_REAL_RECORDS.md for the order and what is still refused.

private const val MATERIAL3 = "androidx.wear.compose.material3"
private const val MATERIAL_THEME = "$MATERIAL3.MaterialTheme"
private const val ITEM_SCOPE =
  "androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope"
private const val TRANSFORMATION_SPEC = "$MATERIAL3.lazy.TransformationSpec"
private const val DP = "androidx.compose.ui.unit.Dp"
private const val LIST_ITEM = "list-item"

/**
 * Colours as the Wear canvas reads them: a theme role is `MaterialTheme.colorScheme.<role>`, with
 * the two roles Wear's `ColorScheme` lacks drawn as their nearest Wear role.
 */
val WEAR_COLOR: ExportProjection.Codec.Color =
  ExportProjection.Codec.Color(
    MATERIAL_THEME,
    roles =
      mapOf("surface" to "surfaceContainer", "surfaceContainerHighest" to "surfaceContainerHigh"),
  )

/** Wear's own type scale, the roles `MaterialTheme.typography` has. */
val WEAR_TYPOGRAPHY: Set<String> =
  setOf(
    "displayLarge",
    "displayMedium",
    "displaySmall",
    "titleLarge",
    "titleMedium",
    "titleSmall",
    "bodyLarge",
    "bodyMedium",
    "bodySmall",
    "bodyExtraSmall",
    "labelLarge",
    "labelMedium",
    "labelSmall",
    "numeralExtraLarge",
    "numeralLarge",
    "numeralMedium",
    "numeralSmall",
    "numeralExtraSmall",
  )

/**
 * The style roles the shelf keeps: Wear's scale plus the Material 3 headline names, kept so older
 * designs stay valid. A headline has no Wear member to export to, so the projection refuses it by
 * name until a token codec can rename it onto the matching title role.
 */
val WEAR_STYLE_ROLES: Set<String> =
  WEAR_TYPOGRAPHY + setOf("headlineLarge", "headlineMedium", "headlineSmall")

/**
 * A string the builder stores, empty for unset, exported through [export]. The catalog's existing
 * wire values stay exactly as they are; only the export is typed.
 */
private fun exported(
  export: ExportProjection.Codec,
  allowed: Set<String>? = null,
): AdapterValueCodec<String> =
  object : AdapterValueCodec<String> by AdapterValueCodecs.String {
    override val stateType: String? = null
    override val exportCodec = export
    override val allowedValues = allowed
  }

private fun wireEnum(typeFqn: String, members: Map<String, String>) =
  exported(ExportProjection.Codec.Enum(typeFqn, members), members.keys)

private val OVERFLOW =
  "androidx.compose.ui.text.style.TextOverflow"
    .let { type ->
      wireEnum(
        type,
        mapOf("clip" to "$type.Clip", "ellipsis" to "$type.Ellipsis", "visible" to "$type.Visible"),
      )
    }

private val TEXT_ALIGN =
  "androidx.compose.ui.text.style.TextAlign"
    .let { type ->
      wireEnum(
        type,
        listOf("left", "right", "center", "justify", "start", "end").associateWith { wire ->
          "$type.${wire.replaceFirstChar(Char::uppercaseChar)}"
        },
      )
    }

private val FONT_WEIGHT =
  "androidx.compose.ui.text.font.FontWeight"
    .let { type ->
      wireEnum(
        type,
        mapOf(
          "normal" to "$type.Normal",
          "medium" to "$type.Medium",
          "semiBold" to "$type.SemiBold",
          "bold" to "$type.Bold",
        ),
      )
    }

private val FONT_STYLE =
  "androidx.compose.ui.text.font.FontStyle"
    .let { type -> wireEnum(type, mapOf("normal" to "$type.Normal", "italic" to "$type.Italic")) }

private val TEXT_DECORATION =
  "androidx.compose.ui.text.style.TextDecoration"
    .let { type ->
      wireEnum(
        type,
        mapOf(
          "none" to "$type.None",
          "underline" to "$type.Underline",
          "lineThrough" to "$type.LineThrough",
        ),
      )
    }

private val STYLE =
  exported(
    ExportProjection.Codec.Token(
      MATERIAL_THEME,
      listOf("typography"),
      "androidx.compose.ui.text.TextStyle",
      WEAR_TYPOGRAPHY,
    ),
    WEAR_STYLE_ROLES,
  )

private val COLOR = AdapterValueCodecs.color(WEAR_COLOR)

/** What a row of a transforming list writes: its height follows the list's `spec`. */
private val transformedHeight =
  ChainLink(
    "$MATERIAL3.lazy.transformedHeight",
    positional =
      listOf(
        ScreenValue.ScopeRead.Builder(ITEM_SCOPE).build(),
        ScreenValue.LocalRead.Builder("spec", TRANSFORMATION_SPEC).build(),
      ),
  )

private fun dp(owner: String, member: String): ScreenValue =
  ScreenValue.Reference("$MATERIAL3.$owner", listOf(member), typeFqn = DP)

/**
 * `minimumVerticalContentPadding(…)`, a member of the item scope, with the component's minimums.
 */
private fun minimumPadding(vararg minimums: ScreenValue) =
  ChainLink(
    "$ITEM_SCOPE.minimumVerticalContentPadding",
    positional = minimums.toList(),
    receiverScopeFqn = ITEM_SCOPE,
  )

/** `transformation = SurfaceTransformation(spec)`, for a component that draws a surface. */
private val surfaceTransformation =
  ExportProjection.Value.Fixed(
    ScreenValue.Construct(
      "$MATERIAL3.SurfaceTransformation",
      positional = listOf(ScreenValue.LocalRead.Builder("spec", TRANSFORMATION_SPEC).build()),
      typeFqn = "$MATERIAL3.SurfaceTransformation",
    )
  )

data class RealTextProperties(
  val text: String,
  val style: String,
  val fontWeight: String,
  val fontStyle: String,
  val color: WireColor,
  val fontSizeSp: Float,
  val lineHeightSp: Float,
  val letterSpacingSp: Float,
  val fontVariationSettings: String,
  val fontFeatureSettings: String,
  val minLines: Int,
  val maxLines: Int,
  val softWrap: Boolean,
  val overflow: String,
  val textAlign: String,
  val textDecoration: String,
)

/** `wear-m3/text` as `material3.Text(text: String, …)`. */
class RealTextAdapter(record: ComponentRecord) :
  TypedComponentAdapter<RealTextProperties>("wear-m3/text", record) {
  val text = property(RealTextProperties::text, AdapterValueCodecs.String, "", bindable = false)
  val style = wireProperty(RealTextProperties::style, STYLE, "")
  val fontWeight = wireProperty(RealTextProperties::fontWeight, FONT_WEIGHT, "")
  val fontStyle = wireProperty(RealTextProperties::fontStyle, FONT_STYLE, "")
  val color = wireProperty(RealTextProperties::color, COLOR, WireColor.Unset)
  val fontSizeSp =
    wireProperty(RealTextProperties::fontSizeSp, AdapterValueCodecs.unit("sp", positive = true), 0f)
  val lineHeightSp =
    wireProperty(
      RealTextProperties::lineHeightSp,
      AdapterValueCodecs.unit("sp", positive = true),
      0f,
    )
  val letterSpacingSp =
    wireProperty(RealTextProperties::letterSpacingSp, AdapterValueCodecs.unit("sp"), 0f)
  // Reach the call only through a `TextStyle` copy, which a projection cannot write.
  val fontVariationSettings =
    wireProperty(RealTextProperties::fontVariationSettings, AdapterValueCodecs.String, "")
  val fontFeatureSettings =
    wireProperty(RealTextProperties::fontFeatureSettings, AdapterValueCodecs.String, "")
  val minLines = property(RealTextProperties::minLines, AdapterValueCodecs.Int, 1, bindable = false)
  val maxLines =
    property(RealTextProperties::maxLines, AdapterValueCodecs.Int, Int.MAX_VALUE, bindable = false)
  val softWrap =
    property(RealTextProperties::softWrap, AdapterValueCodecs.Boolean, true, bindable = false)
  val overflow = wireProperty(RealTextProperties::overflow, OVERFLOW, "")
  val textAlign = wireProperty(RealTextProperties::textAlign, TEXT_ALIGN, "")
  val textDecoration = wireProperty(RealTextProperties::textDecoration, TEXT_DECORATION, "")

  init {
    exportAs(
      target(
        record,
        mapOf(
          "text" to from(text),
          "style" to from(style),
          "color" to from(color),
          "fontWeight" to from(fontWeight),
          "fontStyle" to from(fontStyle),
          "fontSize" to from(fontSizeSp),
          "lineHeight" to from(lineHeightSp),
          "letterSpacing" to from(letterSpacingSp, omitWhen = JsonPrimitive(0)),
          "textDecoration" to from(textDecoration, omitWhen = JsonPrimitive("none")),
          "textAlign" to from(textAlign),
          "minLines" to from(minLines, omitWhen = JsonPrimitive(1)),
          "maxLines" to from(maxLines, omitWhen = JsonPrimitive(Int.MAX_VALUE)),
          "softWrap" to from(softWrap, omitWhen = JsonPrimitive(true)),
          "overflow" to from(overflow),
        ),
        // A bare `Text` draws no surface: the height treatment and `TextDefaults`' list padding.
        contexts =
          mapOf(
            LIST_ITEM to
              ExportProjection.Context(
                modifier =
                  listOf(
                    transformedHeight,
                    minimumPadding(
                      dp("TextDefaults", "minimumTopListContentPadding"),
                      dp("TextDefaults", "minimumBottomListContentPadding"),
                    ),
                  )
              )
          ),
      )
    )
    exportRefuses(fontVariationSettings, fontFeatureSettings)
  }
}

data class RealListHeaderProperties(val text: String, val maxLines: Int, val overflow: String)

/** `wear-m3/list-header` as `material3.ListHeader { Text(…) }`, the label a synthesized text. */
class RealListHeaderAdapter(record: ComponentRecord) :
  TypedComponentAdapter<RealListHeaderProperties>("wear-m3/list-header", record) {
  val text = wireProperty(RealListHeaderProperties::text, AdapterValueCodecs.String, "")
  val maxLines =
    wireProperty(RealListHeaderProperties::maxLines, AdapterValueCodecs.Int, Int.MAX_VALUE)
  val overflow = wireProperty(RealListHeaderProperties::overflow, OVERFLOW, "")

  init {
    exportAs(
      target(
        record,
        emptyMap(),
        synthesize =
          mapOf(
            "content" to
              ExportProjection.Synthesis(
                "wear-m3/text",
                mapOf("text" to "text", "maxLines" to "maxLines", "overflow" to "overflow"),
              )
          ),
        contexts =
          mapOf(
            LIST_ITEM to
              ExportProjection.Context(
                parameters = mapOf("transformation" to surfaceTransformation),
                modifier =
                  listOf(
                    transformedHeight,
                    minimumPadding(
                      dp("ListHeaderDefaults", "minimumTopListContentPadding"),
                      dp("ListHeaderDefaults", "minimumBottomListContentPadding"),
                    ),
                  ),
              )
          ),
      )
    )
  }
}

data class RealIconProperties(
  val iconKey: String,
  val sizeDp: Float,
  val contentDescription: String,
  val color: WireColor,
)

/**
 * `wear-m3/icon` as `material3.Icon(imageVector, contentDescription, tint)`: the key is written
 * from the host's Material icon table, as `m3/icon` is. A set `sizeDp` becomes `Modifier.size(…)`,
 * as the hand exporter writes it; unset, the icon keeps the library's default size.
 */
class RealIconAdapter(record: ComponentRecord) :
  TypedComponentAdapter<RealIconProperties>("wear-m3/icon", record) {
  val iconKey =
    wireProperty(RealIconProperties::iconKey, AdapterValueCodecs.materialIcon(), "filled/add")
  val sizeDp = wireProperty(RealIconProperties::sizeDp, AdapterValueCodecs.unit("dp"), 0f)
  val contentDescription =
    wireProperty(RealIconProperties::contentDescription, AdapterValueCodecs.String, "")
  val color = wireProperty(RealIconProperties::color, COLOR, WireColor.Unset)

  init {
    exportAs(
      target(
        record,
        mapOf(
          "imageVector" to from(iconKey),
          "contentDescription" to from(contentDescription),
          "tint" to from(color),
        ),
        contexts =
          mapOf(LIST_ITEM to ExportProjection.Context(modifier = listOf(transformedHeight))),
        modifier =
          listOf(
            ExportProjection.ModifierLink(
              "androidx.compose.foundation.layout.size",
              listOf(from(sizeDp)),
            )
          ),
      )
    )
  }
}

data class RealButtonProperties(
  val variant: String,
  val enabled: Boolean,
  val containerColor: WireColor,
  val contentColor: WireColor,
  val onClick: () -> Unit,
)

/**
 * `wear-m3/button` as Wear's four buttons, picked by `variant`. Its content is recognised as the
 * slot overload (an optional icon, then one or two lines as `label` and `secondaryLabel`) and
 * otherwise goes to the `content` overload, as the hand-written exporter does. Outlined and child
 * buttons draw no container, so their colour factories take no `containerColor` and setting one
 * there is refused.
 */
class RealButtonAdapter(
  record: ComponentRecord,
  private val tonal: ComponentRecord,
  private val outlined: ComponentRecord,
  private val child: ComponentRecord,
  colorFactories: List<FactorySignature>,
) : TypedComponentAdapter<RealButtonProperties>("wear-m3/button", record) {
  val variant =
    wireProperty(
      RealButtonProperties::variant,
      exported(ExportProjection.Codec.Text, setOf("filled", "filled-tonal", "outlined", "child")),
      "",
    )
  val enabled =
    property(RealButtonProperties::enabled, AdapterValueCodecs.Boolean, true, bindable = false)
  val containerColor = wireProperty(RealButtonProperties::containerColor, COLOR, WireColor.Unset)
  val contentColor = wireProperty(RealButtonProperties::contentColor, COLOR, WireColor.Unset)
  val onClick = event(RealButtonProperties::onClick)
  val content = slot("content", required = true)

  private val contentPatterns =
    ExportProjection.SlotPatterns(
      listOf(
        ExportProjection.SlotPattern(
          listOf(
            ExportProjection.Matcher.Component("wear-m3/icon", "icon", optional = true),
            ExportProjection.Matcher.Lines("wear-m3/text", listOf("label", "secondaryLabel"), 1, 2),
          )
        )
      ),
      otherwise = "content",
    )

  private val listItem =
    ExportProjection.Context(
      parameters = mapOf("transformation" to surfaceTransformation),
      modifier =
        listOf(
          transformedHeight,
          minimumPadding(dp("ButtonDefaults", "minimumVerticalListContentPadding")),
        ),
    )

  private fun button(
    record: ComponentRecord,
    factory: String,
    variants: Set<String>?,
    container: Boolean,
    colorFactories: List<FactorySignature>,
  ) =
    target(
      record,
      mapOf(
        "enabled" to from(enabled, omitWhen = JsonPrimitive(true)),
        "colors" to
          call(
            colorFactories.named("$MATERIAL3.ButtonDefaults.$factory"),
            // The content colour tints an untinted icon too, as the canvas draws it.
            *(listOfNotNull(
                ("containerColor" to from(containerColor)).takeIf { container },
                "contentColor" to from(contentColor),
                "iconColor" to from(contentColor),
              )
              .toTypedArray()),
          ),
      ),
      variants = variants,
      events = mapOf("click" to "onClick"),
      contexts = mapOf(LIST_ITEM to listItem),
      slotPatterns = mapOf("content" to contentPatterns),
    )

  init {
    exportAs(
      button(record, "buttonColors", null, true, colorFactories),
      button(record, "buttonColors", setOf("filled"), true, colorFactories),
      button(tonal, "filledTonalButtonColors", setOf("filled-tonal"), true, colorFactories),
      button(outlined, "outlinedButtonColors", setOf("outlined"), false, colorFactories),
      button(child, "childButtonColors", setOf("child"), false, colorFactories),
      variant = variant,
    )
  }
}

data class RealCardProperties(
  val variant: String,
  val containerColor: WireColor,
  val contentColor: WireColor,
  val onClick: () -> Unit,
)

/**
 * `wear-m3/card` as Wear's four cards, picked by `variant`, as the hand exporter writes them. A
 * `title` card (the default) is `TitleCard`, its first line the title and the rest its content; an
 * `app` card is `AppCard`, which needs an app name and a title, so fewer than two lines are
 * refused; `outlined` and `plain` are `OutlinedCard` and `Card`, which take their children as
 * content. A card's lines are a text, or a plain column of texts; anything else is the title whole.
 */
class RealCardAdapter(
  title: ComponentRecord,
  private val app: ComponentRecord,
  private val outlined: ComponentRecord,
  private val plain: ComponentRecord,
  colorFactories: List<FactorySignature>,
) : TypedComponentAdapter<RealCardProperties>("wear-m3/card", title) {
  val variant =
    wireProperty(
      RealCardProperties::variant,
      exported(ExportProjection.Codec.Text, setOf("title", "app", "outlined", "plain")),
      "",
    )
  val containerColor = wireProperty(RealCardProperties::containerColor, COLOR, WireColor.Unset)
  val contentColor = wireProperty(RealCardProperties::contentColor, COLOR, WireColor.Unset)
  val onClick = event(RealCardProperties::onClick)
  val content = slot("content", required = true)

  private val listItem =
    ExportProjection.Context(
      parameters = mapOf("transformation" to surfaceTransformation),
      modifier =
        listOf(
          transformedHeight,
          minimumPadding(dp("CardDefaults", "minimumVerticalListContentPadding")),
        ),
    )

  private fun card(
    record: ComponentRecord,
    variants: Set<String>?,
    container: Boolean,
    patterns: ExportProjection.SlotPatterns?,
    colorFactories: List<FactorySignature>,
  ) =
    target(
      record,
      mapOf(
        "colors" to
          call(
            colorFactories.named(
              "$MATERIAL3.CardDefaults.${if (container) "cardColors" else "outlinedCardColors"}"
            ),
            *(listOfNotNull(
                ("containerColor" to from(containerColor)).takeIf { container },
                "contentColor" to from(contentColor),
              )
              .toTypedArray()),
          )
      ),
      variants = variants,
      events = mapOf("click" to "onClick"),
      contexts = mapOf(LIST_ITEM to listItem),
      slotPatterns = patterns?.let { mapOf("content" to it) }.orEmpty(),
    )

  private val titleLines =
    ExportProjection.SlotPatterns(
      listOf(
        ExportProjection.SlotPattern(
          listOf(ExportProjection.Matcher.Lines("wear-m3/text", listOf("title"), rest = "content"))
        )
      ),
      otherwise = "title",
    )

  private val appLines =
    ExportProjection.SlotPatterns(
      listOf(
        ExportProjection.SlotPattern(
          listOf(
            ExportProjection.Matcher.Lines(
              "wear-m3/text",
              listOf("appName", "title"),
              min = 3,
              rest = "content",
            )
          )
        )
      )
    )

  init {
    exportAs(
      card(title, null, true, titleLines, colorFactories),
      card(title, setOf("title"), true, titleLines, colorFactories),
      card(app, setOf("app"), true, appLines, colorFactories),
      card(outlined, setOf("outlined"), false, null, colorFactories),
      card(plain, setOf("plain"), true, null, colorFactories),
      variant = variant,
    )
  }
}

data class RealIconButtonProperties(
  val variant: String,
  val enabled: Boolean,
  val containerColor: WireColor,
  val contentColor: WireColor,
  val onClick: () -> Unit,
)

/**
 * `wear-m3/icon-button` as Wear's icon buttons, picked by `variant`, each with its own
 * `IconButtonDefaults` colours. An outlined icon button draws no container, so setting one there is
 * refused. `filled-variant` has no target: `FilledVariantIconButton` is not in the Wear Compose
 * this module discovers, so that variant is refused by name until it is. Icon buttons take no
 * `transformation`, so a list row writes only the row modifiers.
 */
class RealIconButtonAdapter(
  record: ComponentRecord,
  private val filled: ComponentRecord,
  private val tonal: ComponentRecord,
  private val outlined: ComponentRecord,
  colorFactories: List<FactorySignature>,
) : TypedComponentAdapter<RealIconButtonProperties>("wear-m3/icon-button", record) {
  val variant =
    wireProperty(
      RealIconButtonProperties::variant,
      exported(
        ExportProjection.Codec.Text,
        setOf("standard", "filled", "filled-tonal", "filled-variant", "outlined"),
      ),
      "",
    )
  val enabled =
    property(RealIconButtonProperties::enabled, AdapterValueCodecs.Boolean, true, bindable = false)
  val containerColor =
    wireProperty(RealIconButtonProperties::containerColor, COLOR, WireColor.Unset)
  val contentColor = wireProperty(RealIconButtonProperties::contentColor, COLOR, WireColor.Unset)
  val onClick = event(RealIconButtonProperties::onClick)
  val content = slot("content", required = true)

  private val listItem =
    ExportProjection.Context(
      modifier =
        listOf(
          transformedHeight,
          minimumPadding(dp("IconButtonDefaults", "minimumVerticalListContentPadding")),
        )
    )

  private fun iconButton(
    record: ComponentRecord,
    factory: String,
    variants: Set<String>?,
    container: Boolean,
    colorFactories: List<FactorySignature>,
  ) =
    target(
      record,
      mapOf(
        "enabled" to from(enabled, omitWhen = JsonPrimitive(true)),
        "colors" to
          call(
            colorFactories.named("$MATERIAL3.IconButtonDefaults.$factory"),
            *(listOfNotNull(
                ("containerColor" to from(containerColor)).takeIf { container },
                "contentColor" to from(contentColor),
              )
              .toTypedArray()),
          ),
      ),
      variants = variants,
      events = mapOf("click" to "onClick"),
      contexts = mapOf(LIST_ITEM to listItem),
    )

  init {
    exportAs(
      iconButton(record, "iconButtonColors", null, true, colorFactories),
      iconButton(record, "iconButtonColors", setOf("standard"), true, colorFactories),
      iconButton(filled, "filledIconButtonColors", setOf("filled"), true, colorFactories),
      iconButton(
        tonal,
        "filledTonalIconButtonColors",
        setOf("filled-tonal"),
        true,
        colorFactories,
      ),
      iconButton(outlined, "outlinedIconButtonColors", setOf("outlined"), false, colorFactories),
      variant = variant,
    )
  }
}

class RealButtonGroupProperties

/**
 * `wear-m3/button-group` as `ButtonGroup`, its children the group's content, where
 * `Modifier.weight` shares out its width. In a list it is a transformed surface with
 * `ButtonGroupDefaults` padding.
 */
class RealButtonGroupAdapter(record: ComponentRecord) :
  TypedComponentAdapter<RealButtonGroupProperties>("wear-m3/button-group", record) {
  val children = slot("children")

  init {
    exportAs(
      target(
        record,
        emptyMap(),
        slots = mapOf("children" to "content"),
        contexts =
          mapOf(
            LIST_ITEM to
              ExportProjection.Context(
                parameters = mapOf("transformation" to surfaceTransformation),
                modifier =
                  listOf(
                    transformedHeight,
                    minimumPadding(dp("ButtonGroupDefaults", "minimumVerticalListContentPadding")),
                  ),
              )
          ),
      )
    )
  }
}

data class RealAlertDialogProperties(val title: String, val text: String, val visible: Boolean)

/**
 * `wear-m3/alert-dialog` as `AlertDialog`'s confirm-and-dismiss overload, which a screen frame
 * writes beside the scaffold (its `overlays` slot). `visible` is the authored value: nothing in a
 * design shows the dialog, so a literal `false` and no-op dismiss behave as the hand exporter's
 * hoisted state does. The title and text are synthesized `Text`s; the buttons are the design's own.
 */
class RealAlertDialogAdapter(record: ComponentRecord) :
  TypedComponentAdapter<RealAlertDialogProperties>("wear-m3/alert-dialog", record) {
  val title = wireProperty(RealAlertDialogProperties::title, AdapterValueCodecs.String, "")
  val text = wireProperty(RealAlertDialogProperties::text, AdapterValueCodecs.String, "")
  val visible =
    property(
      RealAlertDialogProperties::visible,
      AdapterValueCodecs.Boolean,
      false,
      bindable = false,
    )
  val confirmButton = slot("confirmButton")
  val dismissButton = slot("dismissButton")

  init {
    exportAs(
      target(
        record,
        mapOf("visible" to from(visible)),
        synthesize =
          mapOf(
            "title" to ExportProjection.Synthesis("wear-m3/text", mapOf("text" to "title")),
            "text" to ExportProjection.Synthesis("wear-m3/text", mapOf("text" to "text")),
          ),
      )
    )
  }
}

data class RealEdgeButtonProperties(
  val size: String,
  val enabled: Boolean,
  val containerColor: WireColor,
  val contentColor: WireColor,
  val onClick: () -> Unit,
)

private val EDGE_BUTTON_SIZE =
  "$MATERIAL3.EdgeButtonSize"
    .let { type ->
      wireEnum(
        type,
        mapOf(
          "extra-small" to "$type.ExtraSmall",
          "small" to "$type.Small",
          "medium" to "$type.Medium",
          "large" to "$type.Large",
        ),
      )
    }

/**
 * `wear-m3/edge-button` as `EdgeButton`, which the screen frame writes into `ScreenScaffold`'s
 * `edgeButton`. It is always the filled shape, so its colours are `ButtonDefaults.buttonColors`; an
 * unset size keeps the library's default, as the hand exporter writes it.
 */
class RealEdgeButtonAdapter(record: ComponentRecord, colorFactories: List<FactorySignature>) :
  TypedComponentAdapter<RealEdgeButtonProperties>("wear-m3/edge-button", record) {
  val size = wireProperty(RealEdgeButtonProperties::size, EDGE_BUTTON_SIZE, "")
  val enabled =
    property(RealEdgeButtonProperties::enabled, AdapterValueCodecs.Boolean, true, bindable = false)
  val containerColor =
    wireProperty(RealEdgeButtonProperties::containerColor, COLOR, WireColor.Unset)
  val contentColor = wireProperty(RealEdgeButtonProperties::contentColor, COLOR, WireColor.Unset)
  val onClick = event(RealEdgeButtonProperties::onClick)
  val content = slot("content", required = true)

  init {
    exportAs(
      target(
        record,
        mapOf(
          "buttonSize" to from(size),
          "enabled" to from(enabled, omitWhen = JsonPrimitive(true)),
          "colors" to
            call(
              colorFactories.named("$MATERIAL3.ButtonDefaults.buttonColors"),
              "containerColor" to from(containerColor),
              "contentColor" to from(contentColor),
              "iconColor" to from(contentColor),
            ),
        ),
        events = mapOf("click" to "onClick"),
      )
    )
  }
}

data class RealListProperties(val verticalSpacingDp: Float, val transformation: String)

/**
 * `wear-m3/transforming-lazy-column` as `TransformingLazyColumn`, reading the screen frame's
 * `listState` and `contentPadding`, each child wrapped in `item { … }`. With `transformation =
 * spec` (the default) each item opens the `list-item` role, so its child writes that context;
 * `none` wraps the rows without it.
 */
class RealListAdapter(record: ComponentRecord, private val arrangement: List<FactorySignature>) :
  TypedComponentAdapter<RealListProperties>("wear-m3/transforming-lazy-column", record) {
  val verticalSpacingDp =
    wireProperty(RealListProperties::verticalSpacingDp, AdapterValueCodecs.unit("dp"), 4f)
  val transformation =
    wireProperty(
      RealListProperties::transformation,
      exported(ExportProjection.Codec.Text, setOf("spec", "none")),
      "",
    )
  val items = slot("items")

  private fun list(variants: Set<String>?, roles: Map<String, String>) =
    target(
      component,
      mapOf(
        "state" to
          ExportProjection.Value.Fixed(
            ScreenValue.LocalRead.Builder(
                "listState",
                "androidx.wear.compose.foundation.lazy.TransformingLazyColumnState",
              )
              .build()
          ),
        "contentPadding" to
          ExportProjection.Value.Fixed(
            ScreenValue.LocalRead.Builder(
                "contentPadding",
                "androidx.compose.foundation.layout.PaddingValues",
              )
              .build()
          ),
        "verticalArrangement" to call(arrangement, "space" to from(verticalSpacingDp)),
      ),
      variants = variants,
      slots = mapOf("items" to "content"),
      items = mapOf("content" to "item"),
      itemRoles = roles,
    )

  init {
    val rows = mapOf("content" to LIST_ITEM)
    exportAs(
      list(null, rows),
      list(setOf("spec"), rows),
      list(setOf("none"), emptyMap()),
      variant = transformation,
    )
  }
}
