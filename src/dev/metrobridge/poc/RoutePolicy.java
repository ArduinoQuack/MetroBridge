package dev.metrobridge.poc;

import java.util.*;
import java.util.regex.*;

public final class RoutePolicy {
 public static final class Entry {
  public final String title,id;
  public Entry(String title,String id){this.title=title;this.id=id;}
 }
 public static final class Decision {
  public final String action,reason; public final boolean shuffled;
  public final List<Entry> choices;
  public boolean requiresConfirmation(){return "choose".equals(action);}
  public boolean shuffleFor(boolean defaultShuffle,String raw){return !Pattern.compile("(?i)\\b(?:in order|in original order|without shuffle|not shuffled)[.!?]*$").matcher(raw==null?"":raw.trim()).find()&&(shuffled||defaultShuffle);}
  Decision(String action,String reason,boolean shuffled,List<Entry> choices){this.action=action;this.reason=reason;this.shuffled=shuffled;this.choices=choices;}
 }
 private static Decision pass(String reason){return new Decision("pass",reason,false,Collections.emptyList());}
 public static Decision resolve(String raw,String playlist,String focus,String title,boolean exactBareNames,List<Entry> library) {
  String q=raw==null?"":raw.trim();String p=playlist==null?"":playlist.trim();
  String f=focus==null?"":focus.toLowerCase(Locale.ROOT);
  if(Pattern.compile("(?i)\\b(?:and|then)\\s+(?:play|add|queue|remove|skip|repeat|shuffle)\\b|\\bplay\\b.+\\bnext\\b|\\badd\\b.+\\bqueue\\b").matcher(q).find())return pass("compound-or-queue-not-supported");
  boolean explicit=!p.isEmpty()||f.contains("playlist")||Pattern.compile("(?i)\\bplaylist\\b").matcher(q).find();
  if(p.isEmpty()&&!f.contains("playlist")&&((title!=null&&!title.isEmpty())||f.contains("artist")||f.contains("album")||f.contains("audio")))return pass("structured-non-playlist");
  q=q.replaceFirst("(?i)^hey google[,! ]+","").replaceFirst("(?i)^please\\s+","");
  boolean shuffled=Pattern.compile("(?i)^shuffle\\s+|\\bshuffled[.!?]*$|\\bin (?:a )?random order[.!?]*$|\\bon shuffle[.!?]*$").matcher(q).find();
  if(!p.isEmpty()&&NameMatcher.normalize(p).equals(NameMatcher.normalize(q)))shuffled=false;
  if(p.isEmpty()) {
   q=q.replaceFirst("(?i)\\s+(?:on|in|using) youtube music[.!?]*$","");
   q=q.replaceFirst("(?i)\\s+(?:shuffled|on shuffle|in (?:a )?random order|in order|in original order|without shuffle|not shuffled)[.!?]*$","");
   q=q.replaceFirst("(?i)^(?:play|start|shuffle|listen to)\\s+","");
   q=q.replaceFirst("(?i)^my\\s+","").replaceFirst("(?i)\\s+playlist[.!?]*$","");
   p=q.trim();
  }
  String key=NameMatcher.normalize(p);
  if(key.isEmpty())return explicit?new Decision("choose","playlist-name-missing",shuffled,new ArrayList<>(library)):pass("query-empty");
  ArrayList<Entry> matches=new ArrayList<>();
  for(Entry e:library)if(NameMatcher.normalize(e.title).equals(key))matches.add(e);
  if(matches.size()==1&&(explicit||exactBareNames))return new Decision("play","unique-library-playlist",shuffled,matches);
  if(matches.size()>1&&(explicit||exactBareNames))return new Decision("choose","ambiguous-playlist",shuffled,matches);
  if(!explicit)return pass("not-an-explicit-or-exact-playlist");
  ArrayList<Entry> close=new ArrayList<>();
  for(Entry e:library)if(NameMatcher.score(p,e.title)>=0.35)close.add(e);
  final String wanted=p;
  close.sort((a,b)->Double.compare(NameMatcher.score(wanted,b.title),NameMatcher.score(wanted,a.title)));
  return new Decision("choose","confirm-playlist-match",shuffled,close.subList(0,Math.min(8,close.size())));
 }
 private RoutePolicy() {}
}
