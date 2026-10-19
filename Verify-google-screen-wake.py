#!/usr/bin/env python3
"""Confirm screen-wake mechanism and stable Gemini components in the supplied17.68 APK."""
import sys,zipfile
from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX
apk=APK(sys.argv[1]);assert apk.get_package()=='com.google.android.googlequicksearchbox'
assert apk.get_androidversion_name()=='17.68.17.ve.arm64' and apk.get_androidversion_code()=='301824130'
for n in ['com.google.android.apps.search.assistant.surfaces.voice.robin.main.MainActivity','com.google.android.apps.search.assistant.surfaces.voice.robin.ui.floaty.activity.FloatyActivity']:assert n in apk.get_activities()
with zipfile.ZipFile(sys.argv[1]) as z:
 classes={c.get_name():c for c in DEX(z.read('classes12.dex')).get_classes()}
 def body(cls,name):return '\n'.join(i.get_name()+' '+i.get_output() for m in classes[cls].get_methods() if m.get_name()==name for i in m.get_instructions())
 b=body('Lbhya;','a');assert '128' in b and 'Landroid/view/Window;->addFlags(I)V' in b and 'Landroid/view/Window;->clearFlags(I)V' in b
 b=body('Laorl;','a');assert 'Lbhya;->a(Landroid/view/Window; Z)V' in b
 b=body('Laorj;','close');assert 'Ma-Robin-ScreenWakeLock' in b and 'Lbhya;->a(Landroid/view/Window; Z)V' in b
print('Google17.68 Gemini reference-counted screen-wake controller, keep-on bit128 and target activities verified in actual APK; not proof of current device flag state')
