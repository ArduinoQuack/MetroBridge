package dev.metrobridge.poc;
import android.content.*;import android.os.*;
/** Public Android launch path, independent of private Google speech compatibility. */
final class GoogleForeground {
 private static final String SIGNER="3cedb394a29f13f38bd61b288d16bafdc1be55193e8257ed95acb5e66e6aa9e0";
 static void install(Context c){try{c.registerReceiver(new BroadcastReceiver(){@Override public void onReceive(Context ignored,Intent intent){try{
  int owner=c.getPackageManager().getApplicationInfo("dev.metrobridge.poc",0).uid;
  if(Build.VERSION.SDK_INT<34||getSentFromUid()!=owner||!VoiceConfirmation.ACTION.equals(intent.getAction())||!BridgeStore.signedBy(c,"dev.metrobridge.poc",SIGNER))return;
  Bundle permit=c.getContentResolver().call(BridgeStore.URI,"foreground-take",null,null);if(permit==null||!permit.getBoolean("allow",false))return;
  Intent metro=c.getPackageManager().getLaunchIntentForPackage(BridgeStore.TARGET);if(metro==null)return;metro.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);c.startActivity(metro);
 }catch(Throwable e){}}},new IntentFilter(VoiceConfirmation.ACTION),Context.RECEIVER_EXPORTED);}catch(Throwable e){}}
 private GoogleForeground(){}
}
