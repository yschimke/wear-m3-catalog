# Remote write core

This module merges the JVM sources that AndroidX publishes as `remote-core`,
`remote-creation-core`, and `remote-creation` at patch set 17 of CL 4307936. It is the source base
for phases 3–4 of the port.

Already removed or disconnected:

- the `androidx.compose.remote.creation.json` document importer;
- player operation-registry construction during document creation;
- independently unreachable execution helpers (`SystemClock`, `RemoteContextActions`, recording
  replay support, and platform paint adapters);
- the KMP `expect` wrapper around `RemotePath`, flattened to its JVM implementation.

The remaining operation classes still mix their static wire-writing functions with decoder and
player methods. They cannot be deleted package-by-package: `RemoteComposeWriter` calls those static
functions directly, and Java therefore type-checks the whole mixed class. Phase 4 should replace
those classes with Kotlin write codecs over `WireBuffer`; once callers use the codecs, the operation
objects, layout managers, evaluator, event, and semantics execution trees can be removed together.

The portability boundary is the document writer, not a cross-platform imitation of Android's
`RecordingCanvas`. Creation Compose already lowers its drawing, layout, and state APIs to writer
calls. Shared code must call those high-level writer methods without reaching through to
`RemoteComposeBuffer`; platform canvas adapters remain compatibility inputs only. Animation packing
is the first utility moved all the way to `commonMain` under that rule.

Creation Compose retains a typed `RemoteDocumentProgram` above the writer. It owns expression CSE
and hoisting, dependency ordering, dead-scope pruning, save/restore elision, and transform fusion.
Optimization and serialization are separate phases. Its declaration preamble is written before the
body structurally. Global state now enters that preamble directly; the old writer
`beginGlobal`/`endGlobal` API and `WireBuffer.moveBlock` byte relocation have been removed.

`:vendor:remote-write-core` is the common Kotlin write-side module. It currently owns the growable
wire sink, the writer-shaped document builder and canvas writer interface, animation packing
dependencies, protocol IDs, layout/text constants, colour conversion, deterministic number
formatting, and easing primitives. Creation Compose retains remote values in typed `WriterOp`
nodes until optimization is complete, then lowers primitive drawing, clipping, transforms, path
data, text, and scaled bitmaps through that interface. Conditional, loop, and offscreen scopes are
typed composite program nodes rather than writer-capturing draw lambdas. JVM and Android use a
temporary adapter to the Java writer; the common encoder implements the same interface directly.
JVM parity tests compare those operations, protocol primitives, and the 149-byte smoke document
with this Java core while the remaining paint, modifier, action, layout, state, and platform-image
calls are migrated.

The common writer now also owns scalar and collection declaration primitives: integer, float,
long, colour and string constants, named variables, reserved float IDs, float arrays and ID lists.
Creation Compose routes those state allocations through the common interface; expression codecs and
lookup/attribute operations are the remaining state-side dependency on the Java writer.

The Desktop graph substitutes all three original Maven coordinates with this project. Android keeps
the published `remote-creation` variant temporarily because its bitmap and path adapters use Android
platform types.
