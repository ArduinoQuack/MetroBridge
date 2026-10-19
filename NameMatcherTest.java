import dev.metrobridge.poc.NameMatcher;
public class NameMatcherTest {
 public static void main(String[] args) {
  check(NameMatcher.normalize("  RAP!!! ").equals("rap"));
  check(NameMatcher.normalize("Ｒａｐ").equals("rap"));
  check(NameMatcher.normalize("My Playlist").equals("my playlist"));
  check(!NameMatcher.normalize("Rap 2").equals(NameMatcher.normalize("Rap")));
  check(NameMatcher.normalize("POP—Hits").equals(NameMatcher.normalize("pop hits")));
  check(NameMatcher.normalize("日本語").equals("日本語"));
  check(NameMatcher.score("rap","rap")>NameMatcher.score("rap","pop"));
  check(NameMatcher.score("","")==1.0);
  System.out.println("8 matcher checks passed");
 }
 static void check(boolean b){if(!b)throw new AssertionError();}
}
