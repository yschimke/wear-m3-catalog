@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.Action
import androidx.compose.remote.creation.compose.action.hostAction
import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.shaders.RemoteBrush
import androidx.compose.remote.creation.compose.shaders.linearGradient
import androidx.compose.remote.creation.compose.shaders.solidColor
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.animateRemoteFloat
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteFloat
import androidx.compose.remote.creation.compose.state.rememberMutableRemoteInt
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.ri
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.remote.creation.compose.state.selectIfGt
import androidx.compose.remote.creation.compose.state.tween
import androidx.compose.remote.creation.compose.text.RemoteFontFamily
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.remote.material3.RemoteAppCard
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteButtonDefaults
import androidx.wear.compose.remote.material3.RemoteButtonGroup
import androidx.wear.compose.remote.material3.RemoteCard
import androidx.wear.compose.remote.material3.RemoteCircularProgressIndicator
import androidx.wear.compose.remote.material3.RemoteCompactButton
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteIconButton
import androidx.wear.compose.remote.material3.RemoteIconButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteOutlinedCard
import androidx.wear.compose.remote.material3.RemoteText
import androidx.wear.compose.remote.material3.RemoteTextButton
import androidx.wear.compose.remote.material3.RemoteTextButtonDefaults
import androidx.wear.compose.remote.material3.RemoteTitleCard
import androidx.wear.compose.remote.material3.buttonSizeModifier
import ee.schimke.composeai.daemon.rememberOverridableRemoteColor
import ee.schimke.composeai.daemon.rememberOverridableRemoteDp
import ee.schimke.composeai.daemon.rememberOverridableRemoteFloat
import ee.schimke.composeai.daemon.rememberOverridableRemoteString
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.OverrideVariant

// ---------------------------------------------------------------------------
// Remote Compose design-catalog sticker sheet.
//
// Each `@CatalogRemoteModes` / `@CatalogRemoteLarge`-annotated function is one
// component sticker: the remote content wrapped in the module's `RemoteSticker`
// frame (RemotePreview → RemoteDocument → player raster). The function names below
// are the stable keys `catalog.spec.json` joins on.
//
// The set mirrors the **Wear Compose Remote Material 3**
// (`androidx.wear.compose.remote.material3`) component surface — the port of Wear
// Compose Material 3 to Remote Compose — so every sticker here has a Wear M3
// parallel (declared per-component in `catalog.spec.json`'s `parallel` field, and
// surfaced side-by-side by the branch's cross-system compare page). The two
// non-component helpers are omitted: `RemoteContainerPainter` (a painter factory,
// not a drawable component) and `RemoteTypographyTokens` (the raw token table).
// `RemoteTimeText` has source samples in ComponentVariantPreviews.kt but no sticker:
// the resolved player still rejects its DrawTextOnCircle opcode (57). The
// `remote-creation-compose` shader sticker is the one Remote-only extra.
// ---------------------------------------------------------------------------

// `hostAction(...)` — the Remote Compose way to signal the *host*: it posts a payload out of the
// document and leaves the rendered document untouched. It used to be the `onClick` of every button
// on this sheet, which is exactly why a click in the preview player never did anything visible —
// nothing in the document changed, so there was nothing to repaint. Every clickable sticker now
// uses [countedRemote] / [toggledRemote], which mutate document state so the player resolves the
// click itself. Kept here, unused by the catalog, as the documented counterpart: reach for it when
// a component genuinely means "tell the host", not "change me".
@Suppress("unused") private val hostSignalAction = hostAction("catalogAction".rs, 1.rf)

/**
 * A click counter that lives **inside the RemoteDocument** — Remote Compose's answer to `remember {
 * mutableStateOf(0) }`.
 *
 * Returns the label to draw and the [Action] to hand a component's `onClick`. The action is a
 * [valueChange] that writes `clicks + 1` back into a [rememberMutableRemoteInt], so the player
 * re-evaluates the label expression and repaints on its own — no host round-trip, which is exactly
 * what `hostAction` could not do.
 *
 * The label is a document-level conditional: `clicks > 0` picks `"<base> (n)"`, otherwise plain
 * [base]. A freshly-built document has `clicks == 0`, so the **baked catalog capture renders the
 * bare label it always has** — the counter is only reachable once a player dispatches a real touch.
 */
@Composable
internal fun countedRemote(base: String): Pair<RemoteString, Action> = countedRemote(base.rs)

/**
 * [countedRemote] over a label that is itself a remote value — an overridable named string, say —
 * so a sticker whose label is driven from outside still gets the default click tally rather than a
 * bespoke affordance. The counter is appended to whatever the binding resolves to, so the override
 * it demonstrates stays fully visible.
 */
@Composable
internal fun countedRemote(base: RemoteString): Pair<RemoteString, Action> {
  val clicks = rememberMutableRemoteInt(0)
  val label = selectIfGt(clicks, 0.ri, base + " (" + clicks.toRemoteString() + ")", base)
  return label to valueChange(clicks, (clicks + 1).createReference())
}

/**
 * The size-preserving affordance, for a sticker whose label has no room to grow. Returns a 0→1
 * [RemoteFloat] and the [Action] that flips it, so the caller can [tween] a colour across it.
 *
 * Two kinds of sticker need it. The icon button has no label at all to count into. The **round**
 * text buttons have one that is already the width of their circle: the kit sizes that container
 * with `MMM`, a run of its widest glyph, and [countedRemote] would grow that to `MMM (1)` on the
 * first tap — drawing the tally straight through the edge, which is the very thing quoting the kit
 * fixed in the resting state. A colour tween says "that tap landed" without touching the metrics.
 *
 * Everything with a pill or a card to grow into takes the default [countedRemote] tally.
 *
 * At rest the float is `0f`, and `tween(a, b, 0f)` is `a` — so the baked capture keeps the stock
 * colours and only a live tap moves it.
 */
@Composable
internal fun toggledRemote(): Pair<RemoteFloat, Action> {
  val on = rememberMutableRemoteFloat(0f)
  return animateRemoteFloat(on, duration = 0.45f) to valueChange(on, (1f.rf - on).createReference())
}

// The leading glyph every icon slot on this sheet draws.
//
// It is `Icons.Filled.Add` — the SAME glyph the kit's `Icon=Yes` cells carry and the same one
// `wear-m3-catalog` passes to every slot (`Icon(Icons.Filled.Add, …)`). Remote Compose has no
// bundled icon set and `RemoteIcon` takes an `ImageVector`, so the path is transcribed here
// (`M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z`, the material `add` path on a 24dp viewport) rather than
// depending on `material-icons`. It used to be a hand-built five-point star, which put a different
// glyph in every icon slot from the cell it is compared against — a difference reported on ten
// rows that said nothing about Remote Compose. `RemoteIcon` re-tints it, so the path fill here is
// a placeholder.
internal val addIcon: ImageVector =
  ImageVector.Builder(
      name = "Add",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    )
    .apply {
      path(fill = SolidColor(Color.White)) {
        moveTo(19f, 13f)
        lineTo(13f, 13f)
        lineTo(13f, 19f)
        lineTo(11f, 19f)
        lineTo(11f, 13f)
        lineTo(5f, 13f)
        lineTo(5f, 11f)
        lineTo(11f, 11f)
        lineTo(11f, 5f)
        lineTo(13f, 5f)
        lineTo(13f, 11f)
        lineTo(19f, 11f)
        close()
      }
    }
    .build()

