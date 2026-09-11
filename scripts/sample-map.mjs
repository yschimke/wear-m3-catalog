#!/usr/bin/env node
/**
 * Build `sample-map.json` — which AndroidX sample demonstrates which Compose API — by parsing the
 * `@sample` KDoc tags off the **sources jar of the artifact this catalog compiles against**.
 *
 * Reading it from the compiled-against artifact rather than from the imported sample tree is the
 * whole point: the map then describes the API this catalog actually renders, so a `@sample` naming
 * a function the artifact does not have cannot enter it. See `docs/design/ANDROIDX_SAMPLES.md`.
 *
 * ## Why this is not a regex
 *
 * A line-based "KDoc block, then the next line" scan mis-attributes about 12% of the blocks that
 * carry a `@sample` — 31 of the 260 in `material3` 1.5.0-alpha27, when that approach was tried.
 * The confirmed cause is an **annotated declaration**: `@Deprecated(message = "Use overload with
 * `shape`", level = DeprecationLevel.WARNING)` sits between the KDoc and the `fun`, spans lines,
 * and contains both parentheses and a string, so the "next line" is `message = …` and the tag
 * lands on nothing. Any annotation with arguments does this, and `material3` has many.
 *
 * So this walks the file with a real scanner: strings (including raw `"""` and escapes), line and
 * block comments, and paren depth, then a forward skip over modifiers and annotations — the latter
 * with BALANCED parentheses — to reach the declaration a block belongs to.
 *
 * It also handles a `@sample` on a **parameter's** KDoc, attributing it to the declaration whose
 * parameter list it sits in. Stated as defence, not as a fix for something observed: across
 * `material3` 1.5.0-alpha27, CMP `material3` 1.12.0-alpha03 and `wear.compose:compose-material3`
 * 1.7.0-beta02, **zero** blocks resolve through that path today. (An earlier note claimed
 * `DatePicker`'s `locale:` line as a live example of it. That was wrong: the block in question
 * documents the `DatePickerState` factory and its `@sample` is at block level — the line-based
 * reader simply mis-landed on a `@param` line further down.)
 *
 * Measured coverage on `material3` 1.5.0-alpha27: 260 of 260 blocks attributed, 319 of 319 unique
 * samples reached, nothing dropped. CMP reaches 308 of 308 and Wear 170 of 170.
 *
 * ## Usage
 *
 *     node scripts/sample-map.mjs --sources <sources.jar|dir> --out sample-map.json
 *
 * `--sources` takes an extracted directory or a `*-sources.jar` (unzipped to a temp dir). With
 * `--check` the file is not written: the freshly parsed map is compared against the committed one
 * and a difference exits 1, which is how CI keeps the map honest — the same regenerate-and-diff
 * contract `design-map.json` and `CatalogMatrixAnnotations.kt` already carry here.
 */

import { execFileSync } from "node:child_process";
import { mkdtempSync, readdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join, relative } from "node:path";

/** Kotlin declaration keywords a `@sample` KDoc can sit on. */
const DECLARATION_KEYWORDS = new Set([
  "fun",
  "val",
  "var",
  "class",
  "object",
  "interface",
  "enum",
  "annotation",
  "typealias",
]);

/**
 * Modifiers that may stand between a KDoc block and its declaration. Skipped by name rather than by
 * "any identifier", so a malformed file cannot make the scanner walk past a declaration silently.
 */
const MODIFIERS = new Set([
  "public",
  "private",
  "internal",
  "protected",
  "expect",
  "actual",
  "inline",
  "noinline",
  "crossinline",
  "suspend",
  "operator",
  "infix",
  "tailrec",
  "external",
  "const",
  "lateinit",
  "override",
  "open",
  "final",
  "abstract",
  "sealed",
  "data",
  "value",
  "companion",
  "inner",
  "vararg",
  "reified",
]);

/**
 * Every `@sample` block in one Kotlin source, with the declaration each belongs to.
 *
 * @param {string} text the file's contents.
 * @returns {Array<{decl: string, samples: string[], viaParameter: boolean}>}
 */
