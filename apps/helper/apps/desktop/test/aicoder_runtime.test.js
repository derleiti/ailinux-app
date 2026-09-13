'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const path = require('path');

test('desktop source declares bundled AICoder runtime bridge', () => {
  const main = fs.readFileSync(path.join(__dirname, '..', 'main.js'), 'utf8');
  const preload = fs.readFileSync(path.join(__dirname, '..', 'preload.js'), 'utf8');
  const pkg = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'package.json'), 'utf8'));
  assert.match(main, /aicoderRuntime\.launch/);
  assert.match(preload, /launchAICoder/);
  assert.equal(pkg.productName, 'AILinux App');
  assert.ok(pkg.build.extraResources.some((row) => row.to === 'aicoder'));
});
