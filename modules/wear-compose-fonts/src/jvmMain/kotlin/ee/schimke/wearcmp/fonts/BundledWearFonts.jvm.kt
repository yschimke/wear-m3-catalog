package ee.schimke.wearcmp.fonts

import ee.schimke.wearcmp.port.WearFonts

/**
 * Reads the font out of this artifact's own jar. Synchronous, because a JVM classpath resource is,
 * which makes this the lane where "preview in the real typeface" costs one call and nothing else.
 */
public actual fun installBundledWearFonts(): Boolean {
    if (WearFonts.isRegistered(WearFonts.RobotoFlex)) return true
    val bytes =
        BundledFontsMarker::class
            .java
            .getResourceAsStream("/ee/schimke/wearcmp/fonts/roboto-flex.ttf")
            ?.use { it.readBytes() } ?: return false
    WearFonts.register(WearFonts.RobotoFlex, bytes)
    return true
}

private class BundledFontsMarker