// ---------------------------------------------------------------------------
// Buttons — the Remote Material 3 button emphasis family plus the border / shape /
// named-value variants. Parallels of the Wear M3 button family.
// ---------------------------------------------------------------------------

/**
 * The three `Button` cells this sheet used to publish as three top-level components —
 * `Button/Disabled`, `Button/IconLabel` and `Button/IconLabel-Large` — folded onto the style they
 * are variants OF ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)).
 *
 * The names are the Wear sibling's (`disabled`, `icon`, `icon-large`), because the compare page
 * reads the two columns cell by cell as well as component by component: a cell called
 * `icon-default` here beside `icon` there pairs with nothing, and the row silently halves.
 *
 * Every cell that turns the icon on declares its WHOLE kit vector, because the kit's axes are
 * coupled. There is no `Icon=Yes, Icon size=n/a` node and no `Icon=Yes, Alignment=Center` one — a
 * leading icon is what gives the row the height for the second label, so the kit draws every
 * `Icon=Yes` cell left-aligned and two-line. A cell naming only `Icon=Yes` asks for a node between
 * the ones the kit drew. `disabled` turns exactly one knob and takes `kitAxis` / `kitValue`.
 *
 * TWO icon sizes, not the kit's three. `RemoteButtonDefaults` publishes `IconSize` and
 * `LargeIconSize` and stops there — there is no extra-large on the alpha `remote-material3` surface
 * — so the kit's `Icon size=xLg 36` column has no argument to reach it with. That is a library gap
 * stated here rather than a fourth cell mapped onto a node this cannot draw.
 */
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "icon",
  booleans = ["icon=true"],
  kitProps = ["Icon=Yes", "Icon size=26 (Default)", "Alignment=Left"],
)
@OverrideVariant(
  name = "icon-large",
  booleans = ["icon=true"],
  strings = ["iconSize=large"],
  kitProps = ["Icon=Yes", "Icon size=Lrg 32", "Alignment=Left"],
)
annotation class RemoteButtonKitCells

@CatalogComponent(
  id = "Button/Filled",
  group = "Buttons",
  parallel = "Button/Filled",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93092",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption = "Remote Material 3 filled button — the primary action.",
)
@CatalogRemoteModes
@RemoteButtonKitCells
@Composable
fun FilledRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  val enabled = previewOverrideBoolean("enabled", true).rb
  if (!previewOverrideBoolean("icon", false)) {
    // The kit's `Icon=No` column, and the base render. `buttonSizeModifier()` is what makes a
    // label-only button measure like the kit's one-line cell.
    RemoteButton(
      onClick = onClick,
      modifier = RemoteModifier.buttonSizeModifier(),
      enabled = enabled,
      content = { RemoteText(label) },
    )
  } else {
    // The kit's `Icon=Yes` column: a leading icon, and the SECOND label the kit couples to it.
    // Every `Icon=Yes` cell in the `Button` set draws `Primary label` over `Secondary label` —
    // the icon is what gives the row the height for a second line — so there is no single-line
    // icon cell to map an icon-without-subtitle render onto (#116, and the same finding as the
    // selection rows in #112). No `buttonSizeModifier()` here: a two-line button sizes itself
    // from its slots, and pinning it to the one-line height clips the second row off.
    RemoteButton(
      onClick = onClick,
      enabled = enabled,
      icon = {
        RemoteIcon(
          addIcon,
          contentDescription = null,
          modifier =
            RemoteModifier.size(
              when (previewOverrideChoice("iconSize", "default", listOf("default", "large"))) {
                "large" -> RemoteButtonDefaults.LargeIconSize
                else -> RemoteButtonDefaults.IconSize
              }
            ),
        )
      },
      secondaryLabel = { RemoteText(KitCopy.SECONDARY_LABEL.rs) },
      label = { RemoteText(label) },
    )
  }
}

// Remote Material 3's outlined-emphasis button. Remote Compose alpha06 has no
// separate `RemoteOutlinedButton` (it ships `RemoteOutlinedCard`, but not the
// button), so we build it the same way Wear's own `OutlinedButton` does under the
// hood: a `RemoteButton` with a **transparent container** + a border. Overriding
// `containerColor` is the key — the default `buttonColors()` is `primary`-filled,
// so a bare `RemoteButton` + border would render as a *filled* button with an
// outline, not an outlined one. Every other colour is pulled straight from the
// theme (`buttonColors()` leaves un-passed colours at their exact defaults) rather
// than re-encoded here: the content is `onSurface` and the border is the theme's
// `outline` token — the same tokens Wear's `outlinedButtonColors()` uses — so the
// two systems' outlined buttons stay in lockstep with the theme. Wear M3 parallel:
// `OutlinedButton` (`Button/Outlined`).
@CatalogComponent(
  id = "Button/Outlined",
  group = "Buttons",
  parallel = "Button/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93116",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
  caption =
    "Remote Material 3 outlined-emphasis button (RemoteButton with an explicit border + border " +
      "colour).",
)
@CatalogRemoteModes
@Composable
fun OutlinedRemoteButton() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  RemoteButton(
    onClick = onClick,
    modifier = RemoteModifier.buttonSizeModifier(),
    colors =
      RemoteButtonDefaults.buttonColors(
        containerColor = RemoteColor(Color.Transparent),
        contentColor = RemoteMaterialTheme.colorScheme.onSurface,
      ),
    border = 2.rdp,
    borderColor = RemoteMaterialTheme.colorScheme.outline,
    content = { RemoteText(label) },
  )
}

@CatalogComponent(
  id = "Button/CustomShape",
  group = "Buttons",
  parallel = "Button/Filled",
  noReference =
    "The kit's `Button` set has no shape axis: its five styles are crossed with `Icon`, " +
      "`Icon size`, `Alignment` and `Disabled` and nothing else, so every one of its fifty cells " +
      "is the stock pill. A corner override is a Remote Compose capability the kit does not " +
      "publish a picture of, and mapping it onto the pill cell would report the rounded corners " +
      "as a divergence on every render.",
  caption = "Filled button with a RemoteRoundedCornerShape override.",
)
@CatalogRemoteModes
@Composable
fun CustomShapeRemoteButton() = RemoteSticker {
  // Same label as its `Button/Filled` parallel — only the corner shape differs, so the
  // cross-system comparison isolates that one attribute.
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  RemoteButton(
    onClick = onClick,
    modifier = RemoteModifier.buttonSizeModifier(),
    shape = RemoteRoundedCornerShape(4.rdp),
    content = { RemoteText(label) },
  )
}

