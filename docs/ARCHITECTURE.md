# AILinux App architecture

## One product, platform-specific execution

AILinux App merges AICoder and AILinux Helper at the product and capability-contract layer without pretending that Android, Windows and Linux have identical process models.

### Identity

A single authenticated AILinux account is used by the app. Authentication supports:

- WordPress-backed AILinux email/password through `/v1/auth/login`;
- Google through the existing browser login and PKCE exchange;
- existing AICoder provider-account integrations, whose credentials remain owned by their official CLI clients.

Each device can publish one or more endpoints to `/v1/notify-network`. A username-style `@handle` is the human address for the endpoint and carries presence/directory semantics.

### Android

The APK keeps `me.ailinux.workspace` for upgrade/signature continuity. The launcher is the native unified UI. The previous Helper UI is retained as `WorkspaceActivity`.

Native Android implements account login, model discovery, chat, handle/network directory and the entire AICoder settings contract. The full Python AICoder runtime executes in user-controlled Termux because Android does not provide the desktop Python/CLI process environment. Termux access requires an explicit local release and is not automatically exposed as a remote shell.

### Desktop

Electron remains the local capability host because it already contains the hardened workspace, clipboard, screen, Docker and typed-service boundary. A platform-native PyInstaller `aicoder-sidecar` is built on each GitHub runner and embedded into the same installer/package. Electron exposes only status/start/stop controls for that bundled runtime.

### Capability fabric

There is one logical capability network:

- `/v1/notify-network` — identity, `@handle`, directory, presence and messaging;
- `/v1/mcp` + node/workspace transport — MCP and workspace/device capabilities;
- AILinux Loom contracts — target/grant/lease orchestration;
- AICoder — worker/agent logic consuming the same canonical tool contracts.

Login alone never grants workspace, clipboard, screen, shell or compute access.
