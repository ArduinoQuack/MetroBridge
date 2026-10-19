import dev.metrobridge.poc.PlaylistRequest;
import kotlin.text.StringsKt;
import java.util.List;

public class PlaylistRequestTest {
 public static void main(String[] args) {
  String[] realContainers={"playlist/LPGrpXVrAD","playlist/LPcfWKWZZU","playlist/LP_LIKED","playlist/LP_DOWNLOADED"};
  for(String folder:realContainers) {
   List<String> normal=StringsKt.split(PlaylistRequest.forContainer(folder,false),new String[]{"/"},false,0);
   check(normal.size()==3&&normal.get(0).equals("playlist")&&normal.get(1).equals(folder.substring(9))&&normal.get(2).isEmpty());
   List<String> shuffle=StringsKt.split(PlaylistRequest.forContainer(folder,true),new String[]{"/"},false,0);
   check(shuffle.size()==3&&shuffle.get(2).equals("__shuffle__"));
  }
  for(String bad:new String[]{null,"playlist","playlist/","playlist/x/song","search/rap","playlist/has space"}) {
   boolean refused=false;try{PlaylistRequest.forContainer(bad,false);}catch(IllegalArgumentException e){refused=true;}check(refused);
  }
  System.out.println("14 protocol checks passed against Kotlin splitting semantics");
 }
 static void check(boolean ok){if(!ok)throw new AssertionError();}
}
