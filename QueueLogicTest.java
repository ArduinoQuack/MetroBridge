import dev.metrobridge.poc.*;
import java.util.*;
public final class QueueLogicTest {
 static int checks;
 static void check(boolean value){checks++;if(!value)throw new AssertionError("queue check "+checks);}
 static QueueSnapshot q(String items,String order,String current,String next,int index,boolean shuffle,int repeat,boolean playing,long position){return new QueueSnapshot(Arrays.asList(items.split(",")),Arrays.asList(order.split(",")),current,next,index,shuffle,repeat,playing,position);}
 public static void main(String[] args){
  QueueRequest r=QueueRequest.parse("Hey Google, play Lose Yourself by Eminem next.");
  check(r!=null&&r.title.equals("Lose Yourself")&&r.artist.equals("Eminem")&&r.error.isEmpty());
  for(String text:new String[]{"Play Blinding Lights next","Please add Blinding Lights up next!","Could you queue Blinding Lights next?"}){r=QueueRequest.parse(text);check(r!=null&&r.title.equals("Blinding Lights")&&r.error.isEmpty());}
  for(String text:new String[]{"Play this song next","Add this track next","Queue the current song next","Play it next"})check(QueueRequest.parse(text).current);
  for(String text:new String[]{"Play my rap playlist","Turn on shuffle","Pause music","Add this song to my queue","What's playing next?"})check(QueueRequest.parse(text)==null);
  check(QueueRequest.parse("Shuffle my rap playlist and play Lose Yourself next").error.length()>0);
  check(QueueRequest.parse("Play this album next").error.length()>0);
  check(QueueRequest.parse(null)==null);
  r=QueueRequest.parse("Play Lose Yourself by Eminem next");
  SongChoicePolicy.Song a=new SongChoicePolicy.Song("a","Lose Yourself","Eminem"),b=new SongChoicePolicy.Song("b","Lose Yourself","Cover Artist"),c=new SongChoicePolicy.Song("c","Lose Yourself (Live)","Eminem"),d=new SongChoicePolicy.Song("d","Lose Yourself","Eminem");
  check(SongChoicePolicy.exact(r,Arrays.asList(a,b,c)).size()==1);
  check(SongChoicePolicy.exact(r,Arrays.asList(a,d)).size()==2);
  check(SongChoicePolicy.exact(r,Arrays.asList(a,a)).size()==1);
  check(SongChoicePolicy.exact(r,Arrays.asList(b,c)).isEmpty());
  check(SongChoicePolicy.exact(QueueRequest.parse("Play Lose Yourself next"),Arrays.asList(a,b)).size()==2);
  check(SongChoicePolicy.exact(QueueRequest.parse("Play LOSE YOURSELF! by EMINEM next"),Arrays.asList(a)).size()==1);
  QueueSnapshot before=q("a,b,c","a,b,c","a","b",0,false,0,true,10000),after=q("a,x,b,c","a,x,b,c","a","x",0,false,0,true,10030);
  check(QueueSnapshot.check(before,after,"x").equals("preserved"));
  QueueSnapshot shuffled=q("a,b,c,d","c,a,d,b","a","d",0,true,2,true,10000),inserted=q("a,x,b,c,d","c,a,x,d,b","a","x",0,true,2,true,10020);
  check(QueueSnapshot.check(shuffled,inserted,"x").equals("preserved"));
  check(QueueSnapshot.check(shuffled,q("a,x,b,c,d","c,a,x,b,d","a","x",0,true,2,true,10020),"x").equals("existing-traversal-changed"));
  check(QueueSnapshot.check(before,q("a,x,c","a,x,c","a","x",0,false,0,true,10020),"x").equals("existing-queue-changed"));
  check(QueueSnapshot.check(before,q("a,x,b,c","a,x,b,c","a","b",0,false,0,true,10020),"x").equals("requested-song-not-next"));
  check(QueueSnapshot.check(before,q("a,x,b,c","a,x,b,c","x","b",1,false,0,true,10020),"x").equals("current-track-changed"));
  check(QueueSnapshot.check(before,q("a,x,b,c","a,x,b,c","a","x",0,true,0,true,10020),"x").equals("playback-mode-changed"));
  check(QueueSnapshot.check(before,q("a,x,b,c","a,x,b,c","a","x",0,false,1,true,10020),"x").equals("playback-mode-changed"));
  check(QueueSnapshot.check(before,q("a,x,b,c","a,x,b,c","a","x",0,false,0,false,10020),"x").equals("playback-mode-changed"));
  check(QueueSnapshot.check(before,q("a,x,b,c","a,x,b,c","a","x",0,false,0,true,0),"x").equals("playback-position-changed"));
  check(QueueSnapshot.check(q("a,b,c","a,b,c","a","b",0,false,1,false,10000),q("a,x,b,c","a,x,b,c","a","x",0,false,1,false,10000),"x").equals("preserved"));
  check(QueueSnapshot.check(q("a,x,b,c","a,x,b,c","a","x",0,false,0,true,10000),after,"x").equals("preserved"));
  check(QueueSnapshot.check(before,q("a,a,b,c","a,a,b,c","a","a",0,false,0,true,10020),"a").equals("preserved"));
  check(!before.fingerprint().equals(after.fingerprint()));
  check(!before.fingerprint().equals(q("a,b,c","a,c,b","a","c",0,true,0,true,10000).fingerprint()));
  check(before.fingerprint().equals(q("a,b,c","a,b,c","a","b",0,false,0,false,12000).fingerprint()));
  check(!before.fingerprint().equals(q("a,b,c","a,b,c","a","b",0,false,2,true,10000).fingerprint()));
  System.out.println(checks+" queue parsing, ambiguity and state-preservation checks passed");
 }
}
