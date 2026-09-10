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

### Curved text is curved

This entry described straight text drawn on a tangent, and had been out of date since the Skia
port landed: `basicCurvedText`, `curvedText`, `CurvedLayout` and `TimeText` place **each glyph
individually around the arc**, at its own angle and rotation.

What made it possible is that Skia is reachable from both published targets — `skikoMain` sits
above `jvm` and `wasmJs` — and Skia has exactly the two things Compose does not expose:
`Font.getStringGlyphs` for glyph ids and advances, and `TextBlobBuilder.appendRunRSXform` for
placing each one under its own rotation and translation. The delegate is
`modules/wear-compose-foundation/src/skikoMain/.../CurvedTextDelegate.skiko.kt`.

What is still not ported is `WarpedCurvedTextRenderer`, the renderer that **warps each glyph's
outline** around the arc rather than rotating it as a rigid stamp. Upstream picks between the two,
and the one implemented here is the fallback it uses below API 34 — a shipped configuration, not a
shortcut. The difference shows at large text on a tight radius. Skia can do that too
(`Font.getPath`, and `PathVerb` matches Android's `PathIterator` verb for verb, CONIC included);
`CurvedTextStyle.warpOffset` is in the public surface and currently observed by nothing.

### One-handed gestures

The gesture API is ported — `OneHandedGestureManager` is an interface a host can implement, with
`LocalOneHandedGestureManager` to provide it, minus the `android.view.View` that every upstream
method took. What is not ported is the other half: `OneHandedGestureModifier` registers a gesture
against the `View` under the composition, and the indicators draw their hints from animated vector
drawables loaded out of `R`.

`OneHandedGestureModifier` is ported now, so the interface is live rather than dead code. Its whole
Android surface was one import and one read — `currentValueOf(LocalView)`, passed to the manager to
identify the registration — and everything else in its 352 lines is `androidx.compose.ui.node`,
which is multiplatform. The ported interface hands back an opaque `GestureRegistration` instead, so
the modifier holds the handle and gives it back on detach rather than reconstructing the arguments.

One method had to be added: upstream's manager has `updateGesture(view, oldConfig, newConfig, …)`
for changing a registration in place. Here it takes the handle, whose old configuration is implied,
and has a default that unregisters and registers again — so a host implements two methods and is
done, and overrides the third only if re-registering would cost it a round trip.

What is still not ported is the hint indicators, which draw from animated vector drawables loaded
from `R`. A host gets working gestures; it does not get the built-in visual hint.

### Dates and times

`DatePicker` and `TimePicker` are ported, and **the JVM keeps upstream's exact signature**.

Upstream takes and returns `java.time.LocalDate` and `LocalTime`, which exist only on the JVM.
Dropping them would have kept both components off the target this port exists for; changing them
would have meant a caller who compiles against the real `androidx.wear.compose.material3` could no
longer compile against this one. `PortDateTime.kt` in `:port-runtime` resolves that with an
`expect class` per type, actualised by a `typealias`:

| | `LocalDate` / `LocalTime` is | Date dependency |
| --- | --- | --- |
| JVM (and a future Android target) | `java.time.LocalDate` / `java.time.LocalTime` | none |
| wasmJs | `kotlinx.datetime.LocalDate` / `kotlinx.datetime.LocalTime` | `kotlinx-datetime` |

So `DatePicker(initialDate = LocalDate.now(), …)` compiles and links against the port unchanged on
the JVM, parameter types included, and `kotlinx-datetime` never reaches a JVM consumer's classpath.
On wasm there is no prior API to be compatible with, so the port takes the multiplatform library.

The cost is that a `typealias` cannot add members, and an `expect class` declaring `val year: Int`
would not match `java.time.LocalDate`'s Java getter — so in common code the type has **no members
at all**, and every field the pickers read goes through a `port`-prefixed `expect` extension
(`portYear`, `portMonthNumber`, `portDayOfMonth`, `portHour`, `portMinute`, `portSecond`,
`portLengthOfMonth`, and `compareTo`). The prefix is not decoration: an extension named `year`
would be silently shadowed by the member both platform types already have.

The migration is rules, not patches — `LocalDate.of(y, m, d)` → `portLocalDate(y, m, d)`,
`.monthValue` → `.portMonthNumber`, `.dayOfMonth` → `.portDayOfMonth`, and so on. Two of them are
narrowed deliberately: `.monthValue` carries a `(?!\()` lookahead because
`DatePickerState.monthValue(index)` is a *method* with the same name as the date property and a
literal rule rewrote it into nonsense, and the `.year` / `.hour` / `.minute` / `.second` rules are
pinned to their receivers (`minDate`, `initialTime`, …) because `.second` also means `Pair.second`
elsewhere in the tree. A pinned rule that stops matching after an upstream rename fails the build
with an unresolved reference — the same loud failure a patch gives, and the reason it is safe to
pin.

Only one thing in the two components is not upstream's: `date in minDate..maxDate` in `verifyDates`
needs `Comparable<LocalDate>`, and `java.time.LocalDate` declares `Comparable<ChronoLocalDate>` —
which an `expect class` supertype could not be actualised to. The range check is spelled out as two
comparisons instead, with the same test and the same message.

What is left is locale data. Upstream read it from Android — `getBestDateTimePattern`, a
`DateTimeFormatter` built from a pattern — and neither `java.time` alone nor `kotlinx-datetime`
(which does arithmetic, not presentation) answers those questions the same way.
`PlatformDateTimeFormat` in `:port-runtime` asks the platform instead, stated as the questions the
pickers ask rather than as Android's API:

| Question | JVM | wasm |
| --- | --- | --- |
| Does this locale mark its year (`2022年`)? | format `y` and look for a letter | `Intl.DateTimeFormat(tag, {year})` |
| The twelve month names | `Month.getDisplayName` | `Intl.DateTimeFormat(tag, {month})` |
| Which order does a date go in? | `getLocalizedDateTimePattern` | `formatToParts` order |
| This number, in the locale's own digits | `String.format(locale, …)` | `Intl.NumberFormat` |
| The two day-period words | `DateTimeFormatter.ofPattern("a")` | `formatToParts` dayPeriod |

Both implementations read the same CLDR data through different doors, so neither of those five is a
table this port maintains. Two things below are — `TimePatterns.kt` here and `PluralRules.kt` under
Localisation — and in both cases because the platform exposes no way to ask the question at all.

`TimePicker`'s field pattern is localised too, and by the same route: `getBestDateTimePattern` is
ICU's `DateTimePatternGenerator`, so `TimePatterns.kt` carries ICU's own answers for the 86 shipped
locales rather than a heuristic. Four skeletons each, 4 KB, generated once from ICU4J 77.1.

A table beats a rule here because the data is not guessable. Hungarian puts the day-period marker
*before* the hour, as Chinese and Korean do — an assumption that only CJK does would have been
wrong. Japanese counts its 12-hour clock with `K` (0..11) rather than `h` (1..12). Finnish
separates with a full stop. French Canadian writes `HH 'h' mm`, which upstream's own `parsePattern`
already names in a comment, because it was written to consume exactly this. 109 of the patterns
contain a NARROW NO-BREAK SPACE before the marker, so regenerate with `-Dstdout.encoding=UTF-8` or
they arrive as `?`.

The 12- versus 24-hour choice still comes from the caller's `TimePickerType`, because it is in the
skeleton the table is keyed by — a locale's own preference does not override what the caller asked
for.

### Localisation

Largely done, and this entry now records what is left rather than what is missing.

`GeneratedResources.kt` is generated from the AAR's `res/values*` — the default `values.xml` **and
the 85 localised ones** — so the strings and plurals are translated, chosen against the
composition's own `Locale.current`.

Plural forms go through the locale's real **CLDR category**, not English's rule: `PluralRules.kt`
in `commonPort` maps a language to one of fifteen rule sets, which is what the 86 shipped tags
collapse to. It is verified rather than asserted — `PluralRulesTest` compares every locale at every
count from 0 to 200 against a committed fixture generated from ICU4J, 17,286 comparisons, so a
wrong rule fails the build rather than reaching a screen reader. The fixture is committed instead
of taking an ICU dependency: the answer does not change between runs, and the test module should
not pull 14 MB of CLDR data to look it up.

The keyword is chosen per candidate tag rather than once, because the tag decides the rule as well
as the text — `pt-PT` takes `one` for 1 alone where `pt` takes it for 0 and 1, and choosing against
the locale before falling back to another tag would read a form by the wrong language's rule.

What is still not Android's behaviour is **resource resolution**: `localeCandidates` tries
`lang-REGION` then `lang`, where Android walks a full script and region fallback chain. No locale
the AAR ships needs more than the two steps, so this has not bitten; a locale that did would fall
through to the default resources rather than to a near neighbour.

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
