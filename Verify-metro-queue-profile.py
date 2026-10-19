#!/usr/bin/env python3
"""Static checks of the supplied MetroList APK; not an on-device playback test."""
import sys, zipfile
from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX
apk=APK(sys.argv[1])
assert apk.get_package()=="com.google.android.apps.youtube.music"
assert apk.get_androidversion_name()=="13.6.1"
names={
 "Lcom/metrolist/music/playback/MusicService;",
 "Lcom/metrolist/music/db/MusicDatabase;",
 "Lcoil3/size/SizeKt;", "Lkotlinx/coroutines/flow/StateFlowKt;",
 "Lkotlinx/coroutines/JobImpl;", "Lkotlin/ResultKt;",
 "Landroidx/media3/session/MediaLibrarySessionImpl;",
 "Landroidx/media3/session/MediaSession$ControllerInfo;",
 "Landroidx/media3/session/MediaLibrarySessionImpl$$ExternalSyntheticLambda0;",
}
classes={}
with zipfile.ZipFile(sys.argv[1]) as z:
 for name in z.namelist():
  if name.startswith("classes") and name.endswith(".dex"):
   for c in DEX(z.read(name)).get_classes():
    if c.get_name() in names: classes[c.get_name()]=c
assert classes.keys()==names, names-classes.keys()
checks=0
def method(cls,name,desc):
 global checks
 c=classes[cls]
 assert any(m.get_name()==name and m.get_descriptor().replace(" ","")==desc for m in c.get_methods()),(cls,name,desc)
 checks+=1
def fields(cls,names):
 global checks
 actual={f.get_name() for f in classes[cls].get_fields()}
 assert set(names)<=actual,(cls,set(names)-actual)
 checks+=len(names)
s="Lcom/metrolist/music/playback/MusicService;"
method(s,"playNext","(Ljava/util/List;)V")
method(s,"getDatabase","()Lcom/metrolist/music/db/MusicDatabase;")
fields(s,["player","mediaSession","castConnectionHandler","listenTogetherManager"])
method("Lcom/metrolist/music/db/MusicDatabase;","song","(Ljava/lang/String;)Lkotlinx/coroutines/flow/Flow;")
method("Lcoil3/size/SizeKt;","toMediaItem","(Lcom/metrolist/music/db/entities/Song;)Landroidx/media3/common/MediaItem;")
method("Lkotlinx/coroutines/flow/StateFlowKt;","first","(Lkotlinx/coroutines/flow/Flow;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;")
method("Lkotlinx/coroutines/JobImpl;","<init>","(Lkotlinx/coroutines/Job;)V")
method("Lkotlin/ResultKt;","throwOnFailure","(Ljava/lang/Object;)V")
method("Landroidx/media3/session/MediaLibrarySessionImpl;","onGetSearchResultOnHandler","(Landroidx/media3/session/MediaSession$ControllerInfo;Ljava/lang/String;ILandroidx/media3/session/MediaLibraryService$LibraryParams;)Lkotlinx/coroutines/guava/JobListenableFuture;")
fields("Landroidx/media3/session/MediaLibrarySessionImpl;",["browserServiceLegacyStub","sessionLegacyStub","sessionStub"])
fields("Landroidx/media3/session/MediaSession$ControllerInfo;",["remoteUserInfo"])
handler=next(m for m in classes["Landroidx/media3/session/MediaLibrarySessionImpl;"].get_methods() if m.get_name()=="onGetSearchResultOnHandler")
assert any("MediaLibrarySessionImpl$$ExternalSyntheticLambda0;-><init>" in i.get_output() for i in handler.get_instructions())
validator=next(m for m in classes["Landroidx/media3/session/MediaLibrarySessionImpl;"].get_methods() if m.get_name()=="verifyResultItems")
instructions=list(validator.get_instructions())
assert any(i.get_name().startswith("if-") for i in instructions)
assert any("Invalid size=" in i.get_output() for i in instructions)
assert any("pageSize=" in i.get_output() for i in instructions)
listener=classes["Landroidx/media3/session/MediaLibrarySessionImpl$$ExternalSyntheticLambda0;"]
run=next(m for m in listener.get_methods() if m.get_name()=="run")
assert any("->verifyResultItems(" in i.get_output() for i in run.get_instructions())
checks+=3
print(f"{checks} MetroList native-queue APK profile checks passed")
