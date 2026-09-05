@file:CatalogGroup(name = "Cards", section = "Containment")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardColors
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.OutlinedCard
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.KnobValue
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogArtwork
import ee.schimke.wearm3catalog.CatalogImage
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Card` set — `Layout type = App Card | Title Card + Icon | Title Card 1..3`, over
// `Style = Tonal | Outline | Background Image` and `Content type = Text | Image | Gallery 1 |
// Gallery 2`. 45 cells, and this file used to draw six of them
// ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
//
// WHICH COMPONENT DRAWS WHICH LAYOUT, because the kit's five layout names are three Compose
// functions and the correspondence is not one-to-one:
//
//  - `Title Card 1|2` are `TitleCard` with different slots filled — 1 is title + time + content, 2
//    adds the subtitle under the content — so they fold as a `layout` knob. `Title Card 3` moves
//    the TIMESTAMP to the bottom, under the subtitle, and `TitleCard` draws its `time` slot on the
//    title's row with no argument that moves it. Its nine cells are this set's remaining gap, and
//    the `title-and-subtitle` cell below is what Compose does have, published as itself rather
//    than against a node it is not a picture of.
//  - `App Card` and `Title Card + Icon` are BOTH `AppCard`. The kit's two cells differ only in what
//    sits in the leading slot — an app's square artwork, or a vector icon — and Compose spells both
//    as `appImage`. So that axis folds as a knob on `AppCard` rather than splitting a component.
//  - `Style=Outline` does NOT split a component here, and that is a correction. `OutlinedCard` is
//    the title-less outlined card, and the kit publishes no title-less cell at all — every one of
//    its 45 has a title. What the kit's outline column actually draws is an outlined TITLE card and
//    an outlined APP card, which Compose spells as `colors = outlinedCardColors()` plus
//    `border = outlinedCardBorder()` on those two functions. So it is a cell on each.
//
// Which leaves `Card` and `OutlinedCard` — the two title-less primitives — carrying no kit node.
// They are real Wear Compose components and stay in the inventory through the second door
// (AGENTS.md), with the reason stated on each. They used to name `Title Card 1`, a cell they are
// not a picture of: it draws a title, a timestamp and a body, and they draw a line of text.
//
// The `Background Image` style is the `containerPainter` overload, with the scrim
// `CardDefaults.containerPainter` applies — the scrim is most of what the style is. The kit draws
// that column at `Content type=Text` only, and never on an app card; `AppCard` has no painter
// overload either way, so those two cells have no call site.
//
// The image itself is drawn rather than shipped; see `CatalogImage`. What `Content type` puts in
// the content slot is sample data in exactly that sense — the kit's own cells draw empty
// placeholder frames there — while the card, its slots and its scrim are the real component.
//
// A card is a full-width component on a watch, so the sticker pins a width rather than letting the
// crop decide one: at the measuring bound a card would size to the largest round screen and the
// published sticker would be wider than the small-round device it has to fit.

private val CardWidth = 180.dp

private val GallerySlotWidth = 148.dp
private val GalleryGap = 4.dp

@Composable
private fun galleryRow(height: Dp, leadWidth: Dp, trailingWidth: Dp, shape: Shape) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(GalleryGap),
    modifier = Modifier.width(GallerySlotWidth).height(height),
  ) {
    Image(
      painter = CatalogImage,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.width(leadWidth).height(height).clip(shape),
    )
    Image(
      painter = CatalogImage,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.width(trailingWidth).height(height).clip(shape),
    )
  }
}

/**
 * The kit's `Content type` axis, as what the card's content slot holds.
 *
 * `Text` is the body copy; the other three are imagery. The galleries use the kit's unequal 86/58
 * frames in opposite orders; Gallery 2 is also shorter and pill-shaped. [bodyText] is null on the
 * one layout the kit draws without body copy (`Title Card 3`), and its `Text` cell is then an empty
 * slot rather than a line of text.
 */
