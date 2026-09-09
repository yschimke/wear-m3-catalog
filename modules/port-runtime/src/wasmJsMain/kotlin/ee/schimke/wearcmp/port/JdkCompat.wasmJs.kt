package ee.schimke.wearcmp.port

/**
 * Kotlin/Wasm's default `Any.hashCode()` is already identity-based — it is assigned per object and
 * never derived from state — so for a class that has not overridden it this IS the identity hash.
 * Every call site in the ported sources is exactly that case: a class defining
 * `hashCode() = System.identityHashCode(this)` because it wants reference identity.
 */
public actual fun identityHashCode(value: Any): Int = value.hashCode()
