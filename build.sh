#!/usr/bin/env bash
set -euo pipefail
umask 077
cd "$(dirname "$0")"
: "${ANDROID_JAR:?Set ANDROID_JAR to platform android-36/android.jar}"
: "${BUILD_TOOLS:?Set BUILD_TOOLS to Android SDK build-tools/36.0.0}"
: "${XPOSED_API:?Set XPOSED_API to the compile-only Xposed API 82 jar}"
rm -rf build/classes
mkdir -p build/classes .signing
if [[ ! -f .signing/probe.jks ]]; then
 keytool -genkeypair -keystore .signing/probe.jks -storepass android -keypass android -alias probe -keyalg RSA -keysize 2048 -validity 3650 -dname 'CN=MetroBridge Probe Development' -noprompt
fi
# Bind native IPC to the key actually signing this build. Preserve .signing/probe.jks
# privately for future in-place updates; do not publish it with source archives.
python3 - <<'PY'
import hashlib,re,subprocess,os
from pathlib import Path
metro=os.environ.get('METRO_CERT_SHA256','').replace(':','').strip().lower()
if metro and not re.fullmatch(r'[a-f0-9]{64}',metro):raise SystemExit('METRO_CERT_SHA256 must be a SHA-256 certificate fingerprint')
cert=subprocess.check_output(['keytool','-exportcert','-keystore','.signing/probe.jks','-storepass','android','-alias','probe'])
for name in ['NativeQueueBridge','GoogleReplyVoice','GoogleForeground','ScreenTimeoutGuard']:
 p=Path('src/dev/metrobridge/poc/'+name+'.java')
 text,count=re.subn(r'SIGNER="[a-f0-9]{64}"','SIGNER="'+hashlib.sha256(cert).hexdigest()+'"',p.read_text())
 assert count==1
 p.write_text(text)
if metro:
 p=Path('src/dev/metrobridge/poc/BridgeStore.java')
 text,count=re.subn(r'METRO_CERT="[a-f0-9]{64}"','METRO_CERT="'+metro+'"',p.read_text())
 assert count==1
 p.write_text(text)
PY
javac -source 8 -target 8 -cp "$ANDROID_JAR:$XPOSED_API" -d build/classes src/dev/metrobridge/poc/*.java
"$BUILD_TOOLS/aapt2" compile --dir res -o build/resources.zip
"$BUILD_TOOLS/aapt2" link build/resources.zip -o build/unsigned.apk --manifest AndroidManifest.xml -I "$ANDROID_JAR" -A assets
find build/classes -name '*.class' -print > build/classes.txt
"$BUILD_TOOLS/d8" --lib "$ANDROID_JAR" --classpath "$XPOSED_API" --min-api 29 --output build @build/classes.txt
python3 - <<'PY'
import zipfile
with zipfile.ZipFile('build/unsigned.apk','a') as z:z.write('build/classes.dex','classes.dex')
PY
"$BUILD_TOOLS/zipalign" -f 4 build/unsigned.apk build/aligned.apk
"$BUILD_TOOLS/apksigner" sign --ks .signing/probe.jks --ks-pass pass:android --key-pass pass:android --out MetroBridge.apk build/aligned.apk
"$BUILD_TOOLS/apksigner" verify --verbose MetroBridge.apk
