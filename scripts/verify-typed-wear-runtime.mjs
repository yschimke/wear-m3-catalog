#!/usr/bin/env node
// Exercise the typed Wear pilot's actual ZIP in a sandboxed iframe, through protocol v2: archive
// integrity, a rejected production runtime pin, every template and owned component, and a pointer
// click on a bound checkbox row writing its state back. Ported from m3-catalog's pilot check.
//
//   node scripts/verify-typed-wear-runtime.mjs <compose-ui-builder checkout> [archive]
import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { execFileSync } from 'node:child_process';
import { mkdtemp, readFile, rm, writeFile, mkdir } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join, resolve, extname, basename } from 'node:path';
import { pathToFileURL } from 'node:url';
import { createHash } from 'node:crypto';
import { existsSync } from 'node:fs';

const root = resolve(new URL('..', import.meta.url).pathname);
const builder = resolve(process.argv[2] ?? join(root, '../compose-ui-builder'));
const archive = resolve(process.argv[3] ?? join(root, 'catalog-typed-adapters/build/distributions/wear-m3-typed-pilot-renderer.zip'));
const { launch } = await import(pathToFileURL(join(builder, 'scripts/ui-builder-web-smoke/cdp.mjs')));
const scratch = await mkdtemp(join(tmpdir(), 'typed-wear-runtime-'));
const output = join(root, 'catalog-typed-adapters/build/browser-evidence');
await mkdir(output, { recursive: true });
execFileSync('unzip', ['-q', archive, '-d', scratch]);
const manifest = JSON.parse(await readFile(join(scratch, 'runtime-manifest.json'), 'utf8'));
assert.equal(manifest.protocolVersion, 2);
const paths = execFileSync('unzip', ['-Z1', archive], { encoding: 'utf8' }).trim().split('\n').filter(x => !x.endsWith('/') && x !== 'runtime-manifest.json').sort();
const digest = createHash('sha256');
for (const path of paths) {
  const bytes = await readFile(join(scratch, path));
  digest.update(path); digest.update('\0'); digest.update(String(bytes.length)); digest.update('\0'); digest.update(bytes);
}
assert.equal(digest.digest('hex'), manifest.integritySha256);

