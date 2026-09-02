package ee.schimke.wearm3catalog.remote

/**
 * Which artifact line this test run is measuring — see the `remoteSnapshot` property in
 * `remote-catalog/build.gradle.kts`.
 *
 * `false` is the committed default: the released alphas `gradle/libs.versions.toml` pins. `true`
 * means the run was started with `-PremoteSnapshot=<androidx.dev build id>`, so the two Remote
 * groups resolve to `1.0.0-SNAPSHOT` and the sheet is drawing against unreleased code.
 *
 * Only the fixtures that record LIBRARY behaviour should branch on this — `knownDuplicate`,
 * `knownBlank`, and anything else whose entries are claims about what the library draws. A test
 * that branches on the lane for any other reason is testing two different things and should be two
 * tests.
 */
internal val onSnapshotLane: Boolean = System.getProperty("wearm3.remoteLane") == "snapshot"
