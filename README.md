# AILinux App

**3.0.0 alpha 1** — the unified AILinux client built from **AICoder + AILinux Helper**.

One installation combines AI/coding, authenticated AILinux access, the `@handle` AI network, MCP/workspace sharing and device capabilities across Android, Linux, Windows and macOS.

## What is merged

| Layer | Unified behavior |
|---|---|
| Account | AILinux/WordPress email+password login plus Google browser PKCE login |
| AI | TriForce model catalog/chat and the complete imported AICoder runtime |
| Settings | Canonical AICoder settings registry exported to a shared contract and rendered natively on Android |
| AI Network | Existing TriForce `/v1/notify-network` with per-device `@handle`, presence and directory |
| Workspace | Existing Helper SAF/browser/desktop workspace leases, read-only/write grants and reconnect |
| Device | Clipboard, screen observation, resource advertising, Docker compute and typed services where supported |
| Provider accounts | Provider-owned CLI sessions remain owned by Codex/Claude/Vibe/Antigravity/etc.; Android reaches them through the explicitly released Termux runtime instead of copying OAuth tokens |

## Android

The Android application keeps package id `me.ailinux.workspace` so a correctly signed AILinux App can upgrade the existing Helper installation.

The launcher now contains:

- WordPress/AILinux manual login;
- Google browser + PKCE login;
- model selection and native TriForce chat;
- AI Network endpoint publication under a username-style `@handle`;
- network directory;
- the complete generated AICoder settings surface;
- the original Helper workspace/device executor as a dedicated screen;
- Termux integration for local provider runtimes and approved commands.

Session material is encrypted using Android Keystore AES-GCM.

## Desktop

Linux, Windows and macOS packages use the existing Electron Helper capability host and bundle AICoder as an internal `aicoder-sidecar` executable. The tray can open both the workspace/device host and the bundled AICoder GUI from the same installed product.

## Repository layout

```text
core/aicoder/                 imported AICoder Python runtime
apps/helper/apps/android/     unified native Android app + Helper executor
apps/helper/apps/desktop/     unified Electron capability host
apps/helper/apps/ios/         iOS design target
shared/contracts/             cross-platform settings/network contracts
shared/design/                shared visual tokens
tests/                        merge/integration contracts
```

Import provenance is recorded in `docs/MERGE_BASELINE.md`; the initial import used remote Git trees rather than dirty local working copies.

## Local verification

```bash
python3 -m compileall -q core/aicoder/aicoder
python3 -m unittest -v tests.test_unified_contract

cd apps/helper/apps/desktop
npm ci
npm run check

cd ../android
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

## Build matrix

GitHub Actions builds:

- Linux: AppImage + `.deb` with bundled AICoder sidecar
- Windows: installer/portable `.exe` with bundled AICoder sidecar
- macOS: `.dmg` + `.zip` with bundled AICoder sidecar
- Android: APK using the existing AILinux package/signing identity for upgrade continuity
- Termux: versioned AICoder source/runtime bundle for Android provider runtimes

## Security boundaries

Sharing remains opt-in and capability-scoped. Logging into the AILinux account does not automatically release a workspace, terminal, clipboard, screen or compute resource. Provider credentials remain in provider-owned clients/keyrings and are not copied into the network.

## License

AILinux-authored material in this integration repository uses the AILinux Proprietary Source License. Imported historical code retains any rights already granted for its historical versions; third-party components retain their own licenses.
