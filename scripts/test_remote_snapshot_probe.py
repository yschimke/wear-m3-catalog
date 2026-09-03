#!/usr/bin/env python3
"""Tests for the pure logic in remote-snapshot-probe.py.

The image measurement needs Pillow and a rendered sheet, so it is exercised by
the job itself rather than here. What IS covered is everything that decides
whether a human gets pinged — the comparison, the verdict and the overlay — and
the overlay's anchors, which fail closed if the build files move under them.
"""

from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path

_spec = importlib.util.spec_from_file_location(
    "probe", Path(__file__).resolve().parent / "remote-snapshot-probe.py"
)
probe = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(probe)


def report(*, compiled=True, captures=None, probes=None, build_id="1", awaited_api=None):
    return {
        "build": {"buildId": build_id, "lastUpdated": "20260101010101", "compiled": compiled,
                  "awaitedApi": awaited_api or {}},
        "captures": captures if captures is not None else {"A": "sha-a", "B": "sha-b"},
        "probes": probes
        if probes is not None
        else [
            {"issue": 89, "preview": "P", "summary": "s", "rendered": True,
             "identicalToKnownBroken": True, "metrics": {}},
        ],
    }


class BuildIdTest(unittest.TestCase):
    def test_reads_the_id_out_of_a_redirected_url(self):
        self.assertEqual(
            probe.build_id_from_url(
                "https://androidx.dev/snapshots/builds/16201507/artifacts/repository/a/b.xml"
            ),
            "16201507",
        )

    def test_a_url_with_no_build_id_is_fatal(self):
        # Fail closed: a silently-unpinned probe tests whatever `latest` moves to
        # mid-run, which is not reproducible and not worth reporting on.
        with self.assertRaises(SystemExit):
            probe.build_id_from_url("https://androidx.dev/snapshots/latest/artifacts")

    def test_last_updated_is_optional(self):
        self.assertEqual(probe.last_updated("<lastUpdated>20260828055457</lastUpdated>"),
                         "20260828055457")
        self.assertIsNone(probe.last_updated("<metadata/>"))


class FingerprintGateTest(unittest.TestCase):
    """The gate that decides whether a week costs a runner or thirty seconds.

    androidx.dev publishes many builds a day and most carry byte-identical Remote
    artifacts — 16201507 and 16202139, two hours apart, ship the same AAR for all
    three groups under different timestamps. Gating on the build ID would render
    the whole sheet weekly to learn nothing."""

    FP = {
        "compose-remote": {"version": "1.0.0-20260828.073841-1", "sha256": "aaa"},
        "wear-compose-remote": {"version": "1.0.0-20260828.073841-1", "sha256": "bbb"},
        "glance-wear": {"version": "1.0.0-20260828.073841-1", "sha256": "ccc"},
    }

    def _previous(self, fingerprint):
        return {"build": {"buildId": "1", "compiled": True, "fingerprint": fingerprint}}

    def test_same_bytes_under_a_new_timestamp_is_not_worth_a_render(self):
        older = {ref: {"version": "1.0.0-20260828.055457-1", "sha256": v["sha256"]}
                 for ref, v in self.FP.items()}
        self.assertTrue(probe.unchanged_artifacts(self._previous(older), self.FP))

    def test_a_changed_artifact_is_worth_a_render(self):
        moved = dict(self.FP, glance_wear=None)
        moved = {**self.FP, "glance-wear": {"version": "x", "sha256": "MOVED"}}
        self.assertFalse(probe.unchanged_artifacts(self._previous(self.FP), moved))

    def test_the_first_run_always_renders(self):
        self.assertFalse(probe.unchanged_artifacts(None, self.FP))

    def test_a_group_appearing_or_vanishing_always_renders(self):
        # The trio moves together; a set that no longer lines up is exactly when
        # a human wants the render rather than a skip.
        fewer = {k: v for k, v in self.FP.items() if k != "glance-wear"}
        self.assertFalse(probe.unchanged_artifacts(self._previous(fewer), self.FP))

    def test_an_unfingerprinted_run_always_renders(self):
        self.assertFalse(probe.unchanged_artifacts(self._previous(self.FP), {}))


