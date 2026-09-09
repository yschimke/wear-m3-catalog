pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    if (providers.gradleProperty("centralMirror").orNull == "true") {
      maven("https://maven-central.storage-download.googleapis.com/maven2")
    }
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    // Google first: every androidx.* coordinate lives there and nowhere else, and Gradle DISABLES
    // a repository for the rest of the build after one transport failure — so a Central hiccup
    // while it walks past looking for an AndroidX artifact fails the build on a coordinate Central
    // was never going to have.
    google()

    // Central, optionally through Google's read-only GCS mirror of it. Central rate-limits shared
    // egress IPs hard (HTTP 429 on a cold cache), which makes a first build from a sandbox or a
    // shared runner fail on resolution rather than on anything real. `-PcentralMirror=true` puts
    // the mirror in front; it serves identical bytes, and the default stays Central itself.
    if (providers.gradleProperty("centralMirror").orNull == "true") {
      maven("https://maven-central.storage-download.googleapis.com/maven2") {
        name = "GoogleCentralMirror"
      }
    }
    mavenCentral()
  }
}

rootProject.name = "wear-compose-cmp"

// The port's own shared runtime — multiplatform stand-ins for JDK types, named after what they
// replace. Not generated from anything, so it is included by hand.
include(":port-runtime")
project(":port-runtime").projectDir = file("modules/port-runtime")

// One Gradle project per AndroidX artifact, named after it and living under `modules/`. The
// artifact -> module mapping is declared once, in `upstream.json`, and read here, so adding an
// artifact to the port is a one-file edit rather than three files that can disagree.
val upstream = groovy.json.JsonSlurper().parse(file("upstream.json")) as Map<*, *>

@Suppress("UNCHECKED_CAST")
(upstream["artifacts"] as List<Map<String, String>>).forEach { entry ->
  val module = entry["module"]!!
  include(":$module")
  project(":$module").projectDir = file("modules/$module")
}
