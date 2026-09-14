#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
AICODER="$ROOT/upstream/aicoder"
HELPER="$ROOT/upstream/helper"
LOOM="${AILINUX_LOOM_ROOT:-/home/zombie/workspace/ailinux-ai-suite}"
LOG_DIR="${XDG_STATE_HOME:-$HOME/.local/state}/ailinux-app"
LOG_FILE="$LOG_DIR/source.log"
mkdir -p "$LOG_DIR"

if [[ -z "${AILINUX_APP_PYTHON:-}" ]]; then
  if [[ -x "$ROOT/.venv/bin/python" ]]; then
    export AILINUX_APP_PYTHON="$ROOT/.venv/bin/python"
  else
    export AILINUX_APP_PYTHON="/usr/bin/python3"
  fi
else
  export AILINUX_APP_PYTHON
fi
export AILINUX_APP_SOURCE_ROOT="$ROOT"
export AILINUX_HELPER_ROOT="${AILINUX_HELPER_ROOT:-$HELPER}"

if [[ "${1:-}" == "--check" ]]; then
  printf 'AILinux App compatibility root: %s\n' "$ROOT"
  printf 'Version: %s\n' "$(cat "$ROOT/VERSION")"
  printf 'Preferred desktop: AILinux Loom (%s)\n' "$LOOM"
  [[ -f "$AICODER/pyproject.toml" ]] || { echo 'AICoder submodule missing; run scripts/setup-source.sh'; exit 1; }
  [[ -f "$HELPER/apps/desktop/package.json" ]] || { echo 'Helper submodule missing; run scripts/setup-source.sh'; exit 1; }
  if [[ -f "$LOOM/pyproject.toml" && -d "$LOOM/src/ailinux_suite" ]]; then
    echo 'AILinux Loom source runtime: OK'
  else
    echo 'AILinux Loom source runtime: unavailable; legacy AICoder compatibility fallback remains available'
  fi
  "$AILINUX_APP_PYTHON" - <<'PY'
import importlib.util
required = ["aicoder", "PyQt6", "keyring"]
missing = [name for name in required if importlib.util.find_spec(name) is None]
if missing:
    raise SystemExit("missing Python modules: " + ", ".join(missing))
print("AICoder primary runtime dependencies: OK")
PY
  [[ -d "$HELPER/apps/desktop/node_modules/electron" ]] || { echo 'Helper Electron dependencies missing; run scripts/setup-source.sh'; exit 1; }
  python3 "$ROOT/scripts/validate_upstreams.py"
  echo 'Independent Helper runtime: OK'
  exit 0
fi

if [[ ! -x "$ROOT/.venv/bin/python" || ! -d "$HELPER/apps/desktop/node_modules/electron" ]]; then
  "$ROOT/scripts/setup-source.sh"
fi

if [[ "${AILINUX_APP_LEGACY_AICODER:-0}" != "1" && -f "$LOOM/pyproject.toml" && -d "$LOOM/src/ailinux_suite" ]]; then
  cd "$LOOM"
  export PYTHONPATH="$LOOM/src${PYTHONPATH:+:$PYTHONPATH}"
  exec "$AILINUX_APP_PYTHON" -m ailinux_suite --root "$LOOM" gui >>"$LOG_FILE" 2>&1
fi

# Compatibility fallback for systems that have not installed/mounted Loom yet.
cd "$AICODER"
exec "$AILINUX_APP_PYTHON" -m aicoder.cli gui >>"$LOG_FILE" 2>&1
