import dev.metrobridge.poc.*;
public final class LiveTurnGateTest {
 static int count;
 static void check(boolean b){count++;if(!b)throw new AssertionError("live turn case "+count);}
 public static void main(String[] args){
  LiveTurnGate gate=new LiveTurnGate();
  check(gate.accept("one","Play Armed and Dangerous next").equals("new-turn"));
  check(gate.accept("one","Play Armed and Dangerous next").equals("duplicate-turn"));
  check(gate.accept("one","Play Lose Yourself next").equals("conflicting-turn"));
  check(gate.accept("two","Play Armed and Dangerous next").equals("new-turn"));
  check(gate.accept(null,"Play x next").equals("invalid"));
  check(gate.accept("", "Play x next").equals("invalid"));
  check(gate.accept("three", "").equals("invalid"));
  check(gate.accept("three",new String(new char[301])).equals("invalid"));
  for(int i=0;i<256;i++)gate.accept("fresh"+i,"Play x next");
  check(gate.accept("fresh255","Play x next").equals("duplicate-turn"));
  check(gate.accept("one","Play Armed and Dangerous next").equals("new-turn"));
  CurrentTurnReaderTest.State state=new CurrentTurnReaderTest.State();
  CurrentTurnReaderTest.User user=new CurrentTurnReaderTest.User("voice","Play Armed and Dangerous next");
  state.d=new CurrentTurnReaderTest.Pair(user);Object input=user.b;
  Object execution=new CurrentTurnReaderTest.Execution("voice",state);
  check(CurrentTurnReader.read(execution,input).text.equals("Play Armed and Dangerous next"));
  check(CurrentTurnReader.readOngoing(execution,input).text.equals("Play Armed and Dangerous next"));
  check(CurrentTurnReader.read(execution,new CurrentTurnReaderTest.Input("Play Armed and Dangerous next")).text.isEmpty());
  state.d=null;state.c.add(new CurrentTurnReaderTest.Pair(user));
  check(CurrentTurnReader.read(execution,input).text.equals("Play Armed and Dangerous next"));
  check(CurrentTurnReader.readOngoing(execution,input).text.isEmpty());
  state.d=new CurrentTurnReaderTest.Pair(new CurrentTurnReaderTest.User("newer","Unrelated newer request"));
  check(CurrentTurnReader.readOngoing(execution,input).text.isEmpty());state.d=null;
  user.g=true;check(CurrentTurnReader.read(execution,input).text.isEmpty());user.g=false;
  check(CurrentTurnReader.read(new CurrentTurnReaderTest.Execution("different",state),input).text.isEmpty());
  user.b=new Object();check(CurrentTurnReader.read(execution,input).status.equals("no-exact-user-turn"));
  System.out.println(count+" committed-input identity and live-turn deduplication checks passed");
 }
}
