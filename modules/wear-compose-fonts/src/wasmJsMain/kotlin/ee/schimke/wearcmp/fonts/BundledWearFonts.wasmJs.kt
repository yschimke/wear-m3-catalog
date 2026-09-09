package ee.schimke.wearcmp.fonts

import ee.schimke.wearcmp.port.WearFonts

/**
 * There is no bundled copy in the browser, and this returns `false`.
 *
 * Not an oversight: a wasm binary cannot read a file synchronously, and a 1.7 MB font embedded in
 * the module would be paid for by every page whether it drew Wear text or not. The host already
 * knows how to fetch — it is a web page — so it fetches once and registers the bytes:
 *
 *     val bytes = window.fetch("/fonts/roboto-flex.ttf").arrayBuffer().toByteArray()
 *     WearFonts.register(WearFonts.RobotoFlex, bytes)
 *
 * A `@font-face` rule alone is not enough: Skia draws the canvas, and the browser's font table is
 * not Skia's. The bytes have to reach [WearFonts].
 */
public actual fun installBundledWearFonts(): Boolean =
    WearFonts.isRegistered(WearFonts.RobotoFlex)
