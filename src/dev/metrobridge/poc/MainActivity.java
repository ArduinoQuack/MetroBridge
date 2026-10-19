package dev.metrobridge.poc;
import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.browse.MediaBrowser;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.text.*;
/** Settings only: no launcher, diagnostic collection, test controls or media transport. */
public final class MainActivity extends Activity {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private MediaBrowser browser;private boolean visible,refreshing,dark;
 private int foreground,secondary,panel,refreshGeneration;private TextView libraryStatus,problem;private Button refresh;private LinearLayout wordingEditors;
 @Override public void onCreate(Bundle state){
  super.onCreate(state);BridgeStore.initializeSettings(this);
  dark=(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
  foreground=dark?0xffecf4ef:0xff15251e;secondary=dark?0xffa6b9ad:0xff617268;panel=dark?0xff1b2720:Color.WHITE;
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(dark?0xff111a15:0xfff3f7f4);
  LinearLayout content=column();content.setFocusableInTouchMode(true);content.requestFocus();content.setPadding(dp(22),dp(22),dp(22),dp(28));scroll.addView(content);
  if(Build.VERSION.SDK_INT>=30)scroll.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
  TextView heading=text("MetroBridge",30,foreground);heading.setTypeface(null,Typeface.BOLD);content.addView(heading);
  TextView subtitle=text("Your music, by voice.",16,secondary);subtitle.setPadding(0,dp(8),0,dp(24));content.addView(subtitle);
  LinearLayout main=card(content);setting(main,"Enable integration","Let Gemini control your MetroList music.","enabled",false,false);
  section(content,"Playback");LinearLayout playback=card(content);
  setting(playback,"Shuffle playlists","Start requested playlists in random order.","shuffle_default",false,false);divider(playback);
  setting(playback,"Play next","Queue a requested song without replacing your music.","queue_enabled",false,true);divider(playback);
  setting(playback,"Spoken confirmations","Gemini voice: playlist name, or song and artist played next.","speak_confirmations",true,false);
  section(content,"Gemini");LinearLayout gemini=card(content);
  setting(gemini,"Allow normal screen timeout","Let Gemini use your phone’s normal screen timeout.","normal_screen_timeout",false,false);
  section(content,"Confirmation wording");LinearLayout wording=card(content);
  Switch custom=setting(wording,"Custom wording","Use your own playlist and Play next confirmations.","custom_confirmations",false,false);
  LinearLayout editors=column();wording.addView(editors);editors.setVisibility(custom.isChecked()?View.VISIBLE:View.GONE);
  custom.setOnClickListener(v->editors.setVisibility(custom.isChecked()?View.VISIBLE:View.GONE));
  // Row taps also toggle the switch, so visibility tracks the saved preference on every change.
  wordingEditors=editors;
  template(editors,"Playlist confirmation","playlist_template",ConfirmationTemplate.PLAYLIST,false);
  template(editors,"Play next confirmation","next_template",ConfirmationTemplate.NEXT,true);
  TextView variables=text("Variables: {playlist}, {song}, {artist} (or {author}), {album}, {title}, {action}, {position}, {queue_size}, {shuffle}, {app}.\n{by_artist} adds ‘ by Artist’ only when known. {from_album} adds ‘ from Album’ only when known. Missing metadata stays empty. Playlist requests provide playlist/title, shuffle, action and app.",13,secondary);variables.setPadding(dp(18),dp(4),dp(18),dp(18));editors.addView(variables);
  section(content,"Library");LinearLayout library=card(content);
  libraryStatus=text("",15,secondary);libraryStatus.setPadding(dp(18),dp(18),dp(18),dp(8));library.addView(libraryStatus);updateLibrary();
  refresh=new Button(this);refresh.setText("Refresh playlists");refresh.setAllCaps(false);refresh.setOnClickListener(v->refreshLibrary());
  LinearLayout.LayoutParams button=new LinearLayout.LayoutParams(-1,dp(52));button.setMargins(dp(12),0,dp(12),dp(10));library.addView(refresh,button);
  TextView note=text("After adding or renaming playlists in MetroList, refresh them here.",13,secondary);note.setPadding(dp(4),dp(12),dp(4),0);content.addView(note);
  problem=text("",13,secondary);problem.setPadding(dp(4),dp(16),dp(4),0);content.addView(problem);
  TextView version=text("MetroBridge 0.21",12,secondary);version.setPadding(dp(4),dp(26),0,0);content.addView(version);
  setContentView(scroll);BridgeStore.grantGoogleVisibility(this);
 }

 private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);return t;}
 private LinearLayout card(LinearLayout parent){LinearLayout l=column();GradientDrawable shape=new GradientDrawable();shape.setColor(panel);shape.setCornerRadius(dp(20));l.setBackground(shape);parent.addView(l,new LinearLayout.LayoutParams(-1,-2));return l;}
 private void section(LinearLayout parent,String name){TextView t=text(name,14,secondary);t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(4),dp(26),0,dp(10));parent.addView(t);}
 private void divider(LinearLayout parent){View line=new View(this);line.setBackgroundColor(dark?0xff2c3b31:0xffe6eee8);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(1));p.setMargins(dp(18),0,dp(18),0);parent.addView(line,p);}
 private Switch setting(LinearLayout parent,String title,String description,String key,boolean defaultValue,boolean queue){
  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(18),dp(16),dp(12),dp(16));
  LinearLayout labels=column();TextView name=text(title,16,foreground);name.setTypeface(null,Typeface.BOLD);labels.addView(name);TextView detail=text(description,13,secondary);detail.setPadding(0,dp(5),dp(12),0);labels.addView(detail);row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
  Switch toggle=new Switch(this);toggle.setContentDescription(title);toggle.setMinHeight(dp(48));toggle.setMinWidth(dp(52));toggle.setChecked(BridgeStore.prefs(this).getBoolean(key,defaultValue));row.addView(toggle);
  row.setOnClickListener(v->toggle.setChecked(!toggle.isChecked()));
  toggle.setOnCheckedChangeListener((button,checked)->{android.content.SharedPreferences.Editor edit=BridgeStore.prefs(this).edit().putBoolean(key,checked);if(queue)edit.putBoolean("live_queue",checked);edit.apply();if("custom_confirmations".equals(key)&&wordingEditors!=null)wordingEditors.setVisibility(checked?View.VISIBLE:View.GONE);if((key.equals("enabled")||key.equals("speak_confirmations"))&&!checked)ActionFeedback.stop();VoiceConfirmation.signal(this);BridgeStore.grantGoogleVisibility(this);if("enabled".equals(key)){if(checked&&BridgeStore.library(this).isEmpty())refreshLibrary();else if(!checked)disconnect();}});
  parent.addView(row,new LinearLayout.LayoutParams(-1,-2));return toggle;
 }
 private void template(LinearLayout parent,String title,String key,String fallback,boolean next){
  TextView label=text(title,15,foreground);label.setTypeface(null,Typeface.BOLD);label.setPadding(dp(18),dp(14),dp(18),dp(4));parent.addView(label);
  EditText input=new EditText(this);input.setSingleLine(false);input.setMinLines(2);input.setTextColor(foreground);input.setTextSize(15);input.setContentDescription(title);input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(300)});
  LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.setMargins(dp(14),0,dp(14),0);parent.addView(input,params);
  TextView preview=text("",13,secondary);preview.setPadding(dp(18),dp(4),dp(18),dp(14));parent.addView(preview);
  input.setText(BridgeStore.prefs(this).getString(key,fallback));
  Runnable update=()->{String value=input.getText().toString(),error=ConfirmationTemplate.validate(value);if(error.isEmpty()){BridgeStore.prefs(this).edit().putString(key,value).apply();preview.setTextColor(secondary);preview.setText("Preview: "+ConfirmationTemplate.render(value,next,next?"":"Rap",next?"Lose Yourself":"",next?"Eminem":"",next?"8 Mile":"",true));}else{preview.setTextColor(dark?0xffffb4ab:0xffa12d27);preview.setText(error+" Last valid wording stays saved.");}};
  input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence t,int start,int count,int after){}public void onTextChanged(CharSequence t,int start,int before,int count){}public void afterTextChanged(Editable text){update.run();}});update.run();
 }
 private void updateLibrary(){int count=BridgeStore.library(this).size();libraryStatus.setText(count==0?"Refresh your playlists to get started.":count+(count==1?" playlist available":" playlists available"));}
 @Override protected void onStart(){super.onStart();visible=true;String issue=BridgeStore.prefs(this).getString("last_problem","");problem.setText(issue.isEmpty()?"":"Last request: "+issue);problem.setVisibility(issue.isEmpty()?View.GONE:View.VISIBLE);if(BridgeStore.prefs(this).getBoolean("enabled",false)&&BridgeStore.library(this).isEmpty())refreshLibrary();}
 @Override protected void onStop(){visible=false;disconnect();super.onStop();}
 private void refreshLibrary(){
  if(!visible||refreshing)return;if(!BridgeStore.compatibleMetro(this)){libraryStatus.setText("This MetroList version is not supported by this build.");return;}
  refreshing=true;final int request=++refreshGeneration;refresh.setEnabled(false);libraryStatus.setText("Refreshing playlists…");handler.postDelayed(()->{if(request==refreshGeneration)finishRefresh("Couldn’t refresh. Open MetroList and try again.");},12000);
  try{browser=new MediaBrowser(this,new ComponentName(BridgeStore.TARGET,"com.metrolist.music.playback.MusicService"),new MediaBrowser.ConnectionCallback(){
   @Override public void onConnected(){if(!current(request))return;try{browser.subscribe("playlist",new MediaBrowser.SubscriptionCallback(){
    @Override public void onChildrenLoaded(String parent,List<MediaBrowser.MediaItem> items){if(!current(request))return;try{ArrayList<RoutePolicy.Entry> entries=new ArrayList<>();for(MediaBrowser.MediaItem item:items)if(item.isBrowsable()&&item.getDescription().getTitle()!=null)entries.add(new RoutePolicy.Entry(item.getDescription().getTitle().toString(),item.getMediaId()));BridgeStore.cache(MainActivity.this,entries);finishRefresh(null);}catch(Exception e){finishRefresh("Couldn’t refresh. Open MetroList and try again.");}}
    @Override public void onError(String parent){if(!current(request))return;finishRefresh("Couldn’t refresh. Open MetroList and try again.");}
   });}catch(Exception e){finishRefresh("Couldn’t refresh. Open MetroList and try again.");}}
   @Override public void onConnectionFailed(){if(!current(request))return;finishRefresh("Couldn’t connect. Open MetroList and try again.");}
   @Override public void onConnectionSuspended(){if(!current(request))return;finishRefresh("The connection ended. Try refreshing again.");}
  },null);browser.connect();}catch(Exception e){finishRefresh("Couldn’t connect. Open MetroList and try again.");}
 }
 private void finishRefresh(String error){if(!refreshing)return;disconnect();if(visible){if(error==null)updateLibrary();else libraryStatus.setText(error);}}
 private boolean current(int request){return visible&&refreshing&&browser!=null&&request==refreshGeneration;}
 private void disconnect(){refreshGeneration++;handler.removeCallbacksAndMessages(null);refreshing=false;if(refresh!=null)refresh.setEnabled(true);MediaBrowser old=browser;browser=null;if(old!=null)try{old.disconnect();}catch(Exception ignored){}}
}
