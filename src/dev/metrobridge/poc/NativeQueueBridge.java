package dev.metrobridge.poc;

import android.app.Service;
import android.content.*;
import android.os.*;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Optional MetroList-only hook. Uses the app's own search, metadata conversion and playNext. */
public final class NativeQueueBridge {
 public static final String ACTION="dev.metrobridge.poc.NATIVE_QUEUE";
 private static final String COMP="dev.metrobridge.poc",SIGNER="3cedb394a29f13f38bd61b288d16bafdc1be55193e8257ed95acb5e66e6aa9e0";
 private static final Handler MAIN=new Handler(Looper.getMainLooper());
 private static final Map<Service,BroadcastReceiver> RECEIVERS=new WeakHashMap<>();
 private static final Map<String,Bundle> RECEIPTS=new LinkedHashMap<>();
 private static ClassLoader loader;
 public static void install(XC_LoadPackage.LoadPackageParam p){
  loader=p.classLoader;
  try{Class<?> service=XposedHelpers.findClass("com.metrolist.music.playback.MusicService",loader);
   XposedHelpers.findAndHookMethod(service,"onCreate",new XC_MethodHook(){@Override protected void afterHookedMethod(MethodHookParam p){register((Service)p.thisObject);}});
   XposedHelpers.findAndHookMethod(service,"onDestroy",new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){Service s=(Service)p.thisObject;BroadcastReceiver r=RECEIVERS.remove(s);if(r!=null)try{s.unregisterReceiver(r);}catch(Exception ignored){}}});
  }catch(Throwable e){}
 }
 private static void register(Service service){
  if(Build.VERSION.SDK_INT<34||!BridgeStore.compatibleMetro(service)||RECEIVERS.containsKey(service))return;
  try{
   service.getClass().getDeclaredMethod("playNext",List.class);
   XposedHelpers.findClass("coil3.size.SizeKt",loader).getDeclaredMethod("toMediaItem",XposedHelpers.findClass("com.metrolist.music.db.entities.Song",loader));
   XposedHelpers.findClass("kotlinx.coroutines.flow.StateFlowKt",loader).getDeclaredMethod("first",XposedHelpers.findClass("kotlinx.coroutines.flow.Flow",loader),XposedHelpers.findClass("kotlin.coroutines.Continuation",loader));
   XposedHelpers.findClass("kotlinx.coroutines.JobImpl",loader).getDeclaredConstructor(XposedHelpers.findClass("kotlinx.coroutines.Job",loader));
   Class<?> impl=XposedHelpers.findClass("androidx.media3.session.MediaLibrarySessionImpl",loader);
   impl.getDeclaredMethod("onGetSearchResultOnHandler",XposedHelpers.findClass("androidx.media3.session.MediaSession$ControllerInfo",loader),String.class,int.class,XposedHelpers.findClass("androidx.media3.session.MediaLibraryService$LibraryParams",loader));
   for(String name:new String[]{"player","mediaSession","castConnectionHandler","listenTogetherManager"})service.getClass().getDeclaredField(name);
   for(String name:new String[]{"browserServiceLegacyStub","sessionLegacyStub","sessionStub"})impl.getDeclaredField(name);
   BroadcastReceiver receiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent intent){
    int uid=getSentFromUid();if(!trusted(service,uid))return;
    ResultReceiver reply=intent.getParcelableExtra("reply",ResultReceiver.class);if(reply==null)return;
    String request=intent.getStringExtra("request"),lease=intent.getStringExtra("lease");
    if(request==null||!request.matches("[A-Za-z0-9-]{1,80}")||lease==null||!permit(service,lease)){send(reply,request,error("Queue authorization expired or routing disabled."));return;}
    try{
     String op=intent.getStringExtra("operation");
     if("snapshot".equals(op)){Bundle result=describe(snapshot(service));result.putBoolean("ok",true);send(reply,request,result);}
     else if("search".equals(op))search(service,uid,intent,reply,request,lease);
     else if("next".equals(op))next(service,intent,reply,request,lease);
     else send(reply,request,error("Unsupported queue operation."));
    }catch(QueueFailure e){send(reply,request,error(e.getMessage()+". Queue unchanged."));}catch(Throwable e){send(reply,request,error("Queue bridge: "+e.getClass().getSimpleName()+". No automatic retry."));}
   }};
   service.registerReceiver(receiver,new IntentFilter(ACTION),Context.RECEIVER_EXPORTED);RECEIVERS.put(service,receiver);
  }catch(Throwable e){}
 }
 private static boolean trusted(Service s,int uid){try{return uid>=0&&s.getPackageManager().getApplicationInfo(COMP,0).uid==uid&&BridgeStore.signedBy(s,COMP,SIGNER);}catch(Exception e){return false;}}
 private static boolean permit(Service s,String lease){try{Bundle b=new Bundle();b.putString("lease",lease);Bundle result=s.getContentResolver().call(BridgeStore.URI,"queue-permit",null,b);return result!=null&&result.getBoolean("allow",false);}catch(Throwable e){return false;}}
 private static Object field(Object target,String name){return XposedHelpers.getObjectField(target,name);}
 private static Object call(Object target,String name,Object... args){return XposedHelpers.callMethod(target,name,args);}
 private static Object player(Service s){Object p=field(s,"player");if(p==null||call(p,"getApplicationLooper")!=Looper.myLooper())throw new QueueFailure("Player thread unavailable");
  Object cast=field(s,"castConnectionHandler");if(cast!=null&&Boolean.TRUE.equals(call(field(cast,"isCasting"),"getValue")))throw new QueueFailure("Cast queue unsupported");
  Object room=field(s,"listenTogetherManager");if(room!=null&&Boolean.TRUE.equals(call(field(room,"client"),"isInRoom")))throw new QueueFailure("Listen Together queue unsupported");return p;}
 private static String id(Object item){return item==null?"":(String)field(item,"mediaId");}
 private static QueueSnapshot snapshot(Service s){
  Object p=player(s);int size=(Integer)call(p,"getMediaItemCount"),state=(Integer)call(p,"getPlaybackState"),index=(Integer)call(p,"getCurrentMediaItemIndex");
  if(size<1||size>10000||state==1)throw new QueueFailure("Start a playlist before inserting next");
  List<String> items=new ArrayList<>(),order=new ArrayList<>();for(int i=0;i<size;i++)items.add(id(call(p,"getMediaItemAt",i)));
  boolean shuffle=(Boolean)call(p,"getShuffleModeEnabled");Object timeline=call(p,"getCurrentTimeline");int next=(Integer)call(timeline,"getFirstWindowIndex",shuffle);Set<Integer> seen=new HashSet<>();
  while(next>=0&&next<size&&seen.add(next)){order.add(items.get(next));next=(Integer)call(timeline,"getNextWindowIndex",next,0,shuffle);}
  if(order.size()!=size)throw new QueueFailure("Queue traversal unavailable");int nextIndex=(Integer)call(p,"getNextMediaItemIndex");
  return new QueueSnapshot(items,order,id(call(p,"getCurrentMediaItem")),nextIndex>=0&&nextIndex<size?items.get(nextIndex):"",index,shuffle,(Integer)call(p,"getRepeatMode"),(Boolean)call(p,"getPlayWhenReady"),(Long)call(p,"getCurrentPosition"));
 }
 private static Bundle describe(QueueSnapshot q){Bundle b=new Bundle();b.putInt("queueProtocol",2);b.putString("nativeVersion","0.11");b.putString("fingerprint",q.fingerprint());b.putString("current",q.current);b.putString("next",q.next);b.putInt("count",q.items.size());b.putBoolean("shuffle",q.shuffle);b.putInt("repeat",q.repeat);b.putBoolean("playing",q.playing);return b;}
 private static Object controller(Object impl,int uid){
  for(String name:new String[]{"browserServiceLegacyStub","sessionLegacyStub","sessionStub"}){
   Object stub=field(impl,name);if(stub==null)continue;Object manager=field(stub,"connectedControllersManager");Object all=call(manager,"getConnectedControllers");
   if(all instanceof Iterable)for(Object info:(Iterable<?>)all){Object remote=field(field(info,"remoteUserInfo"),"impl");if(XposedHelpers.getIntField(remote,"uid")==uid&&COMP.equals(field(remote,"packageName")))return info;}
  }throw new QueueFailure("Companion media browser is not connected");
 }
 private static void search(Service s,int uid,Intent intent,ResultReceiver reply,String request,String lease){
  String query=intent.getStringExtra("query");if(query==null||query.trim().isEmpty()||query.length()>300)throw new IllegalArgumentException("Invalid song search");
  Object impl=field(field(s,"mediaSession"),"impl"),info=controller(impl,uid);
  Object future=NativeSearchCall.request(impl,info,query,NativeQueueBridge::call);AtomicBoolean done=new AtomicBoolean();
  Runnable timeout=()->{if(done.compareAndSet(false,true)){((Future<?>)future).cancel(true);send(reply,request,error("Song search timed out. Queue unchanged."));}};MAIN.postDelayed(timeout,20000);
  call(future,"addListener",(Runnable)()->{
   if(!done.compareAndSet(false,true))return;MAIN.removeCallbacks(timeout);
   try{if(!permit(s,lease)){send(reply,request,error("Request canceled or superseded."));return;}
    Object result=((Future<?>)future).get();if(XposedHelpers.getIntField(result,"resultCode")!=0)throw new QueueFailure("Search unavailable");
    Object value=field(result,"value");ArrayList<Bundle> songs=new ArrayList<>();Set<String> ids=new HashSet<>();
    if(value instanceof Iterable)for(Object item:(Iterable<?>)value){String raw=id(item),song=raw.substring(raw.lastIndexOf('/')+1);if(!song.matches("[A-Za-z0-9_-]{1,160}")||!ids.add(song))continue;
     Object metadata=field(item,"mediaMetadata");Bundle b=new Bundle();b.putString("id",song);b.putString("title",text(field(metadata,"title")));b.putString("artist",text(field(metadata,"artist")));try{b.putString("album",text(field(metadata,"albumTitle")));}catch(Throwable optionalAlbumUnavailable){}songs.add(b);if(songs.size()>=50)break;}
    Bundle out=new Bundle();out.putBoolean("ok",true);out.putParcelableArrayList("songs",songs);send(reply,request,out);
   }catch(Throwable e){send(reply,request,error("Song search failed: "+e.getClass().getSimpleName()+". Queue unchanged."));}
  },(Executor)r->MAIN.post(r));
 }
 private static String text(Object value){String text=value==null?"":value.toString();return text.substring(0,Math.min(text.length(),300));}
 private static void next(Service s,Intent intent,ResultReceiver reply,String request,String lease){
  if(RECEIPTS.containsKey(request)){send(reply,request,new Bundle(RECEIPTS.get(request)));return;}
  String song=intent.getStringExtra("song"),expected=intent.getStringExtra("fingerprint");if(song==null||!song.matches("[A-Za-z0-9_-]{1,160}")||expected==null)throw new IllegalArgumentException("Invalid song request");
  QueueSnapshot before=snapshot(s);if(!expected.equals(before.fingerprint())){send(reply,request,error("Queue or current track changed during lookup. Ask again."));return;}
  Object database=call(s,"getDatabase"),flow=call(database,"song",song);
  Object job=XposedHelpers.newInstance(XposedHelpers.findClass("kotlinx.coroutines.JobImpl",loader),(Object)null);AtomicBoolean done=new AtomicBoolean();
  Runnable timeout=()->{if(done.compareAndSet(false,true)){cancel(job);send(reply,request,error("Song lookup timed out. Queue unchanged."));}};MAIN.postDelayed(timeout,7000);
  java.util.function.Consumer<Object> finish=value->MAIN.post(()->{
   if(!done.compareAndSet(false,true))return;MAIN.removeCallbacks(timeout);cancel(job);
   try{XposedHelpers.callStaticMethod(XposedHelpers.findClass("kotlin.ResultKt",loader),"throwOnFailure",value);
    if(value==null){send(reply,request,error("Song is not present in MetroList after search. Queue unchanged."));return;}
    if(!permit(s,lease)||!expected.equals(snapshot(s).fingerprint())){send(reply,request,error("Request expired or queue changed. No insertion made."));return;}
    Object media=XposedHelpers.callStaticMethod(XposedHelpers.findClass("coil3.size.SizeKt",loader),"toMediaItem",value);
    if(!song.equals(id(media)))throw new QueueFailure("Resolved song ID mismatch");
    QueueSnapshot immediate=snapshot(s);call(s,"playNext",Collections.singletonList(media));QueueSnapshot after=snapshot(s);
    String check=QueueSnapshot.check(immediate,after,song);Bundle out=describe(after);out.putBoolean("ok","preserved".equals(check));out.putString("verification",check);out.putInt("beforeCount",immediate.items.size());out.putString("song",song);
    if(!out.getBoolean("ok"))out.putString("error","Insertion was sent but queue verification reported "+check+". No retry made.");
    if(RECEIPTS.size()>=32)RECEIPTS.remove(RECEIPTS.keySet().iterator().next());RECEIPTS.put(request,new Bundle(out));send(reply,request,out);
   }catch(Throwable e){send(reply,request,error("Native play-next failed: "+e.getClass().getSimpleName()+". No retry made."));}
  });
  Class<?> continuation=XposedHelpers.findClass("kotlin.coroutines.Continuation",loader);
  Object callback=Proxy.newProxyInstance(loader,new Class<?>[]{continuation},(proxy,method,args)->{
   if(method.getName().equals("getContext"))return job;
   if(method.getName().equals("resumeWith")){finish.accept(args[0]);return null;}
   if(method.getName().equals("toString"))return "MetroBridge song lookup";
   if(method.getName().equals("hashCode"))return System.identityHashCode(proxy);
   if(method.getName().equals("equals"))return proxy==args[0];return null;
  });
  try{Object result=XposedHelpers.callStaticMethod(XposedHelpers.findClass("kotlinx.coroutines.flow.StateFlowKt",loader),"first",flow,callback);
   Object suspended=XposedHelpers.getStaticObjectField(XposedHelpers.findClass("kotlin.coroutines.intrinsics.CoroutineSingletons",loader),"COROUTINE_SUSPENDED");if(result!=suspended)finish.accept(result);
  }catch(Throwable e){if(done.compareAndSet(false,true)){MAIN.removeCallbacks(timeout);cancel(job);send(reply,request,error("Song lookup failed: "+e.getClass().getSimpleName()));}}
 }
 private static void cancel(Object job){try{call(job,"cancel",new CancellationException("MetroBridge lookup ended"));}catch(Throwable ignored){}}
 private static Bundle error(String message){Bundle b=new Bundle();b.putBoolean("ok",false);b.putString("error",message);return b;}
 private static void send(ResultReceiver reply,String request,Bundle b){b.putString("request",request);reply.send(b.getBoolean("ok")?0:1,b);}

 private static final class QueueFailure extends RuntimeException {QueueFailure(String message){super(message);}}
 private NativeQueueBridge(){}
}
