package dev.metrobridge.poc;
import android.content.Context;import android.os.*;import java.util.*;
/** Finite browser operations in the already-running companion process; no Activity/service/polling. */
final class LocalActions {
 private static final Set<Object> active=new HashSet<>();
 static boolean dispatch(Context c,String token){
  BridgeStore.Ticket ticket=BridgeStore.consume(c,token);if(ticket==null)return false;
  Context app=c.getApplicationContext();new Handler(Looper.getMainLooper()).post(()->{
   if(!BridgeStore.isCurrent(ticket)){BridgeStore.release(ticket.routeKey);return;}
   if(ticket.queue!=null){QueueOperation op=new QueueOperation(app,ticket);active.add(op);op.start();}
   else{PlaylistOperation op=new PlaylistOperation(app,ticket);active.add(op);op.start();}
  });return true;
 }
 static void finished(Object op){active.remove(op);}
 private LocalActions(){}
}
