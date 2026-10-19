import dev.metrobridge.poc.ConfirmationGate;
import dev.metrobridge.poc.PlaybackWords;
public final class ConfirmationGateTest {
 static int n;static void c(boolean b){n++;if(!b)throw new AssertionError("confirmation "+n);}
 public static void main(String[] args){
  ConfirmationGate g=new ConfirmationGate();
  c(g.capture("reply","a",0).equals("pass"));
  g.begin("route","reply",100);c(g.capture("other","a",101).equals("pass"));
  c(g.capture("reply","capture",102).equals("pending"));c(g.capture("reply","other",103).equals("duplicate"));
  c(g.take("route","reply","capture",104).isEmpty());c(!g.finish("wrong","Wrong song",104));
  c(g.finish("route","Playing Rap",105));c(g.state("route","reply","capture",106).equals("success"));
  c(g.take("wrong","reply","capture",106).isEmpty());c(g.take("route","other","capture",106).isEmpty());c(g.take("route","reply","other",106).isEmpty());
  c(g.take("route","reply","capture",107).equals("Playing Rap"));c(g.take("route","reply","capture",108).isEmpty());c(g.capture("reply","again",108).equals("duplicate"));
  c(g.ownsClaim("route","reply","capture",109));c(!g.ownsClaim("other","reply","capture",109));c(!g.ownsClaim("route","other","capture",109));c(!g.ownsClaim("route","reply","wrong",109));c(!g.ownsClaim("route","reply","capture",60100));
  g.begin("r2","response2",200);c(!g.ready("response2",200));c(!g.ownsClaim("r2","response2","b",200));c(g.finish("r2","Playing Song by Artist next",201));c(g.ready("response2",202));c(!g.ready("wrong",202));c(g.capture("response2","b",202).equals("success"));c(!g.ready("response2",202));c(g.take("r2","response2","b",203).equals("Playing Song by Artist next"));
  g.begin("r3","response3",300);g.capture("response3","c",301);g.fail("r3");c(!g.finish("r3","False success",302));c(g.state("r3","response3","c",302).equals("cancel"));
  g.begin("r4","response4",400);g.capture("response4","d",401);g.turn("response4");c(g.state("r4","response4","d",402).equals("pending"));g.turn("unrelated turn");c(g.state("r4","response4","d",403).equals("cancel"));
  g.begin("r5","response5",500);c(g.finish("r5","Playing Rap",600));c(g.capture("response5","e",60500).equals("pass"));c(!g.finish("r5","Playing late",60500));
  g.begin("r6","response6",700);g.clear();c(g.capture("response6","f",701).equals("pass"));
  g.begin("r7","response7",800);g.begin("r8","response8",801);c(!g.finish("r7","Old success",802));
  g.begin("r9","",900);c(g.capture("","g",901).equals("pass"));
  c(PlaybackWords.next("Lose Yourself","Eminem").equals("Playing Lose Yourself by Eminem next"));
  c(PlaybackWords.next("Song",null).equals("Playing Song next"));c(PlaybackWords.next(" Song\nTitle "," Artist\tName ").equals("Playing Song Title by Artist Name next"));
  c(PlaybackWords.next("Song"," ").equals("Playing Song next"));c(PlaybackWords.playlist("Rap").equals("Playing Rap"));
  System.out.println(n+" success/reply correlation, cancellation, duplicate and artist wording checks passed");
 }
}