/**
 * Reads its label from a Remote Compose named-value binding. The default render shows
 * [KitCopy.PRIMARY_LABEL] — the same label as its `Button/Filled` parallel, so the static capture
 * lines up apples-to-apples; the connector's override path
 * (`renderNow.overrides.remoteCompose.namedValues = {"label": …}`) flips the label live without
 * rebuilding the document — the interactive story the `:data-remotecompose-connector` demonstrates.
 */
@CatalogComponent(
  id = "Button/NamedLabel",
  group = "Buttons",
  parallel = "Button/Filled",
  noReference =
    "Not a variant cell at all: what this sticker varies is where the label comes from, not what " +
      "the button looks like. Its default render is `Button/Filled`'s picture, and that row " +
      "already names the kit cell — a second component pointing at the same node would score one " +
      "picture twice and say nothing about the binding, which is the only thing this row is for.",
  caption =
    "Filled button whose label is bound to a Remote Compose named value — the connector flips " +
      "it live (the default render shows the kit's own label).",
)
@CatalogRemoteModes
@Composable
fun NamedLabelRemoteButton() = RemoteSticker {
  // The counter composes over the override rather than replacing it: `countedRemote` takes the
  // bound `RemoteString` itself, so the label still resolves from the named value and only picks up
  // a `(n)` suffix once tapped. Both the override path and the click stay demonstrable.
  val (label, onClick) =
    countedRemote(rememberOverridableRemoteString("label", KitCopy.PRIMARY_LABEL))
  RemoteButton(
    onClick = onClick,
    modifier = RemoteModifier.buttonSizeModifier(),
    content = { RemoteText(label) },
  )
}

/**
 * **The kit's `Text-Button` style and size axes, as cells** — the four this sheet used to publish
 * as `Button/Text-Small`, `-Large`, `-Child` and `-Outlined`
 * ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)).
 *
 * They fold for the reason AGENTS.md gives for folding them on the Wear side: the test is the CALL
 * SITE, not the word. `Style=` on this set is not a choice between functions — `remote-material3`
 * ships one `RemoteTextButton` and it takes its emphasis as `colors` — so there is no second
 * function for a reader to pick, and nothing to split. `Size=` is a modifier argument on the same
 * one. The cell names are the Wear sibling's, so the compare page pairs them.
 *
 * Two of the kit's five styles, not five. `RemoteTextButtonDefaults` publishes exactly two colour
 * recipes (the filled base and the container-less child); `Filled-Variant` and `Tonal` have no
 * tokens on the alpha surface and are not written out here by hand, because a style transcribed
 * from the Wear library's resolved colours is a picture of this file rather than of
 * `remote-material3`. The outlined cell is the one exception and it is not a colour recipe: the
 * border is its own parameter, which is what the child colours plus `border` amount to.
 */
@OverrideVariant(
  name = "small",
  strings = ["size=small"],
  kitAxis = "Size",
  kitValue = "Small",
)
@OverrideVariant(
  name = "large",
  strings = ["size=large"],
  kitAxis = "Size",
  kitValue = "Large",
)
@OverrideVariant(
  name = "child",
  strings = ["style=child"],
  kitAxis = "Style",
  kitValue = "Child (No background)",
)
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitAxis = "Style",
  kitValue = "Outline",
)
annotation class RemoteTextButtonKitCells

// A low-emphasis round text button (`RemoteTextButton`), the Remote parallel of Wear
// M3's `TextButton`, with the kit's `Style` and `Size` axes folded in as cells.
@CatalogComponent(
  id = "Button/Text",
  group = "Buttons",
  parallel = "TextButton",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103081",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:103080",
  caption =
    "Low-emphasis round text button (RemoteTextButton), with the kit's style and size " +
      "axes folded in.",
)
@CatalogRemoteModes
@RemoteTextButtonKitCells
@Composable
fun TextRemoteButton() = RemoteSticker {
  // The kit's glyph run, not `PRIMARY_LABEL`: this container is a circle, and a two-word label is
  // drawn straight through its edge. `wear-m3-catalog`'s `TextButton` quotes the same constant.
  // `toggledRemote` rather than `countedRemote` for that same reason — see its KDoc.
  val (on, onClick) = toggledRemote()
  // Read once, used for the colours and the border below.
  val style = previewOverrideChoice("style", "filled", listOf("filled", "child", "outlined"))
  val stock = RemoteTextButtonDefaults.textButtonColors()
  // FILLED is the base, because the kit's `Text-Button` base cell is filled and
  // `wear-m3-catalog`'s `TextButton` is `filledTextButtonColors()` for that reason ("filled IS the
  // base render", as it puts it). `RemoteTextButtonDefaults.textButtonColors()` is the CHILD style
  // — no container at all — which is why taking it as the base once drew the kit's lowest-emphasis
  // cell under the base name.
  //
  // The content travels with the container on the two container-less styles: `primary` is a LIGHT
  // fill in the dark-first scheme, so leaving the label at the stock near-white `onSurface` would
  // land light text on a light container at the end of the tween. At rest `on` is 0f and
  // `tween(a, b, 0f)` is `a`, so every baked capture keeps its stock colours.
  val colors =
    if (style == "filled") {
      RemoteTextButtonDefaults.textButtonColors(
        containerColor =
          tween(
            RemoteMaterialTheme.colorScheme.primary,
            RemoteMaterialTheme.colorScheme.primaryDim,
            on,
          ),
        contentColor = RemoteMaterialTheme.colorScheme.onPrimary,
      )
    } else {
      RemoteTextButtonDefaults.textButtonColors(
        containerColor = tween(stock.containerColor, RemoteMaterialTheme.colorScheme.primary, on),
        contentColor = tween(stock.contentColor, RemoteMaterialTheme.colorScheme.onPrimary, on),
      )
    }
  // The two size cells take the same filled container the base does — the kit draws its size cells
  // on its base style, and the style cells are the ones that change the container.
  val size = previewOverrideChoice("size", "default", listOf("default", "small", "large"))
  RemoteTextButton(
    onClick = onClick,
    modifier =
      when (size) {
        "small" -> RemoteModifier.size(RemoteTextButtonDefaults.SmallButtonSize)
        "large" -> RemoteModifier.size(RemoteTextButtonDefaults.LargeButtonSize)
        else -> RemoteModifier
      },
    colors = colors,
    // The border is its OWN parameter, not part of `colors`: an outlined text button built from
    // the container-less colours alone draws no outline and is pixel-identical to the child cell.
    border = if (style == "outlined") 2.rdp else null,
    borderColor = if (style == "outlined") RemoteMaterialTheme.colorScheme.outline else null,
    content = {
      RemoteText(
        KitCopy.GLYPHS.rs,
        style =
          when (size) {
            "small" -> RemoteTextButtonDefaults.smallButtonTextStyle
            "large" -> RemoteTextButtonDefaults.largeButtonTextStyle
            else -> RemoteTextButtonDefaults.defaultButtonTextStyle
          },
      )
    },
  )
}

