@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.Action
import androidx.compose.remote.creation.compose.action.hostAction
import androidx.compose.remote.creation.compose.action.valueChange
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteImage
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.height
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.shaders.RemoteBrush
import androidx.compose.remote.creation.compose.shaders.linearGradient
import androidx.compose.remote.creation.compose.shaders.solidColor
import androidx.compose.remote.creation.compose.shapes.RemoteCircleShape
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteDp
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.animateRemoteFloat
import androidx.compose.remote.creation.compose.state.asRdp
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.remote.material3.RemoteAppCard
import androidx.wear.compose.remote.material3.RemoteButton
import androidx.wear.compose.remote.material3.RemoteButtonColors
import androidx.wear.compose.remote.material3.RemoteButtonDefaults
import androidx.wear.compose.remote.material3.RemoteButtonGroup
import androidx.wear.compose.remote.material3.RemoteCard
import androidx.wear.compose.remote.material3.RemoteCardDefaults
import androidx.wear.compose.remote.material3.RemoteCircularProgressIndicator
import androidx.wear.compose.remote.material3.RemoteCompactButton
import androidx.wear.compose.remote.material3.RemoteCurvedProgressIndicator
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteIconButton
import androidx.wear.compose.remote.material3.RemoteIconButtonColors
import androidx.wear.compose.remote.material3.RemoteIconButtonDefaults
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteOutlinedCard
import androidx.wear.compose.remote.material3.RemoteText
import androidx.wear.compose.remote.material3.RemoteTextButton
import androidx.wear.compose.remote.material3.RemoteTextButtonDefaults
import androidx.wear.compose.remote.material3.buttonSizeModifier
import ee.schimke.composeai.daemon.rememberOverridableRemoteColor
import ee.schimke.composeai.daemon.rememberOverridableRemoteDp
import ee.schimke.composeai.daemon.rememberOverridableRemoteFloat
import ee.schimke.composeai.daemon.rememberOverridableRemoteString
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.overrides.previewOverrideFloat
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

