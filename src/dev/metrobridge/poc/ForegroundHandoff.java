package dev.metrobridge.poc;
import android.content.Context;import android.os.*;
/** Event-driven foreground request; the authenticated Google process performs the final launch. */
final class ForegroundHandoff {
 private static final Handler main=new Handler(Looper.getMainLooper());
 private static BridgeStore.Ticket pending;private static Context context;private static Runnable deadline;private static boolean ready;
 static void success(Context c,BridgeStore.Ticket ticket){main.post(()->prepare(c,ticket));}
 private static synchronized void prepare(Context c,BridgeStore.Ticket ticket){
  clear();if(!BridgeStore.isCurrent(ticket))return;pending=ticket;context=c.getApplicationContext();
  if(!VoiceConfirmation.enabled(c)){open();return;}
  deadline=()->open();main.postDelayed(deadline,20000);
 }
 static void speechStarted(String route){main.post(()->started(route));}
 private static synchronized void started(String route){if(pending==null||!pending.routeKey.equals(route))return;
  if(deadline!=null)main.removeCallbacks(deadline);
  // Never open a window over an active native job, including long custom wording.
  deadline=()->{synchronized(ForegroundHandoff.class){clear();}};main.postDelayed(deadline,Math.max(1,pending.expires-SystemClock.elapsedRealtime()));
 }
 static void speechFinished(String route){main.post(()->ended(route));}
 private static synchronized void ended(String route){if(pending!=null&&pending.routeKey.equals(route))open();}
 static void newTurn(){main.post(ForegroundHandoff::clear);}
 private static synchronized void open(){
  if(context==null||!BridgeStore.isCurrent(pending)||!BridgeStore.prefs(context).getBoolean("enabled",false)){clear();return;}
  if(deadline!=null)main.removeCallbacks(deadline);ready=true;
  deadline=ForegroundHandoff::clear;main.postDelayed(deadline,Math.max(1,pending.expires-SystemClock.elapsedRealtime()));VoiceConfirmation.signal(context);
 }
 static synchronized boolean take(Context c){if(!ready)return false;
  boolean allowed=BridgeStore.isCurrent(pending)&&BridgeStore.prefs(c).getBoolean("enabled",false);clear();return allowed;
 }
 private static synchronized void clear(){if(deadline!=null)main.removeCallbacks(deadline);deadline=null;pending=null;context=null;ready=false;}
 private ForegroundHandoff(){}
}
