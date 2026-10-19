package dev.metrobridge.poc;
/** One bounded music action/reply association. Contains no conversation text or persisted data. */
public final class ConfirmationGate {
 private String route="",reply="",phrase="",capture="";private long until;private boolean done,consumed,failed;
 public synchronized void begin(String key,String response,long now){
  clear();if(key==null||key.isEmpty()||response==null||response.isEmpty()||response.length()>300)return;
  route=key;reply=response;until=now+60000;
 }
 public synchronized void turn(String response){if(!reply.equals(response))clear();}
 public synchronized boolean finish(String key,String text,long now){
  if(!valid(now)||!route.equals(key)||failed||consumed||text==null||text.trim().isEmpty())return false;
  phrase=text.substring(0,Math.min(400,text.length()));done=true;return true;
 }
 public synchronized void fail(String key){if(route.equals(key))failed=true;}
 /** First process captures; repeated native entries for this reply must not launch duplicate audio. */
 public synchronized String capture(String response,String token,long now){
  if(!valid(now)||!reply.equals(response)||failed)return "pass";
  if(consumed)return "duplicate";
  if(!capture.isEmpty())return "duplicate";capture=token;return done?"success":"pending";
 }
 public synchronized boolean ready(String response,long now){return valid(now)&&reply.equals(response)&&done&&!failed&&!consumed&&capture.isEmpty();}
 public synchronized String route(){return route;}
 public synchronized String state(String key,String response,String token,long now){
  if(!valid(now)||!route.equals(key)||!reply.equals(response)||!capture.equals(token)||failed||consumed)return "cancel";
  return done?"success":"pending";
 }
 public synchronized String take(String key,String response,String token,long now){
  if(!"success".equals(state(key,response,token,now)))return "";consumed=true;return phrase;
 }
 public synchronized boolean ownsClaim(String key,String response,String token,long now){return valid(now)&&consumed&&!failed&&route.equals(key)&&reply.equals(response)&&!capture.isEmpty()&&capture.equals(token);}
 public synchronized void clear(){route="";reply="";phrase="";capture="";until=0;done=false;consumed=false;failed=false;}
 private boolean valid(long now){return !route.isEmpty()&&now<until;}
}