// The glyph the kit's `Title Card + Icon` cells draw, and the ONLY slot on this sheet that is not
// `addIcon`.
//
// `Icons.Filled.Star`, transcribed for the same reason `addIcon` is — Remote Compose bundles no
// icon set. Checked against the kit's own export: `46048:69274` is a star over `Label text`, where
// the `Button` set's `Icon=Yes` cells are a `+`. The card's leading slot used to draw `addIcon`
// too, which put the button sheet's glyph under a card cell that draws a different one, on
// seventeen AppCard rows ([#294](https://github.com/yschimke/wear-m3-catalog/issues/294)).
// `wear-m3-catalog`'s `ApplicationCard` passes `Icons.Filled.Star` into the same slot.
internal val starIcon: ImageVector =
  ImageVector.Builder(
      name = "Star",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    )
    .apply {
      path(fill = SolidColor(Color.White)) {
        moveTo(12f, 17.27f)
        lineTo(18.18f, 21f)
        lineTo(16.54f, 13.97f)
        lineTo(22f, 9.24f)
        lineTo(14.81f, 8.63f)
        lineTo(12f, 2f)
        lineTo(9.19f, 8.63f)
        lineTo(2f, 9.24f)
        lineTo(7.46f, 13.97f)
        lineTo(5.82f, 21f)
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
@OverrideVariant(
  name = "icon-disabled",
  booleans = ["icon=true", "enabled=false"],
  kitProps = ["Icon=Yes", "Icon size=26 (Default)", "Alignment=Left", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-large-disabled",
  booleans = ["icon=true", "enabled=false"],
  strings = ["iconSize=large"],
  kitProps = ["Icon=Yes", "Icon size=Lrg 32", "Alignment=Left", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-extra-large",
  booleans = ["icon=true"],
  strings = ["iconSize=extra-large"],
  kitProps = ["Icon=Yes", "Icon size=xLg 36", "Alignment=Left"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-extra-large-disabled",
  booleans = ["icon=true", "enabled=false"],
  strings = ["iconSize=extra-large"],
  kitProps = ["Icon=Yes", "Icon size=xLg 36", "Alignment=Left", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "left",
  strings = ["alignment=left"],
  kitProps = ["Icon=No", "Icon size=n/a", "Alignment=Left"],
)
// `left-disabled` IS DRAWN, and it is the same picture as `disabled` on every one of the five
// styles. A disabled `RemoteButton` draws its container and NOT its label, so aligning a label that
// is not in the picture changes nothing. The kit publishes the cell; the library collapses it; the
// sheet says so, through `RemoteRenderTest.knownDuplicate` rather than by leaving the slot empty.
@OverrideVariant(
  name = "left-disabled",
  booleans = ["enabled=false"],
  strings = ["alignment=left"],
  kitProps = ["Icon=No", "Icon size=n/a", "Alignment=Left", "Disabled=Yes"],
  secondary = true,
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
fun FilledRemoteButton() = RemoteSticker { RemoteKitButton(RemoteButtonDefaults.buttonColors()) }

/**
 * One style's worth of the kit's `Button` cells: the `Icon` / `Icon size` / `Disabled` axes, drawn
 * against whichever emphasis the caller passes.
 *
 * Hoisted for the same reason [RemoteButtonKitCells] is, one level down. Those axes are ARGUMENTS
 * to `RemoteButton` rather than a choice of function, so the cells are identical for every emphasis
 * — and until this existed only the filled style read the knobs, so `Button/Outlined` and
 * `Button/Tonal` published their base cell and nothing else. Ten of the set's fifty nodes were
 * undrawn on that account alone ([#160](https://github.com/yschimke/wear-m3-catalog/issues/160)).
 *
 * `border` and `borderColor` are passed rather than defaulted because `remote-material3` publishes
 * one `buttonColors()` and no outline treatment: the outlined style is this catalog holding a
 * transparent container against a border, the way Wear's own `OutlinedButton` is built underneath.
 * A zero-width border is a no-op whatever colour rides with it, which is what the filled and tonal
 * callers pass.
 *
 * `internal` rather than `private` because `Button/Tonal` lives in ComponentVariantPreviews.kt — it
 * stays a top-level component so the compare page's `Button/Tonal` card faces something, since the
 * Wear column reaches that emphasis through a separate function.
 */
@Composable
internal fun RemoteKitButton(
  colors: RemoteButtonColors,
  border: RemoteDp = 0.rdp,
  borderColor: RemoteColor = RemoteColor(Color.Transparent),
) {
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  val enabled = previewOverrideBoolean("enabled", true).rb
  if (!previewOverrideBoolean("icon", false)) {
    // The kit's `Icon=No` column, and the base render. `buttonSizeModifier()` is what makes a
    // label-only button measure like the kit's one-line cell.
    RemoteButton(
      onClick = onClick,
      modifier = RemoteModifier.buttonSizeModifier().width(KitRowWidth),
      enabled = enabled,
      colors = colors,
      border = border,
      borderColor = borderColor,
      // The kit's `Alignment` axis, which is not a parameter on either platform: it is what the
      // label does with the width it is given. `Center` is the base cell and what a label-only
      // button does on its own; `Left` fills the row and starts the text. The Wear column spells
      // it the same way, under the same cell name.
      content = {
        if (previewOverrideChoice("alignment", "center", listOf("center", "left")) == "left") {
          RemoteText(label, modifier = RemoteModifier.fillMaxWidth(), textAlign = TextAlign.Start)
        } else {
          RemoteText(label)
        }
      },
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
      colors = colors,
      border = border,
      borderColor = borderColor,
      icon = {
        RemoteIcon(
          addIcon,
          contentDescription = null,
          modifier =
            RemoteModifier.size(
              when (
                previewOverrideChoice(
                  "iconSize",
                  "default",
                  listOf("default", "large", "extra-large"),
                )
              ) {
                "large" -> RemoteButtonDefaults.LargeIconSize
                // A LITERAL, and the only one in this body. `RemoteButtonDefaults` publishes
                // `IconSize` and `LargeIconSize` and stops there — the alpha `remote-material3`
                // surface has no extra-large token — so the kit's `Icon size=xLg 36` column is
                // reached by naming the kit's own 36dp rather than left undrawn. If the library
                // gains the token, this becomes `ExtraLargeIconSize` and nothing else moves.
                "extra-large" -> 36.rdp
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
@RemoteButtonKitCells
@Composable
fun OutlinedRemoteButton() = RemoteSticker {
  RemoteKitButton(
    colors =
      RemoteButtonDefaults.buttonColors(
        containerColor = RemoteColor(Color.Transparent),
        contentColor = RemoteMaterialTheme.colorScheme.onSurface,
      ),
    border = 2.rdp,
    borderColor = RemoteMaterialTheme.colorScheme.outline,
  )
}

@CatalogComponent(
  id = "Button/CustomShape",
  group = "Buttons",
  // NO `parallel`, and the `noReference` below is the same answer one column over. Pointing this
  // at `Button/Filled` compared a corner override against the stock pill and reported the shape —
  // the only thing this row is for — as divergence
  // ([#292](https://github.com/yschimke/wear-m3-catalog/issues/292)).
  //
  // Nothing on the Wear column is a better target: its button family is the five kit styles and
  // `Compact` / `ImageBackground` / `Loading`, none of which overrides a shape, and AGENTS.md names
  // this row as one of the door-2 components that stays top-level under `noReference` precisely
  // because it has no kit call site to fold onto. If `:catalog` ever publishes a shape override of
  // its own — `Button` takes a `shape` there too — this row gains a real counterpart and should
  // name it.
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
  // NO `parallel`, for the reason the `noReference` below already gives about the kit node: what
  // this row varies is where the label comes from, and `:catalog` has no counterpart for that. It
  // pointed at `Button/Filled`, which drew the same picture and said nothing about the binding
  // ([#292](https://github.com/yschimke/wear-m3-catalog/issues/292)).
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
 * **Every cell of the kit's `Text-Button` set this rendition can reach** — 7 of its 30 nodes.
 *
 * Four were top-level components until #116 folded them; the three crossings under them are what
 * phase 3's tier made publishable, and they carry `secondary = true` per the tiering rule in
 * AGENTS.md. `small`, `large`, `child` and `outlined` turn one knob each and stay in the tree;
 * `child-large`, `outlined-small` and `outlined-large` exist to be compared, not browsed.
 *
 * They fold for the reason AGENTS.md gives for folding them on the Wear side: the test is the CALL
 * SITE, not the word. `Style=` on this set is not a choice between functions — `remote-material3`
 * ships one `RemoteTextButton` and it takes its emphasis as `colors` — so there is no second
 * function for a reader to pick, and nothing to split. `Size=` is a modifier argument on the same
 * one. The cell names are the Wear sibling's, so the compare page pairs them.
 *
 * **NO `Disabled` axis at all, and the render is why.** `RemoteTextButton(enabled = false)` draws
 * NOTHING on the alpha surface — no container, no label, a fully transparent capture; the outlined
 * cell keeps only the border, because this sticker draws that itself through `border` rather than
 * through the component. Six disabled cells were written here and all six came out byte-identical
 * blanks, which would have scored an empty frame against six drawn kit nodes. Withdrawn, and
 * recorded here as a library gap rather than carried: the kit's fifteen `Disabled=Yes` cells for
 * this set are absent from this rendition until the library draws a disabled text button.
 * (`RemoteIconButton` does honour `enabled` — see `IconButton/Standard`, which keeps the axis.)
 *
 * **No `child-small` either**, and for the reason the icon button's withdrawn `extra-small` cell
 * gives: the child style draws no container, `SmallButtonSize` clamps to the same glyph metrics as
 * the default, and the capture came out byte-identical to `child`. `child-large` survives because
 * the large TEXT style is bigger, which is a picture.
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
@OverrideVariant(
  name = "child-large",
  strings = ["style=child", "size=large"],
  kitProps = ["Style=Child (No background)", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-small",
  strings = ["style=outlined", "size=small"],
  kitProps = ["Style=Outline", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-large",
  strings = ["style=outlined", "size=large"],
  kitProps = ["Style=Outline", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "child-small",
  strings = ["style=child", "size=small"],
  kitProps = ["Style=Child (No background)", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitAxis = "Style",
  kitValue = "Filled-Variant",
)
@OverrideVariant(
  name = "filled-variant-small",
  strings = ["style=filled-variant", "size=small"],
  kitProps = ["Style=Filled-Variant", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-large",
  strings = ["style=filled-variant", "size=large"],
  kitProps = ["Style=Filled-Variant", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal",
  strings = ["style=tonal"],
  kitAxis = "Style",
  kitValue = "Tonal",
)
@OverrideVariant(
  name = "tonal-small",
  strings = ["style=tonal", "size=small"],
  kitProps = ["Style=Tonal", "Size=Small", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-large",
  strings = ["style=tonal", "size=large"],
  kitProps = ["Style=Tonal", "Size=Large", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "outlined-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined"],
  kitProps = ["Style=Outline", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "size=small"],
  kitProps = ["Style=Outline", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "size=large"],
  kitProps = ["Style=Outline", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant"],
  kitProps = ["Style=Filled-Variant", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "size=small"],
  kitProps = ["Style=Filled-Variant", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "size=large"],
  kitProps = ["Style=Filled-Variant", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal"],
  kitProps = ["Style=Tonal", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "size=small"],
  kitProps = ["Style=Tonal", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "size=large"],
  kitProps = ["Style=Tonal", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child"],
  kitProps = ["Style=Child (No background)", "Size=Default", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-small-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "size=small"],
  kitProps = ["Style=Child (No background)", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-large-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "size=large"],
  kitProps = ["Style=Child (No background)", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=small"],
  kitProps = ["Style=Filled", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "large-disabled",
  booleans = ["enabled=false"],
  strings = ["size=large"],
  kitProps = ["Style=Filled", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
annotation class RemoteTextButtonKitCells

// A low-emphasis round text button (`RemoteTextButton`), the Remote parallel of Wear
// M3's `TextButton`, with the kit's `Style` and `Size` axes folded in as cells.
@CatalogComponent(
  id = "TextButton",
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
  val style =
    previewOverrideChoice(
      "style",
      "filled",
      listOf("filled", "filled-variant", "tonal", "child", "outlined"),
    )
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
    } else if (style == "filled-variant" || style == "tonal") {
      // The kit's other two filled emphases. `RemoteTextButtonDefaults` publishes one
      // `textButtonColors()` — the CHILD style — so, like every other emphasis on this surface,
      // these are the tokens the Wear function they pair with resolves to:
      // `filledVariantTextButtonColors()` is the primary container, `filledTonalTextButtonColors()`
      // the neutral surface container.
      val container =
        if (style == "tonal") RemoteMaterialTheme.colorScheme.surfaceContainer
        else RemoteMaterialTheme.colorScheme.primaryContainer
      val onContainer =
        if (style == "tonal") RemoteMaterialTheme.colorScheme.onSurface
        else RemoteMaterialTheme.colorScheme.onPrimaryContainer
      RemoteTextButtonDefaults.textButtonColors(
        containerColor = tween(container, RemoteMaterialTheme.colorScheme.primaryDim, on),
        contentColor = onContainer,
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
    enabled = previewOverrideBoolean("enabled", true).rb,
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
 * split, because `IconButton/Filled` and `IconButton/Outlined` are separate functions on the Wear
 * column, and this sheet names them identically. This component IS the kit's child style — no
 * container at all — and its cells vary only the size, so they take the stock colours the base
 * sticker takes.
 *
 * **The two `extra-small` cells are a COLLAPSE, and they are drawn.** `iconSizeFor` resolves
 * `ExtraSmallButtonSize` and `SmallButtonSize` to the same glyph, and with no container there is
 * nothing else in the picture to tell them apart — so `extra-small` is byte-identical to `small`,
 * and `extra-small-disabled` to `small-disabled`. They were withdrawn for that, on the reasoning
 * that a mapping which cannot fail is worse than no mapping.
 *
 * That reasoning was half right. A cell publishing one render under two names IS the thing to avoid
 * when this file is what collapses them; when the LIBRARY collapses them however it is called,
 * withdrawing hides the collapse and leaves the kit's node reading as undrawn — which is
 * indistinguishable from nobody having got to it. `RemoteRenderTest.knownDuplicate` is where such a
 * pair is recorded instead, and it fails from the other side if `RemoteIconButton` ever sizes the
 * child style's glyph from the container token
 * ([#178](https://github.com/yschimke/wear-m3-catalog/issues/178)).
 *
 * The Wear sibling's `IconButton/Standard` reached the same place by the same road and now draws
 * them the same way, so the kit's `Size=Extra-Small` column is complete on both sheets.
 */
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitAxis = "Size",
  kitValue = "Extra-Small",
)
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
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=extra-small"],
  kitProps = ["Style=Child (No background)", "Size=Extra-Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=small"],
  kitProps = ["Style=Child (No background)", "Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "large-disabled",
  booleans = ["enabled=false"],
  strings = ["size=large"],
  kitProps = ["Style=Child (No background)", "Size=Large", "Disabled=Yes"],
  secondary = true,
)
annotation class RemoteIconButtonKitCells

/**
 * [RemoteIconButtonKitCells] plus the two `Extra-Small` cells, for the four styles that DRAW a
 * container and can therefore tell that size apart. Eight cells — the kit's whole `Size` run
 * crossed with `Disabled` — and the same eight the Wear column's [IconButtonKitCells] carries.
 *
 * Until this existed, `IconButton/Filled` and `IconButton/Outlined` published their base render and
 * nothing else, and the kit's `Tonal` and `Variant (Highlighted)` columns had no component at all:
 * 32 of the set's 40 nodes were undrawn
 * ([#160](https://github.com/yschimke/wear-m3-catalog/issues/160)).
 */
@OverrideVariant(
  name = "extra-small",
  strings = ["size=extra-small"],
  kitAxis = "Size",
  kitValue = "Extra-Small",
)
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
  name = "disabled",
  booleans = ["enabled=false"],
  kitAxis = "Disabled",
  kitValue = "Yes",
)
@OverrideVariant(
  name = "small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=small"],
  kitProps = ["Size=Small", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "large-disabled",
  booleans = ["enabled=false"],
  strings = ["size=large"],
  kitProps = ["Size=Large", "Disabled=Yes"],
  secondary = true,
)
// `extra-small-disabled` is drawn here for every style, and on three of the four it is the same
// picture as `small-disabled`: a disabled icon button loses its glyph, and with only the container
// left `ExtraSmallButtonSize` and `SmallButtonSize` resolve to one frame. `IconButton/Outlined` is
// the exception — it draws its own border at the container's size — so there the two differ. The
// three collapses are recorded in `RemoteRenderTest.knownDuplicate`.
@OverrideVariant(
  name = "extra-small-disabled",
  booleans = ["enabled=false"],
  strings = ["size=extra-small"],
  kitProps = ["Size=Extra-Small", "Disabled=Yes"],
  secondary = true,
)
annotation class RemoteContainedIconButtonKitCells

/**
 * One style's worth of the kit's `Icon-Button` cells, drawn against whichever emphasis the caller
 * passes — the icon-button twin of [RemoteKitButton], and hoisted for the same reason.
 *
 * `Size` and `Disabled` are arguments to the one `RemoteIconButton`, so the cells are identical
 * across the styles; only the colours (and, for the outlined one, the border it draws itself)
 * differ. `iconSizeFor` is the pairing the library publishes for exactly this: the glyph is sized
 * from the container token rather than left at Material's default.
 */
@Composable
internal fun RemoteKitIconButton(
  colors: RemoteIconButtonColors,
  border: RemoteDp = 0.rdp,
  borderColor: RemoteColor = RemoteColor(Color.Transparent),
) {
  val size =
    when (
      previewOverrideChoice("size", "default", listOf("default", "extra-small", "small", "large"))
    ) {
      "extra-small" -> RemoteIconButtonDefaults.ExtraSmallButtonSize
      "small" -> RemoteIconButtonDefaults.SmallButtonSize
      "large" -> RemoteIconButtonDefaults.LargeButtonSize
      else -> RemoteIconButtonDefaults.DefaultButtonSize
    }
  val (_, onClick) = toggledRemote()
  RemoteIconButton(
    onClick = onClick,
    enabled = previewOverrideBoolean("enabled", true).rb,
    modifier = RemoteModifier.size(size),
    colors = colors,
    border = border,
    borderColor = borderColor,
    content = {
      RemoteIcon(
        addIcon,
        "Add".rs,
        modifier = RemoteModifier.size(RemoteIconButtonDefaults.iconSizeFor(size)),
      )
    },
  )
}

// A round icon button (`RemoteIconButton`) carrying a single `RemoteIcon`. Inside the
// button the icon inherits the button's (contrasting) content colour, so no explicit
// tint is needed. Wear M3 parallel: `IconButton/Standard`.
@CatalogComponent(
  id = "IconButton/Standard",
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
  // `DefaultButtonSize` at the default, and NOT an unpinned `RemoteModifier`. #125 left the base
  // unpinned on the reasoning that the library would pick this token anyway; it does not. Unpinned
  // it renders a 28dp glyph — the same one `SmallButtonSize` resolves to — so the `small` cell was
  // byte-identical to the base and scored against the kit's `Size=Small` node while drawing the
  // default. Pinning the token the kit's `Size=Default` cell means gives 32dp, and the three sizes
  // are three pictures again.
  val size =
    when (
      previewOverrideChoice("size", "default", listOf("default", "extra-small", "small", "large"))
    ) {
      // The kit's fourth size. It resolves to the same glyph `SmallButtonSize` does on a style
      // that draws no container — see the note on `RemoteIconButtonKitCells` — but the value the
      // kit's cell names is this one, so this is the call it takes.
      "extra-small" -> RemoteIconButtonDefaults.ExtraSmallButtonSize
      "small" -> RemoteIconButtonDefaults.SmallButtonSize
      "large" -> RemoteIconButtonDefaults.LargeButtonSize
      else -> RemoteIconButtonDefaults.DefaultButtonSize
    }
  RemoteIconButton(
    onClick = onClick,
    enabled = previewOverrideBoolean("enabled", true).rb,
    modifier = RemoteModifier.size(size),
    colors =
      RemoteIconButtonDefaults.iconButtonColors(
        containerColor = tween(stock.containerColor, RemoteMaterialTheme.colorScheme.primary, on)
      ),
    content = {
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
  name = "disabled",
  booleans = ["enabled=false"],
  kitProps = ["Style=Filled", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-only",
  strings = ["content=icon"],
  kitProps = ["Style=Filled", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-only-disabled",
  booleans = ["enabled=false"],
  strings = ["content=icon"],
  kitProps = ["Style=Filled", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "text-only",
  strings = ["content=text"],
  kitProps = ["Style=Filled", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "text-only-disabled",
  booleans = ["enabled=false"],
  strings = ["content=text"],
  kitProps = ["Style=Filled", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant",
  strings = ["style=filled-variant"],
  kitProps = ["Style=Filled Variant", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-only",
  strings = ["style=filled-variant", "content=icon"],
  kitProps =
    ["Style=Filled Variant", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-text-only",
  strings = ["style=filled-variant", "content=text"],
  kitProps =
    ["Style=Filled Variant", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal",
  strings = ["style=tonal"],
  kitProps = ["Style=Tonal", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-only",
  strings = ["style=tonal", "content=icon"],
  kitProps = ["Style=Tonal", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-text-only",
  strings = ["style=tonal", "content=text"],
  kitProps = ["Style=Tonal", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitProps = ["Style=Outline", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined"],
  kitProps = ["Style=Outline", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-only",
  strings = ["style=outlined", "content=icon"],
  kitProps = ["Style=Outline", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-icon-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "content=icon"],
  kitProps = ["Style=Outline", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-text-only",
  strings = ["style=outlined", "content=text"],
  kitProps = ["Style=Outline", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-text-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=outlined", "content=text"],
  kitProps = ["Style=Outline", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child",
  strings = ["style=child"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "child-icon-only",
  strings = ["style=child", "content=icon"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "child-text-only",
  strings = ["style=child", "content=text"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=No"],
  secondary = true,
)
@OverrideVariant(
  name = "child-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-icon-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "content=icon"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "child-text-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=child", "content=text"],
  kitProps =
    ["Style=Child (No background)", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant"],
  kitProps =
    ["Style=Filled Variant", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-icon-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "content=icon"],
  kitProps =
    ["Style=Filled Variant", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "filled-variant-text-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=filled-variant", "content=text"],
  kitProps =
    ["Style=Filled Variant", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal"],
  kitProps = ["Style=Tonal", "Alignment=Icon left", "Icon=Yes", "Text=Yes", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-icon-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "content=icon"],
  kitProps = ["Style=Tonal", "Alignment=Icon centre", "Icon=Yes", "Text=No", "Disabled=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "tonal-text-only-disabled",
  booleans = ["enabled=false"],
  strings = ["style=tonal", "content=text"],
  kitProps = ["Style=Tonal", "Alignment=Text centre", "Icon=No", "Text=Yes", "Disabled=Yes"],
  secondary = true,
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
  // The kit's `Style` axis, folded the way it folds on `RemoteButton`: `remote-material3` publishes
  // one `buttonColors()` and no emphasis variants, so each style is the tokens the Wear function it
  // pairs with resolves to. Only `Outline` needs a border, which is its own parameter here.
  val style =
    previewOverrideChoice(
      "style",
      "filled",
      listOf("filled", "filled-variant", "tonal", "outlined", "child"),
    )
  val (label, onClick) = countedRemote(KitCopy.PRIMARY_LABEL)
  // The icon-only cell has no label to count into, so it reads as a toggle instead — the same
  // bargain `IconButton/Standard` strikes. At rest `on` is 0f and `tween(a, b, 0f)` is `a`, so the
  // baked capture is whichever style's own container.
  val (on, toggle) = toggledRemote()
  val container =
    when (style) {
      "filled-variant" -> RemoteMaterialTheme.colorScheme.primaryContainer
      "tonal" -> RemoteMaterialTheme.colorScheme.surfaceContainer
      "outlined",
      "child" -> RemoteColor(Color.Transparent)
      else -> RemoteButtonDefaults.buttonColors().containerColor
    }
  val contentColor =
    when (style) {
      "filled-variant" -> RemoteMaterialTheme.colorScheme.onPrimaryContainer
      "tonal",
      "outlined",
      "child" -> RemoteMaterialTheme.colorScheme.onSurface
      else -> RemoteButtonDefaults.buttonColors().contentColor
    }
  RemoteCompactButton(
    onClick = if (content == "icon") toggle else onClick,
    enabled = previewOverrideBoolean("enabled", true).rb,
    border = if (style == "outlined") 2.rdp else 0.rdp,
    borderColor =
      if (style == "outlined") RemoteMaterialTheme.colorScheme.outline
      else RemoteColor(Color.Transparent),
    colors =
      RemoteButtonDefaults.buttonColors(
        containerColor =
          if (content == "icon")
            tween(container, RemoteMaterialTheme.colorScheme.tertiaryContainer, on)
          else container,
        contentColor = contentColor,
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
  id = "ButtonGroup",
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
  // 180dp, the width `wear-m3-catalog`'s `ButtonGroup` pins — and the same bargain the labels
  // above strike, unfinished until now. With no kit node behind either rendition the sibling is the
  // only thing this row can be read against, and it can only be read against it if both are given
  // the same box. Left unpinned the group filled its 454-wide `CatalogRemoteLarge` frame — 410px of
  // ink against the Wear column's 312 — and the row reported ~98px of width difference that was
  // entirely the missing modifier
  // ([#295](https://github.com/yschimke/wear-m3-catalog/issues/295)).
  RemoteButtonGroup(modifier = RemoteModifier.width(180.rdp)) {
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
  noReference =
    "The kit's `Card` set publishes no title-less cell — all 45 cells are App Card or Title " +
      "Card layouts. This is Remote Material 3's plain one-slot card; the mapped title-card " +
      "cells are published by `TitleCard` and `AppCard`.",
  caption = "Remote Material 3's plain one-slot card.",
)
@CatalogRemoteLarge
@Composable
fun CardRemote() = RemoteSticker {
  val (label, onClick) = countedRemote(KitCopy.CARD_CONTENT)
  RemoteCard(
    onClick = onClick,
    modifier = RemoteModifier.width(KitRowWidth),
    content = { RemoteText(label) },
  )
}

@CatalogComponent(
  id = "Card/Outlined",
  group = "Containment",
  parallel = "Card/Outlined",
  noReference =
    "The kit's `Card` set publishes no title-less outlined cell. Its Outline cells are App Card " +
      "or Title Card layouts; this is Remote Material 3's plain `RemoteOutlinedCard`.",
  caption = "Remote Material 3's plain outlined one-slot card.",
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
    modifier = RemoteModifier.width(KitRowWidth),
    content = { RemoteText(label, color = RemoteMaterialTheme.colorScheme.onSurface) },
  )
}

// ---------------------------------------------------------------------------
// The kit's `Content type` axis, as what a card's content slot holds.
//
// Thirty of the `Card` set's forty-five cells put imagery there — ten `Image`, ten `Gallery 1`, ten
// `Gallery 2` — against fifteen that hold text. The remote sheet drew none of them until now, and
// nothing failed: the inventory test asks whether a COMPONENT is mapped, and
// `CatalogKitCoverageTest`
// asks whether a SET is reproduced. Neither looks at the cells inside a set, so two-thirds of this
// one could go undrawn in silence.
//
// The geometry below is read off the kit's own nodes rather than copied from `:catalog`, which has
// the shape wrong on every count and is tracked in #153: it draws two equal 42dp thumbnails for
// both
// galleries where the kit draws stepped frames at 64dp and 58dp, in opposite orders, with different
// corners, plus an overflow badge that `:catalog` has no equivalent of at all.
//
//   Slot Image      148 x 64        (the App Card cell measures 66.2 — its aspect-ratio keeper)
//   Slot Gallery 1  148 x 64        left 86 @0 r14, right 58 @90 r14, badge 24 @120 r26
//   Slot Gallery 2  148 x 58        right 58 @0 r200, left 86 @62 r200, badge 24 @120 r26
//
// The badge OVERLAPS the trailing image in both — 120..144 against a slot ending at 148 — so it is
// a
// sibling in a `RemoteBox` rather than a third item in the row. `RemoteAlignment` is a property of
// the box, not of each child, so start-aligned row and end-aligned badge are two boxes.
private const val GallerySlotWidth = 148
private const val GalleryGap = 4

/** One frame of the kit's imagery, at the size and corner the cell gives it. */
@Composable
private fun imageFrame(width: Int, height: Int, corner: Int) {
  RemoteImage(
    CatalogRemoteImage.bitmap(),
    null,
    modifier =
      RemoteModifier.width(width.rdp).height(height.rdp).clip(RemoteRoundedCornerShape(corner.rdp)),
    // FillBounds, because the placeholder is one colour and these frames are not
    // square. The default scale preserves the bitmap's own 1:1 aspect and
    // letterboxes inside the layout box, which drew every frame wider than tall
    // as a square: the 148dp image slot came out 64x64 and the gallery's 86dp
    // lead frame came out 64 too, with the letterboxing read as a 15dp gap.
    // Stretching a solid colour distorts nothing.
    contentScale = ContentScale.FillBounds,
  )
}

/**
 * A gallery row: two frames of unequal width, and the overflow badge that sits over the second.
 *
 * [lead] is drawn first, which is what separates the kit's two galleries as much as their corners
 * do — `Gallery 1` leads with the wide frame, `Gallery 2` with the narrow one.
 */
@Composable
private fun galleryRow(height: Int, lead: Int, trail: Int, corner: Int) {
  RemoteRow(
    modifier = RemoteModifier.width(GallerySlotWidth.rdp).height(height.rdp),
    horizontalArrangement = RemoteArrangement.spacedBy(GalleryGap.rdp),
  ) {
    imageFrame(lead, height, corner)
    imageFrame(trail, height, corner)
  }
}

/**
 * What the kit's `Content type` axis puts in the content slot.
 *
 * Null for `Text`, so the caller keeps its own body copy — the text cells are the ones the sheet
 * already drew.
 */
@Composable
private fun cardImagery(): (@Composable () -> Unit)? =
  when (
    previewOverrideChoice("content", "text", listOf("text", "image", "gallery-1", "gallery-2"))
  ) {
    "image" -> ({ imageFrame(GallerySlotWidth, 64, 14) })
    "gallery-1" -> ({ galleryRow(height = 64, lead = 86, trail = 58, corner = 14) })
    // `Gallery 2` is 58 tall, not 64, and leads with the narrow frame at a corner radius that
    // rounds
    // it to a pill. Copying `Gallery 1` and only swapping the shape would put the wrong picture
    // under a cell that differs in three ways.
    "gallery-2" -> ({ galleryRow(height = 58, lead = 58, trail = 86, corner = 200) })
    else -> null
  }

// `Title Card 2` — the kit's second layout, which adds a subtitle under the body. It used to be a
// top-level component (`TitleCard/WithSubtitle`); the kit models the layouts as one `Layout type`
// property on one `Card` set, and `RemoteTitleCard` reaches them by filling one more slot rather
// than by being a different function, so it is a cell
// ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)). The Wear sibling's `TitleCard`
// carries it under the same name.
//
// `Title Card 3` is a cell on this function too, as of #202 — the note that used to sit here called
// it a stated absence shipping under its own name, `TitleCard/Subtitle`, and both halves of that
// stopped being true when it folded in. What has NOT changed is why its render will not match: the
// kit draws the timestamp under the subtitle, and neither `RemoteTitleCard` nor Wear's `TitleCard`
// has an argument that moves the time off the title's row. The cell is drawn failing rather than
// withheld, which is this repo's rule when the call site exists.
@OverrideVariant(
  name = "with-subtitle",
  strings = ["layout=title-time-subtitle"],
  kitProps = ["Layout type=Title Card 2", "Style=Tonal", "Content type=Text", "Interactive=Yes"],
)
@OverrideVariant(
  name = "content-image",
  strings = ["content=image"],
  kitProps = ["Layout type=Title Card 1", "Style=Tonal", "Content type=Image", "Interactive=Yes"],
)
@OverrideVariant(
  name = "gallery-1",
  strings = ["content=gallery-1"],
  kitProps =
    ["Layout type=Title Card 1", "Style=Tonal", "Content type=Gallery 1", "Interactive=Yes"],
)
@OverrideVariant(
  name = "gallery-2",
  strings = ["content=gallery-2"],
  kitProps =
    ["Layout type=Title Card 1", "Style=Tonal", "Content type=Gallery 2", "Interactive=Yes"],
)
@OverrideVariant(
  name = "with-subtitle-content-image",
  strings = ["layout=title-time-subtitle", "content=image"],
  kitProps = ["Layout type=Title Card 2", "Style=Tonal", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-gallery-1",
  strings = ["layout=title-time-subtitle", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card 2", "Style=Tonal", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-gallery-2",
  strings = ["layout=title-time-subtitle", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card 2", "Style=Tonal", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
// THE KIT'S THIRD LAYOUT ON THIS FUNCTION. `Title Card 3` is a title over a subtitle with no body,
// and it used to ship as `TitleCard/Subtitle`, a component of its own. The Wear sibling has always
// carried it here, under this exact name (`@OverrideVariant(name = "title-and-subtitle", …)`), and
// two sheets that spell one component two ways cannot be read side by side.
//
// `kitProps` rather than the `reference` the component carried, because a cell resolves through the
// index by its property assignment: `39569:49145` is named
// `Layout type=Title Card 3, Style=Tonal, Content type=Text, Interactive=Yes`, and those four are
// transcribed from the index rather than guessed — a variant whose props miss resolves to nothing,
// which is worse than the component it replaced.
//
// The render still will NOT match the node, and that is deliberate and unchanged: `RemoteTitleCard`
// has no argument that puts the timestamp beside the title. That divergence is what the reference
// exists to surface; withholding it left nine cells reading as nobody's work.
@OverrideVariant(
  name = "title-and-subtitle",
  strings = ["layout=title-subtitle"],
  kitProps = ["Layout type=Title Card 3", "Style=Tonal", "Content type=Text", "Interactive=Yes"],
)
// `Title Card 3` crossed with the set's `Content type` axis — three more of its cells. The kit
// varies content on this layout exactly as it does on `Title Card 1` and `2`, so these select the
// same `cardImagery()` the cells below do, on the layout the cell above selects. #202 drew only the
// `Text` crossing and left these, which is why the set stood at 17 of 45 rather than 20.
@OverrideVariant(
  name = "title-and-subtitle-content-image",
  strings = ["layout=title-subtitle", "content=image"],
  kitProps = ["Layout type=Title Card 3", "Style=Tonal", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "title-and-subtitle-gallery-1",
  strings = ["layout=title-subtitle", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card 3", "Style=Tonal", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "title-and-subtitle-gallery-2",
  strings = ["layout=title-subtitle", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card 3", "Style=Tonal", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
// The `Style=Outline` column. One probe first, on the base layout: if the library's outlined
// palette resolves to the same container the tonal one does, these renders are byte-identical to
// the cells above and `RemoteRenderTest`'s duplicate guard says so — which would mean the axis has
// no call site after all and these belong with the withheld ones.
//
// `outlined`, NOT `outline` — the knob value and the cell names both, here and on `AppCardRemote`
// and in `KitCardStyles`. It is the Wear sheet's spelling (`TitleCardStyle.Outlined`,
// `AppCardStyle.Outlined`) and already this sheet's own everywhere else: the buttons, the edge
// buttons and the compact buttons all spell their outlined cells `outlined`, and only the cards
// carried the short form. Two spellings of one axis value cost nothing while a shared kit node
// carries the pairing, and cost the whole row where there is none: the `Title Card 3` crossings
// below name no node on either column, so `style` matching literally is the only thing that pairs
// them ([#292](https://github.com/yschimke/wear-m3-catalog/issues/292)).
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitProps = ["Layout type=Title Card 1", "Style=Outline", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-content-image",
  strings = ["style=outlined", "content=image"],
  kitProps = ["Layout type=Title Card 1", "Style=Outline", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-gallery-1",
  strings = ["style=outlined", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card 1", "Style=Outline", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-gallery-2",
  strings = ["style=outlined", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card 1", "Style=Outline", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-outlined",
  strings = ["style=outlined", "layout=title-time-subtitle"],
  kitProps = ["Layout type=Title Card 2", "Style=Outline", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-outlined-content-image",
  strings = ["style=outlined", "layout=title-time-subtitle", "content=image"],
  kitProps = ["Layout type=Title Card 2", "Style=Outline", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-outlined-gallery-1",
  strings = ["style=outlined", "layout=title-time-subtitle", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card 2", "Style=Outline", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-outlined-gallery-2",
  strings = ["style=outlined", "layout=title-time-subtitle", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card 2", "Style=Outline", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
// The `Title Card 3` outline crossings. A cell that names no kit node pairs by NAME and seeds or
// not at all, so these four are the ones the spelling actually decides, and `:catalog` publishes
// them under exactly these names.
//
// LAYOUT, THEN STYLE, THEN CONTENT — the Wear sheet's ordering, which is this file's now too. These
// four landed style-first when both columns gained them together, which made them the one card
// family agreeing with each other and with nothing else.
@OverrideVariant(
  name = "title-and-subtitle-outlined",
  strings = ["style=outlined", "layout=title-subtitle"],
  kitProps = ["Layout type=Title Card 3", "Style=Outline", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "title-and-subtitle-outlined-content-image",
  strings = ["style=outlined", "layout=title-subtitle", "content=image"],
  kitProps = ["Layout type=Title Card 3", "Style=Outline", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "title-and-subtitle-outlined-gallery-1",
  strings = ["style=outlined", "layout=title-subtitle", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card 3", "Style=Outline", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "title-and-subtitle-outlined-gallery-2",
  strings = ["style=outlined", "layout=title-subtitle", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card 3", "Style=Outline", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
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
// `Style=Background Image` IS A LANE QUESTION, and the answer lives in
// `RemoteCardBackgroundImage.kt` — one file per lane, same declarations, different bodies.
//
// alpha10 publishes no painter parameter on any card, so on the released lane the style has no call
// site and the annotation below contributes no cells; a cell mapped to a node that lane cannot draw
// would be worse than none ([#157](https://github.com/yschimke/wear-m3-catalog/issues/157)). Since
// that release `RemoteTitleCard` has gained a `containerPainter` overload, so the snapshot lane
// draws three of the kit's five image-backed cells — the three that are this component's layouts.
// The other two are `RemoteAppCard`'s and it gained nothing; the Wear sibling is missing the same
// two for the same reason.
@RemoteCardBackgroundImageCells
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
  // All three of the kit's numbered layouts are cells on THIS function now. `Title Card 1` is the
  // base: title, time and content. `Title Card 2` adds the subtitle under the body. `Title Card 3`
  // has no body at all — `RemoteTitleCard` cannot arrange the timestamp beside the title, so the
  // closest it draws is a title over a subtitle, which is what `title-subtitle` selects.
  val layout =
    previewOverrideChoice(
      "layout",
      "title-time",
      listOf("title-time", "title-time-subtitle", "title-subtitle"),
    )
  val titleOverSubtitle = layout == "title-subtitle"
  // THE KIT'S `Style` AXIS, and the cell it produces will not match — deliberately. The kit crosses
  // every layout with `Outline`, and `remote-material3` splits that across two functions it does
  // not join: `RemoteOutlinedCard` has the border but one content slot and no title, while
  // `RemoteTitleCard` has the slots and NO border parameter at all — `RemoteCardColors` carries
  // container, content, appName, time, title and subtitle, and no stroke among them.
  //
  // So this passes the library's own `outlinedCardColors()`, which is the call site that exists,
  // and the render comes out with the outlined palette and no outline. That is the finding, and
  // drawing it is the rule this repo works to: a cell whose API exists is drawn failing rather than
  // withheld. It is a different case from `Style=Background Image` below, which is withheld because
  // no painter parameter exists anywhere to call.
  //
  // The list of values is the LANE's (`KitCardStyles`), because `image` is only drawable on one of
  // them, and the call goes through `KitTitleCard` for the same reason: on the snapshot lane that
  // style is a different `RemoteTitleCard` overload rather than a different argument.
  val style = previewOverrideChoice("style", "tonal", KitCardStyles)
  KitTitleCard(
    onClick = onClick,
    style = style,
    modifier = RemoteModifier.width(KitRowWidth),
    title = { RemoteText(title) },
    // `Title Card 3` carries no timestamp slot the library can fill beside the title, and no body,
    // so this cell drops both rather than drawing the base cell's furniture around a subtitle.
    time = if (titleOverSubtitle) null else ({ RemoteText(KitCopy.TIMESTAMP.rs) }),
    // `Title Card 2`'s subtitle sits UNDER the body, which is where `RemoteTitleCard` draws its
    // `subtitle` slot. The base cell leaves it empty; `Title Card 3` is the subtitle alone.
    subtitle =
      if (layout == "title-time-subtitle" || titleOverSubtitle)
        ({ RemoteText(KitCopy.SUBTITLE.rs) })
      else null,
    // `Title Card 3` carries a `Content type` axis like the other layouts — the kit crosses it with
    // Image, Gallery 1 and Gallery 2 — so the imagery goes in the content slot here too. What it
    // does NOT get is the body TEXT: `Content type=Text` on this layout is the title-over-subtitle
    // picture with nothing under it, which is why the fallback is null on this branch and the body
    // string on the others.
    content =
      cardImagery() ?: if (titleOverSubtitle) null else ({ RemoteText(KitCopy.CARD_CONTENT.rs) }),
  )
}

@OverrideVariant(
  name = "content-image",
  strings = ["content=image"],
  kitProps = ["Layout type=App Card", "Style=Tonal", "Content type=Image", "Interactive=Yes"],
)
@OverrideVariant(
  name = "gallery-1",
  strings = ["content=gallery-1"],
  kitProps = ["Layout type=App Card", "Style=Tonal", "Content type=Gallery 1", "Interactive=Yes"],
)
@OverrideVariant(
  name = "gallery-2",
  strings = ["content=gallery-2"],
  kitProps = ["Layout type=App Card", "Style=Tonal", "Content type=Gallery 2", "Interactive=Yes"],
)
// THE KIT'S SECOND LAYOUT ON THIS FUNCTION. `App Card` and `Title Card + Icon` differ only in what
// sits in the leading slot — the app's own square artwork on one, a vector icon on the other — and
// `RemoteAppCard` spells both as `appImage`. So the kit axis is a knob here rather than a second
// component, exactly as it is on the Wear column's `ApplicationCard`. Nine of the set's cells had
// no component naming them at all before this
// ([#160](https://github.com/yschimke/wear-m3-catalog/issues/160)).
@OverrideVariant(
  name = "icon",
  strings = ["appImage=icon"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Tonal", "Content type=Text", "Interactive=Yes"],
)
@OverrideVariant(
  name = "icon-content-image",
  strings = ["appImage=icon", "content=image"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Tonal", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-gallery-1",
  strings = ["appImage=icon", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Tonal", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-gallery-2",
  strings = ["appImage=icon", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Tonal", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
// THE EMPTY LEADING SLOT, folded on. It used to be `AppCard/NoAppImage`, a component of its own —
// but the slot it varies is `appImage`, the same argument the two cells above turn, so it is a
// third value of that knob rather than a second card for one function.
//
// It stays a STATED ABSENCE and does not name a node; the reason is unchanged from the component's
// own `noReference`, and #197 is why it is worth restating rather than dropping. The kit's leading
// slot is always FILLED — the app's square artwork on its `App Card` cells, a vector icon on its
// `Title Card + Icon` ones — and never empty, which is what this draws. Both of those layouts are
// already drawn, by the base cell and by `icon` respectively, so there is no unclaimed node left
// for an empty slot to name. It briefly carried `46048:69274` (#194) and that was a double-claim
// on `icon`'s node: two renders on one node means one is scored against nothing while reading as
// mapped.
@OverrideVariant(name = "no-app-image", strings = ["appImage=none"], secondary = true)
// The `Style=Outline` column for this function's two layouts. Same call site and same
// expected divergence as `TitleCardRemote`'s: the palette lands, the stroke does not.
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitProps = ["Layout type=App Card", "Style=Outline", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-content-image",
  strings = ["style=outlined", "content=image"],
  kitProps = ["Layout type=App Card", "Style=Outline", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-gallery-1",
  strings = ["style=outlined", "content=gallery-1"],
  kitProps = ["Layout type=App Card", "Style=Outline", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "outlined-gallery-2",
  strings = ["style=outlined", "content=gallery-2"],
  kitProps = ["Layout type=App Card", "Style=Outline", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-outlined",
  strings = ["style=outlined", "appImage=icon"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Outline", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-outlined-content-image",
  strings = ["style=outlined", "appImage=icon", "content=image"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Outline", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-outlined-gallery-1",
  strings = ["style=outlined", "appImage=icon", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Outline", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-outlined-gallery-2",
  strings = ["style=outlined", "appImage=icon", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Outline", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
@CatalogComponent(
  id = "AppCard",
  group = "Containment",
  parallel = "AppCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5712",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "App card with app name, icon and content slots.",
)
@CatalogRemoteLarge
// NO `Style=Background Image` CELL, and it is the library rather than this file. The kit crosses
// its `Card` layouts with an image-backed style — five of the set's forty-five cells — and at
// `remote-material3-1.0.0-alpha10` neither `RemoteCardKt` nor `RemoteCardDefaults` exposes a
// painter or container-painter parameter: `RemoteCard`, `RemoteTitleCard` and `RemoteAppCard` take
// colours and shapes only, checked against the published API jar. So there is no call site to draw
// those cells from, and a cell mapped to a node this cannot draw would be worse than none
// ([#157](https://github.com/yschimke/wear-m3-catalog/issues/157)).
//
// The painter is not the gap — `remoteContainerPainter(RemoteImageBitmap, …)` exists as a free
// function and `RemoteButton` takes one. But it does not draw either: a `RemoteButton` handed
// `RemoteButtonDefaults.containerPainter(CatalogRemoteImage.bitmap())` renders an opaque black
// pill, with the image absent, under the default scrim, a transparent scrim,
// `ContentScale.FillBounds`, and a bitmap 64x larger. `RemoteImage` draws that same bitmap in the
// content slots above, so it is the container painter specifically. Recorded on #157; revisit when
// either half moves.
@Composable
fun AppCardRemote() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  // The kit's `Style` axis, on the same terms as `TitleCardRemote` above: `RemoteAppCard` takes
  // colours and no border, so the outlined cells draw the outlined palette with no stroke, and the
  // missing stroke is the finding rather than a reason to withhold them.
  val outlined = previewOverrideChoice("style", "tonal", listOf("tonal", "outlined")) == "outlined"
  RemoteAppCard(
    onClick = onClick,
    modifier = RemoteModifier.width(KitRowWidth),
    colors =
      if (outlined) RemoteCardDefaults.outlinedCardColors() else RemoteCardDefaults.cardColors(),
    appName = { RemoteText(KitCopy.APP_LABEL.rs) },
    title = { RemoteText(title) },
    // The kit's App Card cell fills its timestamp slot, and so does `wear-m3-catalog`'s `AppCard`.
    // Leaving it empty here dropped the one thing that tells an app card from a title card at a
    // glance, on the row scored against that cell.
    time = { RemoteText(KitCopy.TIMESTAMP.rs) },
    // The base render draws the app's square ARTWORK, because that is what the kit's `App Card`
    // cell — this row's `reference` — puts in the slot; the icon is the `Title Card + Icon` cell
    // beside it. Drawing the icon under the App Card node claimed one layout and pictured the
    // other. The artwork is the same flat placeholder the content slots take: the kit publishes it
    // as an empty `IMAGE` fill there too.
    appImage =
      when (previewOverrideChoice("appImage", "image", listOf("image", "icon", "none"))) {
        "icon" -> ({ RemoteIcon(starIcon, null, modifier = RemoteModifier.size(16.rdp)) })
        // NOT a kit cell, and the `none` cell's note below says why: the kit's leading slot is
        // always filled. This draws the empty one because `RemoteAppCard` allows it, which is the
        // library's shape rather than the kit's.
        "none" -> null
        // The kit's `App Card` cell fills this slot with an app avatar, so it takes the ARTWORK
        // stand-in rather than `imageFrame`'s empty-slot placeholder — the same split
        // `:catalog` draws between `CatalogArtwork` and `CatalogImage`, and the same picture. A
        // white square under a slot the kit fills is not a closer answer than a coloured one.
        else -> ({
            RemoteImage(
              CatalogRemoteImage.artwork(),
              null,
              modifier = RemoteModifier.size(16.rdp).clip(RemoteRoundedCornerShape(4.rdp)),
              contentScale = ContentScale.FillBounds,
            )
          })
      },
    content = cardImagery() ?: ({ RemoteText(KitCopy.CARD_CONTENT.rs) }),
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

// The rows `wear-m3-catalog`'s `WearScaffold` draws, and drawn here for that reason alone.
//
// This screen used to be its own invention — a pair of TitleCards reading "Morning run / 5.2 km"
// and "Heart rate / 72 bpm" against the sibling's four "Row n" list headers — so the compare page's
// hero row put two unrelated pictures side by side and scored the difference
// ([#294](https://github.com/yschimke/wear-m3-catalog/issues/294)). The kit publishes no scaffold,
// which makes the sibling the only reference this row has, and the same bargain `ButtonGroup` and
// `KitCopy` strike applies: where there is no kit copy to quote, quote the sibling.
private val screenRows = (1..4).map { "Row $it" }

// `Scaffold`, as the Wear sibling names it. It shipped as `Template/WatchScreen` and had to be
// paired across by `parallel`, which is the tell: two sheets naming one component two things is
// exactly what stops the compare page's columns lining up on their own. NOT a fold — this stays a
// top-level component carrying the `noReference` below, because it is a render the kit publishes no
// cell for and only a component can say so.
//
// THE BREAKPOINT MISMATCH ON THIS ROW IS DELIBERATE, written down here so it is not re-derived as a
// defect ([#292](https://github.com/yschimke/wear-m3-catalog/issues/292) asked for the answer
// rather than a change). The compare page pairs `scaffold__ideal__default__compact` — 454x454 —
// against the sibling's `scaffold__ideal__default__192dp` at 384x384, because
// [CatalogRemoteScreen] pins ONE 227dp canvas where `:catalog`'s `CatalogFullScreenModes` fans the
// kit's five round breakpoints, and the pairing falls back to matching the cell with the size axis
// dropped. That is the arrangement AGENTS.md describes — the breakpoint segment is the one thing
// that does not converge between the sheets — and both frames are the `largeRound` end of the same
// range, so the row compares the same screen at two sizes rather than two different screens.
@CatalogComponent(
  id = "Scaffold",
  group = "Scaffold templates",
  parallel = "Scaffold",
  noReference =
    "The kit publishes no `Scaffold` set — the Wear sibling carries its own counterpart under " +
      "`noReference` for the same reason, so there is no node to compare either rendition " +
      "against.",
  caption =
    "Full-screen Remote Compose watch screen — a status clock over the sibling's four list rows, " +
      "on the theme's own background fill. The catalog's hero: a RemoteDocument driving a whole " +
      "surface, not a single sticker. No scroll indicator: the Wear column draws one down the " +
      "right bezel and remote-material3 publishes no scroll-position component to draw it with.",
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
        screenRows.forEach { row ->
          RemoteText(row.rs, style = RemoteMaterialTheme.typography.titleMedium)
        }
      }
    },
  )
}

// ---------------------------------------------------------------------------
// Communication — the determinate circular progress indicator at a fixed 66%, so the
// static capture is deterministic (the indeterminate overload animates off the
// document clock). Named `CircularProgressIndicator`, as the Wear sibling names it.
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
// The kit's `Progress` axis, which is the `progress` knob this row already carries — three more of
// the set's cells for three seeded floats
// ([#160](https://github.com/yschimke/wear-m3-catalog/issues/160)). The cell names and the values
// are the Wear sibling's, so the two columns pair.
@OverrideVariant(
  name = "complete",
  floats = ["progress=1.0"],
  kitAxis = "Progress",
  kitValue = "Complete",
)
@OverrideVariant(
  name = "overflow",
  floats = ["progress=1.4"],
  kitAxis = "Progress",
  kitValue = "Overflow",
)
@OverrideVariant(
  name = "zero",
  floats = ["progress=0.0"],
  kitAxis = "Progress",
  kitValue = "Zero",
)
// The crossing of the two, and the last cell of this set that has a call site. It was withheld as
// an EMPTY FRAME — a disabled ring draws entirely in its disabled track colour, and at zero
// progress that used to leave nothing lit — which is the same reason the Wear sibling gave, and
// which turned out to have expired there
// ([#178](https://github.com/yschimke/wear-m3-catalog/issues/178)).
// A gap held open by a library limitation is worth re-rendering rather than re-reading, so this
// one is drawn and the renderer decides: `StickerBakeCoverageTest` fails it if it really is blank
// and nothing exempts it.
@OverrideVariant(
  name = "zero-disabled",
  booleans = ["enabled=false"],
  floats = ["progress=0.0"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Medium",
      "Progress=Zero",
      "Dot value=No",
      "Disabled=Yes",
    ],
  secondary = true,
)
// The kit publishes four determinate `Progress=` values and no indeterminate one, but the library
// ships both on the same name — so it folds in here as a cell rather than standing up a second
// card for the same component. It used to be `CircularProgressIndicator-Indeterminate`, a
// top-level component; the Wear sibling has carried it as this cell all along, and two sheets that
// spell one component two ways cannot be read side by side.
//
// The cell is a still of something that only reads as itself in motion, which is what
// `motionPreview` on the component below is for.
@OverrideVariant(name = "indeterminate", strings = ["mode=indeterminate"])
// The kit's `Stroke Width` axis, and the second half of every `Progress`/`Disabled` cell above.
// These six are the Wear sibling's `small-stroke*` cells under the same names and the same seeded
// values, so the two columns pair cell for cell and this set stops being one the Remote sheet is
// behind on.
//
// WHERE THE NUMBER COMES FROM, because that is the whole question here. The kit's `Stroke
// Width=Small | Medium` is a pair of dp values, and `RemoteProgressIndicatorDefaults` publishes
// neither — it carries `IndeterminateStrokeWidth` and the `CurvedIndicator*` family and nothing for
// the determinate ring, so the argument has no token on the Remote side to spell it with. Writing a
// literal here would be exactly what the Wear sibling refuses to do for the same set's `Type=Top
// Gap` cells: "an invented number under the kit's name is worse than an honest absence."
//
// It does not come to that. `androidx.wear.compose:compose-material3` publishes
// `CircularProgressIndicatorDefaults.smallStrokeWidth` / `largeStrokeWidth` — where the Wear
// sibling reads the same two values — and `remote-material3`'s POM already depends on it at compile
// scope — so this is not a number invented for the Remote sheet, it is the same named token both
// sheets draw, resolved by the same library. (Both tokens branch on `isSmallScreen`, so they are
// screen-size dependent rather than constant; the capture frame decides, as it does on Wear.)
// `remote-catalog/build.gradle.kts` declares that dependency outright rather than leaning on a
// transitive an upstream POM could quietly demote.
@OverrideVariant(
  name = "small-stroke",
  strings = ["stroke=small"],
  kitAxis = "Stroke Width",
  kitValue = "Small",
)
@OverrideVariant(
  name = "small-stroke-complete",
  strings = ["stroke=small"],
  floats = ["progress=1.0"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=Complete",
      "Dot value=No",
      "Disabled=No",
    ],
  secondary = true,
)
@OverrideVariant(
  name = "small-stroke-overflow",
  strings = ["stroke=small"],
  floats = ["progress=1.4"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=Overflow",
      "Dot value=No",
      "Disabled=No",
    ],
  secondary = true,
)
@OverrideVariant(
  name = "small-stroke-zero",
  strings = ["stroke=small"],
  floats = ["progress=0.0"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=Zero",
      "Dot value=No",
      "Disabled=No",
    ],
  secondary = true,
)
@OverrideVariant(
  name = "small-stroke-disabled",
  booleans = ["enabled=false"],
  strings = ["stroke=small"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=In progress",
      "Dot value=No",
      "Disabled=Yes",
    ],
  secondary = true,
)
@OverrideVariant(
  name = "small-stroke-zero-disabled",
  booleans = ["enabled=false"],
  strings = ["stroke=small"],
  floats = ["progress=0.0"],
  kitProps =
    [
      "Type=Full",
      "Segments=1",
      "Stroke Width=Small",
      "Progress=Zero",
      "Dot value=No",
      "Disabled=Yes",
    ],
  secondary = true,
)
@CatalogComponent(
  id = "CircularProgressIndicator",
  group = "Communication",
  parallel = "CircularProgressIndicator",
  reference = "figma:B24oss2tTeXAFykyeyusz0/41424:58637",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/41424:58385",
  caption = "Determinate circular progress rail against the display edge, at a fixed 60%.",
  // The indeterminate sweep, recorded in ComponentVariantPreviews.kt. It cannot be annotated on
  // the variant itself: a motion annotation rides every `@OverrideVariant` cell and would publish
  // one recording under every progress name.
  motionPreview = "IndeterminateCircularProgressMotionRemote",
)
@CatalogRemoteDisplay
@Composable
fun CircularProgressRemote() = RemoteSticker {
  // The 0..1 fill is an editable `progress` float knob: the viewer's number field reseeds the arc
  // live (`rc.progress=float:<0..1>`) without re-capturing the document. 0.6 keeps the static
  // sticker deterministic AND is the value `wear-m3-catalog`'s `CircularProgressIndicator` pins,
  // so the two renditions of the same kit cell draw the same arc.
  // TWO override paths on one name, and the cells need both. `rememberOverridableRemoteFloat`
  // publishes `progress` as a document NAMED VALUE, which the viewer reseeds live
  // (`rc.progress=float:<0..1>`) without re-capturing — but a `@OverrideVariant` seeds a PREVIEW
  // override, which that call never reads, so the kit's `Progress` cells all baked the same 0.6
  // arc. Feeding the preview override in as the named value's default settles it: a cell picks the
  // value the document is built with, and the live path is untouched.
  val progress = rememberOverridableRemoteFloat("progress", previewOverrideFloat("progress", 0.6f))
  // The kit's `Stroke Width` axis, read off the Wear M3 tokens rather than off a literal — see the
  // `small-stroke*` cells above for why that distinction is the whole point. `medium` is the base
  // cell and maps to `largeStrokeWidth`, which is the same pairing the Wear sibling makes: the kit
  // names two widths where the library names two, and the library's "large" is the kit's "Medium"
  // because the kit publishes no wider one.
  val stroke =
    if (previewOverrideChoice("stroke", "medium", listOf("medium", "small")) == "small")
      CircularProgressIndicatorDefaults.smallStrokeWidth.asRdp()
    else CircularProgressIndicatorDefaults.largeStrokeWidth.asRdp()
  // `fillMaxSize`, not a 72dp box. The kit publishes this as a *display* cell — a ring struck 2dp
  // inside the bezel of the whole round face — which is why the sticker is on the
  // [CatalogRemoteDisplay] frame at all, and why the Wear sibling draws it `fillMaxSize` too. At
  // 72dp it was a small dial floating in the middle of a display-sized capture: a different
  // component from the one the row compares it against.
  //
  // That reaches the kit's 192dp cell only because [CatalogRemoteDisplay] is now based on the
  // 192dp round device. On the single 227dp frame it used to be, the same `fillMaxSize` published
  // the ring 35dp oversized with its stroke out where the bezel would be
  // ([#149](https://github.com/yschimke/wear-m3-catalog/issues/149)) — so the size here is the
  // frame's to state, and it states it.
  // The kit's `Disabled` axis. #125 declared the cell and never wired the knob, so the disabled
  // render was byte-identical to this one and scored against the kit's `Disabled=Yes` node while
  // drawing the enabled picture — a comparison that could not fail. The knob is read at
  // composition, so an unseeded render is the base cell, unchanged.
  if (
    previewOverrideChoice("mode", "determinate", listOf("determinate", "indeterminate")) ==
      "indeterminate"
  ) {
    // The indeterminate overload takes neither progress nor `enabled` — it is a different function
    // on the same name, and the knobs above simply do not reach it. Same shape as the Wear
    // sibling's branch.
    RemoteCircularProgressIndicator(modifier = RemoteModifier.fillMaxSize(), strokeWidth = stroke)
  } else {
    RemoteCircularProgressIndicator(
      progress = progress,
      enabled = previewOverrideBoolean("enabled", true).rb,
      modifier = RemoteModifier.fillMaxSize(),
      strokeWidth = stroke,
    )
  }
}

// ---------------------------------------------------------------------------
// Communication — the CURVED indicator, a different component from the ring above
// rather than a variant of it. Wear M3 parallel: `ArcProgressIndicator`.
// ---------------------------------------------------------------------------

/**
 * The arc struck along the bezel, drawn at a fixed 60%.
 *
 * **IT TAKES THE WEAR SHEET'S NAME, not the library's.** `remote-material3` spells this
 * `RemoteCurvedProgressIndicator` and `androidx.wear.compose.material3` spells it
 * `ArcProgressIndicator`. The two draw the same idea — an arc along the bezel for progress — so
 * this card takes the Wear spelling and pairs with it, on the vocabulary rule in AGENTS.md: a card
 * called `CurvedProgressIndicator` here beside `ArcProgressIndicator` there would pair with nothing
 * and leave both rows reading as one-sided, which is the translation-in-disguise #116 folded out of
 * this sheet.
 *
 * **NO KIT NODE, on both columns and for the same reason.** The kit's progress sets are the full
 * ring, the segmented ring and the linear track; it publishes no arc at all. The Wear sibling's
 * `ArcProgress` says exactly that in its own `noReference`, and this repeats that judgement rather
 * than re-deciding it.
 *
 * **WHERE THE PAIR DIFFERS, and it is API rather than defect.** Wear's `ArcProgressIndicator` takes
 * no `progress` — it is indeterminate only, which is why its caption reads "for a wait with no
 * measurable progress" and its sticker is a still of an animation. The Remote one takes `progress`
 * first and is DETERMINATE. The two therefore pair by role and by size but will never draw the same
 * picture; that difference is the two libraries offering different components under one idea, and
 * it is stated here rather than hidden behind a name of its own.
 *
 * **WHAT THE RENDER SHOWS, and it is the library's.** The arc is drawn as a HAIRLINE — one dp of
 * ink at density 2.0 — however it is asked for. `strokeWidth` is not ignored: 8dp and 24dp bake to
 * different bytes, and the arc's radius insets as the number grows, so the parameter reaches the
 * layout. What it does not reach is the stroke, which stays a hairline at the library's own
 * `RemoteProgressIndicatorDefaults.CurvedIndicatorStrokeWidth` and at every value tried. No
 * workaround is taken: a stroke invented at the call site would draw an arc this library does not
 * produce, under its name, which is the trade `CircularProgressRemote`'s stroke note refuses one
 * component over.
 *
 * No `@OverrideVariant` cells, because there is no kit set to have cells OF. The knobs are real
 * arguments a viewer can seed; they are simply not crossings of anything published.
 */
@CatalogComponent(
  id = "ArcProgressIndicator",
  group = "Communication",
  parallel = "ArcProgressIndicator",
  noReference =
    "The kit publishes no arc indicator: its progress sets are the full ring, the segmented ring " +
      "and the linear track. The Wear column says the same of its `ArcProgressIndicator`; this is " +
      "that component's Remote counterpart, determinate where Wear's is indeterminate.",
  caption =
    "A determinate arc along the bezel, at a fixed 60%. Remote draws it as a hairline whatever " +
      "stroke width it is given — see the KDoc.",
)
@CatalogRemoteLarge
@Composable
fun ArcProgressRemote() = RemoteSticker {
  // Seeded the way `CircularProgressRemote` seeds its ring, and at the same 0.6, so the sheet's
  // two determinate progress cards are read at one value rather than two arbitrary ones.
  val progress = rememberOverridableRemoteFloat("progress", previewOverrideFloat("progress", 0.6f))
  RemoteCurvedProgressIndicator(
    progress = progress,
    // 120dp is the Wear sibling's `Modifier.size(120.dp)` rather than a number chosen here: an arc
    // has no intrinsic size, so the two columns must be handed the same one or the comparison is
    // between two radii. It is also why this sticker is on [CatalogRemoteLarge] — 120dp does not
    // fit the 100dp-tall component frame — rather than on [CatalogRemoteDisplay], which would make
    // it a bezel rail on a 192dp face and stop it pairing with the Wear card's 120dp box.
    modifier = RemoteModifier.size(120.rdp),
    enabled = previewOverrideBoolean("enabled", true).rb,
  )
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

/**
 * **Both cells of the kit's `Text-Body` set.** The set has exactly one axis, `Alignment`, and
 * exactly two nodes; this component named the `Centre` one and nothing drew `Left`
 * ([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)).
 *
 * `kitAxis` / `kitValue` rather than `kitProps`, because `Alignment` is the set's ONLY axis: there
 * is no other assignment for the cell to also name, so the single-axis form resolves exactly. The
 * Wear sibling's `Text/Body` spells the same cell the same way.
 *
 * The axis needs a frame to be an axis at all. This drew `RemoteText(text)` unconstrained, so the
 * run sized itself to its own glyphs and `Left` and `Centre` were the same picture — the base cell
 * was not really drawing `Alignment=Centre`, it was drawing text with no alignment to speak of. The
 * width is `160.rdp`, matching what `wear-m3-catalog`'s `BodyText` gives the same string, so the
 * two renditions of each cell are compared at the same measure rather than differing by the frame
 * this sticker chose. (Its neighbour `Text/MaxLines-Truncated` keeps its narrower `150.rdp`: that
 * one exists to overflow, and the width is what makes it.)
 */
@CatalogComponent(
  id = "Text/Body",
  group = "Text",
  parallel = "Text/Body",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38977:66993",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38977:66990",
  caption = "The Remote Material 3 text primitive, with the kit's alignment axis folded in.",
)
@CatalogRemoteModes
@OverrideVariant(
  name = "left-aligned",
  strings = ["align=left"],
  kitAxis = "Alignment",
  kitValue = "Left",
)
@Composable
fun RemoteTextSticker() = RemoteSticker {
  // The kit's own `Text-Body` string, which is what the `Text/Body` parallel draws — here it flows
  // in full; the `Text/MaxLines-Truncated` sticker below clips the same string. Still an editable
  // `text` string knob, so the viewer can retype it live (`rc.text=<string>`).
  val text = rememberOverridableRemoteString("text", KitCopy.BODY)
  // `centre` is the default because `38977:66993` — the node this row names — is the kit's Centre
  // cell. The knob is read here at composition, so it turns the Compose call and never reaches the
  // RemoteDocument as state; an unseeded render is the base cell, unchanged.
  val align =
    if (previewOverrideChoice("align", "centre", listOf("centre", "left")) == "left")
      TextAlign.Start
    else TextAlign.Center
  // `bodyMedium` EXPLICITLY, because the sibling names it explicitly: `wear-m3-catalog`'s
  // `BodyText` passes `style = MaterialTheme.typography.bodyMedium` for this same string in this
  // same 160dp box. Left to `RemoteText`'s own default the two columns drew one string at two type
  // roles — 67px of ink against 54, a line height of ~33.5 against ~27, and a word wrapping early —
  // which the compare page reported as a layout difference on both cells of this row
  // ([#295](https://github.com/yschimke/wear-m3-catalog/issues/295)).
  RemoteText(
    text,
    modifier = RemoteModifier.width(160.rdp),
    style = RemoteMaterialTheme.typography.bodyMedium,
    textAlign = align,
  )
}

// The text primitive exercising the maxLines / overflow product on a narrow column —
// the Remote parallel of Wear M3's `Text/MaxLines-Truncated`. `RemoteText` carries the
// same `maxLines` + `overflow` knobs as Wear's `Text`.
@CatalogComponent(
  id = "Text/MaxLines-Truncated",
  group = "Text",
  // NO `parallel`. `TextComponents.kt` publishes `Text/Body` and `Text/Caption` and nothing else,
  // and `maxLines` / `overflow` appears nowhere in `:catalog` — so there is no truncation demo on
  // that column to pair with, and pointing this at `Text/Body` compared an ellipsis against body
  // text that flows in full and scored the ellipsis
  // ([#292](https://github.com/yschimke/wear-m3-catalog/issues/292)). Same reasoning as the
  // `noReference` below, one column over. Wear's `Text` carries the same two knobs, so a
  // counterpart is buildable there; until one exists this row is one-sided.
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
  // NO `parallel`, and for the same reason as `Text/MaxLines-Truncated` above: `Text/Body` was
  // comparing a named family against the generic one and reporting the face as divergence
  // ([#292](https://github.com/yschimke/wear-m3-catalog/issues/292)).
  //
  // There is no better target and there is not meant to be one: `:catalog` draws its faces through
  // `CatalogFonts.kt` and publishes its type ramp as a STYLES sheet rather than a component, which
  // `Theme.kt` states as a deliberate choice ("a token sheet is not a component"). This sheet's
  // `Typeface/Specimen` is one-sided for the same reason.
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
  // WEIGHT IS PINNED, and it has to be. These four ask for a family by name and nothing else, so
  // the weight comes from the ambient `RemoteMaterialTheme` type scale — which is 450, a weight
  // only a VARIABLE font can serve. Orbitron, Space Grotesk and JetBrains Mono are variable and
  // resolved it; **Lobster Two ships static 400 and 700**, so Google serves no 450 file for it and
  // the render died on `FontFallbackException` the moment a typography started being installed on
  // every capture (#174). A specimen whose job is to prove four named families RESOLVE must not
  // inherit a weight that decides whether they can.
  val specimenWeight = FontWeight.Normal
  RemoteColumn {
    RemoteText(
      "Orbitron".rs,
      fontSize = 22.rsp,
      fontWeight = specimenWeight,
      fontFamily = RemoteFontFamily.Named("google:Orbitron"),
    )
    RemoteText(
      "Lobster Two".rs,
      fontSize = 22.rsp,
      fontWeight = specimenWeight,
      fontFamily = RemoteFontFamily.Named("google:Lobster Two"),
    )
    RemoteText(
      "Space Grotesk".rs,
      fontSize = 22.rsp,
      fontWeight = specimenWeight,
      fontFamily = RemoteFontFamily.Named("google:Space Grotesk"),
    )
    RemoteText(
      "JetBrains Mono".rs,
      fontSize = 22.rsp,
      fontWeight = specimenWeight,
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