class CompareTest(unittest.TestCase):
    def test_an_unchanged_run_is_silent(self):
        same = report()
        self.assertFalse(probe.compare(same, same)["report"])

    def test_the_first_run_is_silent_too(self):
        # Nothing to compare against, and every probe is still broken — the
        # interesting output is the state it leaves behind, not a ping.
        verdict = probe.compare(None, report())
        self.assertFalse(verdict["report"])
        self.assertTrue(verdict["firstRun"])

    def test_a_failed_compile_is_always_reported(self):
        verdict = probe.compare(report(), report(compiled=False, probes=[]))
        self.assertTrue(verdict["report"])
        self.assertIn("no longer compiles", verdict["reasons"][0])

    def test_a_failed_compile_does_not_also_claim_every_render_vanished(self):
        verdict = probe.compare(report(), report(compiled=False, captures={}, probes=[]))
        self.assertEqual(len(verdict["reasons"]), 1)
        self.assertEqual(verdict["removedCaptures"], [])

    def test_a_probe_leaving_its_known_broken_capture_is_reported(self):
        after = report(probes=[{"issue": 91, "preview": "P", "summary": "s", "rendered": True,
                                "identicalToKnownBroken": False, "metrics": {}}])
        verdict = probe.compare(report(), after)
        self.assertTrue(verdict["report"])
        self.assertEqual([p["issue"] for p in verdict["flipped"]], [91])

    def test_a_probe_that_is_already_off_its_baseline_keeps_reporting(self):
        # It stays loud run after run rather than being "already seen": until a
        # human closes the issue, a bug that stopped reproducing is the single
        # most interesting thing this job can say.
        off = report(probes=[{"issue": 91, "preview": "P", "summary": "s", "rendered": True,
                              "identicalToKnownBroken": False, "metrics": {}}])
        self.assertTrue(probe.compare(off, off)["report"])

    def test_a_probe_watching_nothing_is_reported(self):
        # The regression #116 caused and nothing caught: a probe whose preview was folded into a
        # cell matched no render, printed "not rendered", and added no reason -- so #90 and #91
        # watched nothing for weeks while the report still looked complete.
        blind = report(probes=[{"issue": 91, "preview": "GoneAway", "summary": "s",
                                "rendered": False, "identicalToKnownBroken": None, "metrics": {}}])
        verdict = probe.compare(report(), blind)
        self.assertTrue(verdict["report"])
        self.assertTrue(any("watching nothing" in r for r in verdict["reasons"]))
        self.assertTrue(any("#91" in r for r in verdict["reasons"]))

    def test_a_failed_compile_does_not_also_claim_every_probe_went_blind(self):
        # A build that did not compile rendered nothing by definition; the compile reason already
        # says so, and four "watching nothing" lines under it would bury it.
        verdict = probe.compare(
            report(),
            report(compiled=False, captures={},
                   probes=[{"issue": 91, "preview": "P", "summary": "s", "rendered": False,
                            "identicalToKnownBroken": None, "metrics": {}}]),
        )
        self.assertEqual(len(verdict["reasons"]), 1)
        self.assertIn("no longer compiles", verdict["reasons"][0])

    def test_a_moved_capture_is_reported_with_its_name(self):
        verdict = probe.compare(report(), report(captures={"A": "sha-a", "B": "moved"}))
        self.assertTrue(verdict["report"])
        self.assertEqual(verdict["movedCaptures"], ["B"])

    def test_added_and_removed_captures_are_counted(self):
        verdict = probe.compare(report(), report(captures={"A": "sha-a", "C": "sha-c"}))
        self.assertEqual((verdict["addedCaptures"], verdict["removedCaptures"]), (["C"], ["B"]))


