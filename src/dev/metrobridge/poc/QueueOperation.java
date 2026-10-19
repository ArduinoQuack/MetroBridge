package dev.metrobridge.poc;
import android.app.*;
import android.content.*;
import android.media.browse.MediaBrowser;
import android.os.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class QueueOperation extends ContextWrapper {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private BridgeStore.Ticket ticket;private String lease,fingerprint;private MediaBrowser browser;private boolean failed,complete,destroyed;
 QueueOperation(Context c,BridgeStore.Ticket request){super(c);ticket=request;}
 void start(){if(ticket==null||ticket.queue==null){finish();return;}
  if(!ticket.queue.error.isEmpty()){error(ticket.queue.error);return;}
  if(Build.VERSION.SDK_INT<34||!BridgeStore.prefs(this).getBoolean("queue_enabled",false)){error("Enable Play next in MetroBridge and add MetroList to its LSPosed scope first.");return;}
  lease=BridgeStore.queueLease(ticket);if(lease==null){finish();return;}
  handler.postDelayed(()->{finish();},60000);
  // No activity/window handoff: preserve the active Gemini invocation.
  handler.postDelayed(()->{if(browser==null||!browser.isConnected())error("MetroList did not connect. Queue unchanged.");},15000);
  try{browser=new MediaBrowser(this,new ComponentName(BridgeStore.TARGET,"com.metrolist.music.playback.MusicService"),new MediaBrowser.ConnectionCallback(){
   @Override public void onConnected(){Bundle input=new Bundle();request("snapshot",input,5000,result->{
    if(result.getInt("queueProtocol",0)!=2){error("MetroList is still running an older bridge. Restart MetroList once after this update. No search or insertion was sent.");return;}
    fingerprint=result.getString("fingerprint");
    if(ticket.queue.current){
     String currentTitle="this song",currentArtist="",currentAlbum="";try{android.media.MediaMetadata metadata=new android.media.session.MediaController(QueueOperation.this,browser.getSessionToken()).getMetadata();if(metadata!=null&&metadata.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)!=null)currentTitle=metadata.getString(android.media.MediaMetadata.METADATA_KEY_TITLE);if(metadata!=null){currentArtist=metadata.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST);currentAlbum=metadata.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM);}}catch(Exception ignored){}
     insert(result.getString("current"),currentTitle,currentArtist,currentAlbum);return;
    }
    Bundle search=new Bundle();search.putString("query",ticket.queue.search());request("search",search,24000,QueueOperation.this::choose);
   });}
   @Override public void onConnectionFailed(){error("MetroList connection failed. Queue unchanged.");}
   @Override public void onConnectionSuspended(){error("MetroList connection was interrupted. No automatic retry.");}
  },null);browser.connect();}catch(Exception e){error("Unable to connect to MetroList: "+e.getClass().getSimpleName());}
 }
 private interface Reply {void accept(Bundle result);}
 private void request(String operation,Bundle args,long timeout,Reply callback){

  if(failed||destroyed||!BridgeStore.queueAllowed(this,lease)){error("Request expired, was superseded, or routing is disabled.");return;}
  String id=UUID.randomUUID().toString();AtomicBoolean answered=new AtomicBoolean();
  Runnable deadline=()->{if(answered.compareAndSet(false,true))error("Queue bridge timed out at "+operation+". Check MetroList's LSPosed scope and restart the app once. No retry made.");};handler.postDelayed(deadline,timeout);
  ResultReceiver result=new ResultReceiver(handler){@Override protected void onReceiveResult(int code,Bundle data){
   if(destroyed||failed||data==null||!id.equals(data.getString("request"))||!answered.compareAndSet(false,true))return;handler.removeCallbacks(deadline);
   if(!data.getBoolean("ok",false)){error(data.getString("error","Queue operation failed."));return;}callback.accept(data);
  }};
  Intent intent=new Intent(NativeQueueBridge.ACTION).setPackage(BridgeStore.TARGET);intent.putExtras(args);intent.putExtra("operation",operation);intent.putExtra("request",id);intent.putExtra("lease",lease);intent.putExtra("reply",result);
  try{Bundle options=BroadcastOptions.makeBasic().setShareIdentityEnabled(true).setDeferralPolicy(BroadcastOptions.DEFERRAL_POLICY_NONE).toBundle();sendBroadcast(intent,null,options);}catch(Exception e){handler.removeCallbacks(deadline);error("Unable to send queue request: "+e.getClass().getSimpleName());}
 }
 private void choose(Bundle result){
  ArrayList<Bundle> hits=result.getParcelableArrayList("songs",Bundle.class);List<SongChoicePolicy.Song> songs=new ArrayList<>();
  if(hits!=null)for(Bundle item:hits)songs.add(new SongChoicePolicy.Song(item.getString("id",""),item.getString("title",""),item.getString("artist","")));

  if(songs.isEmpty()){error("No matching song was returned by MetroList. Queue unchanged.");return;}
  SongChoicePolicy.Song selected=SongChoicePolicy.automatic(ticket.queue,songs);
  if(selected==null){error("No playable song was returned. Queue unchanged.");return;}
  String album="";if(hits!=null)for(Bundle item:hits)if(selected.id.equals(item.getString("id"))){album=item.getString("album","");break;}
  insert(selected.id,selected.title,selected.artist,album);
 }
 private void insert(String id,String title,String artist,String album){
  Bundle args=new Bundle();args.putString("song",id);args.putString("fingerprint",fingerprint);
  request("next",args,10000,result->{
   complete=true;BridgeStore.acknowledge(ticket.routeKey);
   ActionFeedback.success(this,ticket,ConfirmationWording.next(this,title,artist,album,result.getBoolean("shuffle",false),result.getInt("count",-1)));finish();
  });
 }
 private void error(String message){if(failed||destroyed||isFinishing())return;failed=true;handler.removeCallbacksAndMessages(null);BridgeStore.revokeQueueLease(lease);disconnect();VoiceConfirmation.failure(this,ticket);ActionFeedback.failure(this,message);finish();}
 private void disconnect(){MediaBrowser old=browser;browser=null;if(old!=null)try{old.disconnect();}catch(Exception ignored){}}
 private boolean isFinishing(){return destroyed;}
 private void finish(){if(destroyed)return;destroyed=true;handler.removeCallbacksAndMessages(null);BridgeStore.revokeQueueLease(lease);if(ticket!=null&&!complete){VoiceConfirmation.failure(this,ticket);BridgeStore.release(ticket.routeKey);}disconnect();LocalActions.finished(this);}
}
