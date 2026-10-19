package dev.metrobridge.poc;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;
public class ScreenTimeoutGuardTest {
 static int n;static void c(boolean b){n++;if(!b)throw new AssertionError("screen "+n);}static final String ROOT="com.google.android.apps.search.assistant.surfaces.voice.robin.";
 static Activity create(Application app,String name){Activity a=new Activity(app,name);app.callbacks.get(0).onActivityCreated(a,null);Handler.run();a.getWindow().attrs.flags=128|2|0x200000;a.getWindow().attrs.width=111;a.getWindow().attrs.title="Gemini";a.getWindow().attrs.token=new Object();new WindowManagerGlobal().addView(a.getWindow().decor,a.getWindow().attrs,null,null,0);return a;}
 static void signal(Application app,boolean enabled,int uid){ContentResolver.enabled=enabled;BroadcastReceiver.uid=uid;app.receiver.onReceive(app,new Intent(VoiceConfirmation.ACTION));Handler.run();}
 public static void main(String[] x){Application app=new Application();ScreenTimeoutGuard.install(app);c(app.callbacks.isEmpty());app.attached=true;Handler.run();c(app.callbacks.size()==1);c(de.robv.android.xposed.XposedBridge.hooks.size()==2);
 Activity main=create(app,ROOT+"main.MainActivity");Window w=main.getWindow();c(w.decor.delivered==w.attrs);c((w.decor.delivered.flags&128)!=0);
 signal(app,true,42);c(w.decor.delivered!=w.attrs);c((w.decor.delivered.flags&128)==0);c(w.attrs.flags==(128|2|0x200000));c(w.decor.delivered.flags==(2|0x200000));c(w.decor.delivered.width==111);c(w.decor.delivered.token==w.attrs.token);c(w.decor.delivered.title.equals("Gemini"));
 Activity search=create(app,"com.google.android.googlequicksearchbox.SearchActivity");c((search.getWindow().decor.delivered.flags&128)!=0);
 Activity similar=create(app,ROOT+"main.MainActivityOther");c((similar.getWindow().decor.delivered.flags&128)!=0);
 Activity floaty=create(app,ROOT+"ui.floaty.activity.FloatyActivity");c((floaty.getWindow().decor.delivered.flags&128)==0);
 View child=new View();new WindowManagerGlobal().addView(child,w.attrs,null,w,0);c((child.delivered.flags&128)!=0);
 signal(app,false,99);c((w.decor.delivered.flags&128)==0);BridgeStore.signature=false;signal(app,false,42);c((w.decor.delivered.flags&128)==0);BridgeStore.signature=true;
 signal(app,false,42);c((w.decor.delivered.flags&128)!=0);c(w.attrs.flags==(128|2|0x200000));
 signal(app,true,42);c((w.decor.delivered.flags&128)==0);ContentResolver.fail=true;signal(app,true,42);c((w.decor.delivered.flags&128)!=0);ContentResolver.fail=false;
 signal(app,true,42);app.callbacks.get(0).onActivityDestroyed(main);w.getWindowManager().updateViewLayout(w.decor,w.attrs);c((w.decor.delivered.flags&128)!=0);
 Activity foreign=new Activity(app,ROOT+"main.MainActivity");foreign.pkg="com.google.android.apps.youtube.music";app.callbacks.get(0).onActivityCreated(foreign,null);Handler.run();foreign.getWindow().attrs.flags=128;new WindowManagerGlobal().addView(foreign.getWindow().decor,foreign.getWindow().attrs,null,null,0);c((foreign.getWindow().decor.delivered.flags&128)!=0);
 System.out.println(n+" actual screen-timeout hook checks passed with host Android/Xposed doubles (not a device screen-timeout test)");}
}
