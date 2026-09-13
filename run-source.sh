#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
DESKTOP="$ROOT/apps/helper/apps/desktop"
LOG_DIR="${XDG_STATE_HOME:-$HOME/.local/state}/ailinux-app"
LOG_FILE="$LOG_DIR/source.log"
mkdir -p "$LOG_DIR"

export AILINUX_APP_SOURCE_ROOT="$ROOT"
if [[ -z "${AILINUX_APP_PYTHON:-}" ]]; then
  if [[ -x "$ROOT/.venv/bin/python" ]]; then
    export AILINUX_APP_PYTHON="$ROOT/.venv/bin/python"
  else
    export AILINUX_APP_PYTHON="/usr/bin/python3"
  fi
else
  export AILINUX_APP_PYTHON
fi
export ELECTRON_ENABLE_LOGGING="${ELECTRON_ENABLE_LOGGING:-0}"

if [[ "${1:-}" == "--check" ]]; then
  printf 'AILinux App source root: %s\n' "$ROOT"
  printf 'Version: %s\n' "$(cat "$ROOT/VERSION")"
  command -v node >/dev/null
  command -v npm >/dev/null
  "$AILINUX_APP_PYTHON" - <<'PY'
import importlib.util
required = ["aicoder", "PyQt6", "keyring"]
missing = [name for name in required if importlib.util.find_spec(name) is None]
if missing:
    raise SystemExit("missing Python modules: " + ", ".join(missing))
print("Python runtime dependencies: OK")
PY
  [[ -x "$DESKTOP/node_modules/.bin/electron" ]] || { echo 'Electron dependencies missing; run scripts/setup-source.sh'; exit 1; }
  echo 'Electron source runtime: OK'
  exit 0
fi

if [[ ! -x "$DESKTOP/node_modules/.bin/electron" ]]; then
  "$ROOT/scripts/setup-source.sh"
fi

cd "$DESKTOP"
exec npm start >>"$LOG_FILE" 2>&1
