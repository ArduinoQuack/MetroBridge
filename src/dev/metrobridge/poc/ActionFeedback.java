package dev.metrobridge.poc;
import android.content.Context;
/** No popups. Confirmation is handed only to the correlated native Google reply. */
public final class ActionFeedback {
 public static void failure(Context context,String message){
  String safe=message==null?"Request could not be completed.":message.replaceAll("[\\r\\n]"," ");
  BridgeStore.prefs(context).edit().putString("last_problem",safe.substring(0,Math.min(400,safe.length()))).apply();
 }
 public static void success(Context context,BridgeStore.Ticket ticket,String phrase){
  BridgeStore.prefs(context).edit().remove("last_problem").apply();
  VoiceConfirmation.success(context,ticket,phrase);
 }
 public static void stop(){VoiceConfirmation.GATE.clear();}
 private ActionFeedback(){}
}
