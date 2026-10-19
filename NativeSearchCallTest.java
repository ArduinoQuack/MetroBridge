import dev.metrobridge.poc.NativeSearchCall;
import java.util.concurrent.atomic.AtomicReference;
public final class NativeSearchCallTest {
 static int count;
 static void check(boolean b){count++;if(!b)throw new AssertionError("search contract case "+count);}
 // Contract taken from the supplied APK's verifyResultItems, not bridge code.
 static Throwable validator(int pageSize,int results)throws Exception {
  AtomicReference<Throwable> failure=new AtomicReference<>();
  Thread handler=new Thread(()->{if(results>pageSize)throw new IllegalStateException("Invalid size="+results+", pageSize="+pageSize);});
  handler.setUncaughtExceptionHandler((thread,error)->failure.set(error));handler.start();handler.join(1000);if(handler.isAlive())throw new AssertionError("validator stuck");return failure.get();
 }
 public static void main(String[] args)throws Exception {
  Object session=new Object(),controller=new Object(),future=new Object();Object[][] actual=new Object[1][];
  Object got=NativeSearchCall.request(session,controller,"Armed and Dangerous",(target,method,values)->{
   check(target==session);check(method.equals("onGetSearchResultOnHandler"));actual[0]=values;return future;
  });
  check(got==future);check(actual[0].length==4);check(actual[0][0]==controller);check(actual[0][1].equals("Armed and Dangerous"));check(actual[0][3]==null);
  int window=(Integer)actual[0][2];check(window>0);
  Throwable original=validator(0,20);check(original instanceof IllegalStateException&&original.getMessage().equals("Invalid size=20, pageSize=0"));
  for(int results:new int[]{0,1,20,50,40000,Integer.MAX_VALUE})check(validator(window,results)==null);
  System.out.println(count+" native search/page-size contract checks passed (reported asynchronous crash reproduced)");
 }
}
