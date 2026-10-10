package ee.schimke.catalog.typed

import ee.schimke.composeai.discovery.*
import ee.schimke.composeai.uibuilder.protocol.JsonValueTypeV1
import ee.schimke.composeai.uibuilder.protocol.PropertyCapabilityV1
import ee.schimke.composeai.uibuilder.protocol.SlotCapabilityV1
import ee.schimke.composeai.uibuilder.protocol.SlotCardinalityV1
import kotlinx.serialization.json.*

// Parallel authored policy. Inventory membership remains annotation-owned.

fun wearM3Policy(): UiBuilderPolicyFile =
  UiBuilderPolicyFile.Builder("compose-ui-builder-policy/v1", "wear")
    .apply {
      jsonSchema =
        "https://raw.githubusercontent.com/yschimke/compose-ai-tools/main/scripts/design-artifacts/ui-builder.policy.schema.json"
      catalogId = "wear-m3"
      platformLabel = "Wear"
      previewSurfaces = policyPreviewSurfaces()
      frame = policyFrame()
      composeSourceExport =
        UiBuilderComposeSourceExport.Builder("wear-compose-screen", 1).apply {}.build()

      builtins =
        linkedMapOf(
          "wear-m3/screen-scaffold" to builtinScreenScaffold(),
          "wear-m3/transforming-lazy-column" to builtinTransformingLazyColumn(),
          "wear-m3/alert-dialog" to builtinAlertDialog(),
          "wear-m3/confirmation-dialog" to builtinConfirmationDialog(),
          "wear-m3/open-on-phone-dialog" to builtinOpenOnPhoneDialog(),
          "wear-m3/progress-indicator" to builtinProgressIndicator(),
          "wear-m3/variable-font-text" to builtinVariableFontText(),
        )
      colorTokens = policyColorTokens()
      designTokens = policyDesignTokens()
      menu =
        UiBuilderMenu.Builder()
          .apply {
            groupOrder =
              listOf(
                "Lists and screens",
                "Fast scrolling",
                "Cards",
                "Dialogs",
                "Buttons",
                "Icon buttons",
                "Edge-hugging buttons",
                "Toggle buttons",
                "Selection buttons",
                "Sliders and steppers",
                "Pickers",
                "Text",
                "Iconography",
                "Progress indicators",
                "Position indicators",
                "Pagers",
                "Swipe to dismiss",
                "Swipe to reveal",
                "One-handed gestures",
                "Sign-in",
                "Media controls",
              )
          }
          .build()
      templates =
        listOf(
          UiBuilderTemplateEntry.Builder("ui-builder/designs/wear-screen.json").build(),
          UiBuilderTemplateEntry.Builder("ui-builder/designs/wear-list.json").build(),
        )
      components =
        linkedMapOf(
          "wear-m3/text" to componentText(),
          "wear-m3/icon" to componentIcon(),
          "wear-m3/list-header" to componentListHeader(),
          "wear-m3/list-sub-header" to componentListSubHeader(),
          "wear-m3/screen-scaffold" to componentScreenScaffold(),
          "wear-m3/card" to componentCard(),
          "wear-m3/app-scaffold" to componentAppScaffold(),
          "wear-m3/time-text" to componentTimeText(),
          "wear-m3/circular-progress-indicator" to componentCircularProgressIndicator(),
          "wear-m3/edge-button" to componentEdgeButton(),
          "wear-m3/sticker" to componentSticker(),
          "wear-m3/screen-sticker" to componentScreenSticker(),
          "wear-m3/transparent-screen-sticker" to componentTransparentScreenSticker(),
          "wear-m3/full-screen-sticker" to componentFullScreenSticker(),
          "wear-m3/edge-button-sticker" to componentEdgeButtonSticker(),
          "wear-m3/animated-sticker" to componentAnimatedSticker(),
          "wear-m3/edge-button-screen" to componentEdgeButtonScreen(),
          "wear-m3/wear-screen" to componentWearScreen(),
          "wear-m3/media-row-sticker" to componentMediaRowSticker(),
          "wear-m3/button" to componentButton(),
          "wear-m3/icon-button" to componentIconButton(),
          "wear-m3/text-button" to componentTextButton(),
          "wear-m3/checkbox-button" to componentCheckboxButton(),
          "wear-m3/switch-button" to componentSwitchButton(),
          "wear-m3/radio-button" to componentRadioButton(),
          "wear-m3/slider" to componentSlider(),
          "wear-m3/stepper" to componentStepper(),
          "wear-m3/button-group" to componentButtonGroup(),
          "wear-m3/date-picker" to componentDatePicker(),
          "wear-m3/time-picker" to componentTimePicker(),
          "wear-m3/alert-dialog-edge-button" to componentAlertDialogEdgeButton(),
          "wear-m3/open-on-phone-dialog-icon" to componentOpenOnPhoneDialogIcon(),
        )
    }
    .build()

private fun policyPreviewSurfaces(): JsonElement = buildJsonObject {
  put(
    "wasm",
    buildJsonObject {
      put("fidelity", JsonPrimitive("approximate"))
      put(
        "reason",
        JsonPrimitive(
          "The canvas draws this catalog's components with a Compose Multiplatform build of Wear Compose — the Android AAR has no browser variant — so each is the component itself rather than an impression of it. What is still the canvas's own: the screen frame (the extent has no viewport for a real scaffold), the unrolled list (a long screenshot turns the row transformation off), and the type scale, which is the mobile theme's. Author on it; check a size on the Android preview, which compiles this design's own generated Kotlin against the real AAR."
        ),
      )
    },
  )
  put(
    "native",
    buildJsonObject {
      put("fidelity", JsonPrimitive("authoritative"))
      put("backend", JsonPrimitive("android"))
      put(
        "reason",
        JsonPrimitive(
          "The generated Kotlin compiled against this module's own classpath and rendered under Robolectric: the same lane that produces this catalog's stickers."
        ),
      )
    },
  )
}

