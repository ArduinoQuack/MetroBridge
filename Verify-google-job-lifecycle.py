#!/usr/bin/env python3
"""Actual pinned APK check for the coroutine job callback used to await native speech end."""
import sys,zipfile
from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX
apk=APK(sys.argv[1]);version=apk.get_androidversion_name()
assert apk.get_package()=='com.google.android.googlequicksearchbox'
assert version in ['17.67.17.ve.arm64','17.68.17.ve.arm64']
modern=version.startswith('17.68.');job,handle,engine,task=('hekv','hekc','awkx','awki') if modern else ('hefq','heex','awhe','avzz')
with zipfile.ZipFile(sys.argv[1]) as z:
 d=DEX(z.read('classes2.dex'));c=next(c for c in d.get_classes() if c.get_name()=='L'+job+';')
 m=next(m for m in c.get_methods() if m.get_name()=='k')
 assert m.get_descriptor().replace(' ','')=='(Lkotlin/jvm/functions/Function1;)L'+handle+';'
 d=DEX(z.read('classes.dex'));c=next(c for c in d.get_classes() if c.get_name()=='L'+engine+';')
 name='F' if modern else 'D';m=next(m for m in c.get_methods() if m.get_name()==name)
 body='\n'.join(i.get_output() for i in m.get_instructions())
 assert 'L'+job+';->n(Ljava/util/concurrent/CancellationException;)V' in body
 assert '->y ' in body
print(version+': actual job completion callback schema and replacement-cancels-previous-job behavior verified')
