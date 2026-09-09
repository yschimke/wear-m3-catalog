package ee.schimke.wearcmp.port

/** A desktop JVM host has no watch settings to read. See the expects for why `false` is honest. */
public actual fun platformReduceMotion(): Boolean = false

public actual fun platformTouchExplorationEnabled(): Boolean = false
