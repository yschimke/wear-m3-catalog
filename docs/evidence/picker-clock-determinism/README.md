# The AndroidX picker samples carried the wall clock

Upstream's `TimePickerSample` family seeds each picker with `LocalTime.now()`, and the
`DatePickerSample` family with `LocalDate.now()`. Rendered as a still, that puts the machine's
clock into the bytes: the time pickers changed every minute and the date pickers every midnight,
so the visual-diff bot flagged them on pull requests that do not touch `:samples-catalog` at all.

Found on [#449](https://github.com/yschimke/wear-m3-catalog/pull/449), a change to `:catalog`'s
typefaces that `:samples-catalog` does not depend on.

## Before — two hashes at one commit

Commit `3b32aba0`, `TimePickerSamplePreview`:

```
./gradlew :samples-catalog:composePreviewRender --rerun -PcomposePreview.filter=TimePickerSample

16:55:54Z  bd75b44745fdfac6300bff72a15c7008
16:56:08Z  bbdc6ba2d1aab162cde0242e550129ff
16:56:18Z  bbdc6ba2d1aab162cde0242e550129ff
```

Two distinct hashes, splitting on the minute boundary. `--rerun` is load-bearing: without it the
render task goes `UP-TO-DATE` off its own outputs and compares a file against itself.

## The fix

`samples/patches/0001-pin-timepicker-clock.patch` and `0002-pin-datepicker-clock.patch` — patches,
not edits, because the vendored tree is upstream's bytes and an edit is reverted by the next
import (`docs/design/ANDROIDX_SAMPLES.md`).

Pinned to **`:catalog`'s own instant** — `00:00` and `2026-01-01`, the values
`catalog/src/commonMain/…/sections/Pickers.kt` already pins its picker stickers to. The compare
page puts a sample beside its kit sticker, so an instant is only worth pinning to the one the other
side already pins to; anything else makes every wheel of every cell differ.

## After — five renders, one hash each

```
run 1  17:21:27Z      run 2  17:22:04Z      run 3  17:22:40Z
run 4  17:23:26Z      run 5  17:24:07Z

601afb5591f3  TimePickerSamplePreview
906872e27add  TimePickerWithSecondsSamplePreview
601afb5591f3  TimePickerWith12HourClockSamplePreview
98ffb71967aa  TimePickerWithMinutesAndSecondsSamplePreview
e2e7a2d504c6  DatePickerSamplePreview
2b557e9cf65a  DatePickerFutureOnlySamplePreview
7f615fea4743  DatePickerYearMonthDaySamplePreview
```

Identical across four minute boundaries. `TimePickerSamplePreview` and
`TimePickerWith12HourClockSamplePreview` share a hash because at `00:00` the 24-hour and
12-hour-with-AM/PM pickers draw the same wheels.

## Two importer bugs this uncovered

Both latent, both found by being the first person to cut a patch:

1. **`applyPatches` could not apply a patch to any directory outside the working tree.**
   `git apply --directory` rejects an absolute destination as an `invalid path`, and the importer
   reported that as "upstream has moved under this patch" — the one diagnosis that is certainly
   wrong, since nothing was compared. It went unnoticed because the only test was a NEGATIVE one
   (a deliberately bad patch), which passed for the wrong reason. Fixed with `--unsafe-paths`, and
   `scripts/import-samples.test.mjs` now has the positive test; it fails without the fix.

2. **Any import deleted every committed drawable.** `fetchUpstream` sparse-checks out
   `manifest.paths` but not `manifest.resourcePaths`, so `vendorResources` found no source — and
   because it clears its destination *before* looking, all 13 drawables were removed and the run
   reported success. Fixed by putting both lists in the sparse set, and by refusing to clear the
   destination when a named subtree is missing.
