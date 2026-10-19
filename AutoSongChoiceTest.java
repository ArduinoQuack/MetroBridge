import dev.metrobridge.poc.*;
import java.util.*;
public final class AutoSongChoiceTest {
 static int checks;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("automatic song choice "+checks);}
 static SongChoicePolicy.Song s(String id,String title,String artist){return new SongChoicePolicy.Song(id,title,artist);}
 static SongChoicePolicy.Song pick(String request,SongChoicePolicy.Song... songs){return SongChoicePolicy.best(QueueRequest.parse(request),Arrays.asList(songs));}
 public static void main(String[] args){
  SongChoicePolicy.Song normal=s("normal","Armed and Dangerous","Juice WRLD"),alternate=s("alternate","Armed and Dangerous","Juice WRLD");
  check(pick("Play Armed and Dangerous next",normal,alternate)==normal);
  check(pick("Play Armed and Dangerous next",alternate,normal)==alternate);
  check(pick("Play Armed and Dangerous by Juice WRLD next",s("cover","Armed and Dangerous","Cover Artist"),normal)==normal);
  check(pick("Play Armed and Dangerous by Juice WRLD next",s("cover","Armed and Dangerous","Cover Artist"))==null);
  check(pick("Play Armed and Dangerous next",s("live","Armed and Dangerous (Live at London)","Juice WRLD"),normal)==normal);
  check(pick("Play Armed and Dangerous next",s("cover","Armed and Dangerous (Cover)","Cover Artist"),normal)==normal);
  check(pick("Play Armed and Dangerous next",s("karaoke","Armed and Dangerous","Karaoke Artist"),normal)==normal);
  check(pick("Play Armed and Dangerous next",s("remix","Armed and Dangerous - Remix","Juice WRLD"),normal)==normal);
  check(pick("Play Armed and Dangerous next",s("slow","Armed and Dangerous (Slowed + Reverb)","Juice WRLD"),normal)==normal);
  SongChoicePolicy.Song official=s("official","Armed and Dangerous (Official Audio)","Juice WRLD - Topic");
  check(pick("Play Armed and Dangerous by Juice WRLD next",official)==official);
  check(pick("Play Armed and Dangerus next",normal)==normal);
  check(pick("Play completely unrelated next",normal)==null);
  check(pick("Play One next",s("more","One More Time","Artist"))==null);
  check(pick("Play One next",s("one","One","Artist")).id.equals("one"));
  SongChoicePolicy.Song remix=s("remix","Armed and Dangerous Remix","Juice WRLD");
  check(pick("Play Armed and Dangerous Remix next",normal,remix)==remix);
  SongChoicePolicy.Song live=s("live","Armed and Dangerous (Live)","Juice WRLD");
  check(pick("Play Armed and Dangerous Live next",normal,live)==live);
  check(pick("Play Armed and Dangerous next",live)==live);
  check(pick("Play Forever next",s("wrong","Live Forever","Artist"))==null);
  check(pick("Play Armed and Dangerous next")==null);
  check(pick("Play Armed and Dangerous next",s("","Armed and Dangerous","Artist"))==null);
  check(pick("Play Armed and Dangerous next",s("bad",null,"Artist"))==null);
  check(pick("Play Armed and Dangerous by Someone Else next",normal)==null);
  check(pick("Play Armed and Dangerous by Juice WRLD next",s("feat","Armed and Dangerous","Juice WRLD feat Other"))!=null);
  check(pick("Play Blinding Lights by The Weeknd next",s("weeknd","Blinding Lights","Weeknd"))!=null);
  check(pick("Play Armed and Dangerous next",normal,normal,alternate)==normal);
  check(pick("Play Armed and Dangerous next",null,normal)==normal);
  System.out.println(checks+" automatic song-ranking and no-unrelated-fallback checks passed");
 }
}