/**
 * **The kit's `Icon-Button` `Size` axis, as cells** — two of the three this sheet used to publish
 * as `Button/Icon-ExtraSmall`, `-Small` and `-Large`
 * ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)).
 *
 * Size is an argument to the one `RemoteIconButton`, so it folds; `Style` is the axis that stays
 * split, because `Button/Icon-Filled` and `Button/Icon-Outlined` pair with `IconButton/Filled` and
 * `IconButton/Outlined` on the Wear column, where they are separate functions. This component IS
 * the kit's child style — no container at all — and its cells vary only the size, so they take the
 * stock colours the base sticker takes.
 *
 * **NO `extra-small` cell, and the render is why.** `Button/Icon-ExtraSmall` named the kit's
 * `Style=Child (No background), Size=Extra-Small` cell (`34732:103021`) and its capture is
 * BYTE-IDENTICAL to the small one: `iconSizeFor` resolves `ExtraSmallButtonSize` and
 * `SmallButtonSize` to the same glyph, and with no container there is nothing else in the picture
 * to tell them apart. So that row was publishing one render twice and scoring it against two
 * different kit nodes — a mapping that cannot fail, which AGENTS.md rates worse than no mapping.
 * Withdrawn here rather than carried across the fold.
 *
 * The Wear sibling's `IconButton/Standard` reaches the same place by the same road and says so at
 * length: the size is a container token, the child style draws no container, and
 * `CatalogRenderTest` caught the duplicate there. It is the one gap of the five styles, on both
 * sheets, and the kit's five `Size=Extra-Small` cells are covered by the four styles that do draw a
 * container.
 */
@OverrideVariant(
  name = "small",
  strings = ["size=small"],
  kitAxis = "Size",
  kitValue = "Small",
)
@OverrideVariant(
  name = "large",
  strings = ["size=large"],
  kitAxis = "Size",
  kitValue = "Large",
)
annotation class RemoteIconButtonKitCells

// A round icon button (`RemoteIconButton`) carrying a single `RemoteIcon`. Inside the
// button the icon inherits the button's (contrasting) content colour, so no explicit
// tint is needed. Wear M3 parallel: `IconButton/Standard`.
@CatalogComponent(
  id = "Button/Icon",
  group = "Buttons",
  parallel = "IconButton/Standard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/34732:103015",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/34732:102972",
  caption = "Round icon button (RemoteIconButton), with the kit's size axis folded in.",
)
@CatalogRemoteModes
@RemoteIconButtonKitCells
@Composable
fun IconRemoteButton() = RemoteSticker {
  // No label to count into, so this one reads as a favourite toggle instead: the container colour
  // tweens to the theme's primary across the in-document flag. At rest the flag is 0f and
  // `tween(a, b, 0f)` is `a`, so the baked sticker keeps the stock icon-button colours.
  val (on, onClick) = toggledRemote()
  val stock = RemoteIconButtonDefaults.iconButtonColors()
  // `null` at the default, NOT `DefaultButtonSize`. The base sticker has always let the library
  // size itself, and pinning it to the token it would have picked anyway is a change to the baked
  // capture for no gain — the cells are what need an explicit size.
  val size =
    when (previewOverrideChoice("size", "default", listOf("default", "small", "large"))) {
      "small" -> RemoteIconButtonDefaults.SmallButtonSize
      "large" -> RemoteIconButtonDefaults.LargeButtonSize
      else -> null
    }
  RemoteIconButton(
    onClick = onClick,
    modifier = if (size == null) RemoteModifier else RemoteModifier.size(size),
    colors =
      RemoteIconButtonDefaults.iconButtonColors(
        containerColor = tween(stock.containerColor, RemoteMaterialTheme.colorScheme.primary, on)
      ),
    content = {
      if (size == null) RemoteIcon(addIcon, "Add".rs)
      else
        RemoteIcon(
          addIcon,
          "Add".rs,
          modifier = RemoteModifier.size(RemoteIconButtonDefaults.iconSizeFor(size)),
        )
    },
  )
}

/**
 * **The kit's `Button-Compact` content axis, as cells** — the two this sheet used to publish as
 * `Button/Compact-TextOnly` and `Button/Compact-IconOnly`
 * ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)).
 *
 * Both cells declare their WHOLE kit vector, because this set's axes are coupled three deep:
 * `Alignment`, `Icon` and `Text` are one choice the kit spells as three properties, so a cell
 * naming `Icon=No` alone asks for a node between the ones the kit drew. The Wear sibling's
 * `Button/Compact` cells carry the same vectors under the same two names.
 *
 * `Style=` stays off this component, unlike the Wear sibling's, and the reason is the same one the
 * text button gives: `RemoteButtonDefaults` publishes one colour recipe, and a tonal or outlined
 * compact button written out here by hand would be a picture of this file rather than of
 * `remote-material3`.
 */
