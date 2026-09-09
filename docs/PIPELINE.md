# The pipeline

How AndroidX Wear Compose becomes a wasm library, what each stage is allowed to do, and what to do
when one of them fails.

## The four mechanisms, in order of preference

Everything the port does to a source file is one of four things. They are listed in the order you
should reach for them, and the order is the whole design: the further down this list a change sits,
the more likely it is to need a human at the next AndroidX release.

### 1. A rule (`transform-rules.json`)

Textual, applied to every file, survives upstream refactors. Four kinds:

- **`dropImports` / `dropAnnotations`** — Android build tooling with no runtime meaning:
  `@SuppressLint`, `@RequiresApi`, `androidx.annotation.ColorInt`.
- **`rewriteReferences`** — a literal rename, applied to the whole file rather than just its
  imports, because AndroidX writes at least one fully-qualified type inline. This is where the
  leverage is. `LocalConfiguration` → `LocalWearDeviceConfiguration` ports a dozen Material 3 files
  by itself, because `WearDeviceConfiguration` was deliberately given Android's own property names.
- **`rewritePatterns`** — a regex, for the one shape a rename cannot reach (the arc fonts' 
  `Font(DeviceFontFamilyName(…), …).toFontFamily()`).
- **`ensureImports`** — adds an import the Kotlin/JVM compiler used to supply for free
  (`kotlin.jvm.JvmInline` and friends are default-imported on JVM and not in common code).

### 2. A shim with the same name

A hand-written declaration in `modules/port-runtime` or a module's `src/commonPort`, named exactly
after what it replaces, so that a rule from (1) is enough to reach it and the call sites never
change. `AtomicReference`, `Log`, `Predicate`, `ColorUtils`, `materialIcon` are all this.

This is the trick that keeps the patch count at twelve. Naming the replacement after the original
converts what would be a patch per call site into one line of JSON.

### 3. A patch (`patches/<module>/<same path as the source>.patch`)

A real diff, one per file, cut by `tools/make_patch.py`:

```bash
scripts/regenerate.sh                                   # start from pristine output
$EDITOR modules/<module>/src/commonMain/kotlin/<path>   # make the edit
tools/make_patch.py <module> <path>                     # record it
scripts/regenerate.sh                                   # prove it replays
```

The pristine side of the diff is recomputed from `upstream/` with the same rules, so the patch
contains your edit and nothing else even if the file already had one.

### 4. An exclusion (`transform-rules.json` → `modules.<module>.exclude`)

The file is dropped from `commonMain`, with a reason that ends up in
[`ANDROID_SURFACE.md`](ANDROID_SURFACE.md). Two very different cases share the mechanism:

- **Replaced** — a hand-written file under `src/commonPort` restates the API and implements it
  differently (`TouchExplorationStateProvider`, `rotary/Haptics`, `KeepScreenOn`, `Strings`).
  Preferred over a patch when the Android body is most of the file: a patch that deletes 120 of 138
  lines has to be re-cut every time any of them moves.
- **Not yet ported** — the file is gone and nothing replaces it. Always says so in capitals in its
  reason, so the report reads as a worklist.

## When a patch stops applying

`tools/transform.py` stops the run and names the patch. This is working as intended — upstream moved
under a hand-written port, and only a human can say what it now means.

```bash
rm patches/<module>/<path>.patch     # drop the stale patch
scripts/regenerate.sh                # regenerate pristine
# re-read the upstream file: does the port still make sense against what it now says?
$EDITOR modules/<module>/src/commonMain/kotlin/<path>
tools/make_patch.py <module> <path>
scripts/regenerate.sh && ./gradlew assemble
```

If the patch turns out to be unnecessary — a rule now covers it, or upstream changed the code so it
no longer needs porting — delete it and do not replace it. `make_patch.py` removes an empty patch
for you.

## Updating to a new AndroidX release

1. Edit `version` in `upstream.json`; reset `portRevision` to 1.
2. `scripts/sync-upstream.sh`
3. `./gradlew assemble`
4. Read the diff. `upstream/` says what AndroidX changed; `modules/` says what it meant here;
   `docs/ANDROID_SURFACE.md` says whether the Android surface grew.

`.github/workflows/upstream-sync.yml` does exactly this on a schedule and opens a pull request, so
the usual path is reviewing that PR rather than running the steps.

Three things can go wrong, and all three fail loudly:

- **A patch no longer applies** — see above.
- **A new Android dependency appears** — the transform reports it and the build fails to compile.
  `ANDROID_SURFACE.md` names the file and the import.
- **A rule silently stops matching** — the only quiet failure mode, and the reason `rewritePatterns`
  has exactly two entries. It surfaces as a compile error, not as wrong output.

## What is not ported, and what it would take

### Curved text

`BasicCurvedText` and `WarpedCurvedTextRenderer` in foundation, and everything downstream of them:
`CurvedText`, `TimeText`, `ConfirmationDialog`, `OpenOnPhoneDialog`.

Upstream lays a text run out with `android.text.StaticLayout`, shapes it to glyphs with
`TextRunShaper`/`PositionedGlyphs`, and warps those glyphs around a `Path` measured with
`PathMeasure`. Compose Multiplatform exposes no glyph-level shaping — `TextMeasurer` measures and
draws, but will not hand back positioned glyphs — so this is an implementation rather than a seam.

The tractable approximation is per-character: measure each character with `TextMeasurer`, place it
at its angle around the arc, and rotate it. That is how most non-Android curved text works, it
handles the Latin case the catalog needs, and it is wrong for scripts with contextual shaping. It
would unblock five components.

### Dates and times

`DatePicker`, `TimePicker`, `TimeText`. `java.time` arithmetic maps cleanly onto `kotlinx-datetime`;
what does not map is the locale-derived field order and 12/24-hour pattern that upstream gets from
`DateFormat.getBestDateTimePattern`. `Intl.DateTimeFormat().formatToParts()` answers the same
question in a browser, which makes this a wasm-side seam rather than a blocker.

### Localisation

`GeneratedResources.kt` is generated from the AAR's default `res/values/values.xml`. The AAR ships
80 more locales beside it, and the generator reads none of them. Extending it is mechanical —
generate a map per locale, key the lookup on the composition's locale — and nothing in the current
design is in the way.

### Dynamic colour and one-handed gestures

Neither has an off-Android meaning. Dynamic colour reads the wearer's watch-face palette out of
platform resources; the one-handed-gesture surface is a Wear system service plus animated vector
drawables. Both stay excluded on purpose rather than as a backlog item.

## Why the sources come from Google Maven and not the monorepo

`github.com/androidx/androidx` has no release tags at all — `git ls-remote --tags` returns nothing —
so a git checkout can only ever be pinned to a commit sha, which is not the thing that "updates when
a release happens". The per-release `-sources.jar` on Google Maven is the same `src/main`, is
addressed by the exact version string, is a megabyte rather than five gigabytes, and has
`group-index.xml` beside it listing every published version — which is what the sync workflow polls.

It also excludes tests, samples and benchmarks by construction, which the monorepo would have made
us filter out.
