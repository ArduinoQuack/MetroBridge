#!/usr/bin/env python3
"""Regression check: trust anchors must cover every verified Google platform signer.
Usage: python3 Verify-google-signers.py /path/to/apksigner /path/to/google/base.apk
Runs signature verification, not just certificate extraction. No APK is modified.
"""
import pathlib
import re
import subprocess
import sys

apksigner, apk = sys.argv[1:]
result = subprocess.run([apksigner, "verify", "--print-certs", apk],
                        check=True, text=True, capture_output=True)
source = (pathlib.Path(__file__).parent / "src/dev/metrobridge/poc/BridgeStore.java").read_text()
anchors = set(re.findall(r'GOOGLE(?:_LEGACY)?_CERT="([a-f0-9]{64})"', source))
signers = re.findall(r'^Signer \(minSdkVersion=(\d+), maxSdkVersion=(\d+)\) certificate SHA-256 digest: ([a-f0-9]{64})$',
                     result.stdout, flags=re.M)
if not signers:
    raise SystemExit("Expected platform-range signers; inspect APK verification output")
for minimum, maximum, digest in signers:
    assert digest in anchors, "Missing verified signer for SDK " + minimum + "-" + maximum
    print("Verified trust anchor covers SDK " + minimum + "-" + maximum + ": " + digest)
print("All verified platform-range Google signers covered")
