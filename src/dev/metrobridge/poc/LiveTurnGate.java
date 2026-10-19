package dev.metrobridge.poc;
import java.util.LinkedHashMap;
/** Bounded, process-local exact turn deduplication. Never persisted or logged. */
public final class LiveTurnGate {
 private final LinkedHashMap<String,String> seen=new LinkedHashMap<>();
 public synchronized String accept(String turn,String text){
  if(turn==null||turn.isEmpty()||turn.length()>512||text==null||text.trim().isEmpty()||text.length()>300)return "invalid";
  String prior=seen.get(turn);if(prior!=null)return prior.equals(text)?"duplicate-turn":"conflicting-turn";
  if(seen.size()>=256)seen.remove(seen.keySet().iterator().next());seen.put(turn,text);return "new-turn";
 }
}
