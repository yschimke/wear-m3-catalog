@file:CatalogGroup(name = "Swipe to reveal", section = "Containment")

package ee.schimke.wearm3catalog.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.RevealState
import androidx.wear.compose.material3.RevealValue
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberRevealState
import ee.schimke.composeai.overrides.previewOverrideChoice
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.OverrideVariant
import ee.schimke.wearm3catalog.CatalogFullScreenModes
import ee.schimke.wearm3catalog.FullScreenSticker
import ee.schimke.wearm3catalog.KitCopy
import ee.schimke.wearm3catalog.kitCopy

// The kit's `STR-card` and `STR-button` sets — the same gesture over the two things it can be
// applied to, which is why they are two kit sets and two components here.
//
// REVEALED, NOT AT REST. A swipe-to-reveal at rest is indistinguishable from the card or button
// underneath it, and the kit's cells all draw the revealed state — so the sticker seeds
// `rememberRevealState(RevealValue.RightRevealing)` and publishes what the gesture uncovers. In a
// live session the swipe still works from there; what is pinned is where the capture starts. The
// state is keyed on the action count because `SwipeToReveal` derives its reveal geometry from that
// count: a held Live session can otherwise retain the one-action geometry when the two-action cell
// is selected, leaving the second action behind the card (issue #66).
//
// ON THE ROUND SCREEN, NOT CROPPED. Both sets draw `192×192` display cells: the item has slid far
// enough left that the display's own edge clips it, and that clip is half of what the cell shows.
// These used to publish as cropped `Sticker`s, which paired a landscape strip with a round watch
// face — the reference arrived squashed into a 392×190 frame and every edge of it read as a
// difference. Same defect as the edge button in issue #31, in the other direction: there a
// component cell was being compared against a screen, here a screen cell against a component.

/**
 * Which of `RevealValue`'s resting positions the sticker is caught in.
 *
 * `RightRevealing` is the baked one — the gesture half-done, which is the only position that shows
 * both the content and the actions it reveals, and the arrangement the kit draws. The rest were
 * pinned out of reach: `Covered` is the card before the swipe and `RightRevealed` is after it, and
 * neither is discoverable from a still.
 *
 * **That is also why each set stops at two of its four cells**
 * ([#101](https://github.com/yschimke/wear-m3-catalog/issues/101)). The kit's other two — `Full
 * swipe (icon only)` and `Full swipe (icon + text)` — are two frames of the expansion that
 * `RightRevealed` ends at, and a cell seeded with that value renders an EMPTY FRAME here: at rest
 * in the revealed position the content has travelled off the screen and the expanded action has not
 * been laid out, so there is nothing in the capture. `Motion.kt` has the recording, which is where
 * a gesture's own frames belong.
 */
@Composable
private fun revealState(secondary: Boolean): RevealState {
  val value =
    when (
      previewOverrideChoice(
        "revealValue",
        "right-revealing",
        listOf("right-revealing", "covered", "right-revealed"),
      )
    ) {
      "covered" -> RevealValue.Covered
      "right-revealed" -> RevealValue.RightRevealed
      else -> RevealValue.RightRevealing
    }
  // Both values are initial conditions read once by the remembered state. The reveal value chooses
  // the anchor, while the action count changes the distance needed to expose that anchor. A baked
  // render always gets a fresh composition; keying is what makes the same promise when Live applies
  // a variant to its held composition.
  return key(value, secondary) { rememberRevealState(initialValue = value) }
}

@CatalogComponent(
  id = "SwipeToReveal/Card",
  reference = "figma:B24oss2tTeXAFykyeyusz0/56392:155753",
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/56392:155752",
  caption = "A card swiped aside to show its actions.",
  // The reveal itself, recorded in Motion.kt. The sticker publishes the card already revealed,
  // because at rest it is indistinguishable from the card underneath; this is the part between.
  motionPreview = "SwipeToRevealMotion",
)
@CatalogFullScreenModes
@OverrideVariant(
  name = "two-actions",
  booleans = ["secondary=true"],
  kitAxis = "State",
  kitValue = "2-actions",
)
@Composable
fun SwipeToRevealCard(secondary: Boolean = false) = FullScreenSticker {
  val state = revealState(secondary)
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
      if (secondary) {
        {
          SecondaryActionButton(
            onClick = {},
            icon = { Icon(Icons.Filled.MoreVert, contentDescription = null) },
          )
        }
      } else null,
    revealState = state,
    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
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
@CatalogFullScreenModes
@OverrideVariant(
  name = "two-actions",
  booleans = ["secondary=true"],
  kitAxis = "State",
  kitValue = "2-actions",
)
@Composable
fun SwipeToRevealButton(secondary: Boolean = false) = FullScreenSticker {
  val state = revealState(secondary)
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
      if (secondary) {
        {
          SecondaryActionButton(
            onClick = {},
            icon = { Icon(Icons.Filled.MoreVert, contentDescription = null) },
          )
        }
      } else null,
    revealState = state,
    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
  ) {
    Button(onClick = {}, label = { Text(kitCopy("label", KitCopy.PRIMARY_LABEL)) })
  }
}
