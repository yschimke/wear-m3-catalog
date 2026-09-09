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

### Curved text is straight

Ported and drawing, but **not curved**. `basicCurvedText`, `curvedText`, `CurvedLayout` and
`TimeText` all render; each run is measured with the real font, then drawn as a single straight
line, rotated to the tangent of its arc and centred on it.

Where that is right and where it is wrong: the run and the arc agree exactly at the centre of the
sweep and diverge towards its ends, by more the longer the run and the tighter the radius. The
labels Wear actually curves — a time, a screen title, a button caption — read correctly. A run
sweeping more than roughly a quarter turn will leave the band the layout allotted it.

The measurement is faithful either way, which is the part that matters structurally: width, height
and baseline are a real measurement of the real font, so the surrounding curved layout allots the
right sweep and everything positioned relative to the text lands where it should.

Why it is not curved: upstream shapes the run to glyphs with `android.text.TextRunShaper`, reads
their positions out of `PositionedGlyphs`, and then either draws the run along a `Path` or warps
each glyph's outline around one with a `PathIterator`
(`WarpedCurvedTextRenderer`, still excluded). Compose Multiplatform publishes none of that:
`TextMeasurer` will measure and draw a run but will not hand back positioned glyphs, and there is
no path-drawing text API.

The next step is per-character placement — measure each character, walk them around the arc at
their own angles, rotate each to its own tangent. It is a real improvement for the Latin text the
catalog draws, and still wrong for scripts whose glyphs change shape in context, which is exactly
why upstream shapes the whole run first. The seam to change is
`modules/wear-compose-foundation/src/commonPort/.../CurvedTextDelegate.kt`; nothing above it needs
to know.

### One-handed gestures

The gesture API is ported — `OneHandedGestureManager` is an interface a host can implement, with
`LocalOneHandedGestureManager` to provide it, minus the `android.view.View` that every upstream
method took. What is not ported is the other half: `OneHandedGestureModifier` registers a gesture
against the `View` under the composition, and the indicators draw their hints from animated vector
drawables loaded out of `R`.

So today nothing in the port calls the interface. Porting the modifier over it is the follow-up
that makes it live, and everything above the modifier then works unchanged.

### Dates and times

`DatePicker` and `TimePicker` are ported, and they are the port's **one deliberate API change**.

Upstream takes and returns `java.time.LocalDate` and `LocalTime`, which exist only on the JVM.
Keeping that signature would have meant keeping both components off the target this port exists
for, so they move to `kotlinx-datetime`, which has the same shapes and publishes a wasmJs target. A
caller writes `kotlinx.datetime.LocalDate` where upstream documents `java.time.LocalDate`;
everything else about the two components is unchanged.

The type migration is rules, not patches — `LocalDate.of(y, m, d)` → `LocalDate(y, m, d)`,
`monthValue` → `month.number`, `dayOfMonth` → `day`. One of those rules needed a lookahead:
`DatePickerState.monthValue(index)` is a *method* with the same name as the date property, and a
literal rule rewrote it into nonsense.

What is left is locale data, which `kotlinx-datetime` deliberately does not carry — it does
arithmetic, not presentation. `PlatformDateTimeFormat` in `:port-runtime` asks the platform
instead, stated as the questions the pickers ask rather than as Android's
`getBestDateTimePattern`:

| Question | JVM | wasm |
| --- | --- | --- |
| Does this locale mark its year (`2022年`)? | format `y` and look for a letter | `Intl.DateTimeFormat(tag, {year})` |
| The twelve month names | `Month.getDisplayName` | `Intl.DateTimeFormat(tag, {month})` |
| Which order does a date go in? | `getLocalizedDateTimePattern` | `formatToParts` order |
| This number, in the locale's own digits | `String.format(locale, …)` | `Intl.NumberFormat` |
| The two day-period words | `DateTimeFormatter.ofPattern("a")` | `formatToParts` dayPeriod |

Both implementations read the same CLDR data through different doors, so neither is a table this
port maintains.

**TODO, in `TimePicker`:** the field pattern is the skeleton as written, not localised. Upstream
passes it through `getBestDateTimePattern`, which reorders fields and swaps separators for the
locales that need it — most visibly the ones that put the am/pm marker first (`ah:mm` in Chinese).
The 12- versus 24-hour choice is *not* lost: it is in the skeleton, chosen by the caller's
`TimePickerType`.

### Localisation

`GeneratedResources.kt` is generated from the AAR's default `res/values/values.xml`. The AAR ships
80 more locales beside it, and the generator reads none of them. Extending it is mechanical —
generate a map per locale, key the lookup on the composition's locale — and nothing in the current
design is in the way.

### Animated vector drawables do not animate

`ConfirmationDialog` and `OpenOnPhoneDialog` draw their icons with `AnimatedVectorDrawable`s: the
check mark draws itself on, the phone icon animates. Both components are ported and both icons are
upstream's own artwork, parsed by Compose Multiplatform's resource pipeline — which reads Android
`<vector>` XML on every target, wasm included. What is missing is the animation.

Compose Multiplatform publishes the AVD *model* — `AnimatedImageVector`, `ObjectAnimator`,
`Keyframe`, `AnimatorSet` are all in the wasm klib — but not the `androidx.compose.animation
.graphics.res` package that parses the XML into one, and not `rememberAnimatedVectorPainter` that
plays it. Those are Android-only.

So `tools/transform.py` freezes each AVD at its **last frame**: it reads every `<target>`'s
animators for the value they end on and writes that onto the named element. Without that step the
extracted vector is the animation's *first* frame, and for a drawing animation the first frame is
blank — the check mark's path carries `trimPathEnd="0"`. A rendered preview caught exactly that.

Two ways forward, if the animation matters: implement the painter over the model CMP already ships
(the subset in use is `trimPathEnd`, `translate`, `scale`, `alpha` and `pathData`), or wait for
Compose Multiplatform to publish `animatedVectorResource` off-Android.

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
