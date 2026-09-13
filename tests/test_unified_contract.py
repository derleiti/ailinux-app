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
        self.assertIn("versionName '3.0.0-alpha.1'", gradle)
        self.assertIn('android:label="AILinux App"', manifest)
        self.assertIn("Sign in with WordPress / password", main)
        self.assertIn("Continue with Google in browser", main)
        self.assertIn("code_challenge_method", main)
        self.assertIn("AI Network · @handle", main)
        self.assertIn("AICoder settings", main)
        self.assertIn("Install / update full AICoder runtime", main)
        self.assertIn("Run AICoder command in Termux", main)
        self.assertIn("aicoder agent ", main)
        self.assertIn("git+https://github.com/derleiti/ailinux-app.git#subdirectory=core/aicoder", main)
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

    def test_desktop_packages_bundled_aicoder(self) -> None:
        package = json.loads((DESKTOP / "package.json").read_text())
        main = (DESKTOP / "main.js").read_text()
        runtime = (DESKTOP / "aicoder_runtime.js").read_text()
        self.assertEqual(package["productName"], "AILinux App")
        self.assertTrue(any(row.get("to") == "aicoder" for row in package["build"]["extraResources"]))
        self.assertIn("Open AICoder", main)
        self.assertIn("aicoder-sidecar", runtime)

    def test_network_contract_uses_existing_triforce_fabric(self) -> None:
        network = json.loads((ROOT / "shared/contracts/app-network.json").read_text())
        self.assertEqual(network["api"]["network"], "/v1/notify-network")
        self.assertEqual(network["sharing"]["workspace_transport"], "/v1/mcp/node/connect")
        self.assertTrue(network["sharing"]["handle_is_endpoint_address"])


if __name__ == "__main__":
    unittest.main()