export function sampleBlocks(text) {
  const found = [];
  // Declarations opened at each paren depth, so a parameter-level block can name its enclosing
  // declaration. Index 0 is unused (a depth-0 block resolves by scanning forward instead).
  const openDeclarations = [];
  let lastTopLevelDecl = null;
  let i = 0;
  let parenDepth = 0;

  while (i < text.length) {
    const c = text[i];
    const next = text[i + 1];

    // --- strings, so a `/**` or a paren inside one is not read as code -----------------------
    if (c === '"' && text.startsWith('"""', i)) {
      const end = text.indexOf('"""', i + 3);
      i = end === -1 ? text.length : end + 3;
      continue;
    }
    if (c === '"' || c === "'") {
      i += 1;
      while (i < text.length && text[i] !== c) {
        if (text[i] === "\\") i += 1;
        if (text[i] === "\n") break; // an unterminated literal must not swallow the file
        i += 1;
      }
      i += 1;
      continue;
    }

    // --- comments ------------------------------------------------------------------------------
    if (c === "/" && next === "/") {
      const end = text.indexOf("\n", i);
      i = end === -1 ? text.length : end + 1;
      continue;
    }
    if (c === "/" && next === "*") {
      const end = text.indexOf("*/", i + 2);
      const stop = end === -1 ? text.length : end + 2;
      const block = text.slice(i, stop);
      const samples = [...block.matchAll(/@sample\s+([\w.]+)/g)].map((m) => m[1]);
      if (samples.length > 0) {
        if (parenDepth > 0) {
          // Case 1: a parameter's own KDoc. It documents the parameter, but the `@sample` is about
          // the declaration whose parameter list we are standing in.
          const decl = openDeclarations[parenDepth] ?? lastTopLevelDecl;
          if (decl) found.push({ decl, samples, viaParameter: true });
        } else {
          // Case 2: an ordinary block. Skip forward over modifiers and annotations to the real
          // declaration — `@Deprecated(…)` between the two is what a naive scan trips on.
          const decl = declarationAfter(text, stop);
          if (decl) found.push({ decl, samples, viaParameter: false });
        }
      }
      i = stop;
      continue;
    }

    // --- depth tracking ------------------------------------------------------------------------
    if (c === "(") {
      parenDepth += 1;
      // The declaration whose parameter list this is: the one most recently seen at depth 0.
      openDeclarations[parenDepth] = lastTopLevelDecl;
      i += 1;
      continue;
    }
    if (c === ")") {
      if (parenDepth > 0) openDeclarations[parenDepth] = undefined;
      parenDepth = Math.max(0, parenDepth - 1);
      i += 1;
      continue;
    }

    // --- declarations, so a parameter block above can find its owner ---------------------------
    if (parenDepth === 0 && /[A-Za-z_]/.test(c)) {
      let j = i;
      while (j < text.length && /[\w]/.test(text[j])) j += 1;
      const word = text.slice(i, j);
      if (DECLARATION_KEYWORDS.has(word)) {
        const name = identifierAfter(text, j);
        if (name) lastTopLevelDecl = name;
      }
      i = j;
      continue;
    }

    i += 1;
  }

  return found;
}

/**
 * The declaration name at [from], skipping whitespace, comments, modifiers and annotations.
 *
 * Annotations are skipped with a BALANCED paren scan, which is the half a line-based reader gets
 * wrong: `@Deprecated(message = "Use overload with `shape`", level = DeprecationLevel.WARNING)`
 * spans lines and contains both parentheses and a string.
 */
function declarationAfter(text, from) {
  let i = from;
  while (i < text.length) {
    const c = text[i];
    if (/\s/.test(c)) {
      i += 1;
      continue;
    }
    if (c === "/" && text[i + 1] === "/") {
      const end = text.indexOf("\n", i);
      i = end === -1 ? text.length : end + 1;
      continue;
    }
    if (c === "/" && text[i + 1] === "*") {
      const end = text.indexOf("*/", i + 2);
      i = end === -1 ? text.length : end + 2;
      continue;
    }
    if (c === "@") {
      i = skipAnnotation(text, i);
      continue;
    }
    if (/[A-Za-z_]/.test(c)) {
      let j = i;
      while (j < text.length && /[\w]/.test(text[j])) j += 1;
      const word = text.slice(i, j);
      if (MODIFIERS.has(word)) {
        i = j;
        continue;
      }
      if (DECLARATION_KEYWORDS.has(word)) return identifierAfter(text, j);
      // Anything else means the block does not introduce a declaration we understand. Give up
      // rather than guess: a wrong attribution is worse than a missing one, because it publishes a
      // sample against an API that does not demonstrate it.
      return null;
    }
    return null;
  }
  return null;
}

