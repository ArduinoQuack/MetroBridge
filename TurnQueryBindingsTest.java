import dev.metrobridge.poc.TurnQueryBindings;
public class TurnQueryBindingsTest {
 static int count;
 static void check(boolean ok){count++;if(!ok)throw new AssertionError("binding case "+count);}
 public static void main(String[] args){
  TurnQueryBindings b=new TurnQueryBindings();Object a=new String("same"),other=new String("same");
  check(b.bind(a,"turn1","Play my rap playlist",0).equals("bound"));
  check(b.query(a,1).equals("Play my rap playlist"));
  check(b.query(other,1).isEmpty());
  check(b.query(a,29999).equals("Play my rap playlist"));
  check(b.query(a,30000).isEmpty());
  b.bind(a,"turn1","rap",40000);b.bind(other,"turn2","pop",40001);
  check(b.query(a,40002).equals("rap")&&b.query(other,40002).equals("pop"));
  check(b.bind(a,"different","rap",40003).equals("action-turn-conflict"));
  check(b.query(a,40004).isEmpty());
  check(b.bind(a,"turn1","rap",40005).equals("action-turn-conflict"));
  check(b.bind(a,"new","pop",80000).equals("bound"));
  b.bind(a,null,null,80001);check(b.query(a,80002).isEmpty());
  b.bind(a,"t","rap",90000);b.bind(a,"t","pop",90001);check(b.query(a,90002).isEmpty());
  TurnQueryBindings bounded=new TurnQueryBindings();Object first=new Object();bounded.bind(first,"first","rap",100);
  for(int i=0;i<32;i++)bounded.bind(new Object(),"t"+i,"pop",101+i);
  check(bounded.query(first,200).isEmpty());
  b.bind(a,"t","rap",100000);check(b.query(a,99999).isEmpty());
  System.out.println(count+" action-identity binding checks passed");
 }
}