private fun policyFrame(): JsonElement = buildJsonObject {
  put("adapter", JsonPrimitive("frame/round-screen"))
  put("seedDevice", JsonPrimitive("id:wearos_small_round"))
  put(
    "geometry",
    buildJsonObject {
      put(
        "contentPadding",
        JsonArray(
          listOf(
            buildJsonObject {
              put("screenDp", JsonPrimitive(192))
              put("horizontalDp", JsonPrimitive(10))
              put("verticalDp", JsonPrimitive(20))
            },
            buildJsonObject {
              put("screenDp", JsonPrimitive(227))
              put("horizontalDp", JsonPrimitive(12))
              put("verticalDp", JsonPrimitive(23))
            },
            buildJsonObject {
              put("screenDp", JsonPrimitive(240))
              put("horizontalDp", JsonPrimitive(13))
              put("verticalDp", JsonPrimitive(24))
            },
          )
        ),
      )
    },
  )
}

private fun builtinScreenScaffold(): UiBuilderBuiltin =
  UiBuilderBuiltin.Builder("screen-root")
    .apply {
      displayName = "Screen"
      group = "Lists and screens"
      canvas = "frame/round-screen"
      traits = listOf("ScreenContent", "WearScreenHost")
      properties =
        listOf(
          property(
            "timeText",
            JsonValueTypeV1.STRING,
            notes =
              "The curved status strip's text. Frozen rather than live, so a design's render does not change every minute.",
          ),
          property(
            "scrollIndicator",
            JsonValueTypeV1.BOOLEAN,
            notes = "Whether the generated screen gives `ScreenScaffold` a scroll indicator.",
          ),
          property(
            "background",
            JsonValueTypeV1.STRING,
            notes = "The screen's background; Wear Material 3's `background` when absent.",
          ),
          property(
            "themePrimaryColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `primary` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnPrimaryColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onPrimary` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themePrimaryContainerColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `primaryContainer` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnPrimaryContainerColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onPrimaryContainer` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeSecondaryColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `secondary` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnSecondaryColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onSecondary` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeSecondaryContainerColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `secondaryContainer` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnSecondaryContainerColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onSecondaryContainer` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeTertiaryColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `tertiary` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnTertiaryColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onTertiary` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeTertiaryContainerColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `tertiaryContainer` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnTertiaryContainerColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onTertiaryContainer` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeSurfaceContainerLowColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `surfaceContainerLow` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeSurfaceContainerColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `surfaceContainer` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeSurfaceContainerHighColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `surfaceContainerHigh` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnSurfaceColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onSurface` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnSurfaceVariantColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onSurfaceVariant` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOutlineColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `outline` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOutlineVariantColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `outlineVariant` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeBackgroundColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `background` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnBackgroundColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onBackground` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeErrorColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `error` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeOnErrorColor",
            JsonValueTypeV1.STRING,
            notes =
              "Re-skins the screen's `onError` role for everything inside it. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"}, or a colourToken naming another role of the stock Wear scheme. Unset keeps Wear's own.",
          ),
          property(
            "themeDisplayTypeface",
            JsonValueTypeV1.STRING,
            notes =
              "The font family for the display roles (displayLarge, displayMedium, displaySmall, numeralExtraLarge, numeralLarge, numeralMedium, numeralSmall, numeralExtraSmall) of everything this theme host contains. A family name: one of the editor's vendored faces or any Google Fonts family (\"Space Grotesk\", \"Michroma\"); a `google:` prefix is accepted. Generated code sets it as a `GoogleFont` family on the theme's typography. Unset keeps the platform's own face for these roles.",
          ),
          property(
            "themeTitleTypeface",
            JsonValueTypeV1.STRING,
            notes =
              "The font family for the title roles (titleLarge, titleMedium, titleSmall) of everything this theme host contains. A family name: one of the editor's vendored faces or any Google Fonts family (\"Space Grotesk\", \"Michroma\"); a `google:` prefix is accepted. Generated code sets it as a `GoogleFont` family on the theme's typography. Unset keeps the platform's own face for these roles.",
          ),
          property(
            "themeBodyTypeface",
            JsonValueTypeV1.STRING,
            notes =
              "The font family for the body roles (bodyLarge, bodyMedium, bodySmall, bodyExtraSmall) of everything this theme host contains. A family name: one of the editor's vendored faces or any Google Fonts family (\"Space Grotesk\", \"Michroma\"); a `google:` prefix is accepted. Generated code sets it as a `GoogleFont` family on the theme's typography. Unset keeps the platform's own face for these roles.",
          ),
          property(
            "themeLabelTypeface",
            JsonValueTypeV1.STRING,
            notes =
              "The font family for the label roles (labelLarge, labelMedium, labelSmall) of everything this theme host contains. A family name: one of the editor's vendored faces or any Google Fonts family (\"Space Grotesk\", \"Michroma\"); a `google:` prefix is accepted. Generated code sets it as a `GoogleFont` family on the theme's typography. Unset keeps the platform's own face for these roles.",
          ),
          property(
            "themeTextStyle",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("displayLarge"),
                JsonPrimitive("displayMedium"),
                JsonPrimitive("displaySmall"),
                JsonPrimitive("titleLarge"),
                JsonPrimitive("titleMedium"),
                JsonPrimitive("titleSmall"),
                JsonPrimitive("labelLarge"),
                JsonPrimitive("labelMedium"),
                JsonPrimitive("labelSmall"),
                JsonPrimitive("bodyLarge"),
                JsonPrimitive("bodyMedium"),
                JsonPrimitive("bodySmall"),
                JsonPrimitive("bodyExtraSmall"),
                JsonPrimitive("numeralExtraLarge"),
                JsonPrimitive("numeralLarge"),
                JsonPrimitive("numeralMedium"),
                JsonPrimitive("numeralSmall"),
                JsonPrimitive("numeralExtraSmall"),
              ),
            notes =
              "The type role a text with no `style` of its own is set in, for everything this theme host contains — and with it that role's size and typeface. Both themes default to `bodyLarge`; choose `labelMedium` for a design whose plain text is mostly labels, or a display role for one that is mostly figures. Generated code provides it as `ProvideTextStyle(MaterialTheme.typography.<role>)` inside the theme.",
          ),
        )
      slots =
        linkedMapOf(
          "content" to
            buildJsonObject {
              put("required", JsonPrimitive(false))
              put("role", JsonPrimitive("list"))
              put("max", JsonPrimitive(1))
              put(
                "acceptedRoles",
                JsonArray(
                  listOf(
                    JsonPrimitive("Scaffold"),
                    JsonPrimitive("Container"),
                    JsonPrimitive("Leaf"),
                  )
                ),
              )
              put("acceptedTraits", JsonArray(listOf(JsonPrimitive("AnyContent"))))
            },
          "edgeButton" to
            buildJsonObject {
              put("acceptedTraits", JsonArray(listOf(JsonPrimitive("Action"))))
              put("max", JsonPrimitive(1))
            },
          "overlays" to
            buildJsonObject {
              put("acceptedTraits", JsonArray(listOf(JsonPrimitive("Overlay"))))
              put("role", JsonPrimitive("overlay"))
            },
        )
    }
    .build()

