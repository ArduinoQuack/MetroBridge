import dev.metrobridge.poc.*;
import java.util.*;
public class RoutePolicyTest {
 static final List<RoutePolicy.Entry> LIB=Arrays.asList(new RoutePolicy.Entry("Rap","playlist/R"),new RoutePolicy.Entry("Pop","playlist/P"),new RoutePolicy.Entry("Liked songs","playlist/LP_LIKED"),new RoutePolicy.Entry("Rock and Roll","playlist/A"));
 static int count;
 static void check(boolean ok){count++;if(!ok)throw new AssertionError("case "+count);}
 static RoutePolicy.Decision route(String q){return RoutePolicy.resolve(q,"","","",true,LIB);}
 public static void main(String[] args){
  check(route("Play my rap playlist").action.equals("play"));
  check(route("Play my rap playlist").choices.get(0).id.equals("playlist/R"));
  check(route("Shuffle my rap playlist").shuffled);
  check(route("Play my pop playlist shuffled").shuffled);
  check(route("Play my pop playlist in random order").shuffled);
  check(route("rap").action.equals("play"));
  check(route("Play my liked songs").action.equals("play"));
  check(route("Play my Rock and Roll playlist").action.equals("play"));
  check(route("Shuffle my rap playlist and play Lose Yourself next").action.equals("pass"));
  check(route("Play Lose Yourself next").action.equals("pass"));
  check(route("Add Rap to my queue").action.equals("pass"));
  check(route("Pause music").action.equals("pass"));
  check(route("Play Blinding Lights").action.equals("pass"));
  check(RoutePolicy.resolve("rap","","vnd.android.cursor.item/audio","Rap",true,LIB).action.equals("pass"));
  check(RoutePolicy.resolve("playlist","","vnd.android.cursor.item/audio","Playlist",true,LIB).action.equals("pass"));
  check(RoutePolicy.resolve("anything","Rap","vnd.android.cursor.item/playlist","",true,LIB).action.equals("play"));
  check(route("Play my raps playlist").action.equals("choose"));
  check(!route("Play my raps playlist").choices.isEmpty());
  check(RoutePolicy.resolve("Rap","","","",false,LIB).action.equals("pass"));
  List<RoutePolicy.Entry> two=Arrays.asList(new RoutePolicy.Entry("Rap!","playlist/a"),new RoutePolicy.Entry("RAP","playlist/b"));
  check(RoutePolicy.resolve("Play my rap playlist","","","",true,two).action.equals("choose"));
  check(route("Hey Google, play my rap playlist on YouTube Music").action.equals("play"));
  check(route("Hey Google, shuffle my rap playlist").shuffled);
  check(RoutePolicy.resolve("Shuffled","Shuffled","playlist","",true,Arrays.asList(new RoutePolicy.Entry("Shuffled","playlist/S"))).shuffled==false);
  check(route("play my missingzzzzz playlist").action.equals("choose"));
  check(route("Play my rap playlist").shuffleFor(true,"Play my rap playlist"));
  check(!route("Play my rap playlist").shuffleFor(false,"Play my rap playlist"));
  check(!route("Play my rap playlist in order").shuffleFor(true,"Play my rap playlist in order"));
  check(route("Play my rap playlist in order").action.equals("play"));
  check(!route("Play my rap playlist without shuffle").shuffleFor(true,"Play my rap playlist without shuffle"));
  check(route("Play my rap playlist without shuffle").action.equals("play"));
  List<RoutePolicy.Entry> israeli=Arrays.asList(new RoutePolicy.Entry("Isreali","playlist/I"));
  RoutePolicy.Decision spelling=RoutePolicy.resolve("Play my Israeli playlist","","","",true,israeli);
  check(spelling.choices.size()==1&&spelling.requiresConfirmation());
  check(!route("Play my rap playlist").requiresConfirmation());
  System.out.println(count+" routing policy checks passed");
 }
}
