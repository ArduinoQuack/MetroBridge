#!/usr/bin/env python3
"""Verify private mapping and native call flow against the user-supplied Google 17.68 APK.
No Google bytes are bundled. Static checks do not prove audible behavior.
"""
import sys,zipfile,hashlib,json
from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX
apk=APK(sys.argv[1]);assert apk.get_package()=='com.google.android.googlequicksearchbox'
assert (apk.get_androidversion_name(),apk.get_androidversion_code())==('17.68.17.ve.arm64','301824130')
checks=0
def check(ok,why):
 global checks
 assert ok,why
 checks+=1
with zipfile.ZipFile(sys.argv[1]) as z:
 classes={}
 for n in ['classes.dex','classes12.dex','classes2.dex']:
  classes.update((c.get_name(),c) for c in DEX(z.read(n)).get_classes())
 def field(c,n,t):check(any(f.get_name()==n and f.get_descriptor()==t for f in classes[c].get_fields()),(c,n,t))
 def method(c,n,desc):
  out=[m for m in classes[c].get_methods() if m.get_name()==n and m.get_descriptor().replace(' ','')==desc]
  check(len(out)==1,(c,n,desc));return out[0]
 def body(m):return '\n'.join(i.get_output() for i in m.get_instructions())
 def refs(m,expected):
  s=body(m)
  for x in expected:check(x in s,x)
 cfg='Lcom/google/android/apps/search/assistant/surfaces/voice/robin/speech/AudioSessionConfig;'
 for c,fields in {'Laptl;':{'a':'Ljava/lang/String;','b':'Lgnwx;'},'Lawld;':{'a':'Ljava/lang/String;','b':'Ljava/lang/String;'},'Lawms;':{'a':'Laptl;','b':'Ljava/lang/String;','c':'Lavvd;'},'Lawmr;':{'c':'Lawmu;','d':'Lawld;'},'Lawkx;':{'A':'Lheib;','y':'Lheib;','c':'Lhejk;','t':'Lavvj;'}}.items():
  for n,t in fields.items():field(c,n,t)
 method('Laptl;','<init>','(Ljava/lang/String;Lgnwx;)V')
 method('Lawms;','<init>','(Laptl;Ljava/lang/String;Lavvd;)V')
 method('Lheib;','a','(Ljava/lang/Object;)Ljava/lang/Object;')
 method('Lasxq;','f','(Lawnq;Ljava/lang/String;)V')
 method('Lawkx;','a','()Lasxq;')
 mode=method('Lawnq;','<clinit>','()V');refs(mode,['AUTO_PLAY','STREAMING_AUTO_PLAY','CONVERSATION_MODE','VOICE_SELECTION'])
 for c,anchor in [('Laptl;','TtsContent(textToSpeak='),('Lawld;','TtsMessageId(id='),('Lawms;','TtsContentSource(content=')]:refs(method(c,'toString','()Ljava/lang/String;'),[anchor])
 stream=method('Lawkx;','t','(Lawky;Lawld;Lawnq;'+cfg+')V')
 refs(stream,['Lawld;->b Ljava/lang/String;','Lasxq;->f(Lawnq; Ljava/lang/String;)','Lawkx;->A Lheib;','Lheib;->a(Ljava/lang/Object;)','Lawkx;->E(Lawnq;)','Lawkp;-><init>','Lawkx;->y Lheib;'])
 focus=method('Lawkx;','E','(Lawnq;)Z');check('static' in focus.get_access_flags_string(),'focus static')
 job=method('Lawkx;','F','(Laptl;Lawld;Lawnq;'+cfg+'Z)Lhekv;')
 refs(job,['Lawki;-><init>','Lhejl;->K(','Lawkx;->y Lheib;','Lhekv;->n('])
 refs(method('Lawki;','invokeSuspend','(Ljava/lang/Object;)Ljava/lang/Object;'),['Lawkx;->y(Laptl; Lawnq; Lawld;'])
 refs(method('Lawkx;','y','(Laptl;Lawnq;Lawld;ZLawok;'+cfg+'Lhdze;)Ljava/lang/Object;'),['Lawkv;-><init>'])
 refs(method('Lawkv;','invokeSuspend','(Ljava/lang/Object;)Ljava/lang/Object;'),['Lawkx;->x(Laptl; Lawnq;'])
 refs(method('Lawkx;','x','(Laptl;Lawnq;'+cfg+'Lawku;Lhdze;)Ljava/lang/Object;'),['Laptl;->a Ljava/lang/String;','Laptl;->b Lgnwx;','Lawkm;-><init>'])
 refs(method('Lawkm;','invokeSuspend','(Ljava/lang/Object;)Ljava/lang/Object;'),['Lawkx;->h(Ljava/lang/String; Ljava/lang/String; Lhdze;)'])
 refs(method('Lawkx;','h','(Ljava/lang/String;Ljava/lang/String;Lhdze;)Ljava/lang/Object;'),['Lavvj;->a(Lhdze;)','Lavve;->a Ljava/lang/String;','Lgype;->b(Ljava/lang/String;)','Lgype;->d(Ljava/lang/String;)','Lgype;->e(Ljava/lang/String;)'])
 method('Lawkx;','s','(Laptl;Lawld;'+cfg+'Lhdze;)Ljava/lang/Object;')
 refs(method('Lawmr;','g','(Lawmu;Lawnq;Lgzmw;Lhdze;)Ljava/lang/Object;'),['Lawms;','Laptl;->a Ljava/lang/String;','Lawms;->b Ljava/lang/String;','Lawms;->c Lavvd;'])
 method('Lawmr;','h','(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lavvd;Lhdze;)Ljava/lang/Object;')
print(json.dumps({'google':'17.68.17.ve.arm64','checks':checks,'result':'static mapping/native job, synthesis and stored voice lookup verified'}))