class SummaryTest(unittest.TestCase):
    def test_it_names_the_build_and_every_probe(self):
        text = probe.summary_markdown(report(build_id="123"), probe.compare(None, report()))
        self.assertIn("build [123]", text)
        self.assertIn("#89", text)
        self.assertIn("never claims a bug is fixed", text)

    def test_metrics_read_as_markdown_not_python(self):
        # Shapes, not specific metrics: the formatter has to render whatever a probe measures, so
        # the names here are stand-ins. They used to be #89's and #90's, which no longer exist.
        text = probe._render_metric("heights_dp", {"B": 16, "A": 16})
        self.assertEqual(text, "`heights_dp` A=16, B=16")
        self.assertEqual(probe._render_metric("glyph_dp", [2, 1]), "`glyph_dp` 2x1")


class OverlayTest(unittest.TestCase):
    SETTINGS = (
        "dependencyResolutionManagement {\n  repositories {\n    mavenCentral()\n"
        "    google()\n  }\n}\n"
    )
    TOML = (
        'compose-remote = "1.0.0-alpha18"\n'
        'wear-compose-remote = "1.0.0-alpha10"\n'
        'glance-wear = "1.0.0-alpha17"\n'
    )

    # Only the shape `apply_density_sweep` patches: a dpi=320 `@Preview` directly above each
    # multipreview annotation it sweeps.
    THEME = (
        '@Preview(showBackground = false, device = "spec:width=227dp,height=100dp,dpi=320")\n'
        "annotation class CatalogRemoteModes\n\n"
        '@Preview(showBackground = false, device = "spec:width=227dp,height=200dp,dpi=320")\n'
        "annotation class CatalogRemoteLarge\n"
    )

    def _tree(self, settings=None, toml=None, theme=None):
        root = Path(tempfile.mkdtemp())
        (root / "gradle").mkdir()
        (root / "settings.gradle.kts").write_text(self.SETTINGS if settings is None else settings)
        (root / "gradle" / "libs.versions.toml").write_text(self.TOML if toml is None else toml)
        source = root / probe.SWEEP_SOURCE
        source.parent.mkdir(parents=True, exist_ok=True)
        source.write_text(self.THEME if theme is None else theme)
        return root

    def test_it_adds_the_repository_and_pins_all_three_refs(self):
        root = self._tree()
        changed = probe.apply_overlay(root, "16201507")
        settings = (root / "settings.gradle.kts").read_text()
        toml = (root / "gradle" / "libs.versions.toml").read_text()
        self.assertIn("/snapshots/builds/16201507/artifacts/repository", settings)
        self.assertIn("includeGroupByRegex", settings)
        # The trio MOVES TOGETHER — a partial overlay is the skew AGENTS.md warns
        # about, and would fail inside the player rather than at compile time.
        self.assertEqual(toml.count('"1.0.0-SNAPSHOT"'), 3)
        # Every edit is reported, because the report is what tells a human reading the run what
        # the probe actually tested. Asserted by CONTENT rather than by count: a bare number says
        # nothing about which edit went missing, and it was a bare number that had to be updated
        # rather than examined when the density sweep was added.
        self.assertEqual(
            sorted(changed),
            sorted(
                [
                    "settings.gradle.kts: + snapshot repository for build 16201507",
                    "CatalogRemoteModes: + dpi 160, 240, 480",
                    "CatalogRemoteLarge: + dpi 160, 240, 480",
                    "compose-remote: 1.0.0-alpha18 -> 1.0.0-SNAPSHOT",
                    "wear-compose-remote: 1.0.0-alpha10 -> 1.0.0-SNAPSHOT",
                    "glance-wear: 1.0.0-alpha17 -> 1.0.0-SNAPSHOT",
                ]
            ),
        )

    def test_the_group_regexes_survive_kotlin_string_escaping(self):
        # The block is written verbatim into a Kotlin string literal, where `\.` is an
        # "unsupported escape sequence" and kills script compilation before a single dependency
        # resolves — so the emitted text needs `\\.`. Caught exactly once, by a rehearsal;
        # pinned here so it cannot come back as a mystery red build.
        block = probe.repository_block("16201507")
        self.assertIn(r'includeGroupByRegex("androidx\\.compose\\.remote.*")', block)
        self.assertNotIn(r'("androidx\.compose', block)
        self.assertIn("/snapshots/builds/16201507/artifacts/repository", block)

    def test_the_sweep_adds_every_density_to_every_annotation(self):
        root = self._tree()
        probe.apply_overlay(root, "123")
        text = (root / probe.SWEEP_SOURCE).read_text()
        for dpi in probe.SWEEP_DPI:
            self.assertIn(f"height=100dp,dpi={dpi}", text)
            self.assertIn(f"height=200dp,dpi={dpi}", text)
        # The committed density survives, and is not duplicated.
        self.assertEqual(text.count("height=100dp,dpi=320"), 1)
        # Each annotation still follows its own previews.
        self.assertIn('dpi=480")\nannotation class CatalogRemoteModes', text)
        self.assertIn('dpi=480")\nannotation class CatalogRemoteLarge', text)

    def test_a_renamed_sweep_annotation_is_fatal(self):
        # Failing closed matters more here than anywhere: a sweep that silently stopped happening
        # leaves the probe measuring one density again, which is the exact blindness #89 and #90
        # asked it to stop having.
        root = self._tree(theme='@Preview(device = "dpi=320")\nannotation class SomethingElse\n')
        with self.assertRaises(SystemExit):
            probe.apply_overlay(root, "123")

    def test_a_sweep_anchor_that_is_not_the_capture_density_is_fatal(self):
        root = self._tree(
            theme='@Preview(showBackground = false, device = "spec:dpi=213")\n'
            "annotation class CatalogRemoteModes\n"
        )
        with self.assertRaises(SystemExit):
            probe.apply_overlay(root, "123")

    def test_a_moved_settings_anchor_is_fatal(self):
        # Rather than leaving the repository out and quietly probing the
        # RELEASED artifacts, which would report "still broken" forever.
        root = self._tree(settings="dependencyResolutionManagement { repositories { } }\n")
        with self.assertRaises(SystemExit):
            probe.apply_overlay(root, "1")

    def test_a_missing_version_ref_is_fatal(self):
        root = self._tree(toml='compose-remote = "1.0.0-alpha18"\n')
        with self.assertRaises(SystemExit):
            probe.apply_overlay(root, "1")

    def test_it_refuses_to_stack_a_second_overlay(self):
        root = self._tree()
        probe.apply_overlay(root, "1")
        with self.assertRaises(SystemExit):
            probe.apply_overlay(root, "2")


