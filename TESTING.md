# Current release checks (0.33)

- 30 reflection, discovery-voting and cache regression checks.
- 29 submitted Play next routing and failure checks with host doubles.
- 49 required type roles and 62 critical native field/method bindings checked against Google 17.68.17 and 17.69.19.
- 4,039 original mapping assertions against the older APK.
- APK signature/alignment checks; existing playback, queue and speech class bodies unchanged from 0.32.
- APK reported working by the maintainer on a Galaxy S24+ with Android 16 and Vector. Other devices and future updates remain unverified.

Current tests and build details are in the [0.33 source bundle](https://github.com/ArduinoQuack/MetroBridge/releases/tag/v0.33). The release source archive contains the baseline build inputs. The earlier checks below describe 0.21.

---

# Verification

The owner confirmed playlist/shuffle, queue insertion and native spoken confirmation working in the0.19 integration. The optional timeout feature was added in0.20; no independent measured idle-timeout result has been supplied.0.21 is a cleanup release, not a new command implementation.

## Automated checks

- 336 host logic checks for routing, matching, pagination, queue preservation, exact-turn identity, duplicate suppression, confirmation templates and job ownership.
- 19 checks running the actual foreground adapter with isolated host Android doubles.
- 24 checks running the actual screen-timeout adapter with isolated Android/Xposed doubles.
- Release manifest/DEX checks: no debug flag, permissions, services, launcher, test activities, trace/report classes or endpoints, AudioTrack observer, Android TTS, microphone or volume changes.
- Fresh Android36 compilation, same-key signing and zip alignment.
- Optional supplied-APK verifiers validate private Google/MetroList profiles and the Gemini screen-wake mechanism.

Host doubles are never APK dependencies. These checks do not verify real Binder/WMS behavior, actual speech audibility or locked-screen operation.

## Device smoke test

Install over the current same-signed app, retain settings/scopes, keep the separate Voice Test disabled, and reload Google using the README LADB block. Avoid clearing app data or force-stopping active MetroList playback.

| Test | Expected result |
|---|---|
| Play my Rap playlist | Correct local playlist starts; full custom confirmation |
| Shuffle my Pop playlist | Correct playlist starts with native functional shuffle |
| Play Lose Yourself by Eminem next | Song inserted next; current track/position, remaining queue, shuffle and repeat preserved; title/artist spoken |
| Play this song next | Current session metadata resolved; queue preserved |
| Play the next song | Ordinary skip, with no queue insertion |
| Normal/custom wording | Valid template saved; optional metadata omitted cleanly; no module chooser popup |
| Unknown/empty search | Queue unchanged; no fabricated success or automatic mutation retry |
| Ambiguous result | Automatic best/first valid candidate per configured project policy; selection may be wrong |
| Concurrent/new request | Old ticket cannot announce or foreground a newer action |
| Missing network or closed MetroList | Bounded result/failure where Android permits binding; no stale insertion retry |
| Pause/resume/Bluetooth controls | Existing native media controls preserved |
| Timeout enabled | Idle Gemini window follows existing display timeout where no other native policy holds it on |
| Timeout disabled again | Google's original current window flags restored |
| Long spoken reply | Check continuity if display times out; Google screen-off behavior is not independently verified |
| Locked invocation | Obeys Android/Gemini lock-screen policy; no bypass |
| Master disabled | New bridge actions stop; pending foreground requests cannot run |
| App update | Compatible schema continues, or private adapter disables safely |
| Device restart | Requires root/LSPosed to be available again; no restart is needed for installation |

Use normal module settings and LSPosed's own module status for troubleshooting. Development capture/export commands and test activities are intentionally absent from this release. Refer to README for rollback.
