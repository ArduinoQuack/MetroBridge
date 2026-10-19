import dev.metrobridge.poc.DispatchGuard;
public class DispatchGuardTest {
 static int n;static void check(boolean ok){n++;if(!ok)throw new AssertionError("case "+n);}
 public static void main(String[] args){
  DispatchGuard g=new DispatchGuard();
  check(g.reserve("play:rap",0,10000));
  check(!g.reserve("play:rap",1,10000));
  check(g.reserve("prepare:rap",2,10000));
  g.release("prepare:rap");check(g.reserve("prepare:rap",3,10000));
  g.acknowledge("play:rap",10);check(!g.reserve("play:rap",1000,10000));
  check(g.reserve("play:rap",1510,10000));
  g.release("play:rap");check(g.reserve("play:rap",1511,10000));
  check(g.reserve("play:rap",11511,10000));
  check(g.reserve("play:pop",11512,10000));
  System.out.println(n+" dispatch deduplication checks passed");
 }
}