class FindRenderTest(unittest.TestCase):
    """Addressing a render by preview and cell, which is what #116's fold made necessary."""

    def _renders(self, *names):
        directory = Path(tempfile.mkdtemp())
        for name in names:
            (directory / name).write_bytes(b"")
        return directory

    def test_a_base_capture_is_not_matched_by_its_cells(self):
        directory = self._renders(
            "FilledRemoteButton_width_227dp-aaaaaaaa.png",
            "FilledRemoteButton_width_227dp_VARIANT_disabled-bbbbbbbb.png",
        )
        found = probe.find_render(directory, "FilledRemoteButton")
        self.assertEqual(found.name, "FilledRemoteButton_width_227dp-aaaaaaaa.png")

    def test_a_cell_is_addressed_by_name(self):
        directory = self._renders(
            "FilledRemoteButton_width_227dp-aaaaaaaa.png",
            "FilledRemoteButton_width_227dp_VARIANT_disabled-bbbbbbbb.png",
            "FilledRemoteButton_width_227dp_VARIANT_icon-cccccccc.png",
        )
        found = probe.find_render(directory, "FilledRemoteButton", "disabled")
        self.assertEqual(found.name, "FilledRemoteButton_width_227dp_VARIANT_disabled-bbbbbbbb.png")

    def test_a_cell_prefix_does_not_match_a_longer_cell(self):
        # `icon` must not match `icon_large`, or a probe silently reads the wrong picture.
        directory = self._renders("FilledRemoteButton_w_VARIANT_icon_large-aaaaaaaa.png")
        self.assertIsNone(probe.find_render(directory, "FilledRemoteButton", "icon"))

    def test_a_missing_render_is_none_rather_than_a_crash(self):
        self.assertIsNone(probe.find_render(self._renders(), "Nope"))

    def test_the_baseline_density_is_preferred_over_sort_order(self):
        # THE BUG THIS PINS. This job's own overlay renders every probe at four densities, and
        # `dpi_160` sorts before `dpi_320` — so taking the first match compared a 160dpi capture
        # against the 320dpi baseline in `docs/evidence`. Never equal, so `identicalToKnownBroken`
        # was stuck False and the probe could state neither of its two verdicts.
        directory = self._renders(
            *(f"EdgeButtonRemote_width_227dp_height_100dp_dpi_{dpi}-{dpi:08d}.png"
              for dpi in (160, 240, 320, 480))
        )
        found = probe.find_render(directory, "EdgeButtonRemote")
        self.assertIn("dpi_320", found.name)

    def test_a_render_at_no_baseline_density_is_still_found(self):
        # The fallback matters: a render captured at some other density must be returned rather
        # than reported missing, or a frame change would read as "not rendered".
        directory = self._renders("EdgeButtonRemote_width_227dp_height_100dp_dpi_480-aaaaaaaa.png")
        self.assertIsNotNone(probe.find_render(directory, "EdgeButtonRemote"))


