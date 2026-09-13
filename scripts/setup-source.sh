#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
DESKTOP="$ROOT/apps/helper/apps/desktop"
VENV="$ROOT/.venv"

echo "[AILinux App] Preparing source runtime in $ROOT"
command -v node >/dev/null || { echo 'Node.js is required.' >&2; exit 1; }
command -v npm >/dev/null || { echo 'npm is required.' >&2; exit 1; }
command -v python3 >/dev/null || { echo 'Python 3 is required.' >&2; exit 1; }
python3 -m venv --help >/dev/null 2>&1 || { echo 'Python venv support is required.' >&2; exit 1; }

if [[ ! -x "$VENV/bin/python" ]]; then
  echo '[AILinux App] Creating local Python runtime...'
  python3 -m venv "$VENV"
fi

echo '[AILinux App] Installing/updating AICoder in local Python runtime...'
"$VENV/bin/python" -m pip install --upgrade pip setuptools wheel
"$VENV/bin/python" -m pip install -e "$ROOT/core/aicoder"

cd "$DESKTOP"
if [[ -f package-lock.json ]]; then npm ci; else npm install; fi

echo '[AILinux App] Running desktop contract tests...'
npm run check
cd "$ROOT"
"$VENV/bin/python" -m unittest -q tests.test_unified_contract
"$VENV/bin/python" -m compileall -q core/aicoder/aicoder
"$VENV/bin/python" -c 'import aicoder, keyring, PyQt6; print("[AILinux App] Python source runtime: OK")'

echo '[AILinux App] Source runtime ready.'
