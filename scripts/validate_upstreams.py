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
    actual = subprocess.check_output(['git', '-C', str(path), 'rev-parse', 'HEAD'], text=True).strip()
    expected = meta['ref']
    if actual != expected:
        raise SystemExit(f'{name}: expected {expected}, got {actual}')
    print(f'{name}: {actual} OK')
