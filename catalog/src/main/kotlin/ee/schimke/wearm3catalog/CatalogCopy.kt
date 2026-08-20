package ee.schimke.wearm3catalog

import androidx.compose.runtime.Composable
import ee.schimke.composeai.overrides.previewOverrideString

/**
 * The placeholder copy the **kit** draws, so a sticker and its reference say the same words.
 *
 * WHY THIS EXISTS. Every sticker here used to carry copy invented for it — a filled button labelled
 * "Filled", a title card reading "Workout", an alert dialog asking "Delete this run?". Read on its
 * own that is the better catalog: it shows what each component is *for*. Read against the kit it is
 * a diff on every single card, because the kit labels the same button "Primary label" and the same
 * card "Title card title text lorem…". `.design-parity.json` says `design-led`, so where the two
 * disagree the kit is right and this code is the defect — and a divergence that is *only* the words
 * still costs the comparison twice over:
 *
 * - **It buries the findings that matter.** A structural difference — a wrong radius, a missing
 *   border, a colour off by a token — is invisible in a diff that is already lit up by text.
 * - **It moves the geometry too.** A sticker is cropped to what it draws, so a button labelled
 *   "Filled" is a different SHAPE from one labelled "Primary label": shorter, rounder, and cropped
 *   to a frame the reference then has to be squashed into. Matching the words is most of what
 *   matches the outline.
 *
 * The kit cannot be edited from here (AGENTS.md: Figma is read-only, in both directions), so the
 * catalog is what moves.
 *
 * WHAT IS LOST, AND HOW IT COMES BACK. The realistic copy was doing a real job for a reader
 * browsing the sheet, and lorem does not do it. That is what [kitCopy] is for: every string below
 * is the *default* of a named override, not a constant, so the preview server's override knobs
 * retype any of them live without a rebuild. The published capture is the kit's words; the sheet
 * stays explorable.
 *
 * The strings are transcribed from the kit's own cells (see `docs/DESIGN_MAP.md` for which node
 * each comes from). Where a cell shows the text truncated — the cards do — the value here is the
 * full string the kit is truncating, so the ellipsis lands in the same place rather than the text
 * simply being shorter.
 */
object KitCopy {
  /** `Button`, `Button-Compact`, `Button-ImageBackground`, `Button-Loading`. */
  const val PRIMARY_LABEL = "Primary label"

  /** The second line of the two-slot buttons. */
  const val SECONDARY_LABEL = "Secondary label"

  /** `Toggle+Selection-Buttons` — shorter than the button sets' wording, and deliberately so. */
  const val PRIMARY = "Primary"

  /** @see PRIMARY */
  const val SECONDARY = "Secondary"

  /** `List-Header`. */
  const val TITLE = "Title"

  /** `List-Subheader`, and the title cards' subtitle slot. */
  const val SUBTITLE = "Subtitle"

  /** `Card`'s app-name slot, on the App Card layout. */
  const val APP_LABEL = "Label text"

  /** Every card's timestamp slot. Not a real time — the kit writes the placeholder literally. */
  const val TIMESTAMP = "XXm"

  /** `Card`, all layouts. Drawn truncated at two lines; this is the string being truncated. */
  const val CARD_TITLE = "Title card title text lorem ipsum dolor sit amet"

  /** `Card`'s body slot. Also truncated in the kit's own cells. */
  const val CARD_CONTENT = "Content lorem ipsum dolor sit amet lorem ipsum dolor sit amet"

  /** `Text-Body`. */
  const val BODY = "Body text lorem ipsum dolor sit amet"

  /** `Text-Caption`. */
  const val CAPTION = "Caption text enim ad minim veniam, quis nostrud"

  /** `Dialog`. The kit's own wording, describing its own slot's limit. */
  const val DIALOG_TITLE = "Dialog title one to three lines"

  /** `OpenOnPhone-Overlay`'s curved text. Wear's default reads "Open on phone". */
  const val OPEN_ON_PHONE = "Check your phone"

  /**
   * `Text-Button` and `Text-Toggle-Button`.
   *
   * A run of the widest glyph in the face, which is how the kit sizes a container it wants drawn at
   * its worst case rather than at some particular word's.
   */
  const val GLYPHS = "MMM"

  /** `Edge-Button`, for the same reason [GLYPHS] is an M-run — long enough that it truncates. */
  const val EDGE_BUTTON_LABEL = "MMMMM MMMMM MMMMM"

  /** `TimeText`, `Type=12hr`. Pinned, never the system clock — see `TextComponents.kt`. */
  const val TIME_12H = "9:30"

  /** `TimeText`, `Type=24hr`. */
  const val TIME_24H = "09:30"

  /** `Stepper`, `Icon=No`. */
  const val STEPPER_LABEL = "This watch's headphones"

  /**
   * The top line of the kit's media header — `.Base / Media / Header`, the `Song Name` text node
   * (`71575:21988`) inside the `Position=Centre, Scrolling=No, …` cell.
   *
   * The Media Controls page is the one page whose copy could not be read off the exported SVG: the
   * kit outlines its text to paths there. These are read off the rendered cell instead — see
   * [MEDIA_ARTIST] for why the layer names alone were not good enough.
   */
  const val MEDIA_TITLE = "Song Name"

  /**
   * The second line of the same header.
   *
   * The text is `Artist name`, which is NOT what the layer is called: the node at `71575:21990` is
   * *named* `Name of artist` and *draws* `Artist name`. This file used the layer name until the
   * rendered cell was actually looked at — a reminder that a Figma layer name is a label somebody
   * typed once, not the content.
   */
  const val MEDIA_ARTIST = "Artist name"

  /**
   * The label on the media player's bottom action.
   *
   * The kit draws that slot as an icon button rather than a labelled one, so it publishes no string
   * for it. Horologist's `ShowPlaylistButton` takes a name, and this is the placeholder in the
   * kit's register rather than a borrowed real playlist.
   */
  const val MEDIA_PLAYLIST = "Playlist name"
}

/**
 * A piece of copy whose default is the kit's and whose value is a live override.
 *
 * This is the whole bargain in one function: the **baked** capture — the thing design-parity diffs
 * against the kit — always carries [kit], so the published comparison is honest by construction;
 * and `key` names a knob the preview server exposes, so anyone reading the sheet can type real copy
 * into it and see the component do its job.
 *
 * `key` is per-sticker, not global: the same `"label"` on two components is two independent knobs.
 * Prefer the slot's name (`"label"`, `"title"`, `"time"`) so the knob reads as the parameter it
 * feeds.
 *
 * Overrides do NOT multiply the inventory. A cell is an `@OverrideVariant`; a knob is not, so
 * nothing below adds a render, a card, or a row to `design-map.json`.
 */
@Composable fun kitCopy(key: String, kit: String): String = previewOverrideString(key, kit)
