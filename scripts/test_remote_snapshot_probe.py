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


def report(*, compiled=True, captures=None, probes=None, build_id="1"):
    return {
        "build": {"buildId": build_id, "lastUpdated": "20260101010101", "compiled": compiled},
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
        text = probe._render_metric("compact_heights_dp", {"B": 16, "A": 16})
        self.assertEqual(text, "`compact_heights_dp` A=16, B=16")
        self.assertEqual(probe._render_metric("icononly_glyph_dp", [2, 1]), "`icononly_glyph_dp` 2x1")


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

    def _tree(self, settings=None, toml=None):
        root = Path(tempfile.mkdtemp())
        (root / "gradle").mkdir()
        (root / "settings.gradle.kts").write_text(self.SETTINGS if settings is None else settings)
        (root / "gradle" / "libs.versions.toml").write_text(self.TOML if toml is None else toml)
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
        self.assertEqual(len(changed), 4)

    def test_the_group_regexes_survive_kotlin_string_escaping(self):
        # The block is written verbatim into a Kotlin string literal, where `\.` is an
        # "unsupported escape sequence" and kills script compilation before a single dependency
        # resolves — so the emitted text needs `\\.`. Caught exactly once, by a rehearsal;
        # pinned here so it cannot come back as a mystery red build.
        block = probe.repository_block("16201507")
        self.assertIn(r'includeGroupByRegex("androidx\\.compose\\.remote.*")', block)
        self.assertNotIn(r'("androidx\.compose', block)
        self.assertIn("/snapshots/builds/16201507/artifacts/repository", block)

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


if __name__ == "__main__":
    unittest.main(verbosity=2)
