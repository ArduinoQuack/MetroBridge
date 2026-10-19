import dev.metrobridge.poc.NextSkipGuard;import dev.metrobridge.poc.QueueRequest;
public final class NextSkipGuardTest {
 static int n;static void c(boolean b){n++;if(!b)throw new AssertionError("skip guard "+n);}
 public static void main(String[] args){
  NextSkipGuard g=new NextSkipGuard();c(!g.block(0));g.accept("old",0);c(!g.block(1));g.turn("music");c(!g.block(1));g.accept("music",10);c(g.block(10));c(g.block(11));g.turn("music");c(g.block(12));
  g.turn("ordinary next request");c(!g.block(13));g.accept("music",14);c(!g.block(15));g.accept("ordinary next request",20);c(g.block(20));c(!g.block(19));c(!g.block(60020));
  g.turn(null);c(!g.block(30));g.accept("",31);c(!g.block(32));
  for(String q:new String[]{"play next song","play the next song","Play the next track!","please play next","next song","skip this song"})c(QueueRequest.parse(q)==null);
  c(QueueRequest.parse("Play Lose Yourself by Eminem next")!=null);c(QueueRequest.parse("Play this song next").current);c(QueueRequest.parse("Play Next next").title.equals("Next"));
  System.out.println(n+" accepted-turn skip suppression and ordinary-next preservation checks passed");
 }
}
