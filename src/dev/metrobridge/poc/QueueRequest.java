package dev.metrobridge.poc;
import java.util.*;
import java.util.regex.*;
public final class QueueRequest {
 public final String title,artist,error;public final boolean current;
 private QueueRequest(String title,String artist,boolean current,String error){this.title=title;this.artist=artist;this.current=current;this.error=error;}
 public static QueueRequest parse(String raw){
  if(raw==null)return null;String q=raw.trim().replaceFirst("(?i)^hey google[,! ]+","").replaceFirst("(?i)^please\\s+","").replaceFirst("(?i)^(?:can|could|would) you\\s+","");
  if(q.matches("(?i)^play\\s+(?:the\\s+)?next(?:\\s+(?:song|track|one))?[.!?]*$"))return null;
  if(!Pattern.compile("(?i)\\b(?:play|queue|add)\\b.+\\bnext\\b").matcher(q).find())return null;
  if(q.length()>300||Pattern.compile("(?i)\\b(?:and|then)\\s+(?:play|shuffle|add|queue|remove|skip)\\b").matcher(q).find())return new QueueRequest("","",false,"Only one play-next request is supported at a time.");
  Matcher m=Pattern.compile("(?i)^(?:play|queue|add)\\s+(.+?)\\s+(?:up\\s+)?next[.!?]*$").matcher(q);
  if(!m.matches())return new QueueRequest("","",false,"Use a song name followed by next.");
  String song=m.group(1).trim();if(song.matches("(?i)this song|this track|the current song|the current track|it"))return new QueueRequest("","",true,"");
  if(song.matches("(?i).*(?:playlist|album|songs)"))return new QueueRequest("","",false,"This version inserts one song, not an album or playlist.");
  int by=song.toLowerCase(Locale.ROOT).lastIndexOf(" by ");String artist=by<0?"":song.substring(by+4).trim();String title=by<0?song:song.substring(0,by).trim();
  title=title.replaceAll("^[\\\"“]|[\\\"”]$","");
  return new QueueRequest(title,artist,false,title.isEmpty()?"Song name is missing.":"");
 }
 public String search(){return title+(artist.isEmpty()?"":" "+artist);}
}
