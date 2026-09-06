package ee.schimke.wearm3catalog.remote

/**
 * The placeholder copy the **M3 Wear OS Apps Design Kit** draws, so a Remote sticker and the kit
 * cell it is compared against say the same words.
 *
 * WHY THIS EXISTS. `catalog.spec.json` points this catalog's `compareWith` at
 * [`wear-m3-catalog`](https://github.com/yschimke/wear-m3-catalog), which reproduces the published
 * kit, and the compare page now carries that kit's own artwork as a third column. So every sticker
 * here is read against a kit cell — and a filled button labelled "Filled" beside a kit cell
 * labelled "Primary label" is a difference reported on every button row. Those differences are
 * noise twice over: they bury the structural findings that matter (a wrong radius, a missing
 * border, a colour off by a token), and they are the *only* kind of difference this catalog can fix
 * without touching the component, because the kit is read-only.
 *
 * This is the same bargain the Wear sibling struck, and the constants are transcribed from its
 * `KitCopy` (`catalog/src/main/kotlin/ee/schimke/wearm3catalog/CatalogCopy.kt`) rather than re-read
 * off the kit, so the two catalogs cannot drift into quoting the same cell differently. It is a
 * duplicate because the sibling lives in another repository and publishes no artifact carrying
 * these strings; if that ever changes, depend on it instead of copying.
 *
 * WHAT THIS DOES NOT FIX. The Wear sibling notes that matching the words is most of what matches
 * the *outline*, because its stickers are cropped to what they draw. That does not carry over: a
 * Remote sticker rasterises the whole `@Preview` frame rather than being cropped to what it draws,
 * so changing a label moves the glyphs and nothing else. The framing question is separate — see the
 * multipreviews in `CatalogTheme.kt`, which is where it is addressed — and copy cannot fix it.
 *
 * Only the strings this catalog actually needs are here. Slots the kit publishes no copy for — the
 * button group's two halves, the watch-screen template's list rows — are deliberately absent: see
 * the call sites.
 */
object KitCopy {
  /** `Button` and its emphasis axis, `Button-Compact`. The label on every single-slot button. */
  const val PRIMARY_LABEL = "Primary label"

  /** The second line of the two-slot buttons. */
  const val SECONDARY_LABEL = "Secondary label"

  /**
   * `Toggle+Selection-Buttons` — shorter than the button sets' wording, and deliberately so.
   *
   * Not [PRIMARY_LABEL]. The selection rows used to draw the button sets' longer string, which the
   * kit does not put there: on the `split` cells the extra word TRUNCATED ("Primary la…"), and
   * since the Wear sibling quotes the kit correctly the compare page reported a truncation defect
   * that was purely the fixture ([#294](https://github.com/yschimke/wear-m3-catalog/issues/294)).
   */
  const val PRIMARY = "Primary"

  /** @see PRIMARY */
  const val SECONDARY = "Secondary"

  /** `Card`'s app-name slot, on the App Card layout. */
  const val APP_LABEL = "Label text"

  /** Every card's timestamp slot. Not a real time — the kit writes the placeholder literally. */
  const val TIMESTAMP = "XXm"

  /** `Card`, all layouts. Drawn truncated at two lines; this is the string being truncated. */
  const val CARD_TITLE = "Title card title text lorem ipsum dolor sit amet"

  /** `Card`'s body slot. Also truncated in the kit's own cells. */
  const val CARD_CONTENT = "Content lorem ipsum dolor sit amet lorem ipsum dolor sit amet"

  /** The title cards' subtitle slot, and `List-Subheader`. */
  const val SUBTITLE = "Subtitle"

  /**
   * `Text-Button`.
   *
   * A run of the widest glyph in the face, which is how the kit sizes a round container it wants
   * drawn at its worst case rather than at some particular word's. It is emphatically NOT
   * [PRIMARY_LABEL]: a round text button given a two-word label draws it outside its own circle.
   */
  const val GLYPHS = "MMM"

  /** `Text-Body`. */
  const val BODY = "Body text lorem ipsum dolor sit amet"

  /** `Text-Caption`. Transcribed from the Wear sibling, so both columns caption the same words. */
  const val CAPTION = "Caption text enim ad minim veniam, quis nostrud"

  /**
   * `Edge-Button`, for the same reason [GLYPHS] is an M-run — long enough that it truncates.
   *
   * Transcribed from the Wear sibling's `EDGE_BUTTON_LABEL`, like every other constant here, so
   * both columns' `Edge-Button` cells are drawn from the same words. Only the Wear one truncates
   * them: `RemoteEdgeButton` does not clip its content to its arc, which is the divergence
   * `EdgeButtonPreviews.kt` states. The M-run stays anyway — shortening it here to flatter the
   * Remote render would hide the difference rather than fix it, and would unpair the two columns.
   */
  const val EDGE_BUTTON_LABEL = "MMMMM MMMMM MMMMM"

  /** `TimeText`, `Type=12hr`. Pinned, never the system clock. */
  const val TIME_12H = "9:30"
}
