package dev.metrobridge.poc;
import android.content.Context;
final class ConfirmationWording {
 static String playlist(Context c,String title,boolean shuffle){return format(c,false,title,"","","",shuffle,-1);}
 static String next(Context c,String title,String artist,String album,boolean shuffle,int queueSize){return format(c,true,"",title,artist,album,shuffle,queueSize);}
 private static String format(Context c,boolean next,String playlist,String song,String artist,String album,boolean shuffle,int queueSize){
  String preset=next?ConfirmationTemplate.NEXT:ConfirmationTemplate.PLAYLIST;
  String template=BridgeStore.prefs(c).getBoolean("custom_confirmations",false)?BridgeStore.prefs(c).getString(next?"next_template":"playlist_template",preset):preset;
  return ConfirmationTemplate.render(template,next,playlist,song,artist,album,shuffle,queueSize);
 }
 private ConfirmationWording(){}
}
