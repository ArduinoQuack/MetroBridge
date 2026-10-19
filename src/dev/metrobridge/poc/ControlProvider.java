package dev.metrobridge.poc;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public final class ControlProvider extends ContentProvider {
 @Override public boolean onCreate(){BridgeStore.initializeSettings(getContext());return true;}
 @Override public Bundle call(String method,String arg,Bundle in){
  Context c=getContext();if(in==null)in=new Bundle();Bundle out=new Bundle();
  if("queue-permit".equals(method)){
   if(!BridgeStore.trustedMetroUid(c,android.os.Binder.getCallingUid()))throw new SecurityException("Signed MetroList caller required");
   out.putBoolean("allow",BridgeStore.queueAllowed(c,in.getString("lease")));return out;
  }
  BridgeStore.authorize(c);

  if(method.startsWith("speech-")){if(!BridgeStore.trustedGoogleUid(c,android.os.Binder.getCallingUid()))throw new SecurityException("Google caller required");return VoiceConfirmation.call(c,method,in);}
  if("block-cloud-next".equals(method)){if(!BridgeStore.trustedGoogleUid(c,android.os.Binder.getCallingUid()))throw new SecurityException("Google caller required");out.putBoolean("block",VoiceConfirmation.blockNext(c));return out;}
  switch(method){
   case "hello":
    if(!BridgeStore.trustedGoogleUid(c,android.os.Binder.getCallingUid()))throw new SecurityException("Google caller required for hook acknowledgment");
    out.putBoolean("enabled",BridgeStore.prefs(c).getBoolean("enabled",false));return out;
   case "turn-config":out.putBoolean("allow",BridgeStore.prefs(c).getBoolean("enabled",false)&&BridgeStore.compatibleMetro(c));return out;
   case "live-queue-config":out.putBoolean("allow",BridgeStore.prefs(c).getBoolean("enabled",false)&&BridgeStore.prefs(c).getBoolean("queue_enabled",false)&&BridgeStore.compatibleMetro(c));return out;
   case "music-query":
    out.putBoolean("allowEnrichment",BridgeStore.prefs(c).getBoolean("enabled",false)&&BridgeStore.compatibleMetro(c));return out;
   case "screen-timeout-config":if(!BridgeStore.trustedGoogleUid(c,android.os.Binder.getCallingUid()))throw new SecurityException("Google caller required");out.putBoolean("allow",BridgeStore.prefs(c).getBoolean("enabled",false)&&BridgeStore.prefs(c).getBoolean("normal_screen_timeout",false));return out;
   case "foreground-take":if(!BridgeStore.trustedGoogleUid(c,android.os.Binder.getCallingUid()))throw new SecurityException("Google caller required");out.putBoolean("allow",ForegroundHandoff.take(c));return out;
   case "execute-ticket":{
    if(!BridgeStore.trustedGoogleUid(c,android.os.Binder.getCallingUid()))throw new SecurityException("Google caller required");
    long identity=android.os.Binder.clearCallingIdentity();try{out.putBoolean("accepted",LocalActions.dispatch(c,in.getString("ticket")));}finally{android.os.Binder.restoreCallingIdentity(identity);}return out;
   }
   case "resolve":return BridgeStore.resolve(c,in);
   case "ack":BridgeStore.acknowledge(in.getString("routeKey",""));return out;
   case "release":BridgeStore.release(in.getString("routeKey",""));return out;
   default:throw new IllegalArgumentException("Unsupported bridge operation");
  }
 }
 @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order){BridgeStore.authorize(getContext());return null;}
 @Override public String getType(Uri uri){return "vnd.android.cursor.item/metrobridge";}
 @Override public Uri insert(Uri uri,ContentValues values){throw new SecurityException();}
 @Override public int delete(Uri uri,String where,String[] args){throw new SecurityException();}
 @Override public int update(Uri uri,ContentValues values,String where,String[] args){throw new SecurityException();}
}
