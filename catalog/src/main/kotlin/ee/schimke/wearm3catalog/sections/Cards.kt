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
import ee.schimke.composeai.overrides.previewOverrideString
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted

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
// The kit's third style, `Background Image`, is NOT here yet, and its absence is deliberate rather
// than an oversight: Compose draws it with the `Card` overload that takes a `Painter`, and this
// catalog ships no image to hand it. A cell seeded but not implemented would publish the plain card
// under the image style's name — a wrong picture that renders green. It arrives with a committed
// sample image, as a cell on `Card`.
//
// A card is a full-width component on a watch, so the sticker pins a width rather than letting the
// crop decide one: at the measuring bound a card would size to the largest round screen and the
// published sticker would be wider than the small-round device it has to fit.

@CatalogComponent(
  id = "Card",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "The plain container: one content slot, tonal by default.",
)
@CatalogModes
@Composable
fun PlainCard() = Sticker {
  val c = counted("Card")
  Card(onClick = c.onClick, modifier = Modifier.width(180.dp)) { Text(c.label) }
}

@CatalogComponent(
  id = "Card/Outlined",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "The kit's outline style — the same container drawn as a border.",
)
@CatalogModes
@Composable
fun OutlineCard() = Sticker {
  val c = counted("Outlined")
  OutlinedCard(onClick = c.onClick, modifier = Modifier.width(180.dp)) { Text(c.label) }
}

@CatalogComponent(
  id = "TitleCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
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
  val c = counted("Workout")
  val content = previewOverrideString("content", "title")
  TitleCard(
    onClick = c.onClick,
    title = { Text(c.label) },
    time =
      if (content == "time") {
        { Text("12m") }
      } else null,
    subtitle =
      if (content == "subtitle") {
        { Text("Outdoor run") }
      } else null,
    modifier = Modifier.width(180.dp),
    colors = CardDefaults.cardColors(),
  )
}

@CatalogComponent(
  id = "AppCard",
  reference = "figma:B24oss2tTeXAFykyeyusz0/38437:5746",
  caption = "A card that names the app it came from — the shape a notification takes.",
)
@CatalogModes
@Composable
fun ApplicationCard() = Sticker {
  val c = counted("Messages")
  AppCard(
    onClick = c.onClick,
    appName = { Text("Messages") },
    title = { Text(c.label) },
    time = { Text("10:10") },
    modifier = Modifier.width(180.dp),
  ) {
    Text("On my way")
  }
}