/** Skip one annotation entry (`@Foo`, `@Foo(...)`, `@file:Foo(...)`), parens balanced. */
function skipAnnotation(text, from) {
  let i = from + 1;
  while (i < text.length && /[\w.:]/.test(text[i])) i += 1;
  // Optional argument list.
  while (i < text.length && /\s/.test(text[i])) i += 1;
  if (text[i] !== "(") return i;
  let depth = 0;
  while (i < text.length) {
    const c = text[i];
    if (c === '"' && text.startsWith('"""', i)) {
      const end = text.indexOf('"""', i + 3);
      i = end === -1 ? text.length : end + 3;
      continue;
    }
    if (c === '"') {
      i += 1;
      while (i < text.length && text[i] !== '"') {
        if (text[i] === "\\") i += 1;
        i += 1;
      }
      i += 1;
      continue;
    }
    if (c === "(") depth += 1;
    if (c === ")") {
      depth -= 1;
      i += 1;
      if (depth === 0) return i;
      continue;
    }
    i += 1;
  }
  return i;
}

/** The identifier following a declaration keyword, skipping a generic parameter list and receiver. */
function identifierAfter(text, from) {
  let i = from;
  while (i < text.length && /\s/.test(text[i])) i += 1;
  // `fun <T> foo()` — skip the type parameters.
  if (text[i] === "<") {
    let depth = 0;
    while (i < text.length) {
      if (text[i] === "<") depth += 1;
      if (text[i] === ">") {
        depth -= 1;
        i += 1;
        if (depth === 0) break;
        continue;
      }
      i += 1;
    }
    while (i < text.length && /\s/.test(text[i])) i += 1;
  }
  let j = i;
  while (j < text.length && /[\w.`]/.test(text[j])) j += 1;
  const raw = text.slice(i, j);
  if (!raw) return null;
  // `fun Modifier.foo()` is an extension: the last segment is the declaration's own name.
  const name = raw.split(".").pop().replaceAll("`", "");
  return name || null;
}

/** Every `.kt` file under [dir], recursively. */
export function kotlinSources(dir) {
  const out = [];
  const walk = (d) => {
    for (const entry of readdirSync(d, { withFileTypes: true })) {
      const path = join(d, entry.name);
      if (entry.isDirectory()) walk(path);
      else if (entry.name.endsWith(".kt")) out.push(path);
    }
  };
  walk(dir);
  return out;
}

/**
 * The `api -> samples` map for a tree of Kotlin sources.
 *
 * Keyed by the DECLARATION the tag sits on, which is the Compose API a catalog component renders —
 * the join key `sample-map.json` exists to provide. Samples are deduplicated and sorted, and so are
 * the entries, so the committed file is stable under the regenerate-and-diff check.
 */
export function buildSampleMap(dir) {
  const byApi = new Map();
  const files = kotlinSources(dir).sort();
  for (const file of files) {
    const text = readFileSync(file, "utf8");
    for (const { decl, samples } of sampleBlocks(text)) {
      const existing = byApi.get(decl) ?? new Set();
      for (const sample of samples) existing.add(sample);
      byApi.set(decl, existing);
    }
  }
  return [...byApi.entries()]
    .map(([api, samples]) => ({ api, samples: [...samples].sort() }))
    .sort((a, b) => a.api.localeCompare(b.api));
}

/** Unzip a sources jar into a temp directory and return its path. */
function extractJar(jar) {
  const dir = mkdtempSync(join(tmpdir(), "sample-map-"));
  execFileSync("unzip", ["-q", jar, "-d", dir]);
  return dir;
}

function main(argv) {
  const args = new Map();
  for (let i = 0; i < argv.length; i += 1) {
    if (argv[i].startsWith("--")) args.set(argv[i].slice(2), argv[i + 1]);
  }
  const sources = args.get("sources");
  const out = args.get("out") ?? "sample-map.json";
  const check = argv.includes("--check");
  if (!sources) {
    console.error(
      "usage: sample-map.mjs --sources <sources.jar|dir> [--out sample-map.json] [--check]",
    );
    process.exit(2);
  }

  const dir = statSync(sources).isDirectory() ? sources : extractJar(sources);
  const map = buildSampleMap(dir);
  const json = `${JSON.stringify(map, null, 2)}\n`;

  if (check) {
    const committed = readFileSync(out, "utf8");
    if (committed !== json) {
      console.error(
        `${out} is stale: regenerate it with \`node scripts/sample-map.mjs --sources <jar> --out ${out}\`.`,
      );
      process.exit(1);
    }
    console.log(`${out} is current (${map.length} API(s)).`);
    return;
  }

  writeFileSync(out, json);
  const samples = new Set(map.flatMap((entry) => entry.samples));
  console.log(
    `${relative(process.cwd(), out)}: ${map.length} API(s), ${samples.size} unique sample(s).`,
  );
}

if (process.argv[1] && process.argv[1].endsWith("sample-map.mjs")) main(process.argv.slice(2));
