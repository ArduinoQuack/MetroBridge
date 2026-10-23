# Current Google hook discovery (0.33)

Google hook names are resolved from the installed APK using method evidence, typed field relationships and hashed property labels. Discovery is cached against APK identity and reference bytes; an update invalidates the cache. Conflicting or missing roles are left disabled. Adaptive startup is isolated from public hook installation.

This removes the Google-version allowlist used by the earlier build. It still depends on private Google behavior and can break after an update. The current code and checks are in the [0.33 source bundle](https://github.com/ArduinoQuack/MetroBridge/releases/tag/v0.33).

The original playback/queue architecture is documented below; version-specific details describe the older implementation.

---

# Architecture

## Routing and authorization

Google retains wake-word recognition, transcription and normal assistant functionality. Google-only hooks observe supported music intents/media calls and, for pinned builds, the exact committed current user turn. The independent Play next route accepts one explicit song request; ordinary “play the next song” remains a normal skip. Reused, edited, regenerated, historical and superseded turns are excluded.

The companion provider authenticates calling UID and signing history. It resolves cached local playlist names or a native song-search request, reserves a bounded dispatch key and issues a one-use ticket. Google submits that ticket for a finite background browser operation. The visibility bootstrap is retained only to establish the signed package/control channel; it carries no diagnostic payload.

## MetroList bridge

PlaylistOperation connects to the inspected MusicService using MediaBrowser and dispatches the app's supported local playlist ID with the native shuffle selector. It observes a fresh playing state after dispatch. This callback alone does not prove intended playlist identity or audibility.

QueueOperation connects a genuine browser controller and uses a short authorization lease. The MetroList-only hook accepts signed companion requests and the current protocol2. Search uses MetroList's own search and Song-to-MediaItem conversion. Media3's result window is nonzero; the returned IPC candidate list is bounded separately.

Song selection ranks actual metadata and falls back to the first valid result, as requested. Queue fingerprints are checked before insertion. Native playNext is followed by verification of current track, position tolerance, play state, shuffle/repeat and other-song physical/traversal sequences. Unsupported casting/shared/empty queue paths fail safely. No mutation retry follows a sent insertion or verification failure.

Operations own and disconnect their browser callbacks, deadlines and leases. No companion action window or persistent service is created. Android can still refuse background binding or kill a process; those constraints are not bypassed.

## Native spoken confirmation

The pinned current-input watcher gets the expected model response ID through Google's inspected helper. The provider associates that exact reply with the current local action before dispatch. A success phrase is saved only after the playlist's playing-state observation or verified queue insertion. Artist/album come from selected/current metadata, never inferred from an unchecked query.

Native void stream/plain-text entries capture the exact eligible automatic reply. Pending local work may hold a void launch for at most15seconds; no coroutine continuation is stored or replayed. A signed event resumes processing when success/failure arrives. Unknown replies/modes pass through. Failure/timeout can restore the original reply only while still current.

The native text job keeps Google's voice lookup, AudioSessionConfig and focus behavior. An in-flight replacement is reused for a duplicate exact engine/reply, preventing a later native job from cancelling it. A new turn clears this association. The inspected native job-completion callback coordinates foreground handoff. No AudioTrack/synthesis observation hook, device TTS fallback, audio capture or trace IPC is present in the release.

After local success, MetroList foregrounding waits for the native job to finish. If no job appears within20seconds, it requests foreground without inventing speech. Once a job starts, that timeout is removed; expiry drops the foreground request rather than interrupting a running reply. A signed event lets one authenticated Google process claim the foreground action. New turns, supersession, expiry or master disable cancel stale requests. Android launch/lock-screen rules remain in force.

## Screen timeout

ScreenTimeoutGuard tracks only exact Gemini MainActivity/FloatyActivity windows using weak keys. Lifecycle registration is posted after Application.attach, when the Application context becomes available. Structurally checked WindowManagerGlobal addView/updateViewLayout hooks filter copied window parameters when the opt-in policy is enabled.

Only FLAG_KEEP_SCREEN_ON is removed from the submitted copy. Google's original Window attributes are preserved and re-submitted when the setting is disabled. Other flags, video/view-level requests, CPU wake locks and other app windows remain native. Configuration is cached and refreshed through activity lifecycle and authenticated settings events; there is no poller, forced sleep or global timeout write. Setup/configuration failure retains native behavior.

## Compatibility and release boundaries

Private Google profiles are exact version/code mappings with runtime type/member checks; MetroList has a version/signature and structural native-bridge guard. Unknown private interfaces disable rather than guessing offsets or bypassing authentication. Future compatibility is not guaranteed.

0.21 removes development-only exporters, phase counters, audio observations, shell report endpoints, empty logging wrappers and legacy action activities. A one-time settings migration deletes private diagnostic keys without clearing the library, templates or preferences. User-created external reports are not deleted. Only normal bounded last-request errors remain in settings.
