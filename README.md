# AILinux App

**3.0.0 alpha 3** — the unified AILinux client built from **AICoder + AILinux Helper**.

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

Linux, Windows and macOS builds now compose **AICoder as the primary executable** with **AILinux Helper as an independent companion package**. The AICoder tray starts or opens Helper on demand; Helper keeps its own process lifecycle and package identity.

## Repository layout

```text
upstream/aicoder/             Git submodule: primary AICoder application
upstream/helper/              Git submodule: independent Helper companion
apps/helper/apps/android/     unified native Android app + Helper executor
apps/helper/apps/desktop/     unified Electron capability host
apps/helper/apps/ios/         iOS design target
shared/contracts/             cross-platform settings/network contracts
shared/design/                shared visual tokens
tests/                        merge/integration contracts
```

Import provenance is recorded in `docs/MERGE_BASELINE.md`; the initial import used remote Git trees rather than dirty local working copies.

## Start from source on Linux

The source checkout can be launched directly from the user home directory:

```bash
cd /home/zombie/workspace/ailinux-app
./run-source.sh
```

`./run-source.sh --check` validates the composed runtime. The installed KDE/Plasma launcher starts **AICoder as the primary application**. The independent AILinux Helper is discovered through `AILINUX_HELPER_ROOT` and can be started/opened from the AICoder tray menu. The composer pins both upstream repositories through Git submodules plus `upstreams.lock.json`.

## Local verification

```bash
python3 -m compileall -q upstream/aicoder/aicoder
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

## Composition model

AILinux App is a composer rather than a source fork. `upstream/aicoder` and `upstream/helper` are Git submodules pinned by `upstreams.lock.json`. AICoder is the primary desktop app and owns the main tray. AILinux Helper remains independently installable/runnable and is launched on demand from AICoder. Android uses the composed native shell and installs the current AICoder runtime directly from `derleiti/ai-coder` inside Termux.
