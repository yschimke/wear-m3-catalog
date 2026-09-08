package ee.schimke.wearm3catalog.remote

import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.width
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.wear.compose.remote.material3.RemoteCardDefaults
import androidx.wear.compose.remote.material3.RemoteText
import androidx.wear.compose.remote.material3.RemoteTitleCard

/**
 * Which ingredient of `RemoteTitleCard` loses the `time` slot under
 * `composePreview.rcDensity=host`.
 *
 * `TitleCardRemote` was the last component whose render changed when density is deferred: its `XXm`
 * timestamp disappears and the title reflows into the freed width. Every plain text sticker in the
 * catalog is byte-identical between the two capture modes, and so is the card's own body text, so
 * this is not the deferred conversion misbehaving across the board.
 *
 * These five bisect it by adding one ingredient at a time. Measured as rendered ink, at 227x200dp,
 * dpi 320:
 *
 * | probe                                                      | fixed  | host       |             |
 * |------------------------------------------------------------|--------|------------|-------------|
 * | [TitleCardHostProbeBareText] two bare `RemoteText`         | 2105   | 2105       | same        |
 * | [TitleCardHostProbeCard] card: title + time                | 55921  | 55921      | same        |
 * | [TitleCardHostProbeFreshWidth] + a `172.rdp` width         | 41841  | 41841      | same        |
 * | [TitleCardHostProbeSharedWidth] + the shared `KitRowWidth` | 41841  | 41841      | same        |
 * | [TitleCardHostProbeWithContent] + a `content` body         | 116145 | **103761** | **differs** |
 *
 * So the trigger is the **`content` slot**: a card carrying title, time and content drops its time
 * under a `Host` capture, and the same card without content does not. Filling the third slot is
 * what breaks the first. The shared-instance question the two width probes exist to answer is
 * settled too — a top-level `val KitRowWidth = 172.rdp` reused across captures behaves exactly like
 * a freshly built one, so `RemoteDp`'s expression cache is not implicated.
 *
 * It is also **not** visible on the shorter 227x100dp canvas, where the card is clipped identically
 * either way — which is why the reduction only reproduced once the probes moved to
 * `@CatalogRemoteLarge`. Worth knowing before trusting a "cannot reproduce" on this.
 *
 * Reported upstream-ready as compose-ai-tools#5310. Delete these once it is fixed and all five
 * agree.
 */
@CatalogRemoteLarge
@Composable
fun TitleCardHostProbeBareText() = RemoteSticker {
  RemoteText("Title card title".rs)
  RemoteText("XXm".rs)
}

/** The same two strings, in `RemoteTitleCard`'s own slots. */
@CatalogRemoteLarge
@Composable
fun TitleCardHostProbeCard() = RemoteSticker {
  val (_, onClick) = countedRemote("probe")
  RemoteTitleCard(
    onClick = onClick,
    modifier = RemoteModifier,
    colors = RemoteCardDefaults.cardColors(),
    title = { RemoteText("Title card title".rs) },
    time = { RemoteText("XXm".rs) },
    subtitle = null,
    content = null,
  )
}

/**
 * The card at the catalog's shared top-level `KitRowWidth` (`172.rdp`), as `TitleCardRemote` uses
 * it.
 */
@CatalogRemoteLarge
@Composable
fun TitleCardHostProbeSharedWidth() = RemoteSticker {
  val (_, onClick) = countedRemote("probe")
  RemoteTitleCard(
    onClick = onClick,
    modifier = RemoteModifier.width(KitRowWidth),
    colors = RemoteCardDefaults.cardColors(),
    title = { RemoteText("Title card title".rs) },
    time = { RemoteText("XXm".rs) },
    subtitle = null,
    content = null,
  )
}

/** Identical, but with a FRESH `172.rdp` built inside the composition instead of the shared val. */
@CatalogRemoteLarge
@Composable
fun TitleCardHostProbeFreshWidth() = RemoteSticker {
  val (_, onClick) = countedRemote("probe")
  RemoteTitleCard(
    onClick = onClick,
    modifier = RemoteModifier.width(172.rdp),
    colors = RemoteCardDefaults.cardColors(),
    title = { RemoteText("Title card title".rs) },
    time = { RemoteText("XXm".rs) },
    subtitle = null,
    content = null,
  )
}

/** Closest to the real `TitleCardRemote`: counted title, time, AND a content body. */
@CatalogRemoteLarge
@Composable
fun TitleCardHostProbeWithContent() = RemoteSticker {
  val (title, onClick) = countedRemote(KitCopy.CARD_TITLE)
  RemoteTitleCard(
    onClick = onClick,
    modifier = RemoteModifier.width(KitRowWidth),
    colors = RemoteCardDefaults.cardColors(),
    title = { RemoteText(title) },
    time = { RemoteText(KitCopy.TIMESTAMP.rs) },
    subtitle = null,
    content = { RemoteText(KitCopy.CARD_CONTENT.rs) },
  )
}
