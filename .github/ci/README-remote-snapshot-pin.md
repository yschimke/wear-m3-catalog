# `remote-snapshot-pin`

One androidx.dev build ID, on one line. It is the **published** Remote sheet's artifact line: both
`design-artifacts.yml` (the `remote-m3` sheet) and `design-parity.yml` (the `remote-m3` board) read
it into `gradle.properties` as `remoteSnapshot`, which turns on the opt-in snapshot lane described
in AGENTS.md → Dependencies.

## Why the file exists rather than a literal in each workflow

The two must name the **same** build. A board scoring a sheet drawn from different bytes makes every
difference between them unattributable, and nothing would have caught that drift: the two callers
are separate reusable-workflow invocations that cannot share a value, so a literal in each is two
things to remember and one commit to get wrong. One file is one edit.

## Bumping it

Deliberately, in a commit of its own, and read the visual diff — the same rule Compose and
Horologist follow. Never point it at `latest`: a floating pin redraws the sheet and moves the parity
verdict several times a day with no commit to explain the change, which is the property that makes a
published board worth reading.

This file is in both jobs' `cache-paths`, so a bump actually forces a re-render instead of
re-applying a cached verdict.

## What it costs

The published Remote sheet and board are not reproducible from released artifacts alone: rebuilding
them needs this build to still be downloadable, and androidx.dev does not keep builds forever. That
is the accepted price of a sheet that can draw kit components the released alphas have not shipped
yet; when they ship, the components move to `src/main` and this file can go.

## When a run silently draws the released line instead

It cannot, by construction: both `design-map-command`s begin `test -s` on this file, so an empty or
missing pin fails the step rather than quietly publishing a sheet with components missing.
