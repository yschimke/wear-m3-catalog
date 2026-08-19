@file:CatalogGroup(name = "Cards", section = "Containment")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.OutlinedCard
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogImage
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy

// The kit's `Card` set — `Layout type = App Card | Title Card 1..3 | Title Card + Icon`, over
// `Style = Tonal | Outline | Background Image` and a content axis.
//
// `Layout type` splits, because each is its own Wear Compose function (`Card`, `TitleCard`,
// `AppCard`) and picking one is the call-site choice. `Style` splits only where Compose does:
// `OutlinedCard` is a function, so it is a component; the background-image style is the `Card`
// overload that takes a `Painter`, and it stays a cell because the same function draws it.
//
// The kit's numbered Title Card layouts are its way of showing which slots are filled — title
// alone, title + time, title + subtitle — and Compose spells all three as which slots you pass. So
// they fold as content cells rather than splitting into three components.
//
// The kit's `Background Image` style is a cell on `Card`, drawn with the `containerPainter`
// overload and the scrim `CardDefaults.containerPainter` applies — the scrim is most of what the
// style is. The image itself is drawn rather than shipped; see `CatalogImage`.
//
// A card is a full-width component on a watch, so the sticker pins a width rather than letting the
// crop decide one: at the measuring bound a card would size to the largest round screen and the
// published sticker would be wider than the small-round device it has to fit.

@CatalogComponent(
  id = "Card",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5747",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption =
    "The plain container: one content slot, tonal by default. A no-`onClick` overload presents " +
      "the same card without the tap.",
)
@CatalogModes
@OverrideVariant(
  name = "background-image",
  strings = ["style=image"],
  kitAxis = "Style",
  kitValue = "Background Image",
)
// The no-`onClick` overload — a card that presents rather than acts — is NOT a cell here. It is
// pixel-identical to this one at rest: what differs is whether a tap does anything, which a still
// cannot show, so a cell would publish the same picture under a second name. It is documented in
// the caption instead.
@Composable
fun PlainCard() = Sticker {
  val c = counted(kitCopy("content", KitCopy.CARD_CONTENT))
  if (previewOverrideChoice("style", "tonal", listOf("tonal", "image")) == "image") {
    Card(
      onClick = c.onClick,
      containerPainter = CardDefaults.containerPainter(image = CatalogImage),
      modifier = Modifier.width(180.dp),
      colors = CardDefaults.cardWithContainerPainterColors(),
    ) {
      Text(c.label)
    }
  } else {
    Card(onClick = c.onClick, modifier = Modifier.width(180.dp)) { Text(c.label) }
  }
}

@CatalogComponent(
  id = "Card/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/39827:105691",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "The kit's outline style — the same container drawn as a border.",
)
@CatalogModes
@Composable
fun OutlineCard() = Sticker {
  val c = counted(kitCopy("content", KitCopy.CARD_CONTENT))
  OutlinedCard(onClick = c.onClick, modifier = Modifier.width(180.dp)) { Text(c.label) }
}

@CatalogComponent(
  id = "TitleCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5747",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "A card led by a title, with the kit's numbered layouts folded in as content cells.",
)
@CatalogModes
@OverrideVariant(
  name = "title-and-time",
  strings = ["content=time"],
  kitAxis = "Layout type",
  kitValue = "Title Card 2",
)
@OverrideVariant(
  name = "title-and-subtitle",
  strings = ["content=subtitle"],
  kitAxis = "Layout type",
  kitValue = "Title Card 3",
)
@Composable
fun TitledCard() = Sticker {
  val c = counted(kitCopy("title", KitCopy.CARD_TITLE))
  val content = previewOverrideChoice("content", "title", listOf("title", "time", "subtitle"))
  TitleCard(
    onClick = c.onClick,
    title = { Text(c.label) },
    time =
      if (content == "time") {
        { Text(kitCopy("time", KitCopy.TIMESTAMP)) }
      } else null,
    subtitle =
      if (content == "subtitle") {
        { Text(kitCopy("subtitle", KitCopy.SUBTITLE)) }
      } else null,
    modifier = Modifier.width(180.dp),
    colors = CardDefaults.cardColors(),
  )
}

@CatalogComponent(
  id = "AppCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5712",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "A card that names the app it came from — the shape a notification takes.",
)
@CatalogModes
@Composable
fun ApplicationCard() = Sticker {
  val c = counted(kitCopy("title", KitCopy.CARD_TITLE))
  AppCard(
    onClick = c.onClick,
    appName = { Text(kitCopy("appName", KitCopy.APP_LABEL)) },
    title = { Text(c.label) },
    time = { Text(kitCopy("time", KitCopy.TIMESTAMP)) },
    modifier = Modifier.width(180.dp),
  ) {
    Text(kitCopy("content", KitCopy.CARD_CONTENT))
  }
}
