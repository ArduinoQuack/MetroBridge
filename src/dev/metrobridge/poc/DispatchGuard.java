package dev.metrobridge.poc;
import java.util.*;
public final class DispatchGuard {
 private final Map<String,Long> pending=new HashMap<>();
 private String last="";private long lastTime=-10000;
 public synchronized boolean reserve(String key,long now,long ttl){
  pending.entrySet().removeIf(e->e.getValue()<=now);
  if(pending.containsKey(key)||(last.equals(key)&&now>=lastTime&&now-lastTime<1500))return false;
  pending.put(key,now+ttl);return true;
 }
 public synchronized void acknowledge(String key,long now){pending.remove(key);last=key;lastTime=now;}
 public synchronized void release(String key){pending.remove(key);}
}
