#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p build/lifecycle-host build/screen-host
mapfile -t lifecycle_sources < <(find lifecycle-host-tests -name '*.java')
javac -source 8 -target 8 -d build/lifecycle-host src/dev/metrobridge/poc/ForegroundHandoff.java "${lifecycle_sources[@]}"
java -cp build/lifecycle-host dev.metrobridge.poc.ForegroundHandoffTest
mapfile -t screen_sources < <(find screen-host-tests -name '*.java')
javac -source 8 -target 8 -d build/screen-host src/dev/metrobridge/poc/ScreenTimeoutGuard.java "${screen_sources[@]}"
java -cp build/screen-host dev.metrobridge.poc.ScreenTimeoutGuardTest
