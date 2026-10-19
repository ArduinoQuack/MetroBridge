package dev.metrobridge.poc;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/** Correlates one exact action object with one exact request turn, never a global last utterance. */
public final class TurnQueryBindings {
 private static final long TTL=30000;
 private final IdentityHashMap<Object,Binding> entries=new IdentityHashMap<>();
 private static final class Binding {
  final String turn,query;final long at;final boolean conflict;
  Binding(String turn,String query,long at,boolean conflict){this.turn=turn;this.query=query;this.at=at;this.conflict=conflict;}
 }
 public synchronized String bind(Object action,String turn,String query,long now){
  expire(now);
  if(action==null)return "action-absent";
  if(turn==null||turn.isEmpty()||query==null||query.trim().isEmpty()||query.length()>300){entries.remove(action);return "input-unavailable";}
  Binding old=entries.get(action);
  if(old!=null&&(old.conflict||!old.turn.equals(turn)||!old.query.equals(query))){entries.put(action,new Binding("","",now,true));return "action-turn-conflict";}
  if(old==null&&entries.size()>=32){Object oldest=null;long at=Long.MAX_VALUE;for(Map.Entry<Object,Binding> e:entries.entrySet())if(e.getValue().at<at){at=e.getValue().at;oldest=e.getKey();}entries.remove(oldest);}
  entries.put(action,new Binding(turn,query,now,false));return "bound";
 }
 public synchronized String query(Object action,long now){expire(now);Binding binding=entries.get(action);return binding==null||binding.conflict?"":binding.query;}
 private void expire(long now){Iterator<Binding> it=entries.values().iterator();while(it.hasNext()){long age=now-it.next().at;if(age<0||age>=TTL)it.remove();}}
}
