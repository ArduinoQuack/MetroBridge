package dev.metrobridge.poc;

import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import org.json.*;
import java.security.*;
import java.util.*;

public final class BridgeStore {
 public static final String GOOGLE="com.google.android.googlequicksearchbox", TARGET="com.google.android.apps.youtube.music";
 public static final String AUTHORITY="dev.metrobridge.poc.control";
 public static final Uri URI=Uri.parse("content://"+AUTHORITY+"/control");
 private static final String GOOGLE_LEGACY_CERT="f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83";
 private static final String GOOGLE_CERT="7ce83c1b71f3d572fed04c8d40c5cb10ff75e6d87d9df6fbd53f0468c2905053";
 private static final String METRO_CERT="b8bb9a23a9b8ddbfa269b8a4ecd87d0e2c88bbb37edb72a7599732b411fb031a";
 private static final SecureRandom RANDOM=new SecureRandom();
 private static final LinkedHashMap<String,Ticket> TICKETS=new LinkedHashMap<>();
 private static final DispatchGuard DISPATCHES=new DispatchGuard();
 private static long latestSerial;
 private static final Map<String,Ticket> QUEUE_LEASES=new HashMap<>();
 public static SharedPreferences prefs(Context c){return c.getSharedPreferences("voice_bridge",Context.MODE_PRIVATE);}
 public static boolean signedBy(Context c,String pkg,String certificate) {
  long token=Binder.clearCallingIdentity();
  try {
   byte[] digest=new byte[32];if(certificate.length()!=64)return false;
   for(int i=0;i<32;i++)digest[i]=(byte)Integer.parseInt(certificate.substring(i*2,i*2+2),16);
   // Android validates signing history, including authenticated key rotation.
   return c.getPackageManager().hasSigningCertificate(pkg,digest,PackageManager.CERT_INPUT_SHA256);
  }catch(Exception ignored){return false;}finally{Binder.restoreCallingIdentity(token);}
 }
 public static void authorize(Context c){
  // Capture the unmodified Binder caller before querying package metadata as ourselves.
  int caller=Binder.getCallingUid();if(caller==Process.myUid())return;
  if(trustedGoogleUid(c,caller))return;
  throw new SecurityException("Google caller required");
 }
 public static boolean trustedGoogleUid(Context c,int uid){
  long token=Binder.clearCallingIdentity();
  try{return uid>=0&&c.getPackageManager().getApplicationInfo(GOOGLE,0).uid==uid&&googleSignerValid(c);}
  catch(Exception e){return false;}finally{Binder.restoreCallingIdentity(token);}
 }
 public static boolean googleSignerValid(Context c){return signedBy(c,GOOGLE,GOOGLE_CERT)||signedBy(c,GOOGLE,GOOGLE_LEGACY_CERT);}

 public static void initializeSettings(Context c){
  if(!prefs(c).getBoolean("native_voice_migrated",false))prefs(c).edit().putBoolean("native_voice_migrated",true).putBoolean("speak_confirmations",true).apply();
  SharedPreferences p=prefs(c);if(p.getBoolean("release_clean_21",false))return;
  SharedPreferences.Editor edit=p.edit();for(String key:new String[]{"capture","events","hook_time","ipc_time","bootstrap_time","hook_process","hook_summary","startup_stage","startup_detail","support_version","support_until","support_steps","support_counts","support_export_uri"})edit.remove(key);
  edit.remove("release_clean").putBoolean("release_clean_21",true).apply();
 }
 public static void grantGoogleVisibility(Context c){
  try {
   if(prefs(c).getBoolean("enabled",false)&&googleSignerValid(c))c.grantUriPermission(GOOGLE,URI,Intent.FLAG_GRANT_READ_URI_PERMISSION);
   else c.revokeUriPermission(GOOGLE,URI,Intent.FLAG_GRANT_READ_URI_PERMISSION);
   if(prefs(c).getBoolean("enabled",false)&&prefs(c).getBoolean("queue_enabled",false)&&compatibleMetro(c))c.grantUriPermission(TARGET,URI,Intent.FLAG_GRANT_READ_URI_PERMISSION);
   else c.revokeUriPermission(TARGET,URI,Intent.FLAG_GRANT_READ_URI_PERMISSION);
  }catch(Exception e){}
 }

