import dev.metrobridge.poc.ConfirmationTemplate;
public final class ConfirmationTemplateTest {
 static int n;static void c(boolean b){n++;if(!b)throw new AssertionError("wording "+n);}
 static String song(String t,String artist,String album){return ConfirmationTemplate.render(t,true,"","Lose Yourself",artist,album,true);}
 public static void main(String[] args){
  c(ConfirmationTemplate.validate("Playing {song}{by_artist} next").isEmpty());c(!ConfirmationTemplate.validate("{missing}").isEmpty());c(!ConfirmationTemplate.validate("{song").isEmpty());c(!ConfirmationTemplate.validate("song}").isEmpty());c(!ConfirmationTemplate.validate("{Song}").isEmpty());c(!ConfirmationTemplate.validate("").isEmpty());c(!ConfirmationTemplate.validate(null).isEmpty());
  c(song(ConfirmationTemplate.NEXT,"Eminem","8 Mile").equals("Playing Lose Yourself by Eminem next"));
  c(song(ConfirmationTemplate.NEXT,null,null).equals("Playing Lose Yourself next"));c(song("Queued {song} by {author}","Eminem","").equals("Queued Lose Yourself by Eminem"));
  c(song("{song}{from_album}","Eminem","8 Mile").equals("Lose Yourself from 8 Mile"));c(song("{song}{from_album}",null,null).equals("Lose Yourself"));
  c(song("{title} | {action} | {position} | {shuffle} | {app}","Eminem","").equals("Lose Yourself | Playing next | next | shuffled | MetroList"));
  c(ConfirmationTemplate.render("{playlist} on {app}, {shuffle}",false,"Rap","","","",false).equals("Rap on MetroList, in order"));
  c(song("{playlist}",null,null).equals("Playing Lose Yourself next"));c(song("{unknown}","Eminem",null).equals("Playing Lose Yourself by Eminem next"));
  c(song("{song}: {artist}","$1 \\ {song}",null).equals("Lose Yourself: $1 \\ {song}"));c(song(" {song}\n{artist} "," A\tB ","").equals("Lose Yourself A B"));
  c(ConfirmationTemplate.render("{title}",false,"Israeli","","","",true).equals("Israeli"));
  c(!ConfirmationTemplate.validate(new String(new char[301]).replace('\0','x')).isEmpty());
  c(song("{artist}{artist}{artist}",new String(new char[160]).replace('\0','x'),null).length()==400);
  c(ConfirmationTemplate.render("{queue_size}",true,"","Song","Artist","",false,42).equals("42"));
  c(ConfirmationTemplate.render("Queue {queue_size}",true,"","Song","Artist","",false).equals("Queue"));
  c(ConfirmationTemplate.render(ConfirmationTemplate.PLAYLIST,false,null,"","","",false).equals("Playing your playlist"));
  c(ConfirmationTemplate.render(ConfirmationTemplate.NEXT,true,"",null,null,null,false).equals("Playing this song next"));
  System.out.println(n+" custom template validation, escaping, metadata and optional-variable checks passed");
 }
}
