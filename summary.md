## Remote Compose snapshot probe — androidx.dev build [16220364](https://androidx.dev/snapshots/builds/16220364/artifacts)

Snapshot artifacts last updated `20260831150421`. Compiled: **yes**.

**Why this is being reported**

- a tracked issue's capture no longer matches its known-broken one: #91, #130
- a tracked issue's density sweep moved: #91 (invariant: True), #130 (invariant: None)

| issue | preview | still byte-identical to the known-broken capture | drawn, by density | measured |
| --- | --- | --- | --- | --- |
| #91 | `FilledRemoteButton#disabled` | **no — look** | invariant ✓<br>`1: 172x52dp 8356dp², 1.5: 172x52dp 8348.9dp², 2: 172x52dp 8362.2dp², 3: 172x52dp 8360.9dp²` | `disabled_max_alpha` 31 |
| #130 | `TextRemoteButton#disabled` | **no — look** | nothing drawn at any density<br>`1: nothing drawn, 1.5: nothing drawn, 2: nothing drawn, 3: nothing drawn` | `text_disabled_max_alpha` 0 |

A component that lays out correctly occupies the **same dp box at every density**; one whose paint is density-conditional does not. That is the shared signature of #89 and #90, and it is why this table sweeps rather than reporting the one density the sheet declares.

_This job never claims a bug is fixed — it reports that something moved. `scripts/remote-snapshot-probe.py` explains the limit._
