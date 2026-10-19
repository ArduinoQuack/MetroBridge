package dev.metrobridge.poc;
import java.util.*;import java.util.regex.*;
/** Text-only, allowlisted variables. No scripting, network requests or fabricated metadata. */
public final class ConfirmationTemplate {
 public static final String PLAYLIST="Playing {playlist}",NEXT="Playing {song}{by_artist} next";
 private static final Set<String> KEYS=new HashSet<>(Arrays.asList("playlist","song","artist","author","album","title","action","position","shuffle","app","by_artist","from_album","queue_size"));
 private static final Pattern VARIABLE=Pattern.compile("\\{([a-z_]+)\\}");
 public static String validate(String template){
  if(template==null||template.trim().isEmpty())return "Enter some wording.";
  if(template.length()>300)return "Use at most 300 characters.";
  Matcher m=VARIABLE.matcher(template);StringBuffer rest=new StringBuffer();
  while(m.find()){if(!KEYS.contains(m.group(1)))return "Unknown variable: "+m.group()+".";m.appendReplacement(rest,"");}m.appendTail(rest);
  return rest.indexOf("{")>=0||rest.indexOf("}")>=0?"Check the variable braces.":"";
 }
 public static String render(String template,boolean next,String playlist,String song,String artist,String album,boolean shuffled){
  return render(template,next,playlist,song,artist,album,shuffled,-1);
 }
 public static String render(String template,boolean next,String playlist,String song,String artist,String album,boolean shuffled,int queueSize){
  if(!validate(template).isEmpty())template=next?NEXT:PLAYLIST;
  String p=clean(playlist),s=clean(song),a=clean(artist),b=clean(album);if(!next&&p.isEmpty())p="your playlist";if(next&&s.isEmpty())s="this song";Map<String,String> values=new HashMap<>();
  values.put("playlist",p);values.put("song",s);values.put("artist",a);values.put("author",a);values.put("album",b);values.put("title",next?s:p);
  values.put("action",next?"Playing next":"Playing");values.put("position",next?"next":"");values.put("shuffle",shuffled?"shuffled":"in order");values.put("app","MetroList");
  values.put("queue_size",queueSize<0?"":Integer.toString(queueSize));
  values.put("by_artist",a.isEmpty()?"":" by "+a);values.put("from_album",b.isEmpty()?"":" from "+b);
  Matcher m=VARIABLE.matcher(template);StringBuffer text=new StringBuffer();while(m.find())m.appendReplacement(text,Matcher.quoteReplacement(values.get(m.group(1))));m.appendTail(text);
  String out=text.toString().replaceAll("[\\p{Cntrl}]+"," ").trim().replaceAll("\\s+"," ");
  if(out.isEmpty())out=next?PlaybackWords.next(song,artist):PlaybackWords.playlist(playlist);
  return out.substring(0,Math.min(400,out.length()));
 }
 private static String clean(String v){if(v==null)return "";String s=v.replaceAll("[\\p{Cntrl}]+"," ").trim().replaceAll("\\s+"," ");return s.substring(0,Math.min(160,s.length()));}
 private ConfirmationTemplate(){}
}
