#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_JAR:?Point ANDROID_JAR at platform36 android.jar (build the module first)}"
: "${KOTLIN_STDLIB:?Point KOTLIN_STDLIB at kotlin-stdlib.jar for the installed protocol regression test}"
mkdir -p build/host-tests
logic=()
for name in CurrentTurnReader TurnQueryBindings RoutePolicy DispatchGuard NameMatcher PlaylistRequest MusicLinkFields QueueRequest SongChoicePolicy PlaybackWords QueueSnapshot NativeSearchCall LiveTurnGate ConfirmationGate ConfirmationTemplate NextSkipGuard SpeechJobGuard; do
 logic+=("src/dev/metrobridge/poc/$name.java")
done
javac -source 8 -target 8 -cp "build/classes:$ANDROID_JAR:$KOTLIN_STDLIB" -d build/host-tests "${logic[@]}" ./*Test.java
for test in ./*Test.java; do
 name="${test##*/}"
 class="${name%.java}"; if [[ "$class" == FreshInputKindTest ]]; then class="dev.metrobridge.poc.FreshInputKindTest"; fi
 java -cp "build/host-tests:build/classes:$ANDROID_JAR:$KOTLIN_STDLIB" "$class"
done
