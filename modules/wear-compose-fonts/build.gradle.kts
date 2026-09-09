plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

// Roboto Flex, the variable font the Wear type scale is designed in, as an OPTIONAL artifact.
//
// It is separate from `:port-runtime` because it weighs 1.7 MB — more than the rest of the port
// put together, and a real cost in a browser bundle for a host that has its own copy of the font
// or is happy with a fallback. Depending on this module is the way to say you want it.
//
// The font is Roboto Flex, © the Roboto Flex Project Authors, under the SIL Open Font License 1.1
// (OFL.txt beside this file, and packaged into the jar). The OFL permits redistribution bundled
// with software; it does not permit selling the font on its own, which nothing here does.

kotlin {
  sourceSets {
    commonMain.dependencies { api(project(":port-runtime")) }
  }
}
