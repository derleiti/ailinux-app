#!/usr/bin/env python3
"""Build the AICoder runtime binary consumed by the Electron AILinux App."""
from __future__ import annotations
import os
from pathlib import Path
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
CORE = ROOT / "core" / "aicoder"
OUT = ROOT / "apps" / "helper" / "apps" / "desktop" / "sidecar"
NAME = "aicoder-sidecar.exe" if os.name == "nt" else "aicoder-sidecar"

OUT.mkdir(parents=True, exist_ok=True)
for candidate in OUT.glob("aicoder-sidecar*"):
    if candidate.name != "README.txt":
        candidate.unlink()

cmd = [
    sys.executable, "-m", "PyInstaller", "--onefile", "--name", "aicoder-sidecar",
    "--console", "--noconfirm", "--clean", "--collect-submodules=aicoder",
    "--collect-submodules=keyring", "--hidden-import=PyQt6.QtWidgets",
    "--hidden-import=PyQt6.QtGui", "--hidden-import=PyQt6.QtCore",
    "--hidden-import=aicoder.gui", "--hidden-import=aicoder.gui.app",
    "--hidden-import=aicoder.gui.main_window", "--hidden-import=aicoder.gui.chat_widget",
    "--hidden-import=aicoder.gui.settings_widget", "--hidden-import=aicoder.gui.autostart",
    "--hidden-import=certifi", "aicoder_main.py",
]
subprocess.run(cmd, cwd=CORE, check=True)
built = CORE / "dist" / NAME
if not built.exists():
    raise SystemExit(f"PyInstaller did not create {built}")
shutil.copy2(built, OUT / NAME)
if os.name != "nt":
    (OUT / NAME).chmod(0o755)
print(OUT / NAME)