@OverrideVariant(
  name = "icon-only",
  strings = ["content=icon"],
  kitProps = ["Style=Filled", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
)
@OverrideVariant(
  name = "text-only",
  strings = ["content=text"],
  kitProps = ["Style=Filled", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
)
annotation class RemoteCompactButtonKitCells

// The compact, single-line button (`RemoteCompactButton`) — Wear M3 parallel:
// `Button/Compact`.
@CatalogComponent(
  id = "Button/Compact",
  group = "Buttons",
  parallel = "Button/Compact",
  reference = "figma:B24oss2tTeXAFykyeyusz0/35276:87975",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35276:87971",
  caption =
    "Compact single-line button (RemoteCompactButton) with the kit's leading icon, and " +
      "its icon-only and text-only cells.",
)
@CatalogRemoteModes
@RemoteCompactButtonKitCells
@Composable
fun CompactRemoteButton() = RemoteSticker {
  // ICON AND LABEL is the base, because that is what the kit cell this row is scored against draws
  // (`Button-Compact`, `Icon=Yes`, `Text=Yes`) and what `wear-m3-catalog`'s `Button/Compact`
  // draws. It was label-only once, which is the kit's `Icon=No` cell — that cell is still here,
  // as the `text-only` cell, rather than standing in for the base one.
  val content =
    previewOverrideChoice("content", "icon-and-text", listOf("icon-and-text", "icon", "text"))
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  // The icon-only cell has no label to count into, so it reads as a toggle instead — the same
  // bargain `Button/Icon` strikes. At rest `on` is 0f and the baked capture is the stock filled
  // container.
  val (on, toggle) = toggledRemote()
  val stock = RemoteButtonDefaults.buttonColors()
  RemoteCompactButton(
    onClick = if (content == "icon") toggle else onClick,
    colors =
      if (content != "icon") RemoteButtonDefaults.buttonColors()
      else
        RemoteButtonDefaults.buttonColors(
          containerColor =
            tween(stock.containerColor, RemoteMaterialTheme.colorScheme.tertiaryContainer, on)
        ),
    icon =
      if (content == "text") null
      else
        ({
          RemoteIcon(
            addIcon,
            // The icon-only cell is the whole button, so it carries the description the label
            // carries on the other two.
            contentDescription = if (content == "icon") "Add".rs else null,
            modifier =
              RemoteModifier.size(
                // Its dedicated 52dp visible width gives the glyph more room than the leading
                // slot beside a label does.
                if (content == "icon") RemoteButtonDefaults.SmallIconSize
                else RemoteButtonDefaults.ExtraSmallIconSize
              ),
          )
        }),
    label = if (content == "icon") null else ({ RemoteText(label) }),
  )
}

// A pair of buttons laid out edge-to-edge by `RemoteButtonGroup`, each taking an equal
// share of the row via `weight`. Wear M3 parallel: `ButtonGroup`.
@CatalogComponent(
  id = "Button/Group",
  group = "Buttons",
  parallel = "ButtonGroup",
  noReference =
    "The kit publishes no `ButtonGroup` set — the Wear sibling carries its own counterpart " +
      "under `noReference` for the same reason, so there is no node to compare either rendition " +
      "against.",
  caption = "Two buttons laid out edge-to-edge by RemoteButtonGroup.",
)
@CatalogRemoteLarge
@Composable
fun ButtonGroupRemote() = RemoteSticker {
  // `A` / `B`, the same two labels `wear-m3-catalog`'s `ButtonGroup` draws (`'A' + index`). The kit
  // publishes no button-group set, so there is no kit copy to quote here — which makes the sibling
  // the only reference this row has, and a `Yes` / `No` pair beside its `A` / `B` was a difference
  // reported on the row that said nothing about either rendition. Same bargain `KitCopy` strikes
  // everywhere else, applied to the one slot the kit leaves unspoken.
  //
  // Each half counts independently, so a live tap tells you which one it landed on.
  val (first, onFirst) = countedRemote("A")
  val (second, onSecond) = countedRemote("B")
  RemoteButtonGroup {
    RemoteButton(onClick = onFirst, modifier = RemoteModifier.weight(1f.rf)) { RemoteText(first) }
    RemoteButton(onClick = onSecond, modifier = RemoteModifier.weight(1f.rf)) { RemoteText(second) }
  }
}

// ---------------------------------------------------------------------------
// Containment — the Remote Material 3 card family. Parallels of the Wear M3 cards.
// ---------------------------------------------------------------------------

@CatalogComponent(
  id = "Card",
  group = "Containment",
  parallel = "Card",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5747",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "Remote Material 3 card.",
)
@CatalogRemoteLarge
@Composable
fun CardRemote() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.CARD_CONTENT)
  RemoteCard(onClick = onClick, content = { RemoteText(label) })
}

@CatalogComponent(
  id = "Card/Outlined",
  group = "Containment",
  parallel = "Card/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39827:105691",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "Outlined card variant (RemoteOutlinedCard).",
)
@CatalogRemoteLarge
@Composable
fun OutlinedCardRemote() = RemoteSticker {
  // Same label as its `Card` parallel — only the outlined (vs filled) treatment differs. Unlike the
  // filled `RemoteCard` (whose surface carries a light content colour), the outlined card's
  // transparent container leaves the default content colour invisible on the sticker canvas, so pin
  // the label to the theme's `onSurface` token — the same token the outlined button uses.
  val (label, onClick) = countedRemote(KitCopy.CARD_CONTENT)
  RemoteOutlinedCard(
    onClick = onClick,
    content = { RemoteText(label, color = RemoteMaterialTheme.colorScheme.onSurface) },
  )
}

// `Title Card 2` — the kit's second layout, which adds a subtitle under the body. It used to be a
// top-level component (`TitleCard/WithSubtitle`); the kit models the layouts as one `Layout type`
// property on one `Card` set, and `RemoteTitleCard` reaches them by filling one more slot rather
// than by being a different function, so it is a cell
// ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)). The Wear sibling's `TitleCard`
// carries it under the same name.
//
// `Title Card 3` is the set's remaining gap in BOTH renditions and stays a stated absence rather
// than a third cell: it draws the timestamp under the subtitle, and neither `RemoteTitleCard` nor
// Wear's `TitleCard` has an argument that moves the time off the title's row. What Compose can
// arrange instead ships under its own name, `TitleCard/Subtitle`.
@OverrideVariant(
  name = "with-subtitle",
  strings = ["layout=title-time-subtitle"],
  kitProps = ["Layout type=Title Card 2", "Style=Tonal", "Content type=Text", "Interactive=Yes"],
)
@CatalogComponent(
  id = "TitleCard",
  group = "Containment",
  parallel = "TitleCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5747",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "Card led by a title, with the kit's numbered layouts folded in as cells.",
)
@CatalogRemoteLarge
@Composable
fun TitleCardRemote() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  // TITLE, TIME AND CONTENT — which is what `Title Card 1` draws. The note that used to sit here
  // said the cell this row names is "a title and nothing else"; its export is not. `38437:5747` is
  // a title, a timestamp at the top right, and a body, and `wear-m3-catalog`'s `TitleCard` fills
  // all three on the same node (#101). Drawing the title alone put a picture the set does not
  // publish under the set's base cell, and reported two empty slots as a divergence on every
  // render.
  //
  // The three layouts, and where each lives: `Title Card 1` is this. `Title Card 2` adds the
  // subtitle under the body — `TitleCard/WithSubtitle`. `Title Card 3` has no body at all, which
  // `RemoteTitleCard` cannot arrange; the closest thing Compose does have is a title over a
  // subtitle, and that ships under its own name rather than on a node it is not a picture of
  // (`TitleCard/Subtitle`).
  RemoteTitleCard(
    onClick = onClick,
    title = { RemoteText(title) },
    time = { RemoteText(KitCopy.TIMESTAMP.rs) },
    // `Title Card 2`'s subtitle sits UNDER the body, which is where `RemoteTitleCard` draws its
    // `subtitle` slot. The base cell leaves it empty.
    subtitle =
      if (
        previewOverrideChoice(
          "layout",
          "title-time",
          listOf("title-time", "title-time-subtitle"),
        ) == "title-time-subtitle"
      )
        ({ RemoteText(KitCopy.SUBTITLE.rs) })
      else null,
    content = { RemoteText(KitCopy.CARD_CONTENT.rs) },
  )
}

@CatalogComponent(
  id = "AppCard",
  group = "Containment",
  parallel = "AppCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5712",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "App card with app name, icon and content slots.",
)
@CatalogRemoteLarge
@Composable
fun AppCardRemote() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  RemoteAppCard(
    onClick = onClick,
    appName = { RemoteText(KitCopy.APP_LABEL.rs) },
    title = { RemoteText(title) },
    // The kit's App Card cell fills its timestamp slot, and so does `wear-m3-catalog`'s `AppCard`.
    // Leaving it empty here dropped the one thing that tells an app card from a title card at a
    // glance, on the row scored against that cell.
    time = { RemoteText(KitCopy.TIMESTAMP.rs) },
    appImage = { RemoteIcon(addIcon, null, modifier = RemoteModifier.size(16.rdp)) },
    content = { RemoteText(KitCopy.CARD_CONTENT.rs) },
  )
}

