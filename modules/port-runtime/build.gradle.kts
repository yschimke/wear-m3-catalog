plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

// The port's shared runtime: multiplatform stand-ins for the handful of JDK types the AndroidX
// sources reach for. Everything here is named after the type it replaces, so `transform-rules.json`
// can rewire a call site by rewriting its import and nothing else — no patch, nothing to re-cut
// when upstream edits the file around it.