const host = `<!doctype html><style>body{margin:0}iframe{border:0;width:960px;height:800px}</style>
<iframe id="renderer" sandbox="allow-scripts allow-same-origin" src="/ui-builder/runtime/${manifest.runtimeId}/index.html"></iframe>
<script>window.messages=[];window.addEventListener('message',e=>{if(e.source===renderer.contentWindow&&typeof e.data==='string'){try{messages.push(JSON.parse(e.data))}catch{}}});</script>`;
const types = { '.html': 'text/html', '.mjs': 'text/javascript', '.js': 'text/javascript', '.wasm': 'application/wasm', '.json': 'application/json', '.ttf': 'font/ttf' };
const server = createServer(async (request, response) => {
  try {
    if (request.url === '/host.html') { response.setHeader('Content-Type', 'text/html'); response.end(host); return; }
    const prefix = `/ui-builder/runtime/${manifest.runtimeId}/`;
    const pathname = new URL(request.url, 'http://localhost').pathname;
    if (!pathname.startsWith(prefix)) throw new Error('Unknown asset');
    const relative = pathname.slice(prefix.length);
    const path = resolve(scratch, relative);
    if (!path.startsWith(scratch + '/')) throw new Error('Unknown asset');
    response.setHeader('Content-Type', types[extname(path)] ?? 'application/octet-stream');
    response.end(await readFile(path));
  } catch { response.statusCode = 404; response.end('Not found'); }
});
await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
let browser;
const errors = [], results = [];
try {
  const chrome = process.env.CHROME_PATH ?? ['/opt/pw-browsers/chromium', '/usr/bin/chromium', '/usr/bin/google-chrome', '/usr/bin/chromium-browser'].find(existsSync);
  assert.ok(chrome, 'Install Chromium or set CHROME_PATH');
  browser = await launch(chrome, ['--enable-unsafe-swiftshader']);
  const page = await browser.newPage();
  await page.send('Runtime.enable');
  page.on('Runtime.exceptionThrown', event => errors.push(event.exceptionDetails.exception?.description ?? event.exceptionDetails.text));
  await page.send('Page.enable');
  await page.send('Page.navigate', { url: `http://127.0.0.1:${server.address().port}/host.html` });
  await page.waitFor('!!document.getElementById("renderer")', 10000);
  let serial = 0;
  const send = async (type, payload = {}) => {
    const requestId = `request-${++serial}`;
    const envelope = { schema: 'compose-ui-builder-renderer/v2', protocolVersion: 2, runtimeId: manifest.runtimeId, requestId, type, payload };
    await page.evaluate(`renderer.contentWindow.postMessage(${JSON.stringify(JSON.stringify(envelope))},location.origin)`);
    return requestId;
  };
  // A module script's load event precedes Wasm initialization. Repeated fresh initialize requests
  // wait for the real receiver, rather than guessing how long a cold browser needs.
  const deadline = Date.now() + 60000;
  while (!await page.evaluate('messages.some(m=>m.type==="initialized")')) {
    assert.ok(Date.now() < deadline, `Runtime did not initialize: ${errors.join('\n')}`);
    await send('initialize');
    await new Promise(resolve => setTimeout(resolve, 250));
  }
  const render = async (document, name, widthDp = 227) => {
    const request = await send('renderDocument', { document, surface: { mode: 'device', widthDp, heightDp: widthDp, density: 2, surfaceId: name } });
    await page.waitFor(`messages.some(m=>m.requestId===${JSON.stringify(request)}&&(m.type==='rendered'||m.type==='error'))`, 60000, name);
    const reply = await page.evaluate(`messages.find(m=>m.requestId===${JSON.stringify(request)}&&(m.type==='rendered'||m.type==='error'))`);
    assert.equal(reply.type, 'rendered', JSON.stringify(reply));
    assert.equal(reply.payload.inspection.documentId, document.id);
    await page.waitFor(`messages.some(m=>m.requestId===${JSON.stringify(request)}&&m.payload?.inspection?.generation?.measuredNodeIds?.length>0)`, 30000, `${name} measured inspection`);
    const settled = await page.evaluate(`messages.filter(m=>m.requestId===${JSON.stringify(request)}&&m.payload?.inspection?.generation?.measuredNodeIds?.length>0).at(-1)`);
    assert.ok(settled.payload.inspection.generation.measuredNodeIds.length > 0, `${name}: nothing measured`);
    const { data } = await page.send('Page.captureScreenshot', { format: 'png' });
    await writeFile(join(output, `${name}.png`), Buffer.from(data, 'base64'));
    results.push({ name, nodes: reply.payload.inspection.nodes.length });
    return reply;
  };
  const policy = JSON.parse(await readFile(join(root, 'ui-builder.policy.json'), 'utf8'));
  const template = policy.templates[0];
  const original = JSON.parse(await readFile(join(root, typeof template === 'string' ? template : template.path), 'utf8'));
  const wrongPin = await send('renderDocument', { document: original, surface: { mode: 'device', widthDp: 227, heightDp: 227, density: 2, surfaceId: 'wrong-pin' } });
  await page.waitFor(`messages.some(m=>m.requestId===${JSON.stringify(wrongPin)})`, 10000);
  assert.equal(await page.evaluate(`messages.find(m=>m.requestId===${JSON.stringify(wrongPin)}).payload.code`), 'RUNTIME_PIN_MISMATCH');
  for (const template of policy.templates) {
    const path = typeof template === 'string' ? template : template.path;
    const document = JSON.parse(await readFile(join(root, path), 'utf8'));
    // Trial copies only. Stored templates and saved runtime pins are never rewritten.
    document.catalogPin = { ...document.catalogPin, systemId: 'wear-m3-typed-pilot', nativeRuntimeId: manifest.runtimeId };
    for (const width of [192, 227]) await render(document, `${basename(path, '.json')}-${width}`, width);
  }
  const published = JSON.parse(await readFile(join(root, 'catalog-typed-adapters/build/catalog/ui-builder.json'), 'utf8'));
  const owned = { ...published.statusSemantics.components, ...published.statusSemantics.builtins };
  for (const [componentId, component] of Object.entries(owned).filter(([, component]) => !component.excluded)) {
    const properties = { text: { type: 'string', value: 'Typed catalog' }, label: { type: 'string', value: 'Typed catalog' }, title: { type: 'string', value: 'Typed catalog' } };
    if (componentId === 'wear-m3/icon') properties.iconKey = { type: 'enum', value: 'favorite' };
    const slots = Object.fromEntries((component.slotCapabilities ?? []).map(slot => [slot.name, ['content']]));
    const document = { ...original, id: componentId.replace('/', '-'), revision: 1, catalogPin: { ...original.catalogPin, systemId: 'wear-m3-typed-pilot', nativeRuntimeId: manifest.runtimeId }, stateVariables: {}, roots: ['root'], nodes: {
      root: { id: 'root', componentId, properties, slots, modifiers: [], eventBindings: {} },
      content: { id: 'content', componentId: 'wear-m3/text', properties: { text: { type: 'string', value: 'Slot content' } }, slots: {}, modifiers: [], eventBindings: {} },
    } };
    await render(document, componentId.replace('/', '-'));
  }
  const control = { ...original, id: 'bound-checkbox', revision: 1, catalogPin: { ...original.catalogPin, systemId: 'wear-m3-typed-pilot', nativeRuntimeId: manifest.runtimeId }, stateVariables: { selection: { type: 'boolean', initialValue: false } }, roots: ['checkbox'], nodes: {
    checkbox: { id: 'checkbox', componentId: 'wear-m3/checkbox-button', properties: { label: { type: 'string', value: 'Notify' }, checked: { type: 'state', variable: 'selection' }, selected: { type: 'stateEquals', variable: 'selection', value: 'true' } }, slots: {}, modifiers: [], eventBindings: {} },
  } };
  await render(control, 'bound-checkbox');
  await page.waitFor(`messages.some(m=>m.payload?.inspection?.documentId==='bound-checkbox'&&m.payload.inspection.nodes.some(n=>n.nodeId==='checkbox'&&n.bounds))`, 10000);
  const checkbox = await page.evaluate(`messages.filter(m=>m.payload?.inspection?.documentId==='bound-checkbox').at(-1).payload.inspection.nodes.find(n=>n.nodeId==='checkbox')`);
  assert.equal(checkbox.semantics.selected, false);
  const bounds = checkbox.bounds;
  const x = bounds.x + bounds.width / 2;
  const y = bounds.y + bounds.height / 2;
  await page.send('Input.dispatchMouseEvent', { type: 'mousePressed', x, y, button: 'left', clickCount: 1 });
  await page.send('Input.dispatchMouseEvent', { type: 'mouseReleased', x, y, button: 'left', clickCount: 1 });
  await page.waitFor(`messages.some(m=>m.payload?.inspection?.documentId==='bound-checkbox'&&m.payload.inspection.nodes.some(n=>n.nodeId==='checkbox'&&n.semantics.selected===true))`, 10000, 'bound checkbox state after pointer click');
  assert.deepEqual(errors, [], 'Browser exceptions');
  await writeFile(join(output, 'results.json'), JSON.stringify({ runtimeId: manifest.runtimeId, integritySha256: manifest.integritySha256, archiveIntegrityVerified: true, originalRuntimePinRejected: true, protocolVersion: 2, boundCheckboxPointerInteractionVerified: true, renders: results }, null, 2));
  const readiness = JSON.parse(await readFile(join(root, 'catalog-typed-adapters/build/catalog/readiness.json'), 'utf8'));
  readiness.unverifiedGates = readiness.unverifiedGates.filter(gate => gate !== 'Packaged browser runtime exercised through the sandbox host');
  readiness.browserVerification = { runtimeId: manifest.runtimeId, integritySha256: manifest.integritySha256, renders: results.length, boundCheckboxPointerInteractionVerified: true };
  // Browser success resolves only this gate; contract differences and production compatibility
  // remain blocking. Keep the generator's pre-execution assessment intact.
  readiness.readyForCutover = false;
  await writeFile(join(output, 'readiness.json'), JSON.stringify(readiness, null, 2));
  console.log(`Typed Wear runtime passed: ${results.length} component/template renders, bound checkbox pointer interaction, ZIP integrity and protocol v2 verified`);
} finally {
  await browser?.close();
  await new Promise(resolve => server.close(resolve));
  await rm(scratch, { recursive: true, force: true });
}
