package ee.schimke.wearcmp.port

/**
 * `prefers-reduced-motion` is the web's spelling of Wear's `reduce_motion` setting, and on a
 * watch-shaped browser host it is fed by the same OS preference. This is one of the few places
 * where the port gets a BETTER answer than a stub: the platform really does know.
 *
 * Read once rather than observed. Upstream registers a `ContentObserver` and recomposes on change;
 * the media query could be watched the same way, and that is a worthwhile follow-up, but a page
 * that flips the preference mid-composition is rare enough not to hold up the port.
 */
public actual fun platformReduceMotion(): Boolean =
    js("window.matchMedia('(prefers-reduced-motion: reduce)').matches")

/** See the expect: the web does not let a page detect assistive technology. */
public actual fun platformTouchExplorationEnabled(): Boolean = false