@Composable
private fun cardContent(bodyText: String?): (@Composable () -> Unit)? =
  when (
    previewOverrideChoice("content", "text", listOf("text", "image", "gallery-1", "gallery-2"))
  ) {
    "image" -> {
      {
        Image(
          painter = CatalogImage,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.width(GallerySlotWidth).height(64.dp).clip(RoundedCornerShape(14.dp)),
        )
      }
    }
    "gallery-1" -> {
      {
        galleryRow(
          height = 64.dp,
          leadWidth = 86.dp,
          trailingWidth = 58.dp,
          shape = RoundedCornerShape(14.dp),
        )
      }
    }
    "gallery-2" -> {
      {
        galleryRow(
          height = 58.dp,
          leadWidth = 58.dp,
          trailingWidth = 86.dp,
          shape = CircleShape,
        )
      }
    }
    else -> bodyText?.let { text -> { Text(text) } }
  }

/** The kit's `Style` axis where it is a colour pair rather than a different function. */
@Composable
private fun cardColorsFor(style: TitleCardStyle): CardColors =
  when (style) {
    TitleCardStyle.Outlined -> CardDefaults.outlinedCardColors()
    TitleCardStyle.Image -> CardDefaults.cardWithContainerPainterColors()
    TitleCardStyle.Tonal -> CardDefaults.cardColors()
  }

/**
 * The same mapping for the app card, which publishes no image style.
 *
 * Two overloads rather than one enum covering both: a shared `CardStyle` would offer `image` on the
 * app card's picker, and a control that offers a value the component does not take is the thing the
 * closed set exists to prevent.
 */
@Composable
private fun cardColorsFor(style: AppCardStyle): CardColors =
  when (style) {
    AppCardStyle.Outlined -> CardDefaults.outlinedCardColors()
    AppCardStyle.Tonal -> CardDefaults.cardColors()
  }

/** The kit's numbered title-card layouts, named for the slots each fills. */
enum class TitleCardLayout {
  @KnobValue("title-time") TitleTime,
  @KnobValue("title-time-subtitle") TitleTimeSubtitle,
  @KnobValue("title-subtitle") TitleSubtitle,
}

/** The kit's `Style` axis for the title card. */
enum class TitleCardStyle {
  @KnobValue("tonal") Tonal,
  @KnobValue("outlined") Outlined,
  @KnobValue("image") Image,
}

/** The kit's `Style` axis for the app card — no image style; the kit does not publish one. */
enum class AppCardStyle {
  @KnobValue("tonal") Tonal,
  @KnobValue("outlined") Outlined,
}

/** The kit's two leading-slot treatments: the app's artwork, or a glyph. */
enum class AppCardImage {
  @KnobValue("image") Image,
  @KnobValue("icon") Icon,
}

/** The kit's `Style` axis for the plain card: a tonal container, or an image behind it. */
enum class PlainCardStyle {
  @KnobValue("tonal") Tonal,
  @KnobValue("image") Image,
}

@CatalogComponent(
  id = "Card",
  noReference =
    "The kit's `Card` set publishes no title-less cell — all 45 of them draw a title, a " +
      "timestamp and a body. This is Wear Compose's plain container, the one-slot card the " +
      "titled ones are built on, and the kit has no picture of it. Its titled cells are on " +
      "`TitleCard` and `AppCard`.",
  caption =
    "The plain container: one content slot, tonal by default. A no-`onClick` overload presents " +
      "the same card without the tap.",
)
@CatalogModes
@OverrideVariant(name = "background-image", strings = ["style=image"])
// The no-`onClick` overload — a card that presents rather than acts — is NOT a cell here. It is
// pixel-identical to this one at rest: what differs is whether a tap does anything, which a still
// cannot show, so a cell would publish the same picture under a second name. It is documented in
// the caption instead.
@Composable
fun PlainCard(style: PlainCardStyle = PlainCardStyle.Tonal) = Sticker {
  val c = counted(kitCopy("content", KitCopy.CARD_CONTENT))
  if (style == PlainCardStyle.Image) {
    Card(
      onClick = c.onClick,
      containerPainter = CardDefaults.containerPainter(image = CatalogImage),
      modifier = Modifier.width(CardWidth),
      colors = CardDefaults.cardWithContainerPainterColors(),
    ) {
      Text(c.label)
    }
  } else {
    Card(onClick = c.onClick, modifier = Modifier.width(CardWidth)) { Text(c.label) }
  }
}

