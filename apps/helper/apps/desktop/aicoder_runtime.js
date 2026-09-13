'use strict';

const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const { app } = require('electron');

let child = null;

function executableName() {
  return process.platform === 'win32' ? 'aicoder-sidecar.exe' : 'aicoder-sidecar';
}

function candidates() {
  const name = executableName();
  return [
    path.join(process.resourcesPath || '', 'aicoder', name),
    path.join(__dirname, 'sidecar', name),
  ];
}

function executable() {
  return candidates().find((candidate) => candidate && fs.existsSync(candidate)) || '';
}

function status() {
  const file = executable();
  return {
    available: Boolean(file),
    running: Boolean(child && child.exitCode === null && !child.killed),
    pid: child && child.exitCode === null ? child.pid : null,
    executable: file ? path.basename(file) : '',
    platform: process.platform,
  };
}

function launch(args = ['gui']) {
  const file = executable();
  if (!file) return { ...status(), ok: false, error: 'Bundled AICoder runtime is unavailable.' };
  if (child && child.exitCode === null && !child.killed) return { ...status(), ok: true, reused: true };
  try {
    child = spawn(file, Array.isArray(args) && args.length ? args.map(String) : ['gui'], {
      cwd: app.getPath('documents'),
      detached: false,
      windowsHide: false,
      stdio: 'ignore',
      env: { ...process.env, AILINUX_UNIFIED_APP: '1' },
    });
    child.on('error', () => { child = null; });
    child.on('exit', () => { child = null; });
    return { ...status(), ok: true };
  } catch (error) {
    child = null;
    return { ...status(), ok: false, error: String(error?.message || error) };
  }
}

function stop() {
  if (!child || child.exitCode !== null || child.killed) return { ...status(), ok: true };
  try { child.kill(); } catch {}
  return { ...status(), ok: true };
}

module.exports = { executableName, candidates, executable, status, launch, stop };
