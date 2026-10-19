import dev.metrobridge.poc.SpeechJobGuard;
public final class SpeechJobGuardTest {
 static int n;static void c(boolean b){n++;if(!b)throw new AssertionError("job guard "+n);}
 public static void main(String[] x){SpeechJobGuard g=new SpeechJobGuard();Object e=new Object(),other=new Object(),a=new Object(),b=new Object();
 c(g.duplicate(e,"r")==null);g.started(e,"r",a);c(g.duplicate(e,"r")==a);c(g.duplicate(other,"r")==null);c(g.duplicate(e,"other")==null);
 g.finished(b);c(g.duplicate(e,"r")==a);g.started(e,"s",b);g.finished(a);c(g.duplicate(e,"s")==b);c(g.duplicate(e,"r")==null);
 g.finished(b);c(g.duplicate(e,"s")==null);g.started(e,"x",a);g.clear();c(g.duplicate(e,"x")==null);
 g.started(e,"x",a);g.started(other,"x",b);c(g.duplicate(e,"x")==null);c(g.duplicate(other,"x")==b);g.started(e,"x",null);c(g.duplicate(e,"x")==null);
 System.out.println(n+" exact-engine/reply in-flight native job and late-completion checks passed");}
}
