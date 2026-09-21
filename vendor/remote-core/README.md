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

The common writer now owns the complete state declaration surface: scalar and collection constants,
named variables, expressions, lookups, text transforms, component values, colour expressions,
dynamic arrays, URL/offscreen images and bitmap fonts. Java-oracle tests pin their wire bytes. The
state implementation and its density/font conversion closure now live in `commonMain`; platform
capture supplies image data and temporarily adapts the same interface to the legacy writer while
layout, paint, modifier and action migration continues.

`RemotePath` is now a common encoded-path builder rather than a platform path wrapper. Its output
feeds path CSE directly, including explicit even-odd winding through the common writer; the Android
Compose `Path` bridge is now a one-way compatibility decoder rather than the encoding boundary.

Paint deltas and gradient/texture payloads now have a common `PaintBundleData` codec. Paint tracking,
shaders, shapes and `RemotePaint` are common code, and typed draw operations apply their paint through
`RemoteWriter`; only conversion from a platform-native Compose color filter remains an actualized
adapter.

The common writer also owns structural layout containers (root, box, row, column, flow, fit box,
canvas and state layout). Their component ids and nested content boundaries are emitted in one pass;
the JVM adapter is retained only as an oracle and temporary sink while modifier operations move to
the same common representation.

Modifier migration is typed as well: resolved modifier chains retain operation data rather than
platform modifier objects. Width, height, padding, background, rectangular clipping, offset,
z-index, ripple and draw-content already have byte-parity coverage; layout nodes continue using the
legacy sink until every modifier and text component can enter the buffered structural path together.

Full CoreText layout payloads now also have a common typed representation, including dynamic color,
font sizing, line behavior, decoration and variable-font axes. The common encoder matches the Java
oracle byte-for-byte without discovering declarations while writing nested layout content.

Image layouts are represented the same way: bitmap id, scaling, alpha and resolved modifiers are a
typed write operation with Java-oracle parity rather than a platform writer call.

Collapsible row/column containers and modifier-level macro calls now use the common structural
encoder too, including locally terminated macro-inflation blocks.

Click and touch containers now retain typed host and value-change actions. Named-action text and
expression dependencies are resolved before structural serialization, so nested actions cannot add
late declarations inside a component body.

Accessibility semantics are encoded through the same common modifier IR, including merge mode,
role, text/state ids, enabled state and clickability.

The Desktop graph substitutes all three original Maven coordinates with this project. Android keeps
the published `remote-creation` variant temporarily because its bitmap and path adapters use Android
platform types.
