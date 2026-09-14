#!/usr/bin/env python3
from __future__ import annotations
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
LOCK = json.loads((ROOT / 'upstreams.lock.json').read_text())['generated_from']
for name, meta in LOCK.items():
    path = ROOT / meta['path']
    if not (path / '.git').exists() and not (path / '.git').is_file():
        raise SystemExit(f'{name}: submodule is not initialized: {path}')
    expected = meta['ref']
    index_line = subprocess.check_output(['git', '-C', str(ROOT), 'ls-files', '-s', meta['path']], text=True).strip()
    parts = index_line.split()
    pinned = parts[1] if len(parts) >= 2 else ''
    if pinned != expected:
        raise SystemExit(f'{name}: parent gitlink expected {expected}, got {pinned or "missing"}')
    actual = subprocess.check_output(['git', '-C', str(path), 'rev-parse', 'HEAD'], text=True).strip()
    suffix = '' if actual == expected else f' (working checkout currently {actual}; parent pin is authoritative)'
    print(f'{name}: {pinned} OK{suffix}')
