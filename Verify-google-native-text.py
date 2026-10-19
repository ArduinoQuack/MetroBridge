#!/usr/bin/env python3
"""Validate exact native plain-text entry and job call in the supplied pinned APK."""
import sys,zipfile
from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX
apk=APK(sys.argv[1]);assert apk.get_package()=='com.google.android.googlequicksearchbox'
version=apk.get_androidversion_name();assert version in ['17.67.17.ve.arm64','17.68.17.ve.arm64']
engine,text,ident,mode,job=('awhe','apsn','awhk','awjx','D') if version.startswith('17.67.') else ('awkx','aptl','awld','awnq','F')
desc='(L'+text+';L'+ident+';L'+mode+';Lcom/google/android/apps/search/assistant/surfaces/voice/robin/speech/AudioSessionConfig;)V'
with zipfile.ZipFile(sys.argv[1]) as z:
 c=next(c for c in DEX(z.read('classes.dex')).get_classes() if c.get_name()=='L'+engine+';')
 matches=[m for m in c.get_methods() if m.get_descriptor().replace(' ','')==desc]
 assert len(matches)==1
 body='\n'.join(i.get_output() for i in matches[0].get_instructions())
 for ref in ['L'+engine+';->'+job+'(L'+text+';', 'L'+ident+';->b Ljava/lang/String;','L'+engine+';->A ']:assert ref in body,ref
 print(version+': unique native plain-text entry '+engine+'.'+matches[0].get_name()+' and native job prologue verified')
