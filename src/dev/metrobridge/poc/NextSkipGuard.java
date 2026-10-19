package dev.metrobridge.poc;
/** Suppress a cloud NEXT only for the current turn already accepted as queue insertion. */
public final class NextSkipGuard {
 private String current="",accepted="";private long at;
 public synchronized void turn(String reply){current=reply==null?"":reply;if(!current.equals(accepted))accepted="";}
 public synchronized void accept(String reply,long now){if(reply!=null&&!reply.isEmpty()&&reply.equals(current)){accepted=reply;at=now;}}
 public synchronized boolean block(long now){long age=now-at;return !accepted.isEmpty()&&accepted.equals(current)&&age>=0&&age<60000;}
}
