# Changelog

## 0.33

- Fix Google update compatibility so MetroBridge catches music requests again.
- Fix input-hook discovery choosing the wrong coroutine.
- Discard stale compatibility caches and keep unrelated hook failures from blocking startup.
- Use runtime discovery instead of a Google-version allowlist. Google updates can still break it.

## 0.21

- Remove temporary phase capture/export, shell support endpoints and audio observation hooks.
- Remove legacy action activities, trace logging wrappers and diagnostic payloads.
- Clear old private diagnostic preferences once on upgrade; preserve settings, library and templates.
- Retain native music/voice behavior and the optional screen-timeout setting.
- Refresh build, compatibility, testing and rollback documentation.

## 0.20

- Add an opt-in setting allowing Gemini activity windows to follow the normal screen timeout.

## 0.19

- Execute music actions without opening a companion window over Gemini.
- Protect the native confirmation from duplicate same-reply jobs.
- Coordinate MetroList foregrounding with native speech completion.
