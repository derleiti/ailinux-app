#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
if command -v konsole >/dev/null; then
  exec konsole --workdir "$ROOT"
elif command -v x-terminal-emulator >/dev/null; then
  cd "$ROOT" && exec x-terminal-emulator
elif command -v gnome-terminal >/dev/null; then
  exec gnome-terminal --working-directory="$ROOT"
fi
echo "No supported terminal emulator found" >&2
exit 1
