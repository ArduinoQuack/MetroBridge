package dev.metrobridge.poc;

import android.app.*;
import android.content.*;
import android.media.browse.MediaBrowser;
import android.media.session.*;
import android.os.*;
import android.widget.TextView;

final class PlaylistOperation extends ContextWrapper {
 private MediaBrowser browser;private final Handler handler=new Handler(Looper.getMainLooper());private boolean dispatched=false,finished=false,errorShown=false,complete=false;
 private BridgeStore.Ticket ticket;private RoutePolicy.Entry selectedPlaylist;
 private MediaController controller;private MediaController.Callback callback;private long sentAt;
 PlaylistOperation(Context c,BridgeStore.Ticket request){super(c);ticket=request;}
 void start(){
  if(ticket==null){finish();return;}
  if(ticket.choices.isEmpty()){error("No matching playlist was returned. Refresh playlists in module settings.");return;}
  play(ticket.choices.get(0));
 }
 private void play(RoutePolicy.Entry entry){
  if(!BridgeStore.isCurrent(ticket)){error("This playlist choice expired or was superseded. Ask again.");return;}
  if(!BridgeStore.prefs(this).getBoolean("enabled",false)||!BridgeStore.compatibleMetro(this)){finish();return;}
  selectedPlaylist=entry;
  handler.postDelayed(()->{if(!dispatched&&!finished)error("MetroList did not connect in time. No playback request was sent.");},15000);
  try{browser=new MediaBrowser(this,new ComponentName(BridgeStore.TARGET,"com.metrolist.music.playback.MusicService"),new MediaBrowser.ConnectionCallback(){
   @Override public void onConnected(){if(finished||errorShown||isFinishing())return;
    try{
     if(!BridgeStore.isCurrent(ticket)||!BridgeStore.prefs(PlaylistOperation.this).getBoolean("enabled",false)||!BridgeStore.compatibleMetro(PlaylistOperation.this)){finish();return;}
     controller=new MediaController(PlaylistOperation.this,browser.getSessionToken());
     callback=new MediaController.Callback(){
      @Override public void onPlaybackStateChanged(PlaybackState state){observe(state);}
      @Override public void onSessionDestroyed(){error("MetroList session ended before playback was observed.");}
     };controller.registerCallback(callback,handler);
     String id=PlaylistRequest.forContainer(entry.id,ticket.shuffled);
     sentAt=SystemClock.elapsedRealtime();controller.getTransportControls().playFromMediaId(id,null);dispatched=true;
     BridgeStore.acknowledge(ticket.routeKey);
     handler.postDelayed(()->{if(!finished&&!isFinishing())error("No fresh playing state was observed within 20 seconds. The request was sent and may still finish; no automatic retry was made.");},20000);
    }catch(Exception e){error("Playback request failed: "+e.getClass().getSimpleName());}
   }
   @Override public void onConnectionFailed(){error("MetroList connection failed.");}
   @Override public void onConnectionSuspended(){error(dispatched?"MetroList disconnected after the request; playback is unverified.":"MetroList connection was interrupted.");}
  },null);browser.connect();}catch(Exception e){error("Unable to contact MetroList: "+e.getClass().getSimpleName());}
 }
 private void observe(PlaybackState state){
  if(finished||errorShown||isFinishing()||!dispatched||state==null)return;
  if(!BridgeStore.isCurrent(ticket)){finish();return;}

  if(state.getState()==PlaybackState.STATE_PLAYING&&state.getLastPositionUpdateTime()>=sentAt){

   complete=true;ActionFeedback.success(this,ticket,ConfirmationWording.playlist(this,selectedPlaylist==null?null:selectedPlaylist.title,ticket.shuffled));
   handler.removeCallbacksAndMessages(null);finish();
  }else if(state.getState()==PlaybackState.STATE_ERROR)error("MetroList reported a playback error after the request.");
 }
 private void error(String message){if(finished||errorShown||isFinishing())return;errorShown=true;handler.removeCallbacksAndMessages(null);disconnect();VoiceConfirmation.failure(this,ticket);ActionFeedback.failure(this,message);finish();}
 private void disconnect(){
  if(controller!=null&&callback!=null)try{controller.unregisterCallback(callback);}catch(Exception ignored){}controller=null;callback=null;
  MediaBrowser old=browser;browser=null;if(old!=null)try{old.disconnect();}catch(Exception ignored){}
 }
 private boolean isFinishing(){return finished;}
 private void finish(){if(finished)return;finished=true;if(ticket!=null&&!complete){VoiceConfirmation.failure(this,ticket);if(!dispatched)BridgeStore.release(ticket.routeKey);}handler.removeCallbacksAndMessages(null);disconnect();LocalActions.finished(this);}
}
