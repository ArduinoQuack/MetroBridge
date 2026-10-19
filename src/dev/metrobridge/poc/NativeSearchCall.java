package dev.metrobridge.poc;

/** R8's third integer argument is page SIZE, not page index. */
public final class NativeSearchCall {
 public interface Invoke {Object call(Object target,String method,Object... args);}
 public static Object request(Object session,Object controller,String query,Invoke invoke){
  // The pinned MetroList callback ignores pagination and can return all local matches.
  // Request the legacy-style unpaged window; the IPC result is separately capped at 50.
  return invoke.call(session,"onGetSearchResultOnHandler",controller,query,Integer.MAX_VALUE,null);
 }
 private NativeSearchCall(){}
}
