package dev.metrobridge.poc;
/** Names come from the selected MetroList item, not from an unconfirmed search query. */
public final class PlaybackWords {
 public static String playlist(String name){return "Playing "+clean(name,"your playlist");}
 public static String next(String name){return next(name,null);}
 public static String next(String name,String artist){String who=clean(artist,"");return "Playing "+clean(name,"this song")+(who.isEmpty()?"":" by "+who)+" next";}
 private static String clean(String name,String fallback){String result=name==null?"":name.replaceAll("[\\p{Cntrl}]+"," ").trim().replaceAll("\\s+"," ");return result.isEmpty()?fallback:result.substring(0,Math.min(160,result.length()));}
 private PlaybackWords(){}
}