private fun builtinTransformingLazyColumn(): UiBuilderBuiltin =
  UiBuilderBuiltin.Builder("list")
    .apply {
      displayName = "Scrolling list"
      group = "Lists and screens"
      canvas = "wear-m3/transforming-lazy-column"
      traits =
        listOf(
          "OrderedContent",
          "VerticalContent",
          "ScrollableContent",
          "RepeatedContent",
          "WearListContent",
        )
      properties =
        listOf(
          property(
            "verticalSpacingDp",
            JsonValueTypeV1.NUMBER,
            notes = "`Arrangement.spacedBy` between items; 4dp is the Wear list default.",
          ),
          property(
            "transformation",
            JsonValueTypeV1.STRING,
            allowedValues = listOf(JsonPrimitive("spec"), JsonPrimitive("none")),
            notes =
              "Whether each item carries `SurfaceTransformation(spec)` and `transformedHeight`.",
          ),
        )
      slots =
        linkedMapOf(
          "items" to
            buildJsonObject {
              put("role", JsonPrimitive("list-item"))
              put(
                "acceptedRoles",
                JsonArray(listOf(JsonPrimitive("Container"), JsonPrimitive("Leaf"))),
              )
              put("acceptedTraits", JsonArray(listOf(JsonPrimitive("AnyContent"))))
            }
        )
    }
    .build()

private fun builtinAlertDialog(): UiBuilderBuiltin =
  UiBuilderBuiltin.Builder("overlay")
    .apply {
      shelfRole = "Container"
      displayName = "Alert dialog"
      group = "Dialogs"
      canvas = "wear-m3/alert-dialog"
      traits = listOf("Overlay")
      properties =
        listOf(
          property("title", JsonValueTypeV1.STRING, required = true, notes = "The dialog's title."),
          property(
            "text",
            JsonValueTypeV1.STRING,
            notes = "The supporting line under the title. Empty emits no `text` argument.",
          ),
          property(
            "visible",
            JsonValueTypeV1.BOOLEAN,
            JsonValueTypeV1.OBJECT,
            notes =
              "Whether the dialog is showing. A dialog is a screen state rather than a place in the layout, so this is what the generated `AlertDialog(visible = …)` reads.",
          ),
        )
      slots =
        linkedMapOf(
          "content" to
            buildJsonObject {
              put("acceptedTraits", JsonArray(listOf(JsonPrimitive("AnyContent"))))
              put("ordered", JsonPrimitive(true))
            },
          "confirmButton" to
            buildJsonObject {
              put("acceptedTraits", JsonArray(listOf(JsonPrimitive("Action"))))
              put("max", JsonPrimitive(1))
              put("ordered", JsonPrimitive(true))
            },
          "dismissButton" to
            buildJsonObject {
              put("acceptedTraits", JsonArray(listOf(JsonPrimitive("Action"))))
              put("max", JsonPrimitive(1))
              put("ordered", JsonPrimitive(true))
            },
        )
    }
    .build()

private fun builtinConfirmationDialog(): UiBuilderBuiltin =
  UiBuilderBuiltin.Builder("overlay")
    .apply {
      shelfRole = "Leaf"
      displayName = "Confirmation dialog"
      group = "Dialogs"
      canvas = "wear-m3/confirmation-dialog"
      traits = listOf("Overlay")
      properties =
        listOf(
          property(
            "text",
            JsonValueTypeV1.STRING,
            required = true,
            notes = "The line shown under the icon.",
          ),
          property(
            "variant",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(JsonPrimitive("generic"), JsonPrimitive("success"), JsonPrimitive("failure")),
            notes =
              "`ConfirmationDialog`, `SuccessConfirmationDialog` or `FailureConfirmationDialog`. The two named ones bring their own icon and curved text; the generic one takes the text alone.",
          ),
          property(
            "visible",
            JsonValueTypeV1.BOOLEAN,
            JsonValueTypeV1.OBJECT,
            notes = "Whether it is showing, as for `wear-m3/alert-dialog`.",
          ),
        )
    }
    .build()

private fun builtinOpenOnPhoneDialog(): UiBuilderBuiltin =
  UiBuilderBuiltin.Builder("overlay")
    .apply {
      shelfRole = "Leaf"
      displayName = "Open on phone dialog"
      group = "Dialogs"
      canvas = "wear-m3/open-on-phone-dialog"
      traits = listOf("Overlay")
      properties =
        listOf(
          property(
            "text",
            JsonValueTypeV1.STRING,
            notes = "The curved line under the animation. Empty takes upstream's own.",
          ),
          property(
            "visible",
            JsonValueTypeV1.BOOLEAN,
            JsonValueTypeV1.OBJECT,
            notes = "Whether it is showing, as for `wear-m3/alert-dialog`.",
          ),
        )
    }
    .build()

