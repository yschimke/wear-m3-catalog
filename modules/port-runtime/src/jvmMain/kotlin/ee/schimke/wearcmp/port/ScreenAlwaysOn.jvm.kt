package ee.schimke.wearcmp.port

/** A desktop JVM host has no screen timeout to defeat. */
public actual fun platformSetScreenAlwaysOn(enabled: Boolean) {}