// ---------------------------------------------------------------------------
// Scaffold templates — a full-screen Remote Compose watch screen rather than a
// single component sticker: the whole reason the catalog exists is that a
// RemoteDocument drives a real surface (watch face / tile / widget), and one
// button on transparency doesn't show that. This is the catalog's declared hero
// (`display.hero` in catalog.spec.json), so it is what the preview server's front
// door features for `remote-m3`.
//
// Unlike every sticker above, the screen paints its own `background` fill: a
// screen IS a surface plus its content, so rasterising it onto transparency would
// defeat the point. The dark fill comes from `RemoteMaterialTheme`'s own
// background token, so the screen stays in lockstep with the (dark-first) scheme
// the rest of the sheet reads from.
//
// The status clock is a plain `RemoteText`, NOT `RemoteTimeText`: as the note by
// the text stickers records, curved text is a document op the bundled player
// can't replay yet, so a curved strip would fail the render outright. The time is
// frozen at "10:10" (the same literal the Wear M3 sibling's templates use) so the
// weekly design-artifacts render doesn't churn on the system clock.
//
// Wear M3 parallel: `Template/TimeText` — the base Wear list screen, which this
// mirrors slot for slot (status strip, list header, a stack of TitleCards).
// ---------------------------------------------------------------------------

// Kept to one short line each: at the 150dp list width a wrapping subtitle grows its card past the
// round crop, so the second card would fall off the bottom of the screen.
private val screenActivities = listOf("Morning run" to "5.2 km", "Heart rate" to "72 bpm")

