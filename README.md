# MetroBridge

**Control MetroList with Gemini.** Play local playlists, shuffle your music, and queue a song next without replacing the queue you are listening to.

[![Release](https://img.shields.io/github/v/release/ArduinoQuack/MetroBridge)](https://github.com/ArduinoQuack/MetroBridge/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![LSPosed](https://img.shields.io/badge/Requires-LSPosed-orange.svg)](#requirements)

[**Download APK**](https://github.com/ArduinoQuack/MetroBridge/releases/latest/download/MetroBridge.apk) · [Setup](#setup) · [Example prompts](#example-prompts) · [Compatibility](#compatibility) · [Troubleshooting](#troubleshooting) · [Build](docs/BUILDING.md)

> **Heads up: this is fragile.** MetroBridge hooks private parts of the Google app. Google updates can break playback, Play next or spoken replies, sometimes without an obvious error. Automatic hook discovery helps, but it does not make this update-proof. Root and LSPosed/Vector are required. If an update breaks it, disable MetroBridge until there is a fix.

## Features

- **Local playlist playback:** resolve names against your cached MetroList library, including approximate name matching.
- **Functional playlist shuffle:** request shuffle explicitly or make it the default for playlist requests.
- **Play next without losing the queue:** search for a song and insert it immediately after the current track. Preserve the current song, playback position within tolerance, other queued tracks, shuffle and repeat.
- **Current-song references:** “Play this song next” uses the current media session.
- **Automatic selection:** choose the best available song match, with a first-valid-result fallback. No module chooser popups.
- **Native spoken confirmations:** use Google's speech path to announce the playlist, or the song and artist queued next.
- **Custom wording:** separate playlist and Play next templates with metadata variables and inline previews.
- **Normal screen timeout:** optional Gemini window setting that lets your existing display timeout apply.
- **Minimal runtime:** hidden launcher entry, settings through LSPosed, no software microphone listener, polling, extra service or debug/report interface.

Google's wake word, recognition, normal assistant behavior and native media controls remain in place. MetroBridge is a separate module; it does not patch the Google APK.

## Example prompts

Say “Hey Google” first, or activate Gemini using your usual shortcut.

| Say | Result |
|---|---|
| “Play my Rap playlist.” | Start the matching local playlist. |
| “Shuffle my Pop playlist.” | Start that playlist with native shuffle. |
| “Play my Workout playlist shuffled.” | Start the requested playlist in random order. |
| “Play Lose Yourself by Eminem next.” | Insert the selected song next, keeping current playback and the remaining queue. |
| “Play this song next.” | Insert the current media-session song next. |
| “Pause music.” / “Resume music.” | Use existing Gemini/MetroList playback controls. |
| “Play the next song.” | Ordinary skip; this is different from inserting a named song next. |

**Use separate requests:** “Shuffle my Rap playlist”, then “Play Lose Yourself by Eminem next”. A single compound request combining both is not implemented.

Default spoken replies are **“Playing Rap”** and **“Playing Lose Yourself by Eminem next”**. Gemini's visible/cloud response text is not rewritten.

## Requirements

- A rooted Android setup with a working, device-compatible [LSPosed](https://github.com/LSPosed/LSPosed) environment. MetroBridge itself does not need a root grant.
- Google Gemini configured as your assistant, with the [Google app](https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox) installed.
- A supported [MetroList](https://github.com/MetrolistGroup/Metrolist) APK renamed to `com.google.android.apps.youtube.music` using [Morphe Manager](https://morphe.software/).
- Gemini's [YouTube Music connection](https://support.google.com/gemini/answer/15237118?co=GENIE.Platform%3DAndroid&hl=en) and `music.youtube.com` links directed to the renamed MetroList app.
- Matching app versions and a trusted MetroList signing certificate; see [Compatibility](#compatibility).

## Setup

Already using MetroList under YouTube Music's package name? Skip to [Install MetroBridge](#2-install-metrobridge). Keep your working installation and data.

### 1. Prepare MetroList with Morphe

The original setup guide is [my Reddit post as u/androidforthewin](https://www.reddit.com/r/metrolist/comments/1tnnm1c/guide_how_to_get_gemini_to_play_songs_on_metrolist/) in r/metrolist. This repository uses Morphe instructions throughout, with current patch names.

1. Back up your MetroList library/settings and your [Morphe signing keystore](https://github.com/MorpheApp/morphe-manager/blob/main/docs/backup-and-keystore.md).
2. Install [Morphe Manager](https://morphe.software/) and download the supported [MetroList 13.6.1 APK](https://github.com/MetrolistGroup/Metrolist/releases/tag/v13.6.1).
3. Enable **Settings → Advanced → Expert settings → Expert mode** in Morphe. Older releases may place Expert mode directly under Settings.
4. Choose the MetroList APK through **Other apps** or the APK file picker.
5. Select the universal **Clone app** patch, called **Change package name** in older versions. Open its settings/gear and set **Package name** to exactly:

   ```text
   com.google.android.apps.youtube.music
   ```

6. Select **Disable Play Store updates**, then patch and install using the regular Android installer. These are the package-preparation patches; MetroList does not need YouTube-specific patches or GmsCore support for this guide.
7. Open the renamed MetroList, restore your backup if needed, sign in using your own account where required, and confirm that normal playback works.
8. In Android **App info → Open by default / Set as default**, enable supported links and select `music.youtube.com` for the renamed MetroList.
9. In Gemini, open **Connected Apps** and connect YouTube Music. Follow [Google's account/activity requirements](https://support.google.com/gemini/answer/15237118?co=GENIE.Platform%3DAndroid&hl=en).

An installed official YouTube Music app occupies the same package name and can conflict with installation. Back up data before replacing an app. Disabling or uninstalling a preinstalled app for one user may leave its system package reserved; this guide does not instruct firmware/system modifications to resolve that case.

**Important:** Morphe signs the patched APK. If its certificate differs from the module's trusted profile, the prebuilt module will refuse that MetroList build. Use the [custom-certificate build instructions](docs/BUILDING.md#use-your-own-morphe-signing-certificate) rather than disabling authentication.

See [the full Morphe setup and update notes](docs/MORPHE-SETUP.md), [official Expert mode guide](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patching-expert-mode.md) and [current universal patch list](https://github.com/MorpheApp/morphe-patches#-patches-list).

### 2. Install MetroBridge

1. Download **MetroBridge.apk** from [Releases](https://github.com/ArduinoQuack/MetroBridge/releases/latest) and install it. Updates use the same signing key and retain settings.
2. Enable MetroBridge in LSPosed. Scope it to:

   | App | Package |
   |---|---|
   | Google | `com.google.android.googlequicksearchbox` |
   | Renamed MetroList | `com.google.android.apps.youtube.music` |

3. Disable the separate experimental **MetroBridge Voice Test** module if you used an earlier test build.
4. Open **LSPosed → MetroBridge → Settings**. Enable integration and Play next, then tap **Refresh playlists**.
5. Reload Google using this **LADB/aShell** block:

   ```sh
   am force-stop com.google.android.googlequicksearchbox
   am start -W -n com.google.android.googlequicksearchbox/com.google.android.googlequicksearchbox.SearchActivity
   ```

6. On the first native queue-hook installation, reopen MetroList after applying its LSPosed scope. No phone reboot or root grant to MetroBridge is needed.

The app is hidden from the launcher. Settings can also be opened using LADB:

```sh
am start -n dev.metrobridge.poc/.MainActivity
```

### 3. Customize

| Setting | Purpose |
|---|---|
| Enable integration | Master switch. |
| Shuffle playlists | Shuffle playlist requests by default. |
| Play next | Enable single-song queue insertion. |
| Spoken confirmations | Announce successful actions through Google's native speech path. |
| Allow normal screen timeout | Opt-in Gemini window policy using your existing screen timeout. |
| Custom wording | Edit playlist and Play next confirmations. |
| Refresh playlists | Update cached names after adding/renaming playlists. |

Example templates:

```text
Playing {playlist}
Playing {song}{by_artist} next
Queued {song}{by_artist} {position}
```

| Variables | Values |
|---|---|
| `{playlist}`, `{song}`, `{title}` | Resolved playlist/song title for that action. |
| `{artist}`, `{author}`, `{album}` | Available media metadata; author is an alias for artist. |
| `{action}`, `{position}`, `{queue_size}`, `{shuffle}`, `{app}` | Action and verified playback/queue context where available. |
| `{by_artist}`, `{from_album}` | Optional complete phrases such as “ by Eminem”; empty when metadata is missing. |

Templates are validated plain text, limited to 300 input and 400 rendered characters. Unknown metadata is left empty.

## Compatibility

| Component | Inspected/tested profile |
|---|---|
| Device | Samsung Galaxy S24+ SM-S926U, Android 16 |
| Google app | 17.69.19.ve.arm64 / 301825986; discovery also checked against 17.68.17.ve.arm64 / 301824130 |
| MetroList | 13.6.1, renamed package and matching signing certificate |
| Module | 0.33, confirmed working on the maintainer’s phone |

The included MetroList certificate profile is:

```text
b8bb9a23a9b8ddbfa269b8a4ecd87d0e2c88bbb37edb72a7599732b411fb031a
```

This is a public certificate fingerprint, not a private key. Different patching keys require a matching module build. Google hooks are discovered from the installed APK rather than selected from a Google-version allowlist. The runtime checks class and member relationships and leaves unresolved hooks disabled. A future Google update can still change those relationships enough to break the integration. MetroList still requires a compatible build and signing certificate.

## Limitations

- Named-playlist, shuffle and single-song Play next are the implemented focus. [Full command coverage](SUPPORTED-COMMANDS.md) distinguishes these from existing media controls.
- Append-to-end, queue editing, aggregate liked/downloaded/recent collections, advanced history/artist/album requests and compound commands are not implemented.
- Automatic matching can choose an unintended result when names or Google links are ambiguous. There is no chooser fallback; empty/unusable results do not create fabricated content.
- Some non-English playlist matching remains unresolved.
- Google account, region, server-side decisions and [YouTube Music playback restrictions](https://support.google.com/gemini/answer/15237118?co=GENIE.Platform%3DAndroid&hl=en) can affect delivery. MetroBridge does not change those checks.
- Locked-screen, Android Auto, casting/shared queues and future app versions are not universally verified. The native bridge refuses unsupported queue states.
- The normal-timeout option targets Gemini's activity-window flag. Other video/view or OS policies can still keep the screen awake.

## Troubleshooting

| Symptom | Check |
|---|---|
| MetroList will not install | Package conflict, or a different signing key from an existing installation. Back up before replacing anything. |
| Gemini asks to install YouTube Music | Exact package name, Gemini connection and supported link routing. Renaming does not implement Google's full provider protocol. |
| Playlist is missing or renamed | Refresh playlists in MetroBridge settings. |
| Play next does nothing | Module enabled in both scopes, Play next enabled, supported versions and trusted MetroList certificate. Reopen MetroList after the first scope change. |
| Voice is unchanged | Spoken confirmations enabled, compatible Google hooks, separate Voice Test disabled; reload Google after module updates. Visible text is expected to remain unchanged. |
| Screen stays awake | Enable Allow normal screen timeout, reload Google after installation, and check whether an independent video/view/OS policy is active. |
| A later update stops working | Check the compatibility table; disable MetroBridge and check Releases for a fix. |

Normal failures retain a bounded last-request message in settings. Debug capture/report tools are intentionally absent from the release.

## Updates

MetroBridge updates independently of Google and MetroList; Google is never repacked for this module. **The MetroList package rename is still an APK-level preparation step:** a replacement MetroList APK must be patched with Morphe again using the same package name and signing key. Preserve your Morphe keystore. A new app version may still require a module update.

## Rollback

Turn off a feature toggle or **Enable integration** to stop new bridge actions. Disable the module in LSPosed and reload Google with the LADB block above to unload its Google hooks. To unload the native MetroList hook completely, remove MetroList from scope and restart that app after saving/stopping the listening session.

No bootloader unlock, firmware flash, system-partition write or Knox modification is part of MetroBridge installation or rollback.

## Development

[Build instructions](docs/BUILDING.md) · [Architecture](ARCHITECTURE.md) · [Tests](TESTING.md) · [Changelog](CHANGELOG.md) · [Validation metadata](Validation.json)

For 0.33, 59 host checks passed and critical hook bindings were checked against both supplied Google APKs. The maintainer confirmed the APK works on their phone. That does not establish compatibility with every device or future update. The [0.33 release source bundle](https://github.com/ArduinoQuack/MetroBridge/releases/tag/v0.33) includes the current changes, tests and inputs needed for the incremental build. The root source tree is the earlier 0.21 release. Private signing keys, account data and supplied third-party APKs are excluded.

## Credits and license

Created by [ArduinoQuack](https://github.com/ArduinoQuack). Package-preparation background: [u/androidforthewin's original MetroList/Gemini guide](https://www.reddit.com/r/metrolist/comments/1tnnm1c/guide_how_to_get_gemini_to_play_songs_on_metrolist/).

Thanks to [MetroList](https://github.com/MetrolistGroup/Metrolist), [Morphe](https://github.com/MorpheApp/morphe-manager) and [LSPosed](https://github.com/LSPosed/LSPosed). This is an independent project, not an official Google, MetroList or Morphe integration.

MetroBridge source is available under the [MIT license](LICENSE). Referenced projects retain their own licenses and trademarks.
