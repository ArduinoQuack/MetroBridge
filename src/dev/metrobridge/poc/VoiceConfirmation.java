package dev.metrobridge.poc;
import android.app.BroadcastOptions;import android.content.*;import android.os.*;
/** Companion-side success notification. Broadcasts carry no song names, text or IDs. */
final class VoiceConfirmation {
 static final String ACTION="dev.metrobridge.poc.SPEECH_RESULT";
 private static String currentReply="";private static long currentAt;private static final NextSkipGuard NEXT=new NextSkipGuard();
 static final ConfirmationGate GATE=new ConfirmationGate();
 static boolean enabled(Context c){return BridgeStore.prefs(c).getBoolean("enabled",false)&&BridgeStore.prefs(c).getBoolean("speak_confirmations",true);}
 static synchronized void begin(Context c,String key,String response){
  long now=SystemClock.elapsedRealtime();if(response==null||response.isEmpty())response=now-currentAt<60000?currentReply:"";
  if(enabled(c)&&response.equals(currentReply))GATE.begin(key,response,now);else GATE.clear();
 }
 static synchronized void nextAccepted(){NEXT.accept(currentReply,SystemClock.elapsedRealtime());}
 static synchronized boolean blockNext(Context c){return BridgeStore.prefs(c).getBoolean("enabled",false)&&BridgeStore.prefs(c).getBoolean("queue_enabled",false)&&NEXT.block(SystemClock.elapsedRealtime());}
 static void success(Context c,BridgeStore.Ticket ticket,String phrase){
  if(enabled(c)&&BridgeStore.isCurrent(ticket)&&GATE.finish(ticket.routeKey,phrase,SystemClock.elapsedRealtime())){ForegroundHandoff.success(c,ticket);signal(c);}else {if(!enabled(c)&&BridgeStore.isCurrent(ticket))ForegroundHandoff.success(c,ticket);}
 }
 static void failure(Context c,BridgeStore.Ticket ticket){if(ticket!=null){GATE.fail(ticket.routeKey);signal(c);}}
 static void signal(Context c){try{
  Intent i=new Intent(ACTION).setPackage(BridgeStore.GOOGLE);
  if(Build.VERSION.SDK_INT>=34)c.sendBroadcast(i,null,BroadcastOptions.makeBasic().setShareIdentityEnabled(true).setDeferralPolicy(BroadcastOptions.DEFERRAL_POLICY_NONE).toBundle());
 }catch(Throwable ignored){}}
 static synchronized Bundle call(Context c,String method,Bundle in){
  Bundle out=new Bundle();long now=SystemClock.elapsedRealtime();
  String response=in.getString("reply",""),key=in.getString("route",""),token=in.getString("capture","");
  if("speech-turn".equals(method)){if(!response.isEmpty()&&response.length()<=300&&!response.equals(currentReply)){currentReply=response;currentAt=now;NEXT.turn(response);GATE.turn(response);ForegroundHandoff.newTurn();signal(c);}return out;}
  boolean current=response.equals(currentReply)&&!response.isEmpty()&&now-currentAt<60000;out.putBoolean("current",current);out.putBoolean("enabled",enabled(c));
  if("speech-current".equals(method))return out;
  if(!enabled(c)){GATE.clear();out.putString("state","cancel");return out;}
  if(!current){out.putString("state","cancel");return out;}
  if("speech-job-started".equals(method)){if(GATE.ownsClaim(key,response,token,now))ForegroundHandoff.speechStarted(key);return out;}
  if("speech-job-finished".equals(method)){
   if(GATE.ownsClaim(key,response,token,now)){ForegroundHandoff.speechFinished(key);}return out;
  }
  if("speech-ready".equals(method)){boolean ready=GATE.ready(response,now);out.putBoolean("ready",ready);return out;}
  if("speech-capture".equals(method)){
   if(token.isEmpty()||token.length()>80)return out;
   String state=GATE.capture(response,token,now);out.putString("state",state);out.putString("route",GATE.route());return out;
  }
  if("speech-state".equals(method)){out.putString("state",GATE.state(key,response,token,now));return out;}
  if("speech-take".equals(method)){String text=GATE.take(key,response,token,now);out.putString("phrase",text);return out;}
  if("speech-cancel".equals(method)){GATE.fail(key);return out;}
  return out;
 }
 private VoiceConfirmation(){}
}