@CatalogComponent(
  id = "Card/Outlined",
  noReference =
    "The same absence `Card` states: the kit's outline column draws outlined TITLE and APP " +
      "cards, which Compose spells as `outlinedCardColors()` and `outlinedCardBorder()` on those " +
      "functions and which ride as cells there. `OutlinedCard` is the title-less outlined " +
      "container, and the kit publishes no title-less cell.",
  caption = "The kit's outline style — the same container drawn as a border.",
)
@CatalogModes
@Composable
fun OutlineCard() = Sticker {
  val c = counted(kitCopy("content", KitCopy.CARD_CONTENT))
  OutlinedCard(onClick = c.onClick, modifier = Modifier.width(CardWidth)) { Text(c.label) }
}

/**
 * **Every `Card` cell drawn by `TitleCard`** — the kit's first two numbered layouts crossed with
 * `Style` and `Content type`, 18 of the set's 45 nodes
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)). The component drew three.
 *
 * The kit's `Background Image` column is `Content type=Text` only, so those cells are two rather
 * than eight — a product would ask for six nodes it never drew.
 *
 * `Title Card 3` is not drawn: see the cell at the end of this list for what its layout needs and
 * what `TitleCard` gives instead.
 */
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
  name = "outlined",
  strings = ["style=outlined"],
  kitProps = ["Layout type=Title Card 1", "Style=Outline", "Content type=Text", "Interactive=Yes"],
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
  name = "background-image",
  strings = ["style=image"],
  kitProps =
    [
      "Layout type=Title Card 1",
      "Style=Background Image",
      "Content type=Text",
      "Interactive=Yes",
    ],
)
@OverrideVariant(
  name = "with-subtitle",
  strings = ["layout=title-time-subtitle"],
  kitProps = ["Layout type=Title Card 2", "Style=Tonal", "Content type=Text", "Interactive=Yes"],
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
@OverrideVariant(
  name = "with-subtitle-outlined",
  strings = ["layout=title-time-subtitle", "style=outlined"],
  kitProps = ["Layout type=Title Card 2", "Style=Outline", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-outlined-content-image",
  strings = ["layout=title-time-subtitle", "style=outlined", "content=image"],
  kitProps = ["Layout type=Title Card 2", "Style=Outline", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-outlined-gallery-1",
  strings = ["layout=title-time-subtitle", "style=outlined", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card 2", "Style=Outline", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-outlined-gallery-2",
  strings = ["layout=title-time-subtitle", "style=outlined", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card 2", "Style=Outline", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "with-subtitle-background-image",
  strings = ["layout=title-time-subtitle", "style=image"],
  kitProps =
    [
      "Layout type=Title Card 2",
      "Style=Background Image",
      "Content type=Text",
      "Interactive=Yes",
    ],
  secondary = true,
)
// NOT a kit cell, and the only cell here that names none. `Title Card 3` is the kit's third
// layout — title, then the content, then the subtitle with the TIMESTAMP UNDER IT — and
// `TitleCard` draws its `time` slot on the title's row, full stop. There is no argument that moves
// it. What is left after taking the time away is this: the arrangement Compose does have for a
// card that leads with a title and a subtitle, published as a cell of its own rather than mapped
// to a node it is not a picture of. The kit's nine `Title Card 3` cells are the set's remaining
// gap.
@OverrideVariant(name = "title-and-subtitle", strings = ["layout=title-subtitle"])
annotation class TitleCardKitCells

@CatalogComponent(
  id = "TitleCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5747",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "A card led by a title, with the kit's numbered layouts folded in as cells.",
)
@CatalogModes
@TitleCardKitCells
@Composable
fun TitledCard(
  layout: TitleCardLayout = TitleCardLayout.TitleTime,
  style: TitleCardStyle = TitleCardStyle.Tonal,
) = Sticker {
  val c = counted(kitCopy("title", KitCopy.CARD_TITLE))
  val time: @Composable () -> Unit = { Text(kitCopy("time", KitCopy.TIMESTAMP)) }
  val subtitle: (@Composable ColumnScope.() -> Unit)? =
    if (layout == TitleCardLayout.TitleTime) null
    else {
      { Text(kitCopy("subtitle", KitCopy.SUBTITLE)) }
    }
  // `Title Card 3` is the layout with no body: its subtitle and time sit straight under the title.
  val content =
    cardContent(
      if (layout == TitleCardLayout.TitleSubtitle) null
      else kitCopy("content", KitCopy.CARD_CONTENT)
    )
  if (style == TitleCardStyle.Image) {
    TitleCard(
      title = { Text(c.label) },
      containerPainter = CardDefaults.containerPainter(image = CatalogImage),
      onClick = c.onClick,
      time = time,
      subtitle = subtitle,
      colors = CardDefaults.cardWithContainerPainterColors(),
      modifier = Modifier.width(CardWidth),
      content = content,
    )
  } else {
    TitleCard(
      title = { Text(c.label) },
      onClick = c.onClick,
      time = time,
      subtitle = subtitle,
      colors = cardColorsFor(style),
      // The outline is a `border`, not a colour — the same trap the button pages carry a note
      // about. Without it an outlined card is the tonal one's picture with a transparent
      // container, which is not what the kit's outline column draws.
      border = if (style == TitleCardStyle.Outlined) CardDefaults.outlinedCardBorder() else null,
      modifier = Modifier.width(CardWidth),
      content = content,
    )
  }
}

/**
 * **Every `Card` cell drawn by `AppCard`** — 16 of the set's 45 nodes, against the one this drew
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)).
 *
 * `Layout type=Title Card + Icon` is this component, not `TitleCard`: the kit's two cells differ
 * only in what sits in the leading slot — an app's square artwork on `App Card`, a vector icon on
 * `Title Card + Icon` — and Compose spells both as the `appImage` slot of one function. So the kit
 * axis is a knob here rather than a second component.
 *
 * The two `Background Image` cells are absent because `AppCard` has no `containerPainter` overload;
 * the painter-backed card is `Card` and `TitleCard` only.
 */
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
@OverrideVariant(
  name = "outlined",
  strings = ["style=outlined"],
  kitProps = ["Layout type=App Card", "Style=Outline", "Content type=Text", "Interactive=Yes"],
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
@OverrideVariant(
  name = "icon-outlined",
  strings = ["appImage=icon", "style=outlined"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Outline", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-outlined-content-image",
  strings = ["appImage=icon", "style=outlined", "content=image"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Outline", "Content type=Image", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-outlined-gallery-1",
  strings = ["appImage=icon", "style=outlined", "content=gallery-1"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Outline", "Content type=Gallery 1", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "icon-outlined-gallery-2",
  strings = ["appImage=icon", "style=outlined", "content=gallery-2"],
  kitProps =
    ["Layout type=Title Card + Icon", "Style=Outline", "Content type=Gallery 2", "Interactive=Yes"],
  secondary = true,
)
annotation class AppCardKitCells

@CatalogComponent(
  id = "AppCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5712",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "A card that names the app it came from — the shape a notification takes.",
)
@CatalogModes
@AppCardKitCells
@Composable
fun ApplicationCard(
  style: AppCardStyle = AppCardStyle.Tonal,
  // The leading slot is the kit's two layout names. `App Card` draws the app's own square artwork
  // there; `Title Card + Icon` draws a glyph. The slot used to be left empty, which is neither.
  appImage: AppCardImage = AppCardImage.Image,
) = Sticker {
  val c = counted(kitCopy("title", KitCopy.CARD_TITLE))
  AppCard(
    onClick = c.onClick,
    appName = { Text(kitCopy("appName", KitCopy.APP_LABEL)) },
    appImage =
      if (appImage == AppCardImage.Icon) {
        { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
      } else {
        {
          Image(
            painter = CatalogArtwork,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)),
          )
        }
      },
    title = { Text(c.label) },
    time = { Text(kitCopy("time", KitCopy.TIMESTAMP)) },
    colors = cardColorsFor(style),
    border = if (style == AppCardStyle.Outlined) CardDefaults.outlinedCardBorder() else null,
    modifier = Modifier.width(CardWidth),
    // `AppCard`'s content slot is `ColumnScope`-scoped where `TitleCard`'s is not, so the shared
    // helper's lambda is invoked inside one here.
    content = { cardContent(kitCopy("content", KitCopy.CARD_CONTENT))?.invoke() },
  )
}
