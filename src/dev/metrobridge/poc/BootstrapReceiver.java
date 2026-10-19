package dev.metrobridge.poc;
import android.content.*;
import android.os.Build;
/** Retained for the signed Google visibility handshake; stores no startup report. */
public final class BootstrapReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){
  BridgeStore.grantGoogleVisibility(context);
  if(Build.VERSION.SDK_INT>=34&&BridgeStore.trustedGoogleUid(context,getSentFromUid())){
   setResultCode(-1);
  }
 }
}
