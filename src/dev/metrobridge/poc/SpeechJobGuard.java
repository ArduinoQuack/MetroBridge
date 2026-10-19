package dev.metrobridge.poc;
/** Only an in-flight replacement for the exact engine and reply may be reused. */
public final class SpeechJobGuard {
 private Object engine,job;private String reply="";
 public synchronized void started(Object owner,String response,Object task){engine=owner;reply=response;job=task;}
 public synchronized Object duplicate(Object owner,String response){return engine==owner&&job!=null&&reply.equals(response)?job:null;}
 public synchronized void finished(Object task){if(job==task)clear();}
 public synchronized void clear(){engine=null;job=null;reply="";}
}