private fun builtinProgressIndicator(): UiBuilderBuiltin =
  UiBuilderBuiltin.Builder("list-item")
    .apply {
      shelfRole = "Leaf"
      displayName = "Progress indicator"
      group = "Progress indicators"
      canvas = "wear-m3/progress-indicator"
      traits = listOf("ListItem", "WearListContent", "ScreenContent")
      properties =
        listOf(
          property(
            "variant",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("circular"),
                JsonPrimitive("segmented-circular"),
                JsonPrimitive("linear"),
                JsonPrimitive("arc"),
              ),
            notes =
              "Which indicator is written: `CircularProgressIndicator`, `SegmentedCircularProgressIndicator`, `LinearProgressIndicator` or `ArcProgressIndicator`. The circular ones ring the whole display; the linear one is a list row.",
          ),
          property(
            "progress",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "0..1. Absent is the indeterminate form, which takes no progress.",
          ),
          property(
            "segments",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "How many segments the segmented circular form is divided into.",
          ),
          property(
            "indicatorColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
          property(
            "trackColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
        )
    }
    .build()

private fun builtinVariableFontText(): UiBuilderBuiltin =
  UiBuilderBuiltin.Builder("list-item")
    .apply {
      shelfRole = "Leaf"
      displayName = "Variable font text"
      group = "Text"
      canvas = "wear-m3/variable-font-text"
      traits = listOf("TextContent")
      properties =
        listOf(
          property(
            "text",
            JsonValueTypeV1.STRING,
            required = true,
            notes =
              "A literal: the outline is worked out from the font when the design exports, so the text cannot change at run time.",
          ),
          property(
            "font",
            JsonValueTypeV1.STRING,
            allowedValues = listOf(JsonPrimitive("robotoFlex"), JsonPrimitive("googleSansFlex")),
            notes = "The variable font the text is drawn from. Absent is Roboto Flex.",
          ),
          property(
            "wght",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes =
              "The `wght` axis: weight, 100–1000 in Roboto Flex and 1–1000 in Google Sans Flex. A literal holds it; a `state` read of a decimal variable animates it. Unset is the font's own 400.",
          ),
          property(
            "wdth",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes =
              "The `wdth` axis: width, 25–151 (100 is normal). A literal or a `state` read of a decimal variable. Unset is 100.",
          ),
          property(
            "slnt",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes =
              "The `slnt` axis: slant, -10–0 degrees. A literal or a `state` read of a decimal variable. Unset is upright.",
          ),
          property(
            "rond",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes =
              "The `ROND` axis: roundness, 0–100, which only Google Sans Flex has. A literal or a `state` read of a decimal variable. Unset is 0.",
          ),
          property("fontSizeSp", JsonValueTypeV1.NUMBER, notes = "The text's size. Absent is 32."),
          property(
            "color",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is refused at commit.",
          ),
        )
      code =
        UiBuilderBuiltinCode.Builder("VariableFontText")
          .apply { imports = listOf("ee.schimke.flexpress.compose.VariableFontText") }
          .build()
      wasm =
        UiBuilderBuiltinWasm.Builder()
          .apply {
            notes =
              "flexpress's VariableFontText draws the text from the font's outlines, its axes free to move. flexpress runs on the JVM only, so the canvas draws a stand-in: Wear's Text in the vendored variable font, instanced at the same axes."
          }
          .build()
    }
    .build()

private fun policyColorTokens(): JsonElement = buildJsonObject {
  put(
    "roles",
    JsonArray(
      listOf(
        JsonPrimitive("background"),
        JsonPrimitive("surface"),
        JsonPrimitive("surfaceContainer"),
        JsonPrimitive("surfaceContainerLow"),
        JsonPrimitive("surfaceContainerHigh"),
        JsonPrimitive("surfaceContainerHighest"),
        JsonPrimitive("primary"),
        JsonPrimitive("onPrimary"),
        JsonPrimitive("tertiary"),
        JsonPrimitive("onTertiary"),
        JsonPrimitive("onSurface"),
        JsonPrimitive("onSurfaceVariant"),
        JsonPrimitive("outlineVariant"),
        JsonPrimitive("transparent"),
      )
    ),
  )
}

private fun componentText(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.TextKt.Text"
      group = "Text"
      propertyCapabilities =
        listOf(
          property("text", JsonValueTypeV1.STRING, required = true),
          property(
            "style",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("displayLarge"),
                JsonPrimitive("displayMedium"),
                JsonPrimitive("displaySmall"),
                JsonPrimitive("headlineLarge"),
                JsonPrimitive("headlineMedium"),
                JsonPrimitive("headlineSmall"),
                JsonPrimitive("titleLarge"),
                JsonPrimitive("titleMedium"),
                JsonPrimitive("titleSmall"),
                JsonPrimitive("bodyLarge"),
                JsonPrimitive("bodyMedium"),
                JsonPrimitive("bodySmall"),
                JsonPrimitive("bodyExtraSmall"),
                JsonPrimitive("labelLarge"),
                JsonPrimitive("labelMedium"),
                JsonPrimitive("labelSmall"),
                JsonPrimitive("numeralExtraLarge"),
                JsonPrimitive("numeralLarge"),
                JsonPrimitive("numeralMedium"),
                JsonPrimitive("numeralSmall"),
                JsonPrimitive("numeralExtraSmall"),
              ),
            notes =
              "The role the text is set in, resolved against Wear's own type scale. The numeral roles are Wear's figures (a clock, a count, a reading) and take the theme's display typeface; the headline roles are Material 3 names Wear has no scale for, kept so older designs stay valid, and draw and export as the matching title role.",
          ),
          property(
            "fontWeight",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("normal"),
                JsonPrimitive("medium"),
                JsonPrimitive("semiBold"),
                JsonPrimitive("bold"),
              ),
            notes = "Overrides the role's weight.",
          ),
          property(
            "fontStyle",
            JsonValueTypeV1.STRING,
            allowedValues = listOf(JsonPrimitive("normal"), JsonPrimitive("italic")),
            notes = "Overrides the role's style.",
          ),
          property(
            "color",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
          property("fontSizeSp", JsonValueTypeV1.NUMBER, notes = "Overrides the role's size."),
          property(
            "lineHeightSp",
            JsonValueTypeV1.NUMBER,
            notes = "Overrides the role's line height.",
          ),
          property(
            "letterSpacingSp",
            JsonValueTypeV1.NUMBER,
            notes = "Overrides the role's tracking.",
          ),
          property(
            "fontVariationSettings",
            JsonValueTypeV1.STRING,
            notes =
              "Variable-font axis coordinates, CSS `font-variation-settings` style with bare tags: `wght 650, wdth 87.5, GRAD -50`. Applies to the face the text is set in (its role's theme typeface, or Wear's Roboto Flex, which has thirteen axes: wght 100–1000, wdth 25–151, opsz 8–144, GRAD -200–150, …); an axis that face does not have is ignored. `wght` set here overrides `fontWeight`.",
          ),
          property(
            "fontFeatureSettings",
            JsonValueTypeV1.STRING,
            notes =
              "OpenType layout features, CSS `font-feature-settings` style with bare tags: `tnum, ss01, liga 0` — a tag alone turns the feature on, `0` turns it off and a larger number picks an alternate. `tnum` gives every figure the same width, which a clock or a timer needs. A feature the typeface does not have is ignored.",
          ),
          property(
            "minLines",
            JsonValueTypeV1.INTEGER,
            notes = "The smallest height the text occupies, in lines.",
          ),
          property(
            "maxLines",
            JsonValueTypeV1.INTEGER,
            notes = "How many lines the text may occupy before it truncates.",
          ),
          property(
            "softWrap",
            JsonValueTypeV1.BOOLEAN,
            notes = "Whether the text breaks at soft line breaks.",
          ),
          property(
            "overflow",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(JsonPrimitive("clip"), JsonPrimitive("ellipsis"), JsonPrimitive("visible")),
            notes = "What a truncated text does at its edge.",
          ),
          property(
            "textAlign",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("start"),
                JsonPrimitive("center"),
                JsonPrimitive("end"),
                JsonPrimitive("justify"),
              ),
            notes = "How the lines are aligned within the text's own width.",
          ),
          property(
            "textDecoration",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("none"),
                JsonPrimitive("underline"),
                JsonPrimitive("lineThrough"),
              ),
            notes = "A decoration painted under, or through, the glyphs.",
          ),
          property(
            "alignment",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("topStart"),
                JsonPrimitive("topCenter"),
                JsonPrimitive("topEnd"),
                JsonPrimitive("centerStart"),
                JsonPrimitive("center"),
                JsonPrimitive("centerEnd"),
                JsonPrimitive("bottomStart"),
                JsonPrimitive("bottomCenter"),
                JsonPrimitive("bottomEnd"),
              ),
            notes = "Where the text sits in a parent that gives it more room than it needs.",
          ),
          property(
            "weight",
            JsonValueTypeV1.NUMBER,
            notes = "The share of a parent's remaining space the text takes.",
          ),
        )
      canvas = "wear-m3/text"
      traits = listOf("TextContent", "RemoteAuthorable")
      slotCapabilities = listOf()
      modifierCapabilities =
        listOf(
          "size",
          "fillMaxWidth",
          "padding",
          "alpha",
          "offset",
          "rotate",
          "scale",
          "zIndex",
          "testTag",
          "width",
          "height",
          "widthIn",
          "heightIn",
          "aspectRatio",
          "align",
          "alignHorizontal",
          "alignVertical",
          "weight",
        )
    }
    .build()

