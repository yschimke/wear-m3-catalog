@file:CatalogGroup(name = "Cards", section = "Containment")

package ee.schimke.wearm3catalog.uid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.counted
import ee.schimke.wearm3catalog.kitCopy
import generated.uibuilder.KitOutlinedCard

// Shells: the hand-written stickers' annotations and names, with bodies generated from
// `stickers/<name>.uid`.

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
  KitOutlinedCard(Modifier, c.label, c.onClick)
}
