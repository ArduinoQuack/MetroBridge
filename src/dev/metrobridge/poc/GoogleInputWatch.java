package dev.metrobridge.poc;
import android.content.Context;import android.os.*;import de.robv.android.xposed.*;import java.lang.reflect.*;
/** Current reply identity only; independent of the optional native speech engine. */
final class GoogleInputWatch {
 private static volatile String response="";private static volatile long at;
 static String response(){return SystemClock.elapsedRealtime()-at<60000?response:"";}
 static boolean install(Context c,ClassLoader loader){try{
  if(!BridgeStore.googleSignerValid(c)||!GoogleSchema.configure(c))return false;
  Class<?> frame=GoogleSchema.type("aobd",loader),fresh=GoogleSchema.type("anjl",loader),ids=GoogleSchema.type("aofp",loader),token=GoogleSchema.type("apso",loader);
  if(frame.getDeclaredField("t").getType()!=GoogleSchema.type("appx",loader)||frame.getDeclaredField("k").getType()!=GoogleSchema.type("anjy",loader)||frame.getDeclaredField("u").getType()!=GoogleSchema.type("aofw",loader))return false;
  Method make=ids.getDeclaredMethod("i",token);if(make.getReturnType().getDeclaredField("a").getType()!=String.class)return false;make.setAccessible(true);
  XposedBridge.hookMethod(frame.getDeclaredMethod("invokeSuspend",Object.class),new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam h){try{
   if(!GoogleSchema.fresh(XposedHelpers.getObjectField(h.thisObject,"k"),fresh)||XposedHelpers.getObjectField(h.thisObject,"u")!=null)return;
   Object message=make.invoke(null,XposedHelpers.getObjectField(h.thisObject,"t")),raw=XposedHelpers.getObjectField(message,"a");
   if(!(raw instanceof String)||((String)raw).isEmpty())return;
   if(!raw.equals(response)){response=(String)raw;at=SystemClock.elapsedRealtime();GoogleReplyVoice.newTurn();}Bundle b=new Bundle();b.putString("reply",response);c.getContentResolver().call(BridgeStore.URI,"speech-turn",null,b);
  }catch(Throwable e){}}});return true;
 }catch(Throwable e){return false;}}
 private GoogleInputWatch(){}
}