private fun componentIcon(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.IconKt.Icon"
      group = "Iconography"
      canvas = "wear-m3/icon"
      traits = listOf("Adornment")
      propertyCapabilities =
        listOf(
          property(
            "iconKey",
            JsonValueTypeV1.STRING,
            required = true,
            notes =
              "A Material icon key, resolved to `Icons.…` by the same table `m3/icon` uses. The vectors are `androidx.compose.material.icons`, which both platforms share, so this key means the same thing on a watch as on a phone.",
            allowedValues = iconAllowedValues(),
          ),
          property(
            "sizeDp",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "The icon's box. Wear's own default is 24dp inside a button.",
          ),
          property(
            "contentDescription",
            JsonValueTypeV1.STRING,
            notes =
              "What a screen reader says for the icon. Leave it empty beside a label, which already names the action; set it when the icon is the whole button — an icon button's only accessible name is this.",
          ),
          property(
            "color",
            JsonValueTypeV1.STRING,
            notes =
              "The icon's tint, written as `tint =`. A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentListHeader(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.ListHeaderKt.ListHeader"
      canvas = "wear-m3/list-header"
      group = "Lists and screens"
      traits = listOf("TextContent", "RemoteAuthorable", "ListItem")
      propertyCapabilities =
        listOf(
          property("text", JsonValueTypeV1.STRING, required = true, notes = "The label."),
          property(
            "maxLines",
            JsonValueTypeV1.INTEGER,
            notes = "How many lines the label may occupy before it truncates.",
          ),
          property(
            "overflow",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(JsonPrimitive("clip"), JsonPrimitive("ellipsis"), JsonPrimitive("visible")),
            notes = "What a truncated label does at its edge.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentListSubHeader(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.ListHeaderKt.ListSubHeader"
      canvas = "wear-m3/list-sub-header"
      traits = listOf("ListItem", "WearListContent")
      propertyCapabilities =
        listOf(
          property("text", JsonValueTypeV1.STRING, required = true, notes = "The label."),
          property(
            "maxLines",
            JsonValueTypeV1.INTEGER,
            notes = "How many lines the label may occupy before it truncates.",
          ),
          property(
            "overflow",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(JsonPrimitive("clip"), JsonPrimitive("ellipsis"), JsonPrimitive("visible")),
            notes = "What a truncated label does at its edge.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentScreenScaffold(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.ScreenScaffoldKt.ScreenScaffold"
      excluded =
        "the `wear-m3/screen-scaffold` builtin above is the screen root, and this record derives the same id. Left in, the record wins the id and the builtin is dropped: the screen composes as a Container with ScreenScaffold's lambda parameters as slots (`timeText`, `scrollIndicator`) and none of the builder's properties, so every Wear template is refused on creation. The Wear screen exporter writes `ScreenScaffold` without a record."
    }
    .build()

private fun componentCard(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.CardKt.Card"
      propertyCapabilities =
        listOf(
          property(
            "variant",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("title"),
                JsonPrimitive("app"),
                JsonPrimitive("outlined"),
                JsonPrimitive("plain"),
              ),
            notes =
              "Which card is written: `TitleCard`, `AppCard`, `OutlinedCard` or `Card`. Wear publishes four composables with different content lambdas rather than four styles of one, so this selects the composable.",
          ),
          property(
            "stableKey",
            JsonValueTypeV1.STRING,
            notes =
              "The item's identity, written as the generated lazy list's `key`. An identity rather than a look: two rows sharing one are one row to a lazy layout.",
          ),
          property("onClickAction", JsonValueTypeV1.OBJECT, JsonValueTypeV1.NULL),
          property(
            "containerColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
          property(
            "contentColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
        )
      canvas = "wear-m3/card"
      traits = listOf("GridItem", "CarouselItem", "ListItem", "OverlayContent")
      slotCapabilities =
        listOf(
          slot(
            "content",
            min = 1,
            max = null,
            ordered = true,
            roles = listOf("Container", "Leaf"),
            traits = listOf("AnyContent"),
          )
        )
      modifierCapabilities =
        listOf(
          "size",
          "fillMaxWidth",
          "padding",
          "clip",
          "alpha",
          "offset",
          "rotate",
          "scale",
          "zIndex",
          "testTag",
          "width",
          "height",
          "widthIn",
          "heightIn",
          "aspectRatio",
          "background",
          "border",
          "shadow",
          "wrapContentSize",
          "align",
          "alignHorizontal",
          "alignVertical",
          "weight",
        )
    }
    .build()

private fun componentAppScaffold(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.AppScaffoldKt.AppScaffold"
      group = "Lists and screens"
    }
    .build()

private fun componentTimeText(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.TimeTextKt.TimeText"
      group = "Text"
    }
    .build()

private fun componentCircularProgressIndicator(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record =
        "catalog/androidx.wear.compose.material3.CircularProgressIndicatorKt.CircularProgressIndicator"
      group = "Progress indicators"
    }
    .build()

private fun componentEdgeButton(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.EdgeButtonKt.EdgeButton"
      group = "Edge-hugging buttons"
      canvas = "wear-m3/edge-button"
      traits = listOf("Action")
      propertyCapabilities =
        listOf(
          property(
            "size",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("extra-small"),
                JsonPrimitive("small"),
                JsonPrimitive("medium"),
                JsonPrimitive("large"),
              ),
            notes =
              "`EdgeButtonSize`. The button's shape comes from the screen's bottom curve, so its size is chosen from upstream's four rather than set in dp.",
          ),
          property(
            "containerColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
          property(
            "contentColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
        )
      slotCapabilities =
        listOf(
          slot(
            "content",
            min = 1,
            max = 1,
            ordered = true,
            roles = listOf(),
            traits = listOf("AnyContent"),
          )
        )
    }
    .build()

private fun componentSticker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.CatalogThemeKt.Sticker"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The bare frame — `CatalogMaterialTheme { Box { content() } }`. Published on `Text`."
    }
    .build()

private fun componentScreenSticker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.CatalogThemeKt.ScreenSticker"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The same frame inside an `AppScaffold` with a stubbed `TimeText`, so a sticker draws as a screen. Published on `Fast scrolling`."
    }
    .build()

private fun componentTransparentScreenSticker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.CatalogThemeKt.TransparentScreenSticker"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The screen frame without a background fill. Published on `Progress indicators`."
    }
    .build()

private fun componentFullScreenSticker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.CatalogThemeKt.FullScreenSticker"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The screen frame with the theme background filled. Published on `Sign-in`."
    }
    .build()

private fun componentEdgeButtonSticker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.CatalogThemeKt.EdgeButtonSticker"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The frame pinned to the 192dp base screen width, so an edge button draws at the width whose arc it is clipped to. Published on `Edge-hugging buttons`."
    }
    .build()

private fun componentAnimatedSticker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.CatalogThemeKt.AnimatedSticker"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The frame for a motion recording — an `AppScaffold` with the clock stubbed out. Already absent from the component menu, so this entry changes nothing there; it is here to put the published reason on the record, which keeps every excluded component and carries the `excluded` string beside it."
    }
    .build()

private fun componentEdgeButtonScreen(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.CatalogThemeKt.EdgeButtonScreen"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The scrollable screen the edge-button reveal is recorded through. Its own KDoc says it is not a component frame: the kit publishes no screen cell for it. Already absent from the component menu, so this entry changes nothing there; it is here to put the published reason on the record, which keeps every excluded component and carries the `excluded` string beside it."
    }
    .build()

private fun componentWearScreen(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.sections.ListsKt.WearScreen"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The list sections' screen frame — `MaterialTheme` plus an `AppScaffold` with a stubbed `TimeText`. It sits in a file that carries a `@file:CatalogGroup`, which is the only reason it published on a plausible-looking shelf (`Lists and screens`) rather than a scattered one."
    }
    .build()

private fun componentMediaRowSticker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/ee.schimke.wearm3catalog.sections.MediaControlsKt.MediaRowSticker"
      excluded =
        "the catalog's own preview frame, not a Wear Material 3 component: every sticker is drawn inside one, the Design Kit publishes no node for it, and nobody places one in a design. Same exclusion, and the same reason, as `m3/sticker` in the sibling m3-catalog. The Media Controls row frame — a `Sticker` narrowed to the kit's 192dp base screen width and a fixed row height. Same as `wear-screen`: its file's `@file:CatalogGroup` is why it published on `Media controls` rather than a scattered shelf."
    }
    .build()

private fun componentButton(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.ButtonKt.Button"
      canvas = "wear-m3/button"
      traits = listOf("Action", "ToolbarItem")
      propertyCapabilities =
        listOf(
          property(
            "variant",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("filled"),
                JsonPrimitive("filled-tonal"),
                JsonPrimitive("outlined"),
                JsonPrimitive("child"),
              ),
            notes =
              "Which button is written: `Button`, `FilledTonalButton`, `OutlinedButton` or `ChildButton`. There is no `fab` and no `elevated` — a watch publishes neither.",
          ),
          property(
            "enabled",
            JsonValueTypeV1.BOOLEAN,
            notes = "Whether the button responds to a press.",
          ),
          property(
            "containerColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
          property(
            "contentColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
          property("onClickAction", JsonValueTypeV1.OBJECT, JsonValueTypeV1.NULL),
        )
      slotCapabilities =
        listOf(
          slot(
            "content",
            min = 1,
            max = null,
            ordered = true,
            roles = listOf("Leaf", "Container"),
            traits = listOf("AnyContent"),
          )
        )
      modifierCapabilities =
        listOf(
          "size",
          "fillMaxWidth",
          "padding",
          "alpha",
          "offset",
          "rotate",
          "scale",
          "zIndex",
          "testTag",
          "width",
          "height",
          "widthIn",
          "heightIn",
          "aspectRatio",
          "align",
          "alignHorizontal",
          "alignVertical",
          "weight",
        )
    }
    .build()

private fun componentIconButton(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.IconButtonKt.IconButton"
      canvas = "wear-m3/icon-button"
      traits = listOf("Action", "ListItem")
      propertyCapabilities =
        listOf(
          property(
            "variant",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("filled"),
                JsonPrimitive("filled-tonal"),
                JsonPrimitive("filled-variant"),
                JsonPrimitive("outlined"),
                JsonPrimitive("standard"),
              ),
            notes =
              "Which of the five `IconButton` overloads is written: `FilledIconButton`, `FilledTonalIconButton`, `FilledVariantIconButton`, `OutlinedIconButton` or plain `IconButton`. A variant selects the composable rather than tinting one, the way `m3/button`'s style does.",
          ),
          property(
            "containerColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
          property(
            "contentColor",
            JsonValueTypeV1.STRING,
            notes =
              "A colour, written as {\"type\":\"color\",\"value\":\"#RRGGBB\"} or as {\"type\":\"colorToken\",\"value\":\"primary\"} naming one of statusSemantics.colorTokens. A string wrapper, or a role the canvas does not draw, is rejected rather than guessed at.",
          ),
        )
      slotCapabilities =
        listOf(
          slot(
            "content",
            min = 1,
            max = 1,
            ordered = true,
            roles = listOf(),
            traits = listOf("Adornment"),
          )
        )
      modifierCapabilities = listOf("weight", "size", "width", "padding", "testTag")
    }
    .build()

private fun componentTextButton(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.TextButtonKt.TextButton"
      canvas = "wear-m3/text-button"
      traits = listOf("Action", "ListItem")
      propertyCapabilities =
        listOf(
          property(
            "variant",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("filled"),
                JsonPrimitive("filled-tonal"),
                JsonPrimitive("filled-variant"),
                JsonPrimitive("outlined"),
                JsonPrimitive("standard"),
              ),
            notes =
              "As `wear-m3/icon-button`'s: the variant names the composable — `FilledTextButton` and the rest — rather than recolouring one.",
          )
        )
      slotCapabilities =
        listOf(
          slot(
            "content",
            min = 1,
            max = 1,
            ordered = true,
            roles = listOf(),
            traits = listOf("AnyContent"),
          )
        )
      modifierCapabilities = listOf("weight", "size", "width", "padding", "testTag")
    }
    .build()

private fun componentCheckboxButton(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.CheckboxButtonKt.CheckboxButton"
      canvas = "wear-m3/checkbox-button"
      traits = listOf("ListItem", "WearListContent", "Selection")
      propertyCapabilities =
        listOf(
          property(
            "label",
            JsonValueTypeV1.STRING,
            required = true,
            notes =
              "The row's primary label. Wear's selection controls are labelled rows, not bare boxes.",
          ),
          property(
            "secondaryLabel",
            JsonValueTypeV1.STRING,
            notes =
              "The second line, where there is one. Empty emits no `secondaryLabel` argument.",
          ),
          property(
            "checked",
            JsonValueTypeV1.BOOLEAN,
            JsonValueTypeV1.OBJECT,
            notes = "Whether the box is ticked. Bindable to a state variable.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentSwitchButton(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.SwitchButtonKt.SwitchButton"
      canvas = "wear-m3/switch-button"
      traits = listOf("ListItem", "WearListContent", "Selection")
      propertyCapabilities =
        listOf(
          property(
            "label",
            JsonValueTypeV1.STRING,
            required = true,
            notes =
              "The row's primary label. Wear's selection controls are labelled rows, not bare boxes.",
          ),
          property(
            "secondaryLabel",
            JsonValueTypeV1.STRING,
            notes =
              "The second line, where there is one. Empty emits no `secondaryLabel` argument.",
          ),
          property(
            "checked",
            JsonValueTypeV1.BOOLEAN,
            JsonValueTypeV1.OBJECT,
            notes = "Whether the switch is on. Bindable to a state variable.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentRadioButton(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.RadioButtonKt.RadioButton"
      canvas = "wear-m3/radio-button"
      traits = listOf("ListItem", "WearListContent", "Selection")
      propertyCapabilities =
        listOf(
          property(
            "label",
            JsonValueTypeV1.STRING,
            required = true,
            notes =
              "The row's primary label. Wear's selection controls are labelled rows, not bare boxes.",
          ),
          property(
            "secondaryLabel",
            JsonValueTypeV1.STRING,
            notes =
              "The second line, where there is one. Empty emits no `secondaryLabel` argument.",
          ),
          property(
            "selected",
            JsonValueTypeV1.BOOLEAN,
            JsonValueTypeV1.OBJECT,
            notes = "Whether this row is the chosen one of its group.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentSlider(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.SliderKt.Slider"
      canvas = "wear-m3/slider"
      traits = listOf("ListItem", "WearListContent")
      propertyCapabilities =
        listOf(
          property(
            "value",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "The current value, between `valueFrom` and `valueTo`.",
          ),
          property(
            "valueFrom",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "The low end of the range. 0 when absent.",
          ),
          property(
            "valueTo",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "The high end of the range. 1 when absent.",
          ),
          property(
            "steps",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "How many discrete stops sit between the ends. 0 is continuous.",
          ),
          property(
            "segmented",
            JsonValueTypeV1.STRING,
            allowedValues = listOf(JsonPrimitive("segmented"), JsonPrimitive("continuous")),
            notes =
              "Whether the track is drawn as separated segments, which is what Wear's stepped slider looks like.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentStepper(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.StepperKt.Stepper"
      canvas = "wear-m3/stepper"
      traits = listOf("ScreenContent")
      propertyCapabilities =
        listOf(
          property(
            "value",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "The current value.",
          ),
          property(
            "valueFrom",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "The low end of the range. 0 when absent.",
          ),
          property(
            "valueTo",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "The high end of the range. 1 when absent.",
          ),
          property(
            "steps",
            JsonValueTypeV1.NUMBER,
            JsonValueTypeV1.OBJECT,
            notes = "How many discrete stops sit between the ends. 0 is continuous.",
          ),
        )
      slotCapabilities =
        listOf(
          slot(
            "content",
            min = 1,
            max = 1,
            ordered = true,
            roles = listOf(),
            traits = listOf("AnyContent"),
          )
        )
    }
    .build()

private fun componentButtonGroup(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.ButtonGroupKt.ButtonGroup"
      canvas = "wear-m3/button-group"
      traits = listOf("ListItem", "WearListContent")
      propertyCapabilities = listOf()
      slotCapabilities =
        listOf(
          slot(
            "children",
            min = 0,
            max = null,
            ordered = true,
            roles = listOf(),
            traits = listOf("Action"),
          )
        )
    }
    .build()

private fun componentDatePicker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.DatePickerKt.DatePicker"
      canvas = "wear-m3/date-picker"
      traits = listOf("ScreenContent")
      propertyCapabilities =
        listOf(
          property(
            "initialDate",
            JsonValueTypeV1.STRING,
            notes = "ISO-8601 `yyyy-MM-dd`. Empty picks upstream's own initial date.",
          ),
          property(
            "type",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("year-month-day"),
                JsonPrimitive("day-month-year"),
                JsonPrimitive("month-day-year"),
              ),
            notes = "`DatePickerType`, which is field order rather than formatting.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentTimePicker(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.TimePickerKt.TimePicker"
      canvas = "wear-m3/time-picker"
      traits = listOf("ScreenContent")
      propertyCapabilities =
        listOf(
          property(
            "initialTime",
            JsonValueTypeV1.STRING,
            notes = "ISO-8601 `HH:mm[:ss]`. Empty picks upstream's own initial time.",
          ),
          property(
            "type",
            JsonValueTypeV1.STRING,
            allowedValues =
              listOf(
                JsonPrimitive("hours-minutes-seconds"),
                JsonPrimitive("hours-minutes-am-pm"),
                JsonPrimitive("hours-minutes-24h"),
              ),
            notes = "`TimePickerType`, which decides both the columns and the clock.",
          ),
        )
      slotCapabilities = listOf()
    }
    .build()

private fun componentAlertDialogEdgeButton(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.AlertDialogDefaults.EdgeButton"
      group = "Dialogs"
    }
    .build()

private fun componentOpenOnPhoneDialogIcon(): UiBuilderAuthoredComponent =
  UiBuilderAuthoredComponent.Builder()
    .apply {
      record = "catalog/androidx.wear.compose.material3.OpenOnPhoneDialogDefaults.Icon"
      group = "Dialogs"
    }
    .build()

private fun property(
  name: String,
  vararg types: JsonValueTypeV1,
  required: Boolean = false,
  allowedValues: List<JsonElement> = emptyList(),
  notes: String? = null,
): JsonElement =
  Json.encodeToJsonElement(
    PropertyCapabilityV1.serializer(),
    PropertyCapabilityV1.Builder(
        name,
        if (types.size == 1) JsonPrimitive(types.single().name.lowercase())
        else JsonArray(types.map { JsonPrimitive(it.name.lowercase()) }),
      )
      .apply {
        this.required = required
        this.allowedValues = allowedValues
        this.notes = notes
      }
      .build(),
  )

private fun slot(
  name: String,
  min: Int,
  max: Int?,
  ordered: Boolean,
  roles: List<String>,
  traits: List<String>,
): JsonElement =
  Json.encodeToJsonElement(
    SlotCapabilityV1.serializer(),
    SlotCapabilityV1.Builder(
        name,
        SlotCardinalityV1.Builder()
          .apply {
            this.min = min
            this.max = max
          }
          .build(),
        ordered,
      )
      .apply {
        acceptedRoles = roles
        acceptedTraits = traits
      }
      .build(),
  )

/**
 * What a design may re-skin, for the UI builder's Theme panel (`statusSemantics.designTokens`): the
 * nine theme colours a screen re-skins most, on the screen scaffold that hosts the theme, and the
 * shared spacing, icon size and button and card colours on every node of the components they name.
 * The same fifteen compose-ui-builder's Kotlin wear-m3 catalog offered (`wearDesignTokens`), so
 * owning the catalog keeps its Theme panel. Every default is null: unset keeps Wear's own value.
 */
private fun policyDesignTokens(): JsonElement {
  val scaffold = "wear-m3/screen-scaffold"
  fun binding(component: String, property: String) = buildJsonObject {
    put("component", JsonPrimitive(component))
    put("property", JsonPrimitive(property))
  }
  fun theme(id: String, label: String, property: String) = buildJsonObject {
    put("id", JsonPrimitive(id))
    put("label", JsonPrimitive(label))
    put("kind", JsonPrimitive("color"))
    put("default", JsonNull)
    put("theme", JsonArray(listOf(binding(scaffold, property))))
  }
  fun components(
    id: String,
    label: String,
    kind: String,
    targets: List<Pair<String, String>>,
    range: Pair<Int, Int>? = null,
  ) = buildJsonObject {
    put("id", JsonPrimitive(id))
    put("label", JsonPrimitive(label))
    put("kind", JsonPrimitive(kind))
    put("default", JsonNull)
    range?.let { (minimum, maximum) ->
      put("minimum", JsonPrimitive(minimum))
      put("maximum", JsonPrimitive(maximum))
    }
    put(
      "components",
      JsonArray(targets.map { (component, property) -> binding(component, property) }),
    )
  }
  val buttons = listOf("wear-m3/button", "wear-m3/icon-button", "wear-m3/edge-button")
  return buildJsonObject {
    put(
      "tokens",
      JsonArray(
        listOf(
          theme("color.primary", "Primary", "themePrimaryColor"),
          theme("color.onPrimary", "On primary", "themeOnPrimaryColor"),
          theme("color.primaryContainer", "Primary container", "themePrimaryContainerColor"),
          theme("color.secondary", "Secondary", "themeSecondaryColor"),
          theme("color.tertiary", "Tertiary", "themeTertiaryColor"),
          theme("color.surfaceContainer", "Surface container", "themeSurfaceContainerColor"),
          theme("color.onSurface", "On surface", "themeOnSurfaceColor"),
          theme("color.background", "Background", "themeBackgroundColor"),
          theme("color.error", "Error", "themeErrorColor"),
          components(
            "space.list",
            "List spacing",
            "number",
            listOf(
              "wear-m3/transforming-lazy-column" to "verticalSpacingDp",
              "layout/column" to "verticalSpacingDp",
            ),
            range = 0 to 24,
          ),
          components(
            "space.row",
            "Row spacing",
            "number",
            listOf("layout/row" to "horizontalSpacingDp"),
            range = 0 to 24,
          ),
          components(
            "size.icon",
            "Icon size",
            "number",
            listOf("wear-m3/icon" to "sizeDp"),
            12 to 48,
          ),
          components(
            "color.buttonContainer",
            "Button container",
            "color",
            buttons.map { it to "containerColor" },
          ),
          components(
            "color.buttonContent",
            "Button content",
            "color",
            buttons.map { it to "contentColor" },
          ),
          components(
            "color.cardContainer",
            "Card container",
            "color",
            listOf("wear-m3/card" to "containerColor"),
          ),
        )
      ),
    )
  }
}
