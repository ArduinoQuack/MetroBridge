package dev.metrobridge.poc;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class QueueSnapshot {
 public final List<String> items,order;public final String current,next;public final int index;public final boolean shuffle,playing;public final int repeat;public final long position;
 public QueueSnapshot(List<String> items,List<String> order,String current,String next,int index,boolean shuffle,int repeat,boolean playing,long position){this.items=new ArrayList<>(items);this.order=new ArrayList<>(order);this.current=current;this.next=next;this.index=index;this.shuffle=shuffle;this.repeat=repeat;this.playing=playing;this.position=position;}
 public String fingerprint(){try{MessageDigest digest=MessageDigest.getInstance("SHA-256");for(String value:items)feed(digest,value);feed(digest,"ORDER");for(String value:order)feed(digest,value);feed(digest,current);feed(digest,""+index);feed(digest,""+shuffle+":"+repeat);StringBuilder out=new StringBuilder();for(byte x:digest.digest())out.append(String.format(Locale.ROOT,"%02x",x&255));return out.toString();}catch(Exception e){throw new IllegalStateException(e);}}
 private static void feed(MessageDigest digest,String text){byte[] bytes=text.getBytes(StandardCharsets.UTF_8);digest.update((bytes.length+":").getBytes(StandardCharsets.UTF_8));digest.update(bytes);}
 public static String check(QueueSnapshot before,QueueSnapshot after,String added){
  if(!before.current.equals(after.current))return "current-track-changed";
  if(before.shuffle!=after.shuffle||before.repeat!=after.repeat||before.playing!=after.playing)return "playback-mode-changed";
  if(Math.abs(after.position-before.position)>2500)return "playback-position-changed";
  if(!without(before.items,added).equals(without(after.items,added)))return "existing-queue-changed";
  if(!without(before.order,added).equals(without(after.order,added)))return "existing-traversal-changed";
  if(!added.equals(after.next))return "requested-song-not-next";
  return "preserved";
 }
 private static List<String> without(List<String> all,String id){List<String> result=new ArrayList<>();for(String value:all)if(!value.equals(id))result.add(value);return result;}
}
