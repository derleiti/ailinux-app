#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
AICODER="$ROOT/upstream/aicoder"
HELPER="$ROOT/upstream/helper"
HELPER_DESKTOP="$HELPER/apps/desktop"
VENV="$ROOT/.venv"

echo "[AILinux App] Preparing composed source runtime in $ROOT"
command -v git >/dev/null || { echo 'git is required.' >&2; exit 1; }
command -v node >/dev/null || { echo 'Node.js is required.' >&2; exit 1; }
command -v npm >/dev/null || { echo 'npm is required.' >&2; exit 1; }
command -v python3 >/dev/null || { echo 'Python 3 is required.' >&2; exit 1; }
python3 -m venv --help >/dev/null 2>&1 || { echo 'Python venv support is required.' >&2; exit 1; }

if [[ ! -f "$AICODER/pyproject.toml" || ! -f "$HELPER_DESKTOP/package.json" ]]; then
  echo '[AILinux App] Initializing linked upstream repositories...'
  git -C "$ROOT" submodule update --init --recursive
fi

if [[ ! -x "$VENV/bin/python" ]]; then
  echo '[AILinux App] Creating local Python runtime...'
  python3 -m venv "$VENV"
fi

echo '[AILinux App] Installing/updating linked AICoder...'
"$VENV/bin/python" -m pip install --upgrade pip setuptools wheel pytest
"$VENV/bin/python" -m pip install -e "$AICODER"

echo '[AILinux App] Installing/updating independent Helper desktop runtime...'
cd "$HELPER_DESKTOP"
if [[ -f package-lock.json ]]; then npm ci; else npm install; fi
npm run check

cd "$AICODER"
QT_QPA_PLATFORM=offscreen "$VENV/bin/python" -m pytest -q tests/test_helper_control.py
"$VENV/bin/python" -m compileall -q aicoder
"$VENV/bin/python" -c 'import aicoder, keyring, PyQt6; print("[AILinux App] AICoder primary runtime: OK")'

cd "$ROOT"
python3 scripts/validate_upstreams.py
python3 -m unittest -q tests.test_unified_contract

echo '[AILinux App] Composed source runtime ready.'
