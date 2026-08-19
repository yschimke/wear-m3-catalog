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

@CatalogComponent(
  id = "SwipeToReveal/Card",
  reference = "figma:B24oss2tTeXAFykyeyusz0/56392:155753",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/56392:155752",
  caption = "A card swiped aside to show its actions.",
)
@CatalogModes
@OverrideVariant(
  name = "two-actions",
  booleans = ["secondary=true"],
  kitAxis = "State",
  kitValue = "2-actions",
)
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
  reference = "figma:B24oss2tTeXAFykyeyusz0/56392:155785",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/56392:155784",
  caption = "The same gesture over a button rather than a card.",
)
@CatalogModes
@OverrideVariant(
  name = "two-actions",
  booleans = ["secondary=true"],
  kitAxis = "State",
  kitValue = "2-actions",
)
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