 public static synchronized void cache(Context c,List<RoutePolicy.Entry> items){
  JSONArray array=new JSONArray();int count=0;
  for(RoutePolicy.Entry e:items){if(count>=2048)break;
   if(e.id==null||!e.id.matches("playlist/[^/\\s]+")||e.title==null||e.title.length()>300)continue;
   try{JSONObject o=new JSONObject();o.put("title",e.title);o.put("id",e.id);array.put(o);count++;}catch(JSONException ignored){}
  }
  prefs(c).edit().putString("playlists",array.toString()).putLong("library_time",System.currentTimeMillis()).apply();
 }
 public static List<RoutePolicy.Entry> library(Context c){
  ArrayList<RoutePolicy.Entry> entries=new ArrayList<>();
  try{JSONArray array=new JSONArray(prefs(c).getString("playlists","[]"));for(int i=0;i<array.length();i++){JSONObject o=array.getJSONObject(i);entries.add(new RoutePolicy.Entry(o.getString("title"),o.getString("id")));}}catch(JSONException ignored){}
  return entries;
 }
 public static boolean compatibleMetro(Context c){try{return "13.6.1".equals(c.getPackageManager().getPackageInfo(TARGET,0).versionName)&&signedBy(c,TARGET,METRO_CERT);}catch(Exception e){return false;}}
 public static final class Ticket {
  public final QueueRequest queue;public final List<RoutePolicy.Entry> choices;public final boolean shuffled,confirmationRequired;public final String routeKey;public final long expires,serial;
  Ticket(List<RoutePolicy.Entry> choices,boolean shuffled,boolean confirmationRequired,String routeKey,long serial){this.queue=null;this.confirmationRequired=confirmationRequired;this.choices=new ArrayList<>(choices);this.shuffled=shuffled;this.routeKey=routeKey;this.serial=serial;expires=SystemClock.elapsedRealtime()+60000;}
  Ticket(QueueRequest queue,String routeKey,long serial){this.queue=queue;this.choices=Collections.emptyList();this.shuffled=false;this.confirmationRequired=false;this.routeKey=routeKey;this.serial=serial;this.expires=SystemClock.elapsedRealtime()+60000;}
 }
 private static synchronized String ticket(Ticket value){
  Iterator<Map.Entry<String,Ticket>> it=TICKETS.entrySet().iterator();while(it.hasNext())if(it.next().getValue().expires<SystemClock.elapsedRealtime())it.remove();
  while(TICKETS.size()>=32)TICKETS.remove(TICKETS.keySet().iterator().next());
  byte[] bytes=new byte[24];RANDOM.nextBytes(bytes);StringBuilder b=new StringBuilder();for(byte x:bytes)b.append(String.format(Locale.ROOT,"%02x",x&255));String token=b.toString();TICKETS.put(token,value);return token;
 }
 public static synchronized Ticket consume(Context c,String token){
  if(!prefs(c).getBoolean("enabled",false)||!compatibleMetro(c))return null;
  Ticket t=TICKETS.remove(token);return isCurrent(t)?t:null;
 }
 public static synchronized boolean isCurrent(Ticket t){return t!=null&&t.serial==latestSerial&&t.expires>=SystemClock.elapsedRealtime();}
 public static synchronized String queueLease(Ticket ticket){if(!isCurrent(ticket)||ticket.queue==null)return null;String key=UUID.randomUUID().toString();QUEUE_LEASES.entrySet().removeIf(e->!isCurrent(e.getValue()));QUEUE_LEASES.put(key,ticket);return key;}
 public static synchronized void revokeQueueLease(String key){QUEUE_LEASES.remove(key);}
 public static synchronized boolean queueAllowed(Context c,String key){return key!=null&&prefs(c).getBoolean("enabled",false)&&prefs(c).getBoolean("queue_enabled",false)&&compatibleMetro(c)&&isCurrent(QUEUE_LEASES.get(key));}
 public static boolean trustedMetroUid(Context c,int uid){long token=Binder.clearCallingIdentity();try{return uid>=0&&c.getPackageManager().getApplicationInfo(TARGET,0).uid==uid&&compatibleMetro(c);}catch(Exception e){return false;}finally{Binder.restoreCallingIdentity(token);}}
 public static void acknowledge(String key){DISPATCHES.acknowledge(key,SystemClock.elapsedRealtime());}
 public static void release(String key){DISPATCHES.release(key);}
 public static synchronized Bundle resolve(Context c,Bundle in){
  Bundle out=new Bundle();out.putString("action","pass");
  String kind=in.getString("kind","unknown"),q=in.getString("query",""),playlist=in.getString("playlist",""),focus=in.getString("focus",""),title=in.getString("title","");

  if(in.getBoolean("reject",false)){out.putString("reason","request-fields-too-long");return out;}
  SharedPreferences settings=prefs(c);if(!settings.getBoolean("enabled",false))return out;
  if(kind.equals("intent-live-user-next")&&!settings.getBoolean("queue_enabled",false))return out;
  if(!compatibleMetro(c)){return out;}
  QueueRequest queue=QueueRequest.parse(q);
  if(queue!=null){
   if(kind.contains("prepare")){out.putString("action","hold");return out;}
   String key="next:"+NameMatcher.normalize(q);
   if(!DISPATCHES.reserve(key,SystemClock.elapsedRealtime(),60000)){out.putString("action","duplicate");return out;}
   long serial=++latestSerial;out.putString("action","queue");out.putString("routeKey",key);out.putString("ticket",ticket(new Ticket(queue,key,serial)));VoiceConfirmation.begin(c,key,in.getString("voiceReply",""));VoiceConfirmation.nextAccepted();
   return out;
  }
  List<RoutePolicy.Entry> library=library(c);if(library.isEmpty()){return out;}
  RoutePolicy.Decision d=RoutePolicy.resolve(q,playlist,focus,title,settings.getBoolean("bare",true),library);
  if("query-empty".equals(d.reason)&&in.getBoolean("catalogPlaylist",false)&&title.isEmpty()&&focus.isEmpty())d=new RoutePolicy.Decision("choose","google-catalog-link-has-no-local-name",false,new ArrayList<>(library));
  boolean shuffled=d.shuffleFor(settings.getBoolean("shuffle_default",false),q);

  out.putString("reason",d.reason);out.putBoolean("shuffled",shuffled);
  if("pass".equals(d.action))return out;
  String key=(kind.contains("prepare")?"prepare:":"play:")+shuffled+":"+(d.choices.size()==1?d.choices.get(0).id:NameMatcher.normalize(q+playlist));
  if(!DISPATCHES.reserve(key,SystemClock.elapsedRealtime(),"choose".equals(d.action)?60000:10000)){out.putString("action","duplicate");return out;}
  long serial=++latestSerial;
  out.putString("action",d.action);out.putString("routeKey",key);
  out.putString("ticket",ticket(new Ticket(d.choices,shuffled,d.requiresConfirmation(),key,serial)));
  if(!kind.contains("prepare"))VoiceConfirmation.begin(c,key,in.getString("voiceReply",""));
  if("play".equals(d.action))out.putString("mediaId",PlaylistRequest.forContainer(d.choices.get(0).id,shuffled));
  return out;
 }
 private BridgeStore(){}
}
