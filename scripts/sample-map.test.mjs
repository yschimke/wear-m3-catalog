import { test } from "node:test";
import assert from "node:assert/strict";

import { sampleBlocks } from "./sample-map.mjs";

const only = (text) => sampleBlocks(text);

test("attributes a @sample to the function its KDoc introduces", () => {
  const src = `
    /**
     * A button.
     *
     * @sample androidx.compose.material3.samples.ButtonSample
     */
    @Composable
    fun Button(onClick: () -> Unit) {}
  `;
  assert.deepEqual(only(src), [
    { decl: "Button", samples: ["androidx.compose.material3.samples.ButtonSample"], viaParameter: false },
  ]);
});

test("skips an annotation whose arguments span lines and contain parens and strings", () => {
  // THE case a line-based reader gets wrong: the next line is `message = …`, not the declaration.
  const src = `
    /**
     * @sample androidx.compose.material3.samples.TextTabs
     */
    @Deprecated(
      message = "Use overload with \`shape\` (and see IconButton(...))",
      level = DeprecationLevel.WARNING,
    )
    @Composable
    fun TabRow(selectedTabIndex: Int) {}
  `;
  assert.deepEqual(only(src)[0].decl, "TabRow");
});

test("collects several samples from one block, in order", () => {
  const src = `
    /**
     * @sample a.b.One
     * @sample a.b.Two
     */
    fun Tabs() {}
  `;
  assert.deepEqual(only(src)[0].samples, ["a.b.One", "a.b.Two"]);
});

test("skips modifiers between the block and the declaration", () => {
  const src = `
    /** @sample a.b.Sample */
    @Composable
    public expect inline fun Thing() {}
  `;
  assert.equal(only(src)[0].decl, "Thing");
});

test("names the declaration, not the receiver, for an extension", () => {
  const src = `
    /** @sample a.b.Sample */
    fun Modifier.tabIndicatorOffset(): Modifier = this
  `;
  assert.equal(only(src)[0].decl, "tabIndicatorOffset");
});

test("skips a generic parameter list before the name", () => {
  const src = `
    /** @sample a.b.Sample */
    fun <T : Comparable<T>> pick(items: List<T>): T = items.first()
  `;
  assert.equal(only(src)[0].decl, "pick");
});

test("attributes a parameter's @sample to the declaration whose list it sits in", () => {
  // Defence rather than an observed case: no artifact measured resolves through this path today.
  const src = `
    fun DatePicker(
      /** @sample a.b.LocaleSample */
      locale: CalendarLocale,
    ) {}
  `;
  const [entry] = only(src);
  assert.equal(entry.decl, "DatePicker");
  assert.equal(entry.viaParameter, true);
});

test("ignores a @sample inside a string literal", () => {
  const src = `
    val doc = "/** @sample a.b.NotReal */"
    /** @sample a.b.Real */
    fun Thing() {}
  `;
  assert.deepEqual(only(src).map((e) => e.samples).flat(), ["a.b.Real"]);
});

test("ignores a @sample inside a raw string, parens and all", () => {
  const src = `
    val doc = """
      /** @sample a.b.NotReal */
      fun Fake(
    """
    /** @sample a.b.Real */
    fun Thing() {}
  `;
  const samples = only(src).flatMap((e) => e.samples);
  assert.deepEqual(samples, ["a.b.Real"]);
});

test("ignores a block comment that carries no @sample", () => {
  assert.deepEqual(only("/* just a note */\nfun Thing() {}"), []);
});

test("drops a block that introduces no declaration rather than guessing an owner", () => {
  // A wrong attribution publishes a sample against an API that does not demonstrate it, which is
  // worse than a missing one — so the scanner gives up instead.
  const src = `
    /** @sample a.b.Sample */
    val x = 1 + 2
    package foo
  `;
  // `val` IS a declaration keyword, so this one resolves; the guard is for anything that is not.
  assert.equal(only(src)[0].decl, "x");

  const orphan = `
    /** @sample a.b.Sample */
    return 4
  `;
  assert.deepEqual(only(orphan), []);
});

test("handles a file-level annotation without swallowing the file", () => {
  const src = `
    @file:OptIn(ExperimentalMaterial3Api::class)
    package androidx.compose.material3

    /** @sample a.b.Sample */
    fun Thing() {}
  `;
  assert.equal(only(src)[0].decl, "Thing");
});

test("does not trip on an unterminated string literal", () => {
  const src = `
    val broken = "oops
    /** @sample a.b.Sample */
    fun Thing() {}
  `;
  assert.equal(only(src).length, 1);
});
