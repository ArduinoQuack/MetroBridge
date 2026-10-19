package dev.metrobridge.poc;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;
import de.robv.android.xposed.*;import java.lang.reflect.*;import java.util.*;
/** Only Gemini activity-window keep-screen-on requests; never CPU locks, sleep or media state. */
final class ScreenTimeoutGuard {
 private static final String SIGNER="3cedb394a29f13f38bd61b288d16bafdc1be55193e8257ed95acb5e66e6aa9e0";
 private static final Map<Window,Boolean> windows=new WeakHashMap<>();
 private static final Handler main=new Handler(Looper.getMainLooper());private static volatile boolean enabled;
 private static boolean target(Activity a){if(a==null||!BridgeStore.GOOGLE.equals(a.getPackageName())||a.getComponentName()==null)return false;
  String n=a.getComponentName().getClassName();return "com.google.android.apps.search.assistant.surfaces.voice.robin.main.MainActivity".equals(n)||"com.google.android.apps.search.assistant.surfaces.voice.robin.ui.floaty.activity.FloatyActivity".equals(n);
 }
 private static void track(Context c,Activity a){if(!target(a))return;Window w=a.getWindow();if(w!=null)synchronized(windows){windows.put(w,Boolean.TRUE);}refresh(c);}
 private static boolean owns(View decor){synchronized(windows){for(Window w:windows.keySet())if(w!=null&&w.peekDecorView()==decor)return true;}return false;}
 private static void refresh(Context c){main.post(()->{
  boolean next=false;try{Bundle b=c.getContentResolver().call(BridgeStore.URI,"screen-timeout-config",null,null);next=b!=null&&b.getBoolean("allow",false);}catch(Throwable e){}
  enabled=next;List<Window> current;synchronized(windows){current=new ArrayList<>(windows.keySet());}
  for(Window w:current)try{View v=w.peekDecorView();if(v!=null&&v.isAttachedToWindow()&&w.getWindowManager()!=null)w.getWindowManager().updateViewLayout(v,w.getAttributes());}catch(Throwable e){}
 });}
 static void install(Context c){List<XC_MethodHook.Unhook> hooks=new ArrayList<>();try{
  Class<?> global=Class.forName("android.view.WindowManagerGlobal");Set<String> found=new HashSet<>();
  for(Method m:global.getDeclaredMethods()){
   Class<?>[] p=m.getParameterTypes();if(!("addView".equals(m.getName())||"updateViewLayout".equals(m.getName()))||p.length<2||p[0]!=View.class||p[1]!=ViewGroup.LayoutParams.class||m.getReturnType()!=void.class)continue;
   hooks.add(XposedBridge.hookMethod(m,new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam h){try{
    if(!enabled||!(h.args[0] instanceof View)||!(h.args[1] instanceof WindowManager.LayoutParams)||!owns((View)h.args[0]))return;
    WindowManager.LayoutParams original=(WindowManager.LayoutParams)h.args[1];if((original.flags&WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)==0)return;
    // Preserve Google's original Window attributes for immediate, accurate toggle-off restoration.
    WindowManager.LayoutParams copy=new WindowManager.LayoutParams();copy.copyFrom(original);copy.flags&=~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;h.args[1]=copy;
   }catch(Throwable e){}}}));found.add(m.getName());
  }
  if(!found.containsAll(Arrays.asList("addView","updateViewLayout")))throw new NoSuchMethodException();
  main.post(()->attach(c,hooks));

 }catch(Throwable e){enabled=false;for(XC_MethodHook.Unhook h:hooks)try{h.unhook();}catch(Throwable ignored){}}}
 private static void attach(Context c,List<XC_MethodHook.Unhook> hooks){Application application=null;Application.ActivityLifecycleCallbacks callbacks=null;try{
  Context app=c.getApplicationContext();if(!(app instanceof Application))throw new IllegalStateException();application=(Application)app;
  callbacks=new Application.ActivityLifecycleCallbacks(){
   public void onActivityCreated(Activity a,Bundle state){track(c,a);}public void onActivityResumed(Activity a){track(c,a);}
   public void onActivityDestroyed(Activity a){synchronized(windows){windows.remove(a.getWindow());}}
   public void onActivityStarted(Activity a){}public void onActivityPaused(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle state){}
  };application.registerActivityLifecycleCallbacks(callbacks);
  c.registerReceiver(new BroadcastReceiver(){@Override public void onReceive(Context ignored,Intent intent){try{
   if(Build.VERSION.SDK_INT<34||!VoiceConfirmation.ACTION.equals(intent.getAction()))return;
   int owner=c.getPackageManager().getApplicationInfo("dev.metrobridge.poc",0).uid;
   if(getSentFromUid()==owner&&BridgeStore.signedBy(c,"dev.metrobridge.poc",SIGNER))refresh(c);
  }catch(Throwable ignoredError){}}},new IntentFilter(VoiceConfirmation.ACTION),Context.RECEIVER_EXPORTED);
  refresh(c);
 }catch(Throwable e){enabled=false;if(application!=null&&callbacks!=null)application.unregisterActivityLifecycleCallbacks(callbacks);for(XC_MethodHook.Unhook h:hooks)try{h.unhook();}catch(Throwable ignored){}}}
 private ScreenTimeoutGuard(){}
}
