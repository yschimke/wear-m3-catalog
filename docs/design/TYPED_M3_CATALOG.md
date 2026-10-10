# Parallel Kotlin-authored Wear M3 policy

The next stage of the M3 typesafe catalog migration starts here with **32 explicit component policies and seven builtins, plus the minimal wear-m3-samples policy**.
The committed JSON remains the production discovery input. Generation writes a separate build
artifact and does not alter catalog delivery, templates, saved designs, runtime descriptors or
inventory annotations.

`catalog-typed-policy` authors the complete policy with the owning `UiBuilderPolicyFile`,
component, builtin, menu and source-export builders. Property and slot capabilities use the owning
UI-builder protocol builders before serialization. Fields that the catalog API intentionally carries
as JSON use Kotlin JSON builders, rather than duplicating another consumer's schema. Generation
never reads the old policy; the baseline paths are used only by the independent equivalence test.
Documentation comments are omitted, while capability notes and all executable policy data remain.

The round-screen geometry, structural builtins, template order, property/slot/modifier vocabulary, source-export routing and shelf exclusions are preserved. The samples declare identity only: their vendored sources and discovery-owned shelf are untouched.

## Validate

```sh
scripts/validate-typed-m3-policies.sh
```

The JVM policy consumer is an independent Gradle build using this repository's existing version
catalog. It runs without configuring Android, the Wear/Remote libraries or renderer implementation
classes. No existing dependency pin changes. The Remote repository introduces a contracts BOM for
this new consumer on the same 3.26.0 contract line already used by the Mobile and Wear policy builds.

The test compares the entire production policy after normalizing protocol defaults and comments,
and separately checks component/builtin key order and template entries. Both the independent typed
workflow and the required Unit tests job run this validation. CI uploads generated policies and test
reports.

Generated files are beneath `catalog-typed-policy/build/catalog/<catalog>/`:

- `ui-builder.policy.json`: serialized Kotlin-authored policy.
- `readiness.json`: phase, declaration counts and outstanding cutover gates.

## Remaining work

This is the authored-policy stage, not a completed property-reference renderer migration.
`readyForCutover` remains false. Next, discover actual compiled call signatures, bind typed property,
callback and slot references, generate a portable pair, and register the same definitions in the
renderer. Then prove source-export and saved-design compatibility and exercise the packaged runtime
with real rendering, state, events and inspection. Preserve the existing surface fidelity and
structural code contracts throughout. Production consumes the Kotlin output only when those gates
pass together.
