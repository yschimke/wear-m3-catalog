# Typesafe M3 catalogs: handover

Status recorded 11 October 2026. This draft contains a handover only: the implementation PRs below
have merged, and there was no uncommitted implementation left in the three catalog working trees.
The next renderer stage has not been implemented for Wear or Remote M3.

## Goals and authorization

Replace hand-authored UI-builder JSON configuration with Kotlin declarations using the catalog's
own typesafe API. Generate JSON alongside production first, prove compatibility and rendering, then
switch production when the gates pass. Complete the remaining M3 catalogs and fix blockers found.
The user authorized opening related implementation PRs and enabling auto-merge after required
checks. This handover PR is deliberately a draft and has no auto-merge enabled.

Keep annotation-owned inventory and real component implementations. Preserve saved-design IDs,
code contracts, template behavior, state/events and surface fidelity. The experimental renderer
bridge being unreleased is a reason to prove it through this trial, not a reason to stop the trial.
It is also not evidence that the trial is already ready for production.

## Landed work

| Repository | Latest merged PR | Result |
| --- | --- | --- |
| Mobile M3 | [#555](https://github.com/yschimke/m3-catalog/pull/555) | Complete parallel M3 Samples pair, with 168 compiled records preserved. |
| Wear M3 | [#778](https://github.com/yschimke/wear-m3-catalog/pull/778) | Complete parallel Wear pair with 80 records and two templates, plus Wear Samples with 198 records; two renderer fixes. |
| Remote M3 | [#83](https://github.com/yschimke/remote-m3-catalog/pull/83) | Complete parallel Remote M3 pair with 17 compiled records and five templates. |

Earlier work landed the full Mobile typed-adapter trial in
[#549](https://github.com/yschimke/m3-catalog/pull/549) and
[#550](https://github.com/yschimke/m3-catalog/pull/550), and Kotlin-authored policies in Wear
[#776](https://github.com/yschimke/wear-m3-catalog/pull/776), Remote
[#81](https://github.com/yschimke/remote-m3-catalog/pull/81), and Mobile Samples
[#553](https://github.com/yschimke/m3-catalog/pull/553).

The Wear fixes render authored alert-dialog confirm/dismiss slots, including a single action slot,
and register variable-font text using real Wear `Text`, the font registry and supported axes.
Both fixes compile on JVM and Wasm. Eight targeted JVM renderer tests passed, covering these fixes
and existing state/color behavior. The typed workflow repeats those checks against the pinned SDK.

## What complete JSON generation proves

Each repository now has an independent `catalog-typed-generator` Gradle build. It calls the
published owner `UiBuilderCatalogs.generate` API, using the existing pinned tools BOM, authored
Kotlin policies, real compiled discovery records and the existing cover sheets. No dependency
versions were bumped.

Generation preserves `components.json` byte for byte, checks the entire generated UI-builder
contract against fresh production discovery, and carries the templates with the pair. Capability
defaults are normalized with their owning protocol serializers; documentation comments are ignored.
Executable values, identities, mappings, code contracts, exclusions and diagnostics must match.
Three generator tests passed in each repository, including rejection of missing discovery and
preservation of a previous artifact when the contract changes. The complete validation scripts,
policy equivalence checks, formatting checks and shell/diff checks passed locally. All three latest
implementation PRs subsequently merged through CI.

Outputs live under `catalog-typed-generator/build/catalog/<catalog-id>/`: `components.json`,
`ui-builder.json`, the generated policy, templates where present, and `readiness.json`. These are
parallel artifacts. Production discovery/delivery still reads the committed JSON policies.
`readyForCutover` remains false. Full JSON equivalence does not prove typed renderer registration,
new source-export behavior, or packaged-runtime interactions.

## Progress since this handover

Wear gate 1 and the Wear half of gate 2 are implemented in
[#781](https://github.com/yschimke/wear-m3-catalog/pull/781)
([design note](TYPED_WEAR_RENDERER.md)). That PR adds `catalog-typed-adapters`: compiled entry points
calling the real Wear components, `TypedComponentAdapter` definitions over them, typed registrations
through the builder bridge, and a parallel `wear-m3-typed-pilot` pair plus its own packaged runtime.
It covers 17 shelf components and 5 builtins; the screen frame and the transforming list stay the
production adapters.

- 113 JVM cases match the production adapters pixel for pixel, across every offered property value.
- The packaged ZIP passes a Chromium protocol-v2 check: integrity, rejection of the production pin,
  both templates, every owned component, and a bound pointer interaction.
- Generated source fixtures compile against the public entry points.

The work also turned up two problems:

- **Builder pin.** At `1b176e18` the bridge compared each codec's `jsonType`, so it rejected the
  standard `bool`/`int` literal wrappers that tools v2.43.0 generates. The Wear typed lane now pins
  builder `5dbe7a4f` (#653) and tools v2.43.0 (`cb9e0d53`). Mobile's trial still uses the older
  pair.
- **Slider bug.** The production Wear slider ignored the offered `segmented` value. #781 fixes it.

Still open for Wear:

- production source export with callback write-back
- delivery of the pilot pair and runtime
- the five policy components that have no canvas in production either

Remote M3 (gate 1 onward) has not been started.

## Remaining implementation and gates

1. Complete compiled property-reference adapters and real typed renderer registration for Wear M3,
   followed by Remote M3. Reuse actual catalog-owned component calls and preserve receiver contexts,
   slots, defaults, bindings, events, text inspection and scrolling. Do not use placeholder components
   as evidence of completion.
2. Generate and package the parallel typed runtimes, with separate trial identities. Prove real
   rendering and interaction from the packaged artifacts, not only successful Wasm compilation.
3. Compile generated Kotlin source fixtures independently and verify saved-design compatibility.
   Test default/explicit arguments, slots, state reads and callback writeback. Mobile's advanced
   compatibility/source-export and state-callback gates also remain outstanding; do not infer a
   Mobile cutover from its existing trial coverage.
4. Preserve Remote's three surfaces: editable Wear CMP stand-ins, real Remote CMP creation/playback
   for Browser Preview, and authoritative Android Native/Live. JSON equivalence did not change or
   replace its recording/player path. Launcher widgets are a separate system, outside this M3 scope.
5. Switch production only after the relevant gates pass together. Until then, keep the parallel
   artifacts and honest readiness evidence. Open related implementation PRs and enable auto-merge
   once their required checks protect the change; leave this handover draft for review.

Mobile's existing trial is the implementation reference: `catalog-typed-adapters`,
`catalog-typed-consumer`, `catalog-typed-host`, and `scripts/verify-typed-m3-runtime.mjs`. Its prior
verification covered 40 direct-versus-typed pixel comparisons, 86 compiled source fixtures and
51 Chromium runtime cases. Those results apply to Mobile's existing trial, not to Wear or Remote.

## Resume and validate

Read each repository's `AGENTS.md` and `README.md` first. Within each repository:

```sh
scripts/validate-typed-m3-catalogs.sh
```

Wear and Remote also expose the independent policy-only check:

```sh
scripts/validate-typed-m3-policies.sh
```

The complete Remote script selects the released lane with `-PremoteSnapshot=`. Standalone policy
checks do not need Android; fresh Wear/Remote discovery does. A local Android SDK was installed
successfully, so the previous missing-SDK blocker is resolved in this workspace.

For the existing Mobile typed-adapter trial:

```sh
scripts/validate-typed-m3-catalog.sh /workspace/compose-ai-tools /workspace/compose-ui-builder
```

The source-only trial pins are tools `d4c376db8a56df2edc4217fa336eed83e5af92ef` and UI builder
`1b176e188272d86e0e22a8fd8fec5f5670da95a8`. Keep source-only coordinates explicit until released;
do not silently replace the experimental bridge with another version.

Local build setup remains in `/workspace/work/m3-gradle-env.sh` (JDKs 17/21, Gradle cache, proxy/CA
configuration and Skiko). For Android tasks set `ANDROID_HOME=/workspace/work/android-sdk`; the
installed platform is `android-37.0`. For native rendering use the existing Xvfb display `:99` if
still running and a writable `XDG_CACHE_HOME`, such as `/workspace/work/browser-cache`. These paths
are local conveniences, not portable dependencies to commit. Intermediate logs and artifacts are
under `/workspace/work/` and the modules' build directories.

Use `scripts/agent-gradle.sh` for automated local Gradle runs, as repository instructions require.
Avoid overlapping builds of the same checkout: overlapping discovery runs caused an empty-output
failure locally; a serialized rerun passed. The broad Wear property-reading test enumerates a very
large icon vocabulary; the renderer regression command in `.github/workflows/typed-catalog.yml`
selects the eight relevant tests. Do not report that targeted run as the entire renderer test suite.

## Repository constraints

- Figma is read-only. Keep inventory annotations authoritative and vendored samples unchanged.
- Renovate owns version bumps. Added aliases/dependencies use the existing pins.
- Wear stand-ins consumed by Remote are sparse-fetched at
  `.github/ci/wear-m3-catalog-ref`. A shared stand-in change needed by Remote lands in Wear first,
  then Remote updates the pin; do not edit fetched sources. The JSON-generation PR did not bump it.
- Follow Conventional Commits and the installed hooks. Use the configured human Git identity,
  with no agent attribution. Do not bypass required checks or force production cutover.
