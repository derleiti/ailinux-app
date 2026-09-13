#!/usr/bin/env python3
from __future__ import annotations
import os
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parents[1]
platform = sys.argv[1]
version = (ROOT / 'VERSION').read_text().strip()
out = ROOT / 'build' / f'AILinux-App-{version}-{platform}-bundle'
if out.exists(): shutil.rmtree(out)
out.mkdir(parents=True)
primary = ROOT / 'build' / 'primary' / ('ailinux-app.exe' if platform == 'windows' else 'ailinux-app')
shutil.copy2(primary, out / primary.name)
helper_dist = ROOT / 'upstream' / 'helper' / 'apps' / 'desktop' / 'dist'
patterns = {'linux':['*.deb','*.AppImage'], 'windows':['*.exe'], 'macos':['*.dmg','*.zip']}[platform]
count=0
for pattern in patterns:
    for src in helper_dist.glob(pattern):
        shutil.copy2(src, out / src.name); count += 1
if not count: raise SystemExit('No Helper package found')
(out / 'README.txt').write_text(
    'AILinux App composed desktop bundle\n\n'
    'Primary: AICoder (ailinux-app)\n'
    'Companion: AILinux Helper (independent package)\n'
    'Start AICoder first; launch Helper from the AICoder tray menu.\n'
)
archive = shutil.make_archive(str(out), 'zip', root_dir=out)
print(archive)
