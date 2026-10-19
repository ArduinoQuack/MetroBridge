# Prepare MetroList for Gemini with Morphe

This is the package-preparation step used before MetroBridge. The original guide is [u/androidforthewin's Reddit post](https://www.reddit.com/r/metrolist/comments/1tnnm1c/guide_how_to_get_gemini_to_play_songs_on_metrolist/). Instructions here use Morphe Manager throughout.

If your current renamed MetroList already works, keep it and continue with [module installation](../README.md#2-install-metrobridge).

## Downloads

- [Morphe Manager official site](https://morphe.software/) / [GitHub releases](https://github.com/MorpheApp/morphe-manager/releases/latest)
- [MetroList 13.6.1](https://github.com/MetrolistGroup/Metrolist/releases/tag/v13.6.1), the version profiled by MetroBridge 0.21
- [MetroList project and release archive](https://github.com/MetrolistGroup/Metrolist/releases)
- [Official Morphe Expert mode walkthrough](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patching-expert-mode.md)
- [Official Morphe backup/keystore guide](https://github.com/MorpheApp/morphe-manager/blob/main/docs/backup-and-keystore.md)

Use your own original APK and patch locally. This repository does not redistribute Google or patched MetroList APKs.

## Before installing a renamed copy

Export your MetroList settings/library using its backup feature. Preserve your Morphe signing keystore as well: that key determines whether future patched APKs can update the installed one.

Official YouTube Music and renamed MetroList use the same package identifier and cannot coexist as separate apps under it. A conflicting installation may require removal after backup. Disabling a preinstalled system app or uninstalling it only for the current user does not necessarily remove its package/signature reservation. This guide does not provide partition/firmware changes to solve a system-package conflict.

The original MetroList package is different, so renaming creates a distinct installation. Restoring a backup is preferable to assuming app-private data transfers automatically. Do not uninstall a working current setup merely to repeat these instructions.

## Patch

1. Open Morphe Manager and enable **Settings → Advanced → Expert settings → Expert mode**. Some older versions show Expert mode directly in Settings.
2. Select the MetroList APK with **Other apps** or the APK file picker; the exact entry label varies by Manager version.
3. Open the universal patch list and select **Clone app**. Older Morphe builds label this **Change package name**.
4. Open its settings icon and enter this package name:

   ```text
   com.google.android.apps.youtube.music
   ```

5. Include **Disable Play Store updates**. Its purpose is to prevent Play Store updates replacing the renamed app. It raises the version code, so do not use the resulting number as the original MetroList version.
6. Keep the selection limited to these preparation patches. Leave YouTube-only patches, certificate-pinning overrides and GmsCore support out of this MetroList flow.
7. Patch, then install with the normal Android installer. Morphe's root-mount installer is not required for this guide.
8. Open MetroList, restore your exported data as needed, and confirm playback before installing/enabling MetroBridge.

The current names and options are in [Morphe's universal patch list](https://github.com/MorpheApp/morphe-patches#-patches-list). These UI labels can change; the required output is the exact package name above.

## Connect Gemini and route links

In Android App info for the renamed MetroList, open **Open by default** or **Set as default**, enable supported links and select `music.youtube.com`.

In Gemini, connect **YouTube Music** under **Connected Apps**, using your own account. Google's [official connection instructions](https://support.google.com/gemini/answer/15237118?co=GENIE.Platform%3DAndroid&hl=en) describe account/activity requirements and regional availability. Google currently requires Keep Activity for this connection and distinguishes official-app background behavior by membership. Those are Google requirements; this module does not rewrite server-side eligibility.

## Signing profile

A successful package rename does not automatically make the APK trusted by MetroBridge. The prebuilt release checks the inspected MetroList version and certificate fingerprint listed in [Compatibility](../README.md#compatibility).

If your Morphe keystore produces a different certificate, [build MetroBridge with that explicit public fingerprint](BUILDING.md#use-your-own-morphe-signing-certificate). Keep both your MetroList and MetroBridge signing keys for future upgrades. Do not publish private keystores in issues or source repositories.

## Updates

Reapply the same Morphe package rename to a replacement MetroList APK and sign with the same Morphe key. An unpatched upstream APK has the original package name and is not an update to the renamed installation. MetroBridge itself remains a separate APK, but a new MetroList/Google version can require a reviewed hook profile.

Then follow [MetroBridge installation](../README.md#2-install-metrobridge).
