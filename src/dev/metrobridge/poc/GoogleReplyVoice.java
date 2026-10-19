package dev.metrobridge.poc;
import android.content.*;import android.os.*;import de.robv.android.xposed.*;import java.lang.reflect.*;import java.util.*;
/** Event-driven replacement of an exactly matched music reply, after local success. */
final class GoogleReplyVoice {
 private static Context context;private static Handler main;private static SpeechProfile profile;
 private static Method stream,job,focus;private static Constructor<?> content;private static Pending held;private static final SpeechJobGuard JOBS=new SpeechJobGuard();private static Method onComplete;private static Class<?> function;
 private static final String SIGNER="3cedb394a29f13f38bd61b288d16bafdc1be55193e8257ed95acb5e66e6aa9e0";
 static void newTurn(){JOBS.clear();}
 static String response(){return GoogleInputWatch.response();}
 private static Object field(Object o,String f){return XposedHelpers.getObjectField(o,f);}
 private static Bundle call(String op,Bundle in){try{return context.getContentResolver().call(BridgeStore.URI,op,null,in);}catch(Throwable e){return null;}}
 private static Class<?> type(String old,ClassLoader l)throws Throwable{return Class.forName(profile.name(old),false,l);}
 static void install(Context c,ClassLoader loader){
  context=c;main=new Handler(Looper.getMainLooper());List<XC_MethodHook.Unhook> hooks=new ArrayList<>();
  try{
   if(!BridgeStore.googleSignerValid(c)||!GoogleSchema.configure(c))return;profile=GoogleSchema.profile;
   Class<?> engine=type("awhe",loader),dto=type("awhf",loader),id=type("awhk",loader),mode=type("awjx",loader),text=type("apsn",loader),cluster=type("gnqw",loader),cont=type("hdtz",loader),cfg=Class.forName("com.google.android.apps.search.assistant.surfaces.voice.robin.speech.AudioSessionConfig",false,loader);
   if(id.getDeclaredField("a").getType()!=String.class||id.getDeclaredField("b").getType()!=String.class||text.getDeclaredField("a").getType()!=String.class||text.getDeclaredField("b").getType()!=cluster||!mode.isEnum())throw new NoSuchFieldException();
   Set<String> modes=new HashSet<>();for(Object v:mode.getEnumConstants())modes.add(((Enum<?>)v).name());if(!modes.containsAll(Arrays.asList("AUTO_PLAY","STREAMING_AUTO_PLAY","CONVERSATION_MODE")))throw new NoSuchFieldException();
   content=text.getDeclaredConstructor(String.class,cluster);content.setAccessible(true);
   stream=engine.getDeclaredMethod(profile.stream(),dto,id,mode,cfg);job=engine.getDeclaredMethod(profile.job(),text,id,mode,cfg,boolean.class);focus=engine.getDeclaredMethod(profile.focus(),mode);
   if(stream.getReturnType()!=void.class||!job.getReturnType().getName().equals(profile.name("hefq"))||focus.getReturnType()!=boolean.class||!Modifier.isStatic(focus.getModifiers()))throw new NoSuchMethodException();
   Class<?> ref=engine.getDeclaredField("A").getType();if(!ref.getName().equals(profile.name("hecw"))||engine.getDeclaredField("y").getType()!=ref||!engine.getDeclaredField("c").getType().getName().equals(profile.name("heef")))throw new NoSuchFieldException();
   ref.getDeclaredMethod("a",Object.class);Method tracker=engine.getDeclaredMethod("a");if(!tracker.getReturnType().getName().equals(profile.name("aswf"))||tracker.getReturnType().getDeclaredMethod("f",mode,String.class).getReturnType()!=void.class)throw new NoSuchMethodException();
   engine.getDeclaredMethod("h",String.class,String.class,cont);engine.getDeclaredMethod(profile.flow(),text,mode,cfg,type("awhb",loader),cont);job.setAccessible(true);focus.setAccessible(true);
   function=Class.forName("kotlin.jvm.functions.Function1",false,loader);onComplete=job.getReturnType().getDeclaredMethod("k",function);onComplete.setAccessible(true);
   if(!onComplete.getReturnType().getName().equals(profile.modern?"hekc":"heex"))throw new NoSuchMethodException();
   hooks.add(XposedBridge.hookMethod(job,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam h){try{
    if(!automatic(h.args[2]))return;Object raw=field(h.args[1],"a");if(!(raw instanceof String))return;
    Object existing=JOBS.duplicate(h.thisObject,(String)raw);if(existing!=null){Bundle q=new Bundle();q.putString("reply",(String)raw);Bundle current=call("speech-current",q);if(current!=null&&current.getBoolean("current",false)&&current.getBoolean("enabled",false)){h.setResult(existing);}}
   }catch(Throwable e){}}}));
   // Some music acknowledgments use native plain-text playback rather than streamed S3 audio.
   try{
    List<Method> plain=new ArrayList<>();for(Method m:engine.getDeclaredMethods())if(m.getReturnType()==void.class&&Arrays.equals(m.getParameterTypes(),new Class<?>[]{text,id,mode,cfg}))plain.add(m);
    if(plain.size()==1){Method nativeText=plain.get(0);hooks.add(XposedBridge.hookMethod(nativeText,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam h){intercept(h,nativeText);}}));}
   }catch(Throwable e){}
   try{
    Class<?> session=type("awiy",loader),source=type("awjb",loader),textSource=type("awiz",loader),style=type("avrm",loader);
    if(session.getDeclaredField("d").getType()!=id||session.getDeclaredField("c").getType()!=source||textSource.getDeclaredField("a").getType()!=text||textSource.getDeclaredField("b").getType()!=String.class||textSource.getDeclaredField("c").getType()!=style||!source.isAssignableFrom(textSource))throw new NoSuchFieldException();
    Constructor<?> sourceCtor=textSource.getDeclaredConstructor(text,String.class,style);sourceCtor.setAccessible(true);
    Method speak=session.getDeclaredMethod("g",source,mode,type("gzhw",loader),cont);if(speak.getReturnType()!=Object.class)throw new NoSuchMethodException();
    hooks.add(XposedBridge.hookMethod(speak,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam h){try{
     if(!automatic(h.args[1]))return;Object old=h.args[0];if(old==null||old.getClass()!=textSource){return;}
     Object replacement=readyContent(field(h.thisObject,"d"));if(replacement!=null){h.args[0]=sourceCtor.newInstance(replacement,field(old,"b"),field(old,"c"));}
    }catch(Throwable e){}}}));
   }catch(Throwable e){}
   hooks.add(XposedBridge.hookMethod(stream,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam h){intercept(h,stream);}}));
   c.registerReceiver(new BroadcastReceiver(){@Override public void onReceive(Context ignored,Intent i){
    try{int uid=Build.VERSION.SDK_INT>=34?getSentFromUid():-1;int owner=c.getPackageManager().getApplicationInfo("dev.metrobridge.poc",0).uid;
     if(uid==owner&&BridgeStore.signedBy(c,"dev.metrobridge.poc",SIGNER)&&VoiceConfirmation.ACTION.equals(i.getAction())){drain();}
    }catch(Throwable ignoredError){}
   }},new IntentFilter(VoiceConfirmation.ACTION),Context.RECEIVER_EXPORTED);

  }catch(Throwable e){for(XC_MethodHook.Unhook hook:hooks)try{hook.unhook();}catch(Throwable ignored){}}
 }
 private static void intercept(XC_MethodHook.MethodHookParam h,Method original){try{
  if(h.args[0]==null||h.args[1]==null||!automatic(h.args[2])||h.args[3]==null)return;
  Object raw=field(h.args[1],"a");if(!(raw instanceof String))return;String reply=(String)raw;
  Bundle b=new Bundle();String capture=UUID.randomUUID().toString();b.putString("reply",reply);b.putString("capture",capture);Bundle state=call("speech-capture",b);if(state==null)return;String phase=state.getString("state","");
  if("duplicate".equals(phase)){h.setResult(null);return;}
  if(!"pending".equals(phase)&&!"success".equals(phase))return;
  Pending pending=new Pending(h.thisObject,h.args.clone(),reply,state.getString("route",""),capture,original);
  // Both intercepted entries return void. No coroutine continuation is stored or replayed.
  h.setResult(null);main.post(()->{if(held!=null)fallback();held=pending;main.postDelayed(pending.deadline,15000);drain();});
 }catch(Throwable e){}}
 private static boolean automatic(Object value){if(!(value instanceof Enum))return false;String name=((Enum<?>)value).name();return "AUTO_PLAY".equals(name)||"STREAMING_AUTO_PLAY".equals(name);}
 private static Object readyContent(Object id)throws Throwable{
  if(id==null)return null;Object raw=field(id,"a");if(!(raw instanceof String))return null;
  Bundle q=new Bundle();q.putString("reply",(String)raw);Bundle ready=call("speech-ready",q);if(ready==null||!ready.getBoolean("ready",false))return null;
  String token=UUID.randomUUID().toString();q.putString("capture",token);Bundle capture=call("speech-capture",q);if(capture==null||!"success".equals(capture.getString("state","")))return null;
  q.putString("route",capture.getString("route",""));Bundle taken=call("speech-take",q);String phrase=taken==null?"":taken.getString("phrase","");return phrase.isEmpty()?null:content.newInstance(phrase,null);
 }
 private static Bundle query(Pending p){Bundle b=new Bundle();b.putString("route",p.route);b.putString("reply",p.reply);b.putString("capture",p.capture);return b;}
 private static void drain(){Pending p=held;if(p==null)return;
  Bundle state=call("speech-state",query(p));if(state==null){fallback();return;}if(!state.getBoolean("current",false)){discard();return;}String phase=state.getString("state","");if("pending".equals(phase))return;if(!"success".equals(phase)){fallback();return;}
  Bundle permit=call("speech-take",query(p));String phrase=permit==null?"":permit.getString("phrase","");if(phrase.isEmpty()){fallback();return;}
  try{
   Object text=content.newInstance(phrase,null);boolean auto=(Boolean)focus.invoke(null,p.args[2]);Object tracking=XposedHelpers.callMethod(p.engine,"a"),ref=field(p.engine,"A");
   XposedHelpers.callMethod(tracking,"f",p.args[2],field(p.args[1],"b"));XposedHelpers.callMethod(ref,"a",p.args[1]);
   Object result=job.invoke(p.engine,text,p.args[1],p.args[2],p.args[3],auto);if(result==null){fallback();return;}JOBS.started(p.engine,p.reply,result);call("speech-job-started",query(p));observeCompletion(p,result);discard();
  }catch(Throwable e){fallback();}
 }
 private static void observeCompletion(Pending p,Object task){try{
  Object callback=Proxy.newProxyInstance(function.getClassLoader(),new Class<?>[]{function},(proxy,m,args)->{
   if(m.getDeclaringClass()==Object.class){if("hashCode".equals(m.getName()))return System.identityHashCode(proxy);if("equals".equals(m.getName()))return proxy==(args==null?null:args[0]);return "MetroBridgeCompletion";}
   if("invoke".equals(m.getName())){JOBS.finished(task);
    Bundle q=query(p);call("speech-job-finished",q);
   }return null;
  });onComplete.invoke(task,callback);
 }catch(Throwable e){}}
 private static void fallback(){Pending p=held;if(p==null)return;discard();call("speech-cancel",query(p));Bundle current=call("speech-current",query(p));if(current==null||!current.getBoolean("current",false))return;
  try{XposedBridge.invokeOriginalMethod(p.original,p.engine,p.args);}catch(Throwable ignored){}
 }
 private static void discard(){Pending p=held;held=null;if(p!=null)main.removeCallbacks(p.deadline);}
 private static final class Pending {
  final Object engine;final Object[] args;final String reply,route,capture;final Runnable deadline;final Method original;
  Pending(Object e,Object[] a,String r,String key,String token,Method entry){original=entry;engine=e;args=a;reply=r;route=key;capture=token;deadline=()->{if(held==this)fallback();};}
 }
 private GoogleReplyVoice(){}
}
