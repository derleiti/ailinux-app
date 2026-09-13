from __future__ import annotations
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "apps/helper/apps/android"
DESKTOP = ROOT / "apps/helper/apps/desktop"


class UnifiedAppContractTests(unittest.TestCase):
    def test_settings_contract_is_exported_from_aicoder(self) -> None:
        payload = json.loads((ROOT / "shared/contracts/aicoder-settings.json").read_text())
        keys = {row["key"] for row in payload["settings"]}
        for key in {
            "selected_model", "linked_account_providers", "swarm_mode", "workspace_mode",
            "tool_mode", "request_timeout", "max_output_tokens", "approval_mode",
            "runtime_mode", "team_runtime_mode",
        }:
            self.assertIn(key, keys)
        self.assertGreater(len(keys), 20)

    def test_android_is_upgrade_compatible_and_unified(self) -> None:
        gradle = (ANDROID / "app/build.gradle").read_text()
        manifest = (ANDROID / "app/src/main/AndroidManifest.xml").read_text()
        main = (ANDROID / "app/src/main/java/me/ailinux/workspace/MainActivity.java").read_text()
        workspace = (ANDROID / "app/src/main/java/me/ailinux/workspace/WorkspaceActivity.java").read_text()
        self.assertIn("applicationId 'me.ailinux.workspace'", gradle)
        self.assertIn("versionName '3.0.0-alpha.3'", gradle)
        self.assertIn('android:label="AILinux App"', manifest)
        self.assertIn("Sign in with WordPress / password", main)
        self.assertIn("Continue with Google in browser", main)
        self.assertIn("code_challenge_method", main)
        self.assertIn("AI Network · @handle", main)
        self.assertIn("AICoder settings", main)
        self.assertIn("Install / update full AICoder runtime", main)
        self.assertIn("Run AICoder command in Termux", main)
        self.assertIn("aicoder agent ", main)
        self.assertIn("git+https://github.com/derleiti/ai-coder.git", main)
        self.assertIn("class WorkspaceActivity", workspace)

    def test_android_session_uses_keystore(self) -> None:
        secure = (ANDROID / "app/src/main/java/me/ailinux/workspace/SecureStore.java").read_text()
        api = (ANDROID / "app/src/main/java/me/ailinux/workspace/AppApiClient.java").read_text()
        self.assertIn("AndroidKeyStore", secure)
        self.assertIn("AES/GCM/NoPadding", secure)
        self.assertIn("/v1/auth/login", api)
        self.assertIn("/v1/auth/browser/exchange", api)
        self.assertIn("/v1/notify-network/endpoints", api)
        self.assertIn("/v1/client/chat", api)

    def test_desktop_composer_uses_aicoder_primary_and_helper_companion(self) -> None:
        workflow = (ROOT / ".github/workflows/build.yml").read_text()
        primary = (ROOT / "scripts/build_aicoder_primary.py").read_text()
        entry = (ROOT / "scripts/aicoder_primary_entry.py").read_text()
        self.assertIn("upstream/aicoder", workflow)
        self.assertIn("upstream/helper", workflow)
        self.assertIn("build_aicoder_primary.py", workflow)
        self.assertIn("aicoder-primary", primary)
        self.assertIn("from aicoder.cli import main", entry)

    def test_linux_source_launcher_and_assets(self) -> None:
        launcher = (ROOT / "apps/helper/assets/desktop/ailinux-app.desktop").read_text()
        package = json.loads((DESKTOP / "package.json").read_text())
        self.assertIn("Exec=/home/zombie/workspace/ailinux-app/run-source.sh", launcher)
        self.assertIn("Icon=ailinux-app", launcher)
        self.assertIn("Actions=Terminal;Folder;GitHub;", launcher)
        for name in ("ailinux-app.png", "ailinux-app.ico", "ailinux-app-macos-1024.png", "ailinux-app.svg"):
            self.assertTrue((ROOT / "apps/helper/assets/desktop" / name).is_file(), name)
        self.assertEqual(package["build"]["linux"]["icon"], "../../assets/desktop/ailinux-app.png")
        self.assertTrue((ROOT / "run-source.sh").is_file())
        run_source = (ROOT / "run-source.sh").read_text()
        setup_source = (ROOT / "scripts/setup-source.sh").read_text()
        self.assertIn('upstream/aicoder', run_source)
        self.assertIn('upstream/helper', run_source)
        self.assertIn('AILINUX_HELPER_ROOT', run_source)
        self.assertIn('-m aicoder.cli gui', run_source)
        self.assertIn('pip install -e "$AICODER"', setup_source)
        self.assertIn('npm run check', setup_source)

    def test_composer_pins_independent_upstreams(self) -> None:
        lock = json.loads((ROOT / "upstreams.lock.json").read_text())["generated_from"]
        self.assertEqual(lock["aicoder"]["role"], "primary application and tray")
        self.assertEqual(lock["helper"]["role"], "independent device capability companion")
        self.assertTrue((ROOT / ".gitmodules").is_file())
        modules = (ROOT / ".gitmodules").read_text()
        self.assertIn("derleiti/ai-coder.git", modules)
        self.assertIn("derleiti/ailinux-helper.git", modules)

    def test_network_contract_uses_existing_triforce_fabric(self) -> None:
        network = json.loads((ROOT / "shared/contracts/app-network.json").read_text())
        self.assertEqual(network["api"]["network"], "/v1/notify-network")
        self.assertEqual(network["sharing"]["workspace_transport"], "/v1/mcp/node/connect")
        self.assertTrue(network["sharing"]["handle_is_endpoint_address"])


if __name__ == "__main__":
    unittest.main()
