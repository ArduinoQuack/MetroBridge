package dev.metrobridge.poc;import android.content.Context;import android.os.*;
public class ForegroundHandoffTest {static int n;static void c(boolean b){n++;if(!b)throw new AssertionError("foreground "+n);}static void tick(long t){Handler.advance(t);}static Context setup(){ForegroundHandoff.newTurn();tick(0);Handler.reset();BridgeStore.enabled=true;VoiceConfirmation.voice=true;BridgeStore.current=new BridgeStore.Ticket("r");return new Context();}static void success(Context c){ForegroundHandoff.success(c,BridgeStore.current);tick(0);}
public static void main(String[] args){
Context a=setup();success(a);c(a.opened==0);tick(19999);c(a.opened==0);tick(1);c(a.opened==1);tick(60000);c(a.opened==1);
a=setup();success(a);ForegroundHandoff.speechStarted("r");tick(0);tick(20001);c(a.opened==0);ForegroundHandoff.speechFinished("r");tick(0);c(a.opened==1);tick(40000);c(a.opened==1);
a=setup();success(a);ForegroundHandoff.speechStarted("other");ForegroundHandoff.speechFinished("other");tick(0);c(a.opened==0);ForegroundHandoff.speechFinished("r");tick(0);c(a.opened==1);
a=setup();success(a);ForegroundHandoff.speechStarted("r");tick(0);tick(60000);c(a.opened==0);ForegroundHandoff.speechFinished("r");tick(0);c(a.opened==0);
a=setup();success(a);ForegroundHandoff.newTurn();tick(0);ForegroundHandoff.speechFinished("r");tick(20000);c(a.opened==0);
a=setup();success(a);BridgeStore.current=new BridgeStore.Ticket("s");ForegroundHandoff.speechFinished("r");tick(0);c(a.opened==0);
a=setup();VoiceConfirmation.voice=false;success(a);c(a.opened==1);
a=setup();success(a);BridgeStore.enabled=false;ForegroundHandoff.speechFinished("r");tick(0);c(a.opened==0);
a=setup();a.deny=true;success(a);ForegroundHandoff.speechFinished("r");tick(0);c(a.opened==0);
a=setup();ForegroundHandoff.success(a,BridgeStore.current);ForegroundHandoff.speechStarted("r");ForegroundHandoff.speechFinished("r");tick(0);c(a.opened==1);tick(60000);c(a.opened==1);
a=setup();ForegroundHandoff.success(a,BridgeStore.current);ForegroundHandoff.newTurn();tick(0);tick(60000);c(a.opened==0);
System.out.println(n+" actual foreground-helper lifecycle checks passed with host Android doubles (not device UI verification)");}}

