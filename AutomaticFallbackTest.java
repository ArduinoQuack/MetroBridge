import dev.metrobridge.poc.*;
import java.util.*;
public final class AutomaticFallbackTest {
 static int checks;static void check(boolean b){checks++;if(!b)throw new AssertionError("fallback/wording "+checks);}
 static SongChoicePolicy.Song s(String id,String title){return new SongChoicePolicy.Song(id,title,"Artist");}
 static SongChoicePolicy.Song pick(SongChoicePolicy.Song... hits){return SongChoicePolicy.automatic(QueueRequest.parse("Play Lose Yourself next"),Arrays.asList(hits));}
 public static void main(String[] args){
  SongChoicePolicy.Song first=s("first","Other song"),best=s("best","Lose Yourself");
  check(pick(first,best)==best);check(pick(first,s("second","Another song"))==first);
  check(pick(null,s("","Empty ID"),s("bad/id","Bad ID"),s("blank","  "),first)==first);
  check(pick()==null);check(pick(null,s("bad id","Invalid"))==null);
  check(pick(s("ok",null))==null);check(pick(s("good-id_123","First valid")).id.equals("good-id_123"));
  check(PlaybackWords.playlist("Rap").equals("Playing Rap"));check(PlaybackWords.next("Lose Yourself").equals("Playing Lose Yourself next"));
  check(PlaybackWords.playlist("Next").equals("Playing Next"));check(PlaybackWords.next(null).equals("Playing this song next"));
  check(PlaybackWords.playlist(null).equals("Playing your playlist"));check(PlaybackWords.playlist("  ").equals("Playing your playlist"));
  check(PlaybackWords.next(" A\nB\tC ").equals("Playing A B C next"));
  char[] longName=new char[200];Arrays.fill(longName,'x');check(PlaybackWords.playlist(new String(longName)).length()==168);
  System.out.println(checks+" automatic fallback and confirmation wording checks passed");
 }
}
