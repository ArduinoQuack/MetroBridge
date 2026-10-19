package dev.metrobridge.poc;

import android.app.*;
import android.content.*;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.*;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.ref.WeakReference;
import java.util.*;

public final class HookEntry implements IXposedHookLoadPackage {
 private static final String GOOGLE="com.google.android.googlequicksearchbox", MUSIC="com.google.android.apps.youtube.music";
 private static final Uri CONTROL=Uri.parse("content://dev.metrobridge.poc.control/control");
 private static final Map<Object,WeakReference<MediaController>> OWNERS=Collections.synchronizedMap(new WeakHashMap<>());
 private static final ThreadLocal<Boolean> ROUTING=new ThreadLocal<>();
 private static Context context;private static boolean installed=false,visibilityRetrySent=false;
 private static ClassLoader googleLoader;
 private static final TurnQueryBindings TURN_QUERIES=new TurnQueryBindings();
 private static final LiveTurnGate LIVE_TURNS=new LiveTurnGate();
 @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p){
  if(MUSIC.equals(p.packageName)){NativeQueueBridge.install(p);return;}
  if(!GOOGLE.equals(p.packageName))return;
  googleLoader=p.classLoader;

  try{XposedHelpers.findAndHookMethod(Application.class,"attach",Context.class,new XC_MethodHook(){
   @Override protected void afterHookedMethod(MethodHookParam param){
    initialize((Context)param.args[0]);
   }
  });}catch(Throwable e){}
  try{XposedHelpers.findAndHookMethod(Instrumentation.class,"callApplicationOnCreate",Application.class,new XC_MethodHook(){
   @Override protected void beforeHookedMethod(MethodHookParam param){initialize((Application)param.args[0]);}
  });}catch(Throwable e){}
  try{Object app=XposedHelpers.callStaticMethod(Class.forName("android.app.ActivityThread"),"currentApplication");
   if(app instanceof Application)initialize((Application)app);
  }catch(Throwable e){}
 }
 private static synchronized void initialize(Context base){
  if(installed||base==null)return;
  try{if(!GOOGLE.equals(base.getPackageName()))return;}catch(Throwable e){return;}
  installed=true;context=base.getApplicationContext();if(context==null)context=base;

  try{install();bootstrap();}
  catch(Throwable e){visibilityHandshake(null);}
 }

 private static Bundle call(String method,String arg,Bundle input){
  if(context==null)return null;
  try{Bundle b=context.getContentResolver().call(CONTROL,method,arg,input);visibilityRetrySent=false;return b;}
  catch(Throwable e){if(!visibilityRetrySent){visibilityRetrySent=true;visibilityHandshake(null);}return null;}
 }

 private static void bootstrap(){
  Bundle direct=call("hello",null,null);visibilityHandshake(direct==null?()->call("hello",null,null):null);
 }
 private static void visibilityHandshake(Runnable after){
  if(context==null)return;Intent i=new Intent().setComponent(new ComponentName("dev.metrobridge.poc","dev.metrobridge.poc.BootstrapReceiver"));
  BroadcastReceiver completed=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent ignored){if(after!=null)after.run();}};
  try{if(Build.VERSION.SDK_INT>=34){Bundle options=BroadcastOptions.makeBasic().setShareIdentityEnabled(true).setDeferralPolicy(BroadcastOptions.DEFERRAL_POLICY_NONE).toBundle();context.sendOrderedBroadcast(i,null,options,completed,new Handler(Looper.getMainLooper()),0,null,null);}
   else context.sendOrderedBroadcast(i,null,completed,new Handler(Looper.getMainLooper()),0,null,null);
  }catch(Throwable ignored){}
 }
 private static int hook(Class<?> type,String name,XC_MethodHook callback){try{return XposedBridge.hookAllMethods(type,name,callback).size();}catch(Throwable e){return 0;}}

 private static Bundle request(String kind,String query,Bundle extras,Uri uri){
  MusicLinkFields link=MusicLinkFields.read(uri==null?null:uri.toString());
  if((query==null||query.trim().isEmpty())&&extras!=null)try{query=extras.getString("query");}catch(Throwable ignored){}
  if(query==null||query.trim().isEmpty())query=link.searchQuery;
  Bundle b=new Bundle();b.putBoolean("catalogPlaylist",link.playlistOnly);b.putString("kind",kind);b.putString("query",clip(query));
  if(extras!=null)try{b.putBoolean("startPlayback",extras.getBoolean("android.intent.extra.START_PLAYBACK",false));}catch(Throwable ignored){}
  if(query!=null&&query.length()>300)b.putBoolean("reject",true);
  if(extras!=null){for(String key:new String[]{"android.intent.extra.playlist","android.intent.extra.focus","android.intent.extra.title"}){
   try{String value=extras.getString(key);if(value!=null){if(value.length()>300)b.putBoolean("reject",true);b.putString(key.endsWith("playlist")?"playlist":key.endsWith("focus")?"focus":"title",clip(value));}}catch(Throwable ignored){}
  }}
  b.putString("voiceReply",GoogleReplyVoice.response());
  Bundle decision=call("resolve",null,b);

  return decision;
 }
 private static String clip(String text){if(text==null)return "";return text.length()>300?text.substring(0,300):text;}
 private static MediaController owner(Object transport){
  WeakReference<MediaController> r=OWNERS.get(transport);MediaController existing=r==null?null:r.get();if(existing!=null)return existing;
  // A trivial getter can have been inlined before hooks load. Discover only an enclosing controller field.
  try{Class<?> type=transport.getClass();for(int depth=0;type!=null&&depth<2;depth++,type=type.getSuperclass()){
   java.lang.reflect.Field[] fields=type.getDeclaredFields();if(fields.length>64)return null;
   for(java.lang.reflect.Field field:fields)if(MediaController.class.isAssignableFrom(field.getType())){
    Object value=XposedHelpers.getObjectField(transport,field.getName());if(value instanceof MediaController){MediaController c=(MediaController)value;OWNERS.put(transport,new WeakReference<>(c));return c;}
   }
  }}catch(Throwable ignored){}return null;
 }
 private static boolean music(MediaController c){try{return c!=null&&MUSIC.equals(c.getPackageName());}catch(Throwable ignored){return false;}}
 private static void ack(Bundle decision){Bundle b=new Bundle();b.putString("routeKey",decision.getString("routeKey",""));call("ack",null,b);}
 private static void release(Bundle decision){Bundle b=new Bundle();b.putString("routeKey",decision.getString("routeKey",""));call("release",null,b);}
 private static boolean openChoice(Bundle decision){
  String ticket=decision.getString("ticket");if(ticket==null)return false;
  Bundle b=new Bundle();b.putString("ticket",ticket);Bundle accepted=call("execute-ticket",null,b);return accepted!=null&&accepted.getBoolean("accepted",false);
 }
 private static int installTurnContextAdapter(){
  try{
   android.content.pm.PackageInfo version=context.getPackageManager().getPackageInfo(GOOGLE,0);
   if(!GoogleSchema.configure(context)){return 0;}
   Class<?> executor=GoogleSchema.type("arku",googleLoader),music=GoogleSchema.type("gdka",googleLoader),execution=GoogleSchema.type("artg",googleLoader);
   if(execution.getDeclaredField("i").getType()!=String.class||!execution.getDeclaredField("d").getType().getName().equals(GoogleSchema.name("aofa")))throw new NoSuchFieldException("ExecutionContext schema mismatch");
   Class<?> chat=GoogleSchema.type("aofa",googleLoader),state=GoogleSchema.type("aofk",googleLoader),ongoing=GoogleSchema.type("aoft",googleLoader),complete=GoogleSchema.type("aofe",googleLoader),user=GoogleSchema.type("aofx",googleLoader),id=GoogleSchema.type("appq",googleLoader),input=GoogleSchema.type("apvs",googleLoader);
   if(!chat.getDeclaredField("u").getType().getName().equals(GoogleSchema.name("hent"))||state.getDeclaredField("d").getType()!=ongoing||state.getDeclaredField("c").getType()!=List.class||ongoing.getDeclaredField("a").getType()!=user||complete.getDeclaredField("a").getType()!=user||user.getDeclaredField("a").getType()!=id||user.getDeclaredField("b").getType()!=input||user.getDeclaredField("g").getType()!=boolean.class||input.getDeclaredField("a").getType()!=String.class||input.getDeclaredField(CurrentTurnReader.displayField).getType()!=String.class||id.getMethod("b").getReturnType()!=String.class)throw new NoSuchFieldException("Current user turn schema mismatch");
   Class<?> providerType=GoogleSchema.type("gbqj",googleLoader);
   if(providerType.getDeclaredField("c").getType()!=providerType||providerType.getDeclaredField("r").getType()!=int.class||music.getDeclaredField("d").getType()!=int.class||music.getDeclaredField("e").getType()!=String.class||music.getDeclaredField("f").getType()!=String.class)throw new NoSuchFieldException("Music provider schema mismatch");
   final int youtubeProvider=XposedHelpers.getIntField(XposedHelpers.getStaticObjectField(providerType,"c"),"r");
   // o is the shared dispatcher before the j versus B/l foreground branch.
   java.lang.reflect.Method execute=executor.getDeclaredMethod("o",music,GoogleSchema.type("gbuc",googleLoader),GoogleSchema.type("axno",googleLoader),execution,GoogleSchema.type("hdtz",googleLoader));
   if(execute.getReturnType()!=Object.class)throw new NoSuchMethodException("Music dispatcher return type mismatch");
   XposedBridge.hookMethod(execute,new XC_MethodHook(){
    @Override protected void beforeHookedMethod(MethodHookParam p){
     Object action=p.args[0];if(action==null)return; // Coroutine resumes can supply null original parameters.
     try{
      boolean providerMatch=XposedHelpers.getIntField(action,"d")==youtubeProvider;
      boolean linkMatch=isMusicActionUri((String)XposedHelpers.getObjectField(action,"e"))||isMusicActionUri((String)XposedHelpers.getObjectField(action,"f"));
      boolean target=providerMatch||linkMatch;

      if(!target)return;
      Bundle permit=call("turn-config",null,null);
      if(permit==null||!permit.getBoolean("allow",false)){TURN_QUERIES.bind(action,null,null,SystemClock.elapsedRealtime());return;}
      CurrentTurnReader.Result current=CurrentTurnReader.read(p.args[3]);
      TURN_QUERIES.bind(action,current.turn,current.text,SystemClock.elapsedRealtime());

     }catch(Throwable e){TURN_QUERIES.bind(action,null,null,SystemClock.elapsedRealtime());}
    }

   });return 1;
  }catch(Throwable e){return 0;}
 }
 private static boolean isMusicActionUri(String value){try{String host=value==null?null:Uri.parse(value).getHost();return host!=null&&(MUSIC.equalsIgnoreCase(host)||"music.youtube.com".equalsIgnoreCase(host));}catch(Throwable ignored){return false;}}
 private static final class LiveExecution {
  public final String i;public final Object d;
  LiveExecution(String turn,Object chat){i=turn;d=chat;}
 }
 private static int installLiveQueueAdapter(){
  try{
   android.content.pm.PackageInfo version=context.getPackageManager().getPackageInfo(GOOGLE,0);
   if(!GoogleSchema.configure(context)){return 0;}
   Class<?> frame=GoogleSchema.type("aobd",googleLoader),control=GoogleSchema.type("aobk",googleLoader),chat=GoogleSchema.type("aofa",googleLoader),input=GoogleSchema.type("apvs",googleLoader),token=GoogleSchema.type("appx",googleLoader);
   Class<?> newTurn=GoogleSchema.type("anjl",googleLoader),kind=GoogleSchema.type("anjy",googleLoader),reply=GoogleSchema.type("aofw",googleLoader);
   if(frame.getDeclaredField("l").getType()!=control||frame.getDeclaredField("m").getType()!=input||frame.getDeclaredField("t").getType()!=token||frame.getDeclaredField("k").getType()!=kind||frame.getDeclaredField("u").getType()!=reply||control.getDeclaredField("c").getType()!=chat)throw new NoSuchFieldException("Live input frame schema mismatch");
   Class<?> ids=GoogleSchema.type("aofp",googleLoader),user=GoogleSchema.type("aofx",googleLoader);
   ids.getDeclaredMethod("j",GoogleSchema.type("apso",googleLoader));
   if(user.getDeclaredField("b").getType()!=input||user.getDeclaredField("g").getType()!=boolean.class)throw new NoSuchFieldException("Live user schema mismatch");
   java.lang.reflect.Method invoke=frame.getDeclaredMethod("invokeSuspend",Object.class);
   XposedBridge.hookMethod(invoke,new XC_MethodHook(){
    @Override protected void afterHookedMethod(MethodHookParam p){
     if(p.hasThrowable())return;
     try{
      Object event=p.thisObject;
      // Only the new-turn branch. Regenerations/edits/history hydration are excluded.
      Bundle permit=call("live-queue-config",null,null);if(permit==null||!permit.getBoolean("allow",false))return;
      boolean fresh=GoogleSchema.fresh(XposedHelpers.getObjectField(event,"k"),newTurn),reuse=XposedHelpers.getObjectField(event,"u")!=null;

      if(!fresh||reuse)return;
      Object submitted=XposedHelpers.getObjectField(event,"m"),ctrl=XposedHelpers.getObjectField(event,"l");
      if(submitted==null||ctrl==null)return;
      Object messageId=XposedHelpers.callStaticMethod(ids,"j",XposedHelpers.getObjectField(event,"t"));
      Object value=messageId==null?null:XposedHelpers.callMethod(messageId,"b");
      if(!(value instanceof String))return;String turn=(String)value;
      CurrentTurnReader.Result current=CurrentTurnReader.readOngoing(new LiveExecution(turn,XposedHelpers.getObjectField(ctrl,"c")),submitted);
      if(!"matched-current-user-turn".equals(current.status)){return;}
      QueueRequest parsed=QueueRequest.parse(current.text);if(parsed==null||!parsed.error.isEmpty())return;
      String gate=LIVE_TURNS.accept(turn,current.text);
      if(!"new-turn".equals(gate))return;
      Bundle decision=request("intent-live-user-next",current.text,null,null);
      if(decision!=null&&"queue".equals(decision.getString("action"))){if(!openChoice(decision))release(decision);}
     }catch(Throwable e){}
    }
   });return 1;
  }catch(Throwable e){return 0;}
 }
 private static int installGoogleMusicQueryAdapter(){
  // This optional private adapter is specific to the inspected Google APK. Public hooks remain independent.
  try{
   android.content.pm.PackageInfo version=context.getPackageManager().getPackageInfo(GOOGLE,0);
   if(!GoogleSchema.configure(context)){return 0;}
   Class<?> executor=GoogleSchema.type("arku",googleLoader);
   Class<?> musicAction=GoogleSchema.type("gdka",googleLoader);
   Class<?> adapter=GoogleSchema.type("axno",googleLoader);
   java.lang.reflect.Field queryField=musicAction.getDeclaredField("l");
   if(queryField.getType()!=String.class||java.lang.reflect.Modifier.isStatic(queryField.getModifiers())){return 0;}
   java.lang.reflect.Method build=executor.getDeclaredMethod("d",String.class,musicAction,Bundle.class,adapter);
   if(build.getReturnType()!=Intent.class){return 0;}
   XposedBridge.hookMethod(build,new XC_MethodHook(){
    @Override protected void afterHookedMethod(MethodHookParam p){
     if(p.hasThrowable()||!MUSIC.equals(p.args[0])||!(p.getResult() instanceof Intent))return;
     try{
      Intent original=(Intent)p.getResult();
      if(!MUSIC.equals(original.getPackage())||!Intent.ACTION_VIEW.equals(original.getAction())||!original.getBooleanExtra("android.intent.extra.START_PLAYBACK",false))return;
      String query=TURN_QUERIES.query(p.args[1],SystemClock.elapsedRealtime());
      String querySource=query.isEmpty()?"music-action-search":"matched-current-user-turn";
      if(query.isEmpty())query=(String)XposedHelpers.getObjectField(p.args[1],"l");
      if(query==null)query="";
      String existing=original.getStringExtra("query");boolean hasExisting=existing!=null&&!existing.trim().isEmpty();
      // Google may reduce the search query to just a title, losing the queue verb.
      // Only the exact matched current user turn may restore an explicit next request.
      boolean restoreNext="matched-current-user-turn".equals(querySource)&&QueueRequest.parse(query)!=null;
      Bundle permit=call("music-query",null,null);

      if(permit==null||!permit.getBoolean("allowEnrichment",false)||(hasExisting&&!restoreNext)||query.trim().isEmpty()||query.length()>300)return;
      Intent enriched=new Intent(original);enriched.putExtra("query",query);p.setResult(enriched);

     }catch(Throwable e){}
    }
   });return 1;
  }catch(Throwable e){return 0;}
 }
 private static boolean blockCloudNext(){Bundle b=call("block-cloud-next",null,null);return b!=null&&b.getBoolean("block",false);}
 private static boolean nextKey(Object[] args){for(Object a:args)if(a instanceof android.view.KeyEvent&&((android.view.KeyEvent)a).getKeyCode()==android.view.KeyEvent.KEYCODE_MEDIA_NEXT)return true;return false;}
 private static void install(){
  hook(MediaController.class,"getTransportControls",new XC_MethodHook(){
   @Override protected void afterHookedMethod(MethodHookParam p){if(p.getResult()!=null&&p.thisObject instanceof MediaController)OWNERS.put(p.getResult(),new WeakReference<>((MediaController)p.thisObject));}
  });
  for(String method:new String[]{"playFromSearch","prepareFromSearch","playFromUri","prepareFromUri"}){
   final String name=method;
   hook(MediaController.TransportControls.class,method,new XC_MethodHook(){
    @Override protected void beforeHookedMethod(MethodHookParam p){
     if(Boolean.TRUE.equals(ROUTING.get())||!music(owner(p.thisObject)))return;
     try{
      boolean uri=name.endsWith("Uri"),prepare=name.startsWith("prepare");
      Bundle extras=p.args.length>1&&p.args[1] instanceof Bundle?(Bundle)p.args[1]:null;
      String query=uri?null:(String)p.args[0];
      if(uri&&extras!=null)try{query=extras.getString("query");}catch(Throwable ignored){}
      Bundle decision=request("native-"+name,query,extras,uri&&p.args[0] instanceof Uri?(Uri)p.args[0]:null);if(decision==null)return;
      String action=decision.getString("action","pass");
      if("hold".equals(action)){p.setResult(null);return;}
      if("queue".equals(action)){if(!openChoice(decision))release(decision);p.setResult(null);return;}
      if("duplicate".equals(action)){p.setResult(null);return;}
      if("play".equals(action)&&!prepare&&decision.getString("ticket")!=null){if(!openChoice(decision))release(decision);p.setResult(null);return;}
      if("play".equals(action)){
       String id=decision.getString("mediaId");if(id==null)return;
       ROUTING.set(true);
       try{MediaController.TransportControls t=(MediaController.TransportControls)p.thisObject;
        if(prepare)t.prepareFromMediaId(id,null);else t.playFromMediaId(id,null);
        ack(decision);p.setResult(null);
       }catch(Throwable e){release(decision);throw e;}finally{ROUTING.remove();}
      }else if("choose".equals(action)){
       if(!prepare){if(!openChoice(decision))release(decision);p.setResult(null);}else{release(decision);p.setResult(null);}
      }
     }catch(Throwable e){ROUTING.remove();}
    }
   });
  }
  hook(Instrumentation.class,"execStartActivity",new XC_MethodHook(){
   @Override protected void beforeHookedMethod(MethodHookParam p){
    if(Boolean.TRUE.equals(ROUTING.get()))return;
    for(int n=0;n<p.args.length;n++)if(p.args[n] instanceof Intent){
     Intent intent=(Intent)p.args[n];ComponentName component=intent.getComponent();
     if(!MUSIC.equals(intent.getPackage())&&(component==null||!MUSIC.equals(component.getPackageName())))continue;
     String action=intent.getAction();
     try{
      Bundle extras=intent.getExtras();String query=null;if(extras!=null)try{query=extras.getString("query");}catch(Throwable ignored){}

      if(!"android.media.action.MEDIA_PLAY_FROM_SEARCH".equals(action)&&!Intent.ACTION_VIEW.equals(action)){return;}
      if(Intent.ACTION_VIEW.equals(action)&&!intent.getBooleanExtra("android.intent.extra.START_PLAYBACK",false)){return;}
      Bundle decision=request("intent-"+action,query,extras,intent.getData());if(decision==null)return;
      String route=decision.getString("action","pass");if("duplicate".equals(route)){p.setResult(null);return;}
      if(!"play".equals(route)&&!"choose".equals(route)&&!"queue".equals(route))return;
      String ticket=decision.getString("ticket");if(ticket==null)return;
      if(openChoice(decision)){p.setResult(null);}else release(decision);return;
     }catch(Throwable e){return;}
    }
   }
  });
  installGoogleMusicQueryAdapter();installTurnContextAdapter();installLiveQueueAdapter();
  hook(MediaController.TransportControls.class,"skipToNext",new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){if(music(owner(p.thisObject))&&blockCloudNext()){p.setResult(null);}}});
  hook(MediaController.class,"dispatchMediaButtonEvent",new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){if(p.thisObject instanceof MediaController&&music((MediaController)p.thisObject)&&nextKey(p.args)&&blockCloudNext())p.setResult(true);}});
  hook(android.media.AudioManager.class,"dispatchMediaKeyEvent",new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){if(nextKey(p.args)&&blockCloudNext())p.setResult(null);}});
  ScreenTimeoutGuard.install(context);
  GoogleForeground.install(context);
  GoogleInputWatch.install(context,googleLoader);
  GoogleReplyVoice.install(context,googleLoader);

 }
}
