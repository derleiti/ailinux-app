"""Entry point for the AILinux App desktop primary executable."""
from __future__ import annotations
import sys
from aicoder.cli import main

if __name__ == '__main__':
    sys.argv = [sys.argv[0], 'gui', *sys.argv[1:]]
    raise SystemExit(main())
