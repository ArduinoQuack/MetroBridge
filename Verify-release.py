#!/usr/bin/env python3
"""Developer-only structural checks for the clean APK; not a phone/UI test."""
import sys,zipfile
from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX
apk=APK(sys.argv[1]); ns='{http://schemas.android.com/apk/res/android}'
assert apk.get_package()=='dev.metrobridge.poc'
assert apk.get_androidversion_name()=='0.21' and apk.get_androidversion_code()=='21'
assert not apk.get_permissions()
root=apk.get_android_manifest_xml();app=root.find('application')
assert app.get(ns+'debuggable')=='false' and app.get(ns+'allowBackup')=='false'
assert not root.findall('.//category[@'+ns+'name="android.intent.category.LAUNCHER"]')
settings=next(a for a in app.findall('activity') if a.get(ns+'name')=='.MainActivity')
assert settings.get(ns+'exported')=='true' and settings.get(ns+'excludeFromRecents')=='true'
assert any(c.get(ns+'name')=='de.robv.android.xposed.category.MODULE_SETTINGS' for c in settings.findall('./intent-filter/category'))
assert not app.findall('service')
assert len(app.findall('activity'))==1
assert not any(x.get(ns+'name')=='android.intent.action.TTS_SERVICE' for x in root.findall('./queries/intent/action'))
with zipfile.ZipFile(sys.argv[1]) as z:
 assert z.read('assets/xposed_init').decode().strip()=='dev.metrobridge.poc.HookEntry'
 dex=DEX(z.read('classes.dex'));classes={c.get_name():c for c in dex.get_classes()}
 assert not any(c.startswith(('Lde/robv/android/xposed/','Landroid/')) for c in classes)
 main=classes['Ldev/metrobridge/poc/MainActivity;']
 assert not {'export','sendSearch','sendTransport','playPlaylist','showStatus','state'}&{m.get_name() for m in main.get_methods()}
 assert 'Ldev/metrobridge/poc/MainActivity$Loaded;' not in classes
 assert not any('Test;' in c for c in classes)
 assert not any(n in classes for n in ['Ldev/metrobridge/poc/SupportTrace;','Ldev/metrobridge/poc/SupportAccess;','Ldev/metrobridge/poc/ReportExport;','Ldev/metrobridge/poc/QueueActionActivity;','Ldev/metrobridge/poc/VoiceActionActivity;'])
 for c in classes.values():
  for m in c.get_methods():
   for i in m.get_instructions():
    out=i.get_output()
    assert not any(t in out for t in ['Landroid/app/AlertDialog', 'Landroid/app/Dialog;', 'Landroid/widget/Toast;', 'Landroid/widget/PopupWindow;', 'Landroid/speech/tts/TextToSpeech;', 'Landroid/media/AudioRecord;', 'Landroid/media/AudioTrack;->play(', 'Landroid/provider/MediaStore', '->setVolume(', '->setStreamVolume(', '->setAudioAttributes(']),(c.get_name(),m.get_name(),out)
    assert not ('Landroid/util/Log;->' in out or 'Lde/robv/android/xposed/XposedBridge;->log(' in out),(c.get_name(),m.get_name())
 strings=dex.get_strings()
 for old in ['export_support_report','start_support_capture','support-report','support-start','support-event','Test Play next — keep current queue','Send raw search request (diagnostic)','Export diagnostic text to Downloads','Show integration status','MediaStore$Downloads']:
  assert old not in strings,old
 assert 'refreshLibrary' in [m.get_name() for m in main.get_methods()]
 cleanup=next(m for m in classes['Ldev/metrobridge/poc/BridgeStore;'].get_methods() if m.get_name()=='initializeSettings')
 assert any('->remove(' in i.get_output() for i in cleanup.get_instructions())
 assert not any('->clear(' in i.get_output() for i in cleanup.get_instructions())
for name in ['PlaylistOperation','QueueOperation']:
 c=classes['Ldev/metrobridge/poc/'+name+';'];assert c.get_superclassname()=='Landroid/content/ContextWrapper;'
 for m in c.get_methods():
  assert all('->startActivity(' not in i.get_output() for i in m.get_instructions())
start=next(m for m in classes['Ldev/metrobridge/poc/HookEntry;'].get_methods() if m.get_name()=='openChoice')
assert all('->startActivity(' not in i.get_output() for i in start.get_instructions())
assert any('execute-ticket' in i.get_output() for i in start.get_instructions())
assert 'Ldev/metrobridge/poc/GoogleForeground;' in classes
screen=[c for n,c in classes.items() if n.startswith('Ldev/metrobridge/poc/ScreenTimeoutGuard')]
assert screen
for c in screen:
 for m in c.get_methods():
  for i in m.get_instructions():
   assert not any(x in i.get_output() for x in ['Landroid/os/PowerManager;', 'Landroid/app/admin/DevicePolicyManager;', '->setKeepAwake(', '->setKeepScreenOn(', '->goToSleep(', 'screen_off_timeout', '->startActivity(']), i.get_output()
assert any('->copyFrom(' in i.get_output() for c in screen for m in c.get_methods() for i in m.get_instructions())
assert 'Ldev/metrobridge/poc/GoogleReplyVoice;' in classes
assert 'Ldev/metrobridge/poc/ConfirmationGate;' in classes
print('Native Google voice integrated; no Android TTS, microphone or volume-setting calls. Popup-free clean release verified: no launcher/permissions/test UI/platform-log calls; no report/capture endpoints or audio-observation hooks; settings accessible')
