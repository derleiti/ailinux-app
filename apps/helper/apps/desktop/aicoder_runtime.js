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

function sourceRuntime() {
  if (app.isPackaged) return null;
  const root = path.resolve(__dirname, '..', '..', '..', '..');
  const core = path.join(root, 'core', 'aicoder');
  const entry = path.join(core, 'aicoder_main.py');
  if (!fs.existsSync(entry)) return null;
  const pythonCandidates = [
    process.env.AILINUX_APP_PYTHON,
    path.join(root, '.venv', 'bin', 'python'),
    '/usr/bin/python3',
    'python3',
  ].filter(Boolean);
  return { root, core, entry, pythonCandidates };
}

function status() {
  const file = executable();
  const source = sourceRuntime();
  return {
    available: Boolean(file || source),
    running: Boolean(child && child.exitCode === null && !child.killed),
    pid: child && child.exitCode === null ? child.pid : null,
    executable: file ? path.basename(file) : source ? 'aicoder_main.py (source)' : '',
    mode: file ? 'sidecar' : source ? 'source' : 'unavailable',
    platform: process.platform,
  };
}

function launch(args = ['gui']) {
  const file = executable();
  const source = sourceRuntime();
  if (!file && !source) return { ...status(), ok: false, error: 'AICoder runtime is unavailable.' };
  if (child && child.exitCode === null && !child.killed) return { ...status(), ok: true, reused: true };
  try {
    const runtimeArgs = Array.isArray(args) && args.length ? args.map(String) : ['gui'];
    const command = file || source.pythonCandidates[0];
    const commandArgs = file ? runtimeArgs : [source.entry, ...runtimeArgs];
    child = spawn(command, commandArgs, {
      cwd: file ? app.getPath('documents') : source.core,
      detached: false,
      windowsHide: false,
      stdio: 'ignore',
      env: {
        ...process.env,
        AILINUX_UNIFIED_APP: '1',
        PYTHONPATH: source ? [source.core, process.env.PYTHONPATH || ''].filter(Boolean).join(path.delimiter) : process.env.PYTHONPATH,
      },
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
