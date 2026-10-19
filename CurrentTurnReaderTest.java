import dev.metrobridge.poc.CurrentTurnReader;
import java.util.*;
public class CurrentTurnReaderTest {
 static int count;
 static void check(boolean ok){count++;if(!ok)throw new AssertionError("turn case "+count);}
 static class Id {String id;Id(String value){id=value;}public String b(){return id;}}
 static class Input {String a,l,m;Input(String text){a=text;}}
 static class User {Object a,b;boolean g;User(String id,String text){a=new Id(id);b=new Input(text);}}
 static class Pair {User a;Pair(User user){a=user;}}
 static class State {Pair d;List<Pair> c=new ArrayList<>();}
 static class Flow {State state;Flow(State value){state=value;}public State b(){return state;}}
 static class Chat {Flow u;Chat(State value){u=new Flow(value);}}
 static class Execution {String i;Chat d;Execution(String id,State state){i=id;d=new Chat(state);}}
 static CurrentTurnReader.Result read(String id,State state){return CurrentTurnReader.read(new Execution(id,state));}
 public static void main(String[] args){
  State state=new State();state.d=new Pair(new User("new","Play my rap playlist"));
  check(read("new",state).text.equals("Play my rap playlist"));
  check(read("old",state).text.isEmpty());
  state.c.add(new Pair(new User("old","Play my pop playlist")));
  check(read("old",state).text.equals("Play my pop playlist"));
  check(read("absent",state).status.equals("no-exact-user-turn"));
  state.c.add(new Pair(new User("new","Play my pop playlist")));
  check(read("new",state).status.equals("conflicting-matched-turn-text"));
  state.c.remove(1);state.c.add(new Pair(new User("new","Play my rap playlist")));
  check(read("new",state).text.equals("Play my rap playlist"));
  state.c.clear();state.d.a.g=true;
  check(read("new",state).text.isEmpty());
  state.d.a.g=false;state.d.a.b=new Input(new String(new char[301]).replace('\0','x'));
  check(read("new",state).text.isEmpty());
  check(CurrentTurnReader.read(null).status.equals("execution-context-absent"));
  check(read("",state).status.equals("request-turn-id-absent"));
  check(read("new",null).status.equals("turn-state-absent"));
  // A mismatching ID must not cause its unrelated message payload to be inspected.
  state.d.a.a=new Id("other");state.d.a.b=new Object();
  check(read("new",state).status.equals("no-exact-user-turn"));
  state.d=null;state.c.clear();state.c.add(new Pair(new User("wanted","Play my rap playlist")));
  for(int i=0;i<64;i++)state.c.add(new Pair(new User("other"+i,"irrelevant")));
  check(read("wanted",state).text.equals("Play my rap playlist"));
  state.c.clear();for(int i=0;i<129;i++)state.c.add(new Pair(new User(i==64?"wanted":"other"+i,"Play my rap playlist")));
  check(read("wanted",state).text.isEmpty());
  state.c.clear();state.d=new Pair(new User("voice",""));((Input)state.d.a.b).l="Play my rap playlist";
  check(read("voice",state).text.equals("Play my rap playlist"));
  ((Input)state.d.a.b).a="Play my pop playlist";
  check(read("voice",state).text.equals("Play my pop playlist"));
  ((Input)state.d.a.b).a=new String(new char[301]).replace('\0','x');
  check(read("voice",state).text.isEmpty());
  state.c.clear();state.d=new Pair(new User("modern",""));((Input)state.d.a.b).m="Play my rap playlist";
  CurrentTurnReader.displayField="m";check(read("modern",state).text.equals("Play my rap playlist"));
  check(read("other",state).text.isEmpty());CurrentTurnReader.displayField="l";
  System.out.println(count+" exact-turn extraction checks passed");
 }
}
