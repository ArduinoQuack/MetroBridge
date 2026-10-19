# Supported commands

| Request | Coverage |
|---|---|
| Play my Rap/Pop/Workout playlist | Cached local playlist-name resolution; refresh after library changes |
| Shuffle my Rap playlist / play Pop shuffled | Native playlist shuffle; optional default shuffle |
| Play Lose Yourself by Eminem next | Native single-song search and insertion after current track, with queue/state checks |
| Play this song next | Current media-session song reference |
| Pause/resume/stop/next/previous, shuffle/repeat controls | Existing Google/MetroList media controls; not independently implemented for every phrasing |
| Shuffle all playlists; liked/recent/downloaded collections | No dedicated aggregate or collection-language route |
| Add to queue/end; multiple songs; move/remove/clear queue | Not implemented |
| General song/artist/album play/search, similar/newest/most-listened content | No new module route outside Play next; existing Google provider behavior may vary |
| Like/unlike; conversation/history references | Not implemented beyond the current media-session song reference |
| Seek, speed, pause after track, selected playlist start song | No new module implementation |
| Shuffle Rap and play Lose Yourself next in one sentence | Not implemented atomically; use two separate requests |

## Result selection

The module has no chooser dialogs. Song search ranks actual title/artist/edition metadata, then uses the first valid returned song if no strong match exists. Playlist selection uses the first existing resolver candidate. An opaque Google link without a local name cannot establish the intended playlist. Israeli playlist matching remains unresolved. Automatic selection can therefore choose the wrong result for ambiguous or missing identity.

Empty/unusable results do not manufacture content. Failed actions keep a bounded last-request error in settings and do not show a popup or retry a mutation.

## Spoken confirmation

Eligible automatic Google replies are correlated with exact current successful local actions. Defaults are `Playing {playlist}` and `Playing {song}{by_artist} next`. Custom templates support playlist/song/artist/author/album/title/action/position/queue_size/shuffle/app and optional by_artist/from_album phrases. Missing metadata is empty.

The owner confirmed the0.19 integrated native voice working. This release retains that functional path while removing observation/reporting code. Google's visible/cloud text remains unchanged. Cloud/native synthesis latency and failures are outside the module's control; no Android TTS fallback or fabricated success is used.

## Other settings

Allow normal screen timeout is an optional, independent window-policy feature. It does not add commands, change wake words or force the phone to sleep. Lockscreen and future-update behavior remain subject to Android, Google and compatibility guards.