@CatalogComponent(
  id = "Template/WatchScreen",
  group = "Scaffold templates",
  parallel = "Scaffold",
  noReference =
    "The kit publishes no `Scaffold` set — the Wear sibling carries its own counterpart under " +
      "`noReference` for the same reason, so there is no node to compare either rendition " +
      "against.",
  caption =
    "Full-screen Remote Compose watch screen — a status clock, a list header and a stack of " +
      "RemoteTitleCards on the theme's own background fill. The catalog's hero: a RemoteDocument " +
      "driving a whole surface, not a single sticker.",
)
@CatalogRemoteScreen
@Composable
fun WatchScreenRemote() = RemoteSticker {
  RemoteBox(
    // Clipped to a circle, not left square: the watch host crops the document to the round display,
    // so a square capture would advertise pixels the device never shows. `clip` before `background`
    // so the fill is what gets cropped.
    modifier =
      RemoteModifier.fillMaxSize()
        .clip(RemoteCircleShape)
        .background(RemoteBrush.solidColor(RemoteMaterialTheme.colorScheme.background)),
    contentAlignment = RemoteAlignment.Center,
    content = {
      // Narrower than the 227dp screen so the cards clear the round crop at their widest, the same
      // inset a Wear `ScreenScaffold` applies to its list content.
      RemoteColumn(
        modifier = RemoteModifier.width(150.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(8.rdp),
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
      ) {
        RemoteText("10:10".rs, style = RemoteMaterialTheme.typography.labelMedium)
        screenActivities.forEach { (rowTitle, subtitle) ->
          val (title, onClick) = countedRemote(rowTitle)
          RemoteTitleCard(
            onClick = onClick,
            title = { RemoteText(title) },
            subtitle = { RemoteText(subtitle.rs) },
          )
        }
      }
    },
  )
}

// ---------------------------------------------------------------------------
// Communication — the determinate circular progress indicator at a fixed 66%, so the
// static capture is deterministic (the indeterminate overload animates off the
// document clock). Wear M3 parallel: `CircularProgressIndicator` (`Progress/Circular`).
// ---------------------------------------------------------------------------

// The kit's `Disabled=Yes` cell, folded on
// ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116))
// — it used to be `Progress/Circular-Disabled`, a top-level component. `enabled` is an argument to
// the one `RemoteCircularProgressIndicator`, so it is a cell, and the Wear sibling's
// `CircularProgressIndicator` carries the same axis under the same name.
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@CatalogComponent(
  id = "Progress/Circular",
  group = "Communication",
  parallel = "CircularProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/41424:58637",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/41424:58385",
  caption = "Determinate circular progress rail against the display edge, at a fixed 60%.",
)
@CatalogRemoteDisplay
@Composable
fun CircularProgressRemote() = RemoteSticker {
  // The 0..1 fill is an editable `progress` float knob: the viewer's number field reseeds the arc
  // live (`rc.progress=float:<0..1>`) without re-capturing the document. 0.6 keeps the static
  // sticker deterministic AND is the value `wear-m3-catalog`'s `CircularProgressIndicator` pins,
  // so the two renditions of the same kit cell draw the same arc.
  val progress = rememberOverridableRemoteFloat("progress", 0.6f)
  // `fillMaxSize`, not a 72dp box. The kit publishes this as a *display* cell — a ring struck 2dp
  // inside the bezel of the whole round face — which is why the sticker is on the 227dp
  // [CatalogRemoteDisplay] frame at all, and why the Wear sibling draws it `fillMaxSize` too. At
  // 72dp it was a small dial floating in the middle of a display-sized capture: a different
  // component from the one the row compares it against.
  RemoteCircularProgressIndicator(progress = progress, modifier = RemoteModifier.fillMaxSize())
}

// ---------------------------------------------------------------------------
// Iconography — the standalone `RemoteIcon` primitive. Left at its default near-white
// content tint (the dark-first `RemoteMaterialTheme` scheme), which is why the catalog is
// tagged `display.surface: "dark"` — on a white stage this sticker is invisible.
// Wear M3 parallel: `Icon`.
// ---------------------------------------------------------------------------

@CatalogComponent(
  id = "Icon",
  group = "Iconography",
  noReference =
    "The standalone `RemoteIcon` primitive, not a kit component: the kit draws icons only " +
      "inside the components that slot them.",
  caption = "The standalone RemoteIcon primitive.",
)
@CatalogRemoteModes
@Composable
fun IconRemote() = RemoteSticker {
  // An editable `iconSize` dp knob: reseeding `rc.iconSize=dp:<value>` resizes the icon live. dp is
  // carried distinctly from a bare float so the connector binds it as a density-independent value.
  val iconSize = rememberOverridableRemoteDp("iconSize", 48.dp)
  RemoteIcon(addIcon, "Add".rs, modifier = RemoteModifier.size(iconSize))
}

// ---------------------------------------------------------------------------
// Text — the Remote Material 3 text primitive at its default near-white content colour
// (the dark-first `RemoteMaterialTheme` scheme). Don't override it to a dark colour to
// "fix" a washed-out sticker: the catalog declares `display.surface: "dark"`, so the
// stage is what backs it.
// ---------------------------------------------------------------------------

// The kit's `Text-Body` set has one axis and two cells, and only the `Alignment=Centre` one was
// named. `Alignment=Left` (`38977:66991`) is the other, and nothing here drew it — added coverage
// found while folding the rest ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)).
// The Wear sibling's `Text/Body` carries the same cell under the same name.
//
// Alignment is not a parameter of `RemoteText`: it is what the text does with the width it is
// given, so the cell has to widen the box for the alignment to mean anything. `Centre` is what a
// wrapped run does on its own, which is why it is the base and takes no width.
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@CatalogComponent(
  id = "Text/Body",
  group = "Text",
  parallel = "Text/Body",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66993",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66990",
  caption =
    "The Remote Material 3 text primitive on its own, with the kit's alignment axis " +
      "folded in.",
)
@CatalogRemoteModes
@Composable
fun RemoteTextSticker() = RemoteSticker {
  // The kit's own `Text-Body` string, which is what the `Text/Body` parallel draws — here it flows
  // in full; the `Text/MaxLines-Truncated` sticker below clips the same string. Still an editable
  // `text` string knob, so the viewer can retype it live (`rc.text=<string>`).
  val text = rememberOverridableRemoteString("text", KitCopy.BODY)
  if (previewOverrideChoice("align", "centre", listOf("centre", "left")) == "left") {
    RemoteText(text, modifier = RemoteModifier.width(160.rdp), textAlign = TextAlign.Start)
  } else {
    RemoteText(text)
  }
}

// The text primitive exercising the maxLines / overflow product on a narrow column —
// the Remote parallel of Wear M3's `Text/MaxLines-Truncated`. `RemoteText` carries the
// same `maxLines` + `overflow` knobs as Wear's `Text`.
@CatalogComponent(
  id = "Text/MaxLines-Truncated",
  group = "Text",
  parallel = "Text/Body",
  noReference =
    "The kit's `Text-Body` set has exactly one axis, `Alignment`, and exactly two cells: Left and " +
      "Centre. Neither is truncated — the kit draws its body copy in full and says nothing about " +
      "what a body does when it runs out of room. `maxLines` + `overflow` is a Compose product " +
      "with no cell to compare against, and mapping it onto the Centre cell (which `Text/Body` " +
      "already names) would report the ellipsis as a divergence on every render.",
  caption = "maxLines=2 + ellipsis on a narrow column.",
)
@CatalogRemoteLarge
@Composable
fun TruncatedTextRemote() = RemoteSticker {
  RemoteText(
    // The kit's `Card` body string rather than its `Text-Body` one, and deliberately: this sticker
    // exists to carry the `maxLines` + `overflow` product, and `BODY` fits inside two lines at this
    // width, so using it published an ellipsis sticker with no ellipsis in it. `CARD_CONTENT` is
    // the kit's own longest body copy and overflows two lines here, so the capture shows the
    // product working while still quoting the kit rather than inventing a string to overflow with.
    KitCopy.CARD_CONTENT.rs,
    modifier = RemoteModifier.width(150.rdp),
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
  )
}

// The text primitive carrying a *named* font family rather than one of the four generic
// typefaces — the Remote parallel of the compose-m3 catalog's `text-branded` specimen, which says
// `namedFontFamily("Orbitron")` for the same reason. This is the only sticker in any catalog whose
// `.rc` document names a family, and it exists to keep that path rendered and diffed: a named
// family
// reaches the player as a *text id*, not a name, so it is resolved by a code path no other document
// exercises.
//
// `google:` namespaces the name as a Google Fonts family — `RemoteFontFamily.Named` carries an
// opaque string, so the prefix is what tells both render lanes where the face comes from rather
// than
// leaving them to guess from the name. Orbitron because the catalog already vendors its faces
// (`role: "named"` in the fonts manifest), so the snapshot renderer resolves it locally while the
// browser fetches the same family, and the parity page compares like with like.
@CatalogComponent(
  id = "Text/Branded",
  group = "Text",
  parallel = "Text/Body",
  noReference =
    "The kit's `Text-Body` set varies `Alignment` and nothing else; the typeface is fixed across " +
      "both its cells, and the kit publishes its families as a styles page rather than as a " +
      "component axis. A named family is what this row is for, so there is no cell that is a " +
      "picture of it.",
  caption =
    "Text in a named font family (RemoteFontFamily.Named(\"google:Orbitron\")) rather than a " +
      "generic typeface — the google: namespace marks it a Google Fonts family, which the browser " +
      "lane fetches and the snapshot renderer resolves from its vendored copy.",
)
@CatalogRemoteModes
@Composable
fun BrandedTextRemote() = RemoteSticker {
  // Short copy because Orbitron is a wide face and the frame is only 200dp. Note this does not
  // fully avoid clipping in the browser lane, and cannot: the document carries geometry the
  // *authoring* renderer measured, and that renderer resolves this family to Roboto today, so the
  // player draws a wider face into a box measured for a narrower one. That clip is the renderer gap
  // showing through, not a player bug — it closes when the snapshot lane resolves the family too.
  RemoteText("Orbitron".rs, fontFamily = RemoteFontFamily.Named("google:Orbitron"))
}

// A *specimen sheet*: four branded families, each drawing its own name. One sticker, four different
// typefaces — which is the point. A lane that cannot resolve a named family substitutes one face
// for
// all four, and four identical-looking lines is a failure nobody has to measure to see; a single
// branded line (`BrandedTextRemote` above) only looks "a bit off" unless you already know the face.
//
// The four are the ones the catalog vendors (`role: "named"` in the wasm app's fonts manifest), and
// they are deliberately unalike — geometric, script, grotesk, monospace — so a substitution cannot
// hide in a family resemblance. `google:` namespaces each name as a Google Fonts family: the
// browser
// lane fetches it from the CSS API, the wasm lane reads the vendored copy, and the server-side
// lanes
// resolve it through the shared font cache.
@CatalogComponent(
  id = "Typeface/Specimen",
  group = "Typeface",
  noReference =
    "A typeface specimen: four Google Fonts families each drawing their own name. The kit " +
      "publishes type styles, not a family specimen.",
  caption =
    "Four branded Google Fonts families, each drawing its own name — geometric, script, " +
      "grotesk, monospace. A lane that cannot resolve a named family renders four " +
      "identical-looking lines.",
)
@CatalogRemoteLarge
@Composable
fun TypefaceSpecimenRemote() = RemoteSticker {
  RemoteColumn {
    RemoteText(
      "Orbitron".rs,
      fontSize = 22.rsp,
      fontFamily = RemoteFontFamily.Named("google:Orbitron"),
    )
    RemoteText(
      "Lobster Two".rs,
      fontSize = 22.rsp,
      fontFamily = RemoteFontFamily.Named("google:Lobster Two"),
    )
    RemoteText(
      "Space Grotesk".rs,
      fontSize = 22.rsp,
      fontFamily = RemoteFontFamily.Named("google:Space Grotesk"),
    )
    RemoteText(
      "JetBrains Mono".rs,
      fontSize = 22.rsp,
      fontFamily = RemoteFontFamily.Named("google:JetBrains Mono"),
    )
  }
}

// The `wght` axis of a **variable** font, four instances of one face.
//
// This is a different capability from the specimen above, and no other document in any catalog
// carries it: `RemoteText` writes `fontVariationSettings` into the document as an axis-tag/value
// pair list, so the player must both resolve the family *and* apply the axes to the instance it
// draws. A lane that resolves the family but drops the axes renders four lines in one weight —
// visibly wrong, and wrong in a way that a static-weight specimen cannot expose.
//
// Roboto Flex because it is the catalog's own default face (`role: "default"` in the fonts
// manifest) and a genuine variable font: one file serving `wght` 100..1000, so the four lines below
// are four *instances* of a single downloaded file rather than four separate faces.
@CatalogComponent(
  id = "Typeface/VariableWeight",
  group = "Typeface",
  noReference =
    "A variable-font axis specimen (wght). Nothing in the kit varies a font axis as a published " +
      "component.",
  caption =
    "One variable font (Roboto Flex) at wght 100/400/700/1000, written into the document as " +
      "font-variation settings. A lane that resolves the family but drops the axes renders four " +
      "lines in one weight.",
)
@CatalogRemoteLarge
@Composable
fun VariableWeightRemote() = RemoteSticker {
  RemoteColumn {
    for (weight in listOf(100, 400, 700, 1000)) {
      RemoteText(
        "wght $weight".rs,
        fontSize = 20.rsp,
        fontFamily = RemoteFontFamily.Named("google:Roboto Flex"),
        fontVariationSettings = FontVariation.Settings(FontVariation.weight(weight)),
      )
    }
  }
}

// The `wdth` axis of the same variable font — the axis a weight-only implementation misses.
//
// Weight is the axis every text stack can fake: a player that ignores `wght` but honours
// `FontWeight` still lands near the right thickness, so a `wght` ramp alone cannot tell "applied
// the
// axis" from "synthesised a bold". Width can't be faked — nothing in a text API asks for a narrower
// face — so three lines at `wdth` 25 / 100 / 151 either differ in set width or the axes were
// dropped. Roboto Flex serves that whole range from the one file.
@CatalogComponent(
  id = "Typeface/VariableWidth",
  group = "Typeface",
  noReference =
    "A variable-font axis specimen (wdth). Nothing in the kit varies a font axis as a published " +
      "component.",
  caption =
    "The same variable font at wdth 25/100/151 — the axis nothing can fake, since no text API " +
      "asks for a narrower face. Either the set widths differ or the axes were dropped.",
)
@CatalogRemoteLarge
@Composable
fun VariableWidthRemote() = RemoteSticker {
  RemoteColumn {
    for (width in listOf(25f, 100f, 151f)) {
      RemoteText(
        // The same word on every line. Three different strings would each set to their own length
        // and the axis would be invisible next to that difference; sharing a word makes the narrow
        // instance unmistakably narrower, which is the whole claim the sticker exists to make.
        "Hamburg · wdth ${width.toInt()}".rs,
        fontSize = 22.rsp,
        fontFamily = RemoteFontFamily.Named("google:Roboto Flex"),
        fontVariationSettings = FontVariation.Settings(FontVariation.width(width)),
      )
    }
  }
}

// NOTE: `RemoteTimeText` is intentionally NOT stickered. Source samples live in
// ComponentVariantPreviews.kt, but it draws the time as
// *curved* text (a `DrawTextOnCircle` document op) that the Remote Compose player
// bundled in the renderer can't replay ("Operation 57 is not supported for this
// version" — an alpha writer/player version skew), so it fails the render outright.
// Re-add a `TimeText` sticker once the player supports the curved-text op.

// ---------------------------------------------------------------------------
// Theme — the Remote Material 3 theme surfaced as design specimens (the "even themes"
// parallels): a typography ramp and a colour-scheme swatch row, read straight from
// `RemoteMaterialTheme`. Parallels of the M3 typography / colour token sheets.
// ---------------------------------------------------------------------------

@CatalogComponent(
  id = "Theme/Typography",
  group = "Theme",
  noReference =
    "A type ramp read from `RemoteMaterialTheme.typography` — a specimen of the library's " +
      "tokens, which the kit publishes as a styles page rather than a component set.",
  caption = "A type ramp read from RemoteMaterialTheme.typography.",
)
@CatalogRemoteLarge
@Composable
fun TypographyRemote() = RemoteSticker {
  RemoteColumn {
    RemoteText("Body Large".rs, style = RemoteMaterialTheme.typography.bodyLarge)
    RemoteText("Label Medium".rs, style = RemoteMaterialTheme.typography.labelMedium)
    RemoteText("Label Small".rs, style = RemoteMaterialTheme.typography.labelSmall)
  }
}

@CatalogComponent(
  id = "Theme/ColorScheme",
  group = "Theme",
  noReference =
    "Colour-scheme swatches read from `RemoteMaterialTheme.colorScheme` — a token specimen, not " +
      "a component the kit publishes as a set.",
  caption = "Colour-scheme swatches read from RemoteMaterialTheme.colorScheme.",
)
@CatalogRemoteLarge
@Composable
fun ColorSchemeRemote() = RemoteSticker {
  RemoteRow {
    RemoteBox(
      modifier =
        RemoteModifier.size(44.rdp)
          .background(RemoteBrush.solidColor(RemoteMaterialTheme.colorScheme.primary)),
      content = {},
    )
    RemoteBox(
      modifier =
        RemoteModifier.size(44.rdp)
          .background(RemoteBrush.solidColor(RemoteMaterialTheme.colorScheme.surfaceContainer)),
      content = {},
    )
    RemoteBox(
      modifier =
        RemoteModifier.size(44.rdp)
          .background(RemoteBrush.solidColor(RemoteMaterialTheme.colorScheme.onBackground)),
      content = {},
    )
  }
}

// ---------------------------------------------------------------------------
// Shaders — a document-level gradient fill (`remote-creation-compose` shaders),
// serialised into the RemoteDocument and rasterised by the player rather than an
// app-side `ShaderBrush`. The one Remote-only sticker with no Wear M3 component peer
// (it's a creation-compose primitive, not a `remote-material3` component). The middle
// stop is a named-value binding so the connector can recolour it live.
// ---------------------------------------------------------------------------

@CatalogComponent(
  id = "Shader/LinearGradient",
  group = "Shaders",
  noReference =
    "A `remote-creation-compose` document-level shader fill — a Remote Compose primitive with " +
      "no Wear Material 3 component peer, and none in the kit.",
  caption =
    "Document-level gradient shader fill; the middle stop is a named value the connector can " +
      "recolour live. A remote-creation-compose primitive with no Wear Material 3 component peer.",
)
@CatalogRemoteCanvas
@Composable
fun ShaderGradientSticker() = RemoteSticker {
  val shaderColor = rememberOverridableRemoteColor("shaderColor", Color(0xFF7DE2FF))
  val brush =
    RemoteBrush.linearGradient(
      listOf(RemoteColor(Color(0xFF101820)), shaderColor, RemoteColor(Color(0xFFFFB86C)))
    )
  RemoteBox(
    modifier = RemoteModifier.fillMaxSize().background(brush),
    contentAlignment = RemoteAlignment.Center,
    content = { RemoteText("Shader".rs) },
  )
}