class LabelSpillTest(unittest.TestCase):
    """#249's measure: how far content is drawn outside its own container.

    The fixtures draw a container and then a GLYPH RUN over it — a row of small blocks with gaps,
    not a solid bar. That is not decoration: the measure identifies the container as the dominant
    opaque colour and then checks it is actually solid, so a fixture whose "label" is one filled
    rectangle would outweigh the container and cover its own bounding box completely, testing
    neither behaviour. Glyphs are sparse, and that is what the real renders look like.
    """

    CONTAINER = (40, 10, 159, 49)  # 120 x 40

    def _image(self, left, right, fill=(233, 221, 255), solid=True):
        """A container plus a glyph run spanning exactly ``left``..``right`` inclusive."""
        from PIL import Image, ImageDraw

        image = Image.new("RGBA", (200, 60), (0, 0, 0, 0))
        draw = ImageDraw.Draw(image)
        if solid:
            draw.rectangle(self.CONTAINER, fill=fill + (255,))
        else:
            draw.rectangle(self.CONTAINER, outline=fill + (255,), width=2)
        x = left
        while x + 3 < right - 3:
            draw.rectangle((x, 20, x + 3, 39), fill=(20, 20, 30, 255))
            x += 14
        # Pinned flush to the right edge, so the run's extent is the two arguments and not
        # wherever the stride happened to stop — which is what made the first draft of this
        # fixture assert a symmetric overhang it had not actually drawn.
        draw.rectangle((right - 3, 20, right, 39), fill=(20, 20, 30, 255))
        return image

    def test_content_inside_the_container_is_no_spill(self):
        self.assertEqual(probe.label_spill_dp(self._image(60, 139), 2.0), 0.0)

    def test_content_past_both_edges_is_measured_in_dp(self):
        # The container spans x=40..159; the glyph run spans x=30..169, so it overhangs by 10px on
        # each side. 20px at density 2.0 is 10dp.
        image = self._image(30, 169)
        self.assertEqual(probe.label_spill_dp(image, 2.0), 10.0)

    def test_the_measure_is_in_dp_so_density_cancels(self):
        # The same overhang at density 1.0 is twice the dp, which is what makes the number
        # comparable across the sweep rather than a pixel count.
        self.assertEqual(probe.label_spill_dp(self._image(30, 169), 1.0), 20.0)

    def test_a_dark_container_measures_the_same_as_a_light_one(self):
        # tonal and filled-variant. The luminance heuristic this replaced read these backwards,
        # reporting 0.5dp against a real 20.5dp.
        light = probe.label_spill_dp(self._image(30, 169), 2.0)
        dark = probe.label_spill_dp(self._image(30, 169, fill=(51, 46, 60)), 2.0)
        self.assertEqual(dark, light)

    def test_no_solid_fill_is_none_rather_than_a_wrong_number(self):
        # The outlined style: a ring and a label, no fill anywhere. The dominant opaque colour is
        # then the label, and measuring the label against itself reported ~0 on a cell that really
        # spills 22.5dp.
        self.assertIsNone(probe.label_spill_dp(self._image(30, 169, solid=False), 2.0))

    def test_nothing_drawn_is_none(self):
        from PIL import Image

        self.assertIsNone(probe.label_spill_dp(Image.new("RGBA", (10, 10), (0, 0, 0, 0)), 2.0))


