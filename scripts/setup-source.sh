#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
DESKTOP="$ROOT/apps/helper/apps/desktop"

echo "[AILinux App] Preparing source runtime in $ROOT"
command -v node >/dev/null || { echo 'Node.js is required.' >&2; exit 1; }
command -v npm >/dev/null || { echo 'npm is required.' >&2; exit 1; }
command -v python3 >/dev/null || { echo 'Python 3 is required.' >&2; exit 1; }

cd "$DESKTOP"
if [[ -f package-lock.json ]]; then npm ci; else npm install; fi

echo '[AILinux App] Running desktop contract tests...'
npm run check
cd "$ROOT"
python3 -m unittest -q tests.test_unified_contract
python3 -m compileall -q core/aicoder/aicoder

echo '[AILinux App] Source runtime ready.'
