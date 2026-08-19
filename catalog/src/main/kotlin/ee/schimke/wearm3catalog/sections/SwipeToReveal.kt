@file:CatalogGroup(name = "Swipe to reveal", section = "Containment")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.RevealValue
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberRevealState
import ee.schimke.composeai.overrides.previewOverrideBoolean
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogModes
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.Sticker
import ee.schimke.wearm3catalog.kitCopy

// The kit's `STR-card` and `STR-button` sets — the same gesture over the two things it can be
// applied to, which is why they are two kit sets and two components here.
//
// REVEALED, NOT AT REST. A swipe-to-reveal at rest is indistinguishable from the card or button
// underneath it, and the kit's cells all draw the revealed state — so the sticker seeds
// `rememberRevealState(RevealValue.RightRevealing)` and publishes what the gesture uncovers. In a
// live session the swipe still works from there; what is pinned is where the capture starts.
//
// BOTH ARE UNMAPPED, and it is the framing rather than the drawing that costs them the reference.
//
// Every one of the eight cells across the two sets is a **192×192 display** — the watch face, with
// the component mid-swipe and running off the left edge of it. Not one is the component cropped to
// itself. These stickers are the opposite: the whole component, wrap-and-cropped, nothing of the
// screen around it. Pointed at those cells they were diffing a 392×136 crop against a round screen,
// with the comparison squashing one into the other's frame first — the same category error the
// alert dialog had against a long-scroll cell (docs/DESIGN_MAP.md).
//
// The catalog's rule is that the mapped sticker is the component-shaped one and a screen stays
// unmapped, so the honest outcome here is no reference at all rather than a reference of the wrong
// shape. The kit publishes swipe-to-reveal as a picture of a watch mid-gesture; this catalog
// publishes the component. Both are true, and they are not comparable.
//
// What would change that is a display-shaped STR sticker — the component on the round frame, swiped
// — which is a frame this catalog does not have and which `EdgeButton/Screen` is the standing
// example of publishing unmapped anyway.

@CatalogComponent(
  id = "SwipeToReveal/Card",
  noReference =
    "The kit draws `STR-card` only on the display — all four cells are 192×192 watch faces with " +
      "the card mid-swipe running off the edge, and none is the component cropped to itself. " +
      "This sticker is the component, so there is no cell of its shape to compare against.",
  caption = "A card swiped aside to show its actions.",
)
@CatalogModes
// No `kitAxis` any more: a cell resolves by varying one axis from its component's reference, and
// this component has none. The cell is still worth publishing — a second action is a real
// difference — it simply has nothing to be resolved against.
@OverrideVariant(name = "two-actions", booleans = ["secondary=true"])
@Composable
fun SwipeToRevealCard() = Sticker {
  val state = rememberRevealState(initialValue = RevealValue.RightRevealing)
  SwipeToReveal(
    primaryAction = {
      PrimaryActionButton(
        onClick = {},
        icon = { Icon(Icons.Filled.Close, contentDescription = null) },
        text = { Text(kitCopy("action", KitCopy.PRIMARY_LABEL)) },
      )
    },
    onSwipePrimaryAction = {},
    secondaryAction =
      if (previewOverrideBoolean("secondary", false)) {
        {
          SecondaryActionButton(
            onClick = {},
            icon = { Icon(Icons.Filled.MoreVert, contentDescription = null) },
          )
        }
      } else null,
    revealState = state,
    modifier = Modifier.width(180.dp),
  ) {
    Card(onClick = {}) { Text(kitCopy("content", KitCopy.CARD_CONTENT)) }
  }
}

@CatalogComponent(
  id = "SwipeToReveal/Button",
  noReference =
    "The kit draws `STR-button` only on the display — all four cells are 192×192 watch faces with " +
      "the button mid-swipe running off the edge, and none is the component cropped to itself. " +
      "This sticker is the component, so there is no cell of its shape to compare against.",
  caption = "The same gesture over a button rather than a card.",
)
@CatalogModes
// No `kitAxis` any more: a cell resolves by varying one axis from its component's reference, and
// this component has none. The cell is still worth publishing — a second action is a real
// difference — it simply has nothing to be resolved against.
@OverrideVariant(name = "two-actions", booleans = ["secondary=true"])
@Composable
fun SwipeToRevealButton() = Sticker {
  val state = rememberRevealState(initialValue = RevealValue.RightRevealing)
  SwipeToReveal(
    primaryAction = {
      PrimaryActionButton(
        onClick = {},
        icon = { Icon(Icons.Filled.Close, contentDescription = null) },
        text = { Text(kitCopy("action", KitCopy.PRIMARY_LABEL)) },
      )
    },
    onSwipePrimaryAction = {},
    secondaryAction =
      if (previewOverrideBoolean("secondary", false)) {
        {
          SecondaryActionButton(
            onClick = {},
            icon = { Icon(Icons.Filled.MoreVert, contentDescription = null) },
          )
        }
      } else null,
    revealState = state,
    modifier = Modifier.width(180.dp),
  ) {
    Button(onClick = {}, label = { Text(kitCopy("label", KitCopy.PRIMARY_LABEL)) })
  }
}