class ProbeTableTest(unittest.TestCase):
    def test_every_probe_names_a_baseline_that_exists(self):
        # A baseline that is not in the repo makes `identicalToKnownBroken` None forever, which is
        # the same silent blindness the fold caused from the other end.
        root = Path(__file__).resolve().parent.parent
        for entry in probe.PROBES:
            with self.subTest(issue=entry["issue"]):
                self.assertTrue((root / entry["baseline"]).exists(), entry["baseline"])


class DensityInvarianceTest(unittest.TestCase):
    """The measure #89 and #90 asked for: does the component draw the same thing at every density."""

    def _sweep(self, *pairs):
        return {d: {"box": box, "ink": ink} for d, box, ink in pairs}

    def test_a_correct_component_is_invariant_within_the_antialiasing_tolerance(self):
        # The compact button's real numbers: a 0.5% spread on an identical shape.
        sweep = self._sweep(("1", [52, 32], 1452.0), ("1.5", [52, 32], 1453.8),
                            ("2", [52, 32], 1451.0), ("3", [52, 32], 1446.9))
        self.assertIs(probe.is_density_invariant(sweep), True)

    def test_ink_lost_as_density_rises_is_not_invariant(self):
        # #89's real numbers: 3132 -> 2403 dp², a border degenerating rather than a shape moving.
        # Note the BOX is identical throughout — the card keeps its extent while its arcs vanish,
        # which is exactly why ink and not the box is what decides this.
        sweep = self._sweep(("1", [227, 79], 3132.0), ("1.5", [227, 79], 2921.8),
                            ("2", [227, 79], 2629.0), ("3", [227, 79], 2402.6))
        self.assertIs(probe.is_density_invariant(sweep), False)

    def test_nothing_drawn_anywhere_is_not_invariance(self):
        # #130 draws nothing at any density. Reporting that as invariant would let a blank cell
        # read as healthy, which is the opposite of what publishing it was for.
        sweep = self._sweep(("1", None, 0.0), ("2", None, 0.0))
        self.assertIsNone(probe.is_density_invariant(sweep))

    def test_one_density_says_nothing(self):
        # An ordinary checkout, where only the committed dpi=320 preview exists.
        self.assertIsNone(probe.is_density_invariant(self._sweep(("2", [52, 32], 1451.0))))
        self.assertIsNone(probe.is_density_invariant({}))


class SweepComparisonTest(unittest.TestCase):
    def _probe(self, sweep, invariant, issue=90):
        return {"issue": issue, "preview": "P", "summary": "s", "rendered": True,
                "identicalToKnownBroken": True, "densitySweep": sweep,
                "densityInvariant": invariant, "metrics": {}}

    def test_a_sweep_that_moved_is_reported(self):
        before = report(probes=[self._probe({"2": {"box": [52, 16], "ink": 700.0}}, False)])
        after = report(probes=[self._probe({"2": {"box": [52, 32], "ink": 1451.0}}, True)])
        verdict = probe.compare(before, after)
        self.assertTrue(verdict["report"])
        self.assertTrue(any("density sweep moved" in r for r in verdict["reasons"]))

    def test_an_unchanged_sweep_is_silent(self):
        same = report(probes=[self._probe({"2": {"box": [52, 32], "ink": 1451.0}}, True)])
        self.assertFalse(probe.compare(same, same)["report"])


