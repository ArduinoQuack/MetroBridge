package dev.metrobridge.poc;
import java.util.*;
public final class SongChoicePolicy {
 private static final String[] EDITIONS={"karaoke","tribute","cover","instrumental","live","remix","slowed","sped up","nightcore","reverb","clean","censored","tutorial"};
 public static final class Song {public final String id,title,artist;public Song(String id,String title,String artist){this.id=id;this.title=title;this.artist=artist;}}
 public static List<Song> exact(QueueRequest request,List<Song> found){
  LinkedHashMap<String,Song> matches=new LinkedHashMap<>();String title=NameMatcher.normalize(request.title),artist=NameMatcher.normalize(request.artist);
  for(Song song:found)if(NameMatcher.normalize(song.title).equals(title)&&(artist.isEmpty()||NameMatcher.normalize(song.artist).equals(artist)))matches.put(song.id,song);
  return new ArrayList<>(matches.values());
 }
 /** Best plausible result; ties retain MetroList's search order. No popularity is invented. */
 public static Song best(QueueRequest request,List<Song> found){
  String wanted=norm(request.title),artist=artist(request.artist);if(wanted.isEmpty())return null;
  Song best=null;double highest=-Double.MAX_VALUE;Set<String> ids=new HashSet<>();
  for(Song song:found){
   if(song==null||song.id==null||song.id.isEmpty()||song.title==null||!ids.add(song.id))continue;
   String title=norm(song.title),plain=presentation(song.title),version=plain;
   boolean editionRequested=false;for(String marker:EDITIONS)if(word(wanted,marker))editionRequested=true;
   if(!editionRequested)version=presentation(song.title.replaceAll("(?i)\\s*[\\(\\[][^\\)\\]]*\\b(?:live|remix|cover|karaoke|instrumental|slowed|sped up|nightcore|reverb|clean|censored)[^\\)\\]]*[\\)\\]]","")
    .replaceAll("(?i)\\s+[-–—]\\s+(?:live|remix|cover|karaoke|instrumental|slowed|sped up|nightcore|reverb|clean|censored)\\b.*$",""));
   double titleMatch=Math.max(NameMatcher.score(wanted,title),Math.max(NameMatcher.score(wanted,plain),NameMatcher.score(wanted,version)));
   if(titleMatch<0.82||(wanted.length()<=4&&!wanted.equals(title)&&!wanted.equals(plain)&&!wanted.equals(version)))continue;
   String actualArtist=artist(song.artist);double artistMatch=artist.isEmpty()?0:NameMatcher.score(artist,actualArtist);
   if(!artist.isEmpty()){
    if(actualArtist.equals(artist)||actualArtist.startsWith(artist+" feat ")||actualArtist.startsWith(artist+" ft ")||actualArtist.startsWith(artist+" featuring ")||actualArtist.startsWith(artist+" and "))artistMatch=1;
    if(artistMatch<0.82||(artist.length()<=4&&!actualArtist.equals(artist)&&artistMatch<1))continue;
   }
   double score=100*titleMatch+25*artistMatch;
   String metadata=title+" "+norm(song.artist);
   for(String marker:EDITIONS)if(!word(wanted,marker)&&word(metadata,marker))score-=marker.equals("karaoke")||marker.equals("tribute")||marker.equals("cover")?16:9;
   if(score>highest+0.000001){highest=score;best=song;}
  }return best;
 }
 private static String norm(String value){return NameMatcher.normalize(value==null?"":value);}
 /** User-requested no-dialog policy: best match, otherwise first real returned result. */
 public static Song automatic(QueueRequest request,List<Song> found){
  Song selected=best(request,found);if(selected!=null)return selected;
  for(Song song:found)if(song!=null&&song.id!=null&&song.id.matches("[A-Za-z0-9_-]{1,160}")&&song.title!=null&&!song.title.trim().isEmpty())return song;
  return null;
 }
 private static String artist(String value){return norm(value).replaceFirst("^the ","").replaceAll("\\b(?:topic|official)\\b","").trim().replaceAll("\\s+"," ");}
 private static String presentation(String value){return norm(value).replaceAll("\\b(?:official music video|official video|official audio|lyric video|lyrics|visualizer|hd|4k)\\b","").trim().replaceAll("\\s+"," ");}
 private static boolean word(String text,String phrase){return (" "+text+" ").contains(" "+phrase+" ");}
 private SongChoicePolicy(){}
}
