# Build MetroBridge

The prebuilt release is on [GitHub Releases](https://github.com/ArduinoQuack/MetroBridge/releases/latest). Building is useful for reviewed compatibility changes or a different Morphe signing certificate.

## Dependencies

- JDK with javac and keytool
- Android SDK platform 36 and build-tools 36
- Compile-only [Xposed API 82](https://api.xposed.info/) JAR
- Python 3 for signing/profile preparation
- Optional Python androguard/loguru for APK verifiers
- Kotlin stdlib JAR for the protocol host regression

The Android app is plain Java. No Gradle daemon, bundled SDK, vendor APK or private signing key is required in the source repository.

## Build on a development computer

```sh
ANDROID_JAR=/path/android-36/android.jar \
BUILD_TOOLS=/path/build-tools/36.0.0 \
XPOSED_API=/path/api-82.jar \
bash build.sh
```

Output: `MetroBridge.apk`. The script generates `.signing/probe.jks` on a fresh checkout, or reuses it if already present. It binds callback authentication to the actual module signing certificate. Keep the key private and retain it across updates. A newly generated key cannot update an installation signed by another key.

The published APK uses the maintainer's existing private key. That key is intentionally not distributed. If you switch from the published APK to a differently signed self-build, Android treats it as a signing conflict; preserve your settings before replacing an installation.

## Use your own Morphe signing certificate

MetroBridge validates MetroList's UID, version and signing certificate. Renaming an APK does not make a different certificate match the included profile.

Obtain the public certificate fingerprint from your locally patched MetroList APK using the Android SDK build tool:

```sh
/path/build-tools/36.0.0/apksigner verify --print-certs /path/renamed-MetroList.apk
```

Copy the **Signer #1 certificate SHA-256 digest**, then build with:

```sh
METRO_CERT_SHA256=YOUR_64_HEX_CHARACTER_CERTIFICATE_DIGEST \
ANDROID_JAR=/path/android-36/android.jar \
BUILD_TOOLS=/path/build-tools/36.0.0 \
XPOSED_API=/path/api-82.jar \
bash build.sh
```

The value is validated and applied only to the explicit MetroList signing profile in BridgeStore. The module's own callback signing identity remains bound to its own build key. This option does not make another MetroList version structurally compatible or disable signature checks.

Google's inspected signing identities, version/code guards and runtime schema checks remain unchanged. Review the source/verifiers before adding another app version.

## Tests

After compiling the Android sources:

```sh
ANDROID_JAR=/path/android-36/android.jar \
KOTLIN_STDLIB=/path/kotlin-stdlib.jar \
bash Test-host.sh
bash Test-extras.sh
python3 Verify-release.py MetroBridge.apk
```

The clean release passed 336 logic checks plus 19 actual foreground-adapter and 24 actual screen-hook checks with source-only host Android/Xposed doubles. The doubles are compiled separately and never shipped in the APK. Source verifiers accepting a Google/MetroList APK path are for developer use; keep those APKs outside commits.

See [TESTING.md](../TESTING.md) for device smoke tests and [ARCHITECTURE.md](../ARCHITECTURE.md) for hook boundaries.