class AwaitedApiTest(unittest.TestCase):
    """The watchlist: a component this catalog is waiting for, watched by SYMBOL.

    The whole value of this is being loud the week a class appears — `RemoteCheckboxButton` landed
    and nothing said so, which is the miss the watchlist exists to stop repeating. So the assertions
    are about noise in both directions: arrival reports, steady state does not.
    """

    SWITCH = "androidx/wear/compose/remote/material3/RemoteSwitchButtonKt"
    RADIO = "androidx/wear/compose/remote/material3/RemoteRadioButtonKt"

    def test_a_symbol_that_arrived_is_reported(self):
        before = report(awaited_api={self.SWITCH: False, self.RADIO: False})
        after = report(awaited_api={self.SWITCH: True, self.RADIO: False})
        verdict = probe.compare(before, after)
        self.assertTrue(verdict["report"])
        self.assertEqual(verdict["arrivedApi"], [self.SWITCH])
        self.assertTrue(any("now PUBLISHED" in r for r in verdict["reasons"]))

    def test_a_symbol_still_missing_is_silent(self):
        same = report(awaited_api={self.SWITCH: False, self.RADIO: False})
        self.assertFalse(probe.compare(same, same)["report"])

    def test_a_symbol_still_present_is_silent(self):
        # The reason entries get RETIRED once they land: a watch that reports every week means
        # nothing by it. Until someone deletes the entry, at least it must not shout.
        same = report(awaited_api={self.SWITCH: True})
        self.assertFalse(probe.compare(same, same)["report"])

    def test_a_symbol_that_vanished_is_reported(self):
        before = report(awaited_api={self.SWITCH: True})
        after = report(awaited_api={self.SWITCH: False})
        verdict = probe.compare(before, after)
        self.assertEqual(verdict["withdrawnApi"], [self.SWITCH])
        self.assertTrue(any("gone again" in r for r in verdict["reasons"]))

    def test_the_watchlist_is_read_out_of_an_aar(self):
        # `classes_in` is the only part of the lookup with a format to get wrong: an AAR is a zip
        # wrapping `classes.jar`, which is another zip. Nested classes are dropped so a `$1` never
        # answers for its outer class.
        import io, zipfile

        jar = io.BytesIO()
        with zipfile.ZipFile(jar, "w") as z:
            z.writestr("a/b/CKt.class", b"")
            z.writestr("a/b/CKt$inner.class", b"")
            z.writestr("a/b/D.class", b"")
        aar = io.BytesIO()
        with zipfile.ZipFile(aar, "w") as z:
            z.writestr("classes.jar", jar.getvalue())
            z.writestr("AndroidManifest.xml", b"<manifest/>")
        self.assertEqual(probe.classes_in(aar.getvalue()), {"a/b/CKt", "a/b/D"})

    def test_the_watchlist_never_forces_a_render_on_its_own(self):
        # `_awaitedApi` rides inside the fingerprint dict because it is read from an AAR the gate
        # was already downloading. The gate must stay a statement about BYTES: adding an entry to
        # this repo's watchlist is a source change, and spending a runner on it would be spending
        # it to learn nothing.
        artifacts = {"wear-compose-remote": {"version": "v", "sha256": "same"}}
        previous = report()
        previous["build"]["fingerprint"] = dict(artifacts, _awaitedApi={"x": False})
        self.assertTrue(
            probe.unchanged_artifacts(previous, dict(artifacts, _awaitedApi={"x": True, "y": False}))
        )

    def test_a_real_byte_change_still_opens_the_gate(self):
        previous = report()
        previous["build"]["fingerprint"] = {"wear-compose-remote": {"version": "v", "sha256": "a"}}
        self.assertFalse(
            probe.unchanged_artifacts(previous, {"wear-compose-remote": {"version": "v", "sha256": "b"}})
        )

    def test_every_entry_names_a_class_and_a_change(self):
        for entry in probe.AWAITED_API:
            self.assertRegex(entry["symbol"], r"^[a-z0-9/]+/[A-Z][A-Za-z0-9]*$")
            self.assertTrue(entry["unlocks"])
            self.assertTrue(entry["change"].startswith("https://"))


if __name__ == "__main__":
    unittest.main(verbosity=2)
