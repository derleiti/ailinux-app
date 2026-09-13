#!/usr/bin/env python3
"""Build AICoder as the primary desktop executable for the composed AILinux App."""
from __future__ import annotations
import os
from pathlib import Path
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
AICODER = ROOT / 'upstream' / 'aicoder'
ENTRY = ROOT / 'scripts' / 'aicoder_primary_entry.py'
OUT = ROOT / 'build' / 'primary'
NAME = 'ailinux-app.exe' if os.name == 'nt' else 'ailinux-app'
OUT.mkdir(parents=True, exist_ok=True)

cmd = [
    sys.executable, '-m', 'PyInstaller', '--onefile', '--name', 'aicoder-primary',
    '--noconfirm', '--clean', '--windowed', '--collect-submodules=aicoder',
    '--collect-submodules=keyring', '--hidden-import=PyQt6.QtWidgets',
    '--hidden-import=PyQt6.QtGui', '--hidden-import=PyQt6.QtCore',
    '--hidden-import=aicoder.gui', '--hidden-import=aicoder.gui.app',
    '--hidden-import=aicoder.helper_control', '--hidden-import=certifi',
    '--add-data', f'{AICODER / "aicoder" / "gui" / "design_tokens.json"}:aicoder/gui',
    '--add-data', f'{AICODER / "pyproject.toml"}:.',
    str(ENTRY),
]
subprocess.run(cmd, cwd=AICODER, check=True)
built_name = 'aicoder-primary.exe' if os.name == 'nt' else 'aicoder-primary'
built = AICODER / 'dist' / built_name
if not built.exists():
    raise SystemExit(f'PyInstaller did not create {built}')
shutil.copy2(built, OUT / NAME)
if os.name != 'nt':
    (OUT / NAME).chmod(0o755)
print(OUT / NAME)
