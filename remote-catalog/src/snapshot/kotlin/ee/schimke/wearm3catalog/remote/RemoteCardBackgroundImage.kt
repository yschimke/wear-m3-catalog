@file:Suppress("RestrictedApiAndroidX")

package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.action.Action
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteCardDefaults
import androidx.wear.compose.remote.material3.RemoteTitleCard
import ee.schimke.composeai.preview.OverrideVariant

// THE SNAPSHOT LANE'S CARD STYLES — `Background Image` included, because the library grew it.
//
// The counterpart of this file under `src/released` stops at `Tonal | Outline`, because alpha10
// publishes no painter parameter on any card. Since that release `RemoteCard` and `RemoteTitleCard`
// have each gained a `containerPainter: RemotePainter` overload, `RemoteCardDefaults` has gained
// the `containerPainter(RemoteImageBitmap, …)` factory and its `scrimBrush`, and
// `cardWithContainerPainterColors()` is the colour set those overloads default to. That is the
// whole call site [#157](https://github.com/yschimke/wear-m3-catalog/issues/157) said was missing.
//
// THREE CELLS, NOT FIVE. The kit leaves five `Style=Background Image` cells uncovered on this sheet
// and this closes the three that are `RemoteTitleCard`'s — `Title Card 1`, `Title Card 2` and
// `Title Card 3`, which are the three layouts already folded onto that one component. The other two
// (`App Card` and `Title Card + Icon`) are both drawn by `RemoteAppCard`, which did NOT gain a
// painter overload; they stay uncovered, and the Wear sibling has the same two missing for the same
// reason.
//
// THESE THREE CELLS ARE BROKEN, AND THAT IS THE ANSWER THEY WERE DRAWN TO GET.
//
// `Button/ImageBackground` on this sheet calls the equivalent BUTTON painter and draws an opaque
// black pill with no image in it (see `ComponentVariantPreviews.kt`). The open question was whether
// the CARD overload — a different function, added later — behaves differently. It does not: the
// container renders near-black, measured at ~#292929 against a placeholder that is #ECECEC, so the
// image is absent here too. `RemoteImage` draws that same bitmap correctly in the content slots
// of the very same cells, which is what puts it in the container painter rather than in the bitmap,
// the scrim or the player. The defect is `remoteContainerPainter`, which both overloads route
// through, not the button's wrapper around it.
//
// They are published anyway, which is this repo's standing bargain and the same one the button
// row records at length: withdrawing leaves the SET reading as unreproduced, indistinguishable from
// nobody having got to it, while publishing puts the break where a reader meets it, lets
// design-parity score it as the real divergence it is, and makes the fix self-announcing — the day
// the painter draws, these rows move on their own.
//
// ONE MORE THING THE RENDER SHOWS, and it is a second defect rather than the same one: the
// `Title Card 2` cell CLIPS its subtitle. The painter overload defaults to
// `CardWithContainerPainterContentPadding`, which is larger than the plain card's, so identical
// content no longer fits the container. Not adjusted from here — the padding is the library's
// statement about the style, and substituting our own would hide the finding rather than report it.

/** The kit's `Style` values this lane can draw. */
internal val KitCardStyles = listOf("tonal", "outlined", "image")

/**
 * The kit's `Style=Background Image` column, crossed with the three numbered layouts already folded
 * onto [TitleCardRemote].
 *
 * Cell names follow the Wear sibling's where it has one (`background-image`,
 * `with-subtitle-background-image`), so the compare page pairs them rather than setting two
 * spellings of one cell side by side. `Title Card 3` has no Wear counterpart — that sheet draws none
 * of that layout — so its name follows this sheet's own `title-and-subtitle` convention.
 *
 * `Content type=Text` on all three, because that is the only content the kit crosses this style
 * with; there is no image-backed gallery cell to draw.
 */
@OverrideVariant(
  name = "background-image",
  strings = ["style=image"],
  kitProps =
    ["Layout type=Title Card 1", "Style=Background Image", "Content type=Text", "Interactive=Yes"],
)
@OverrideVariant(
  name = "with-subtitle-background-image",
  strings = ["style=image", "layout=title-time-subtitle"],
  kitProps =
    ["Layout type=Title Card 2", "Style=Background Image", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
@OverrideVariant(
  name = "title-and-subtitle-background-image",
  strings = ["style=image", "layout=title-subtitle"],
  kitProps =
    ["Layout type=Title Card 3", "Style=Background Image", "Content type=Text", "Interactive=Yes"],
  secondary = true,
)
annotation class RemoteCardBackgroundImageCells

/**
 * `RemoteTitleCard` with the kit's `Style` axis resolved — the one call site both lanes share.
 *
 * The wrapper exists so the STYLE decides the call, not the caller: `image` is a DIFFERENT overload
 * taking a `containerPainter`, not an argument to the plain one, and that fork is the only thing
 * this lane's card story adds. Keeping it here leaves `TitleCardRemote` reading the same on both
 * lanes.
 *
 * The scrim is the library's own default — `containerPainter` applies `scrimBrush` unless told
 * otherwise, and the scrim is most of what the style IS on both platforms. Passing anything else
 * would be inventing a treatment under the kit's name.
 */
@Composable
@RemoteComposable
internal fun KitTitleCard(
  onClick: Action,
  style: String,
  modifier: RemoteModifier,
  title: @Composable @RemoteComposable () -> Unit,
  time: (@Composable @RemoteComposable () -> Unit)?,
  subtitle: (@Composable @RemoteComposable () -> Unit)?,
  content: (@Composable @RemoteComposable () -> Unit)?,
) {
  if (style == "image") {
    RemoteTitleCard(
      onClick = onClick,
      containerPainter = RemoteCardDefaults.containerPainter(CatalogRemoteImage.bitmap()),
      modifier = modifier,
      title = title,
      time = time,
      subtitle = subtitle,
      content = content,
    )
  } else {
    RemoteTitleCard(
      onClick = onClick,
      modifier = modifier,
      colors =
        if (style == "outlined") RemoteCardDefaults.outlinedCardColors()
        else RemoteCardDefaults.cardColors(),
      title = title,
      time = time,
      subtitle = subtitle,
      content = content,
    )
  }
}
