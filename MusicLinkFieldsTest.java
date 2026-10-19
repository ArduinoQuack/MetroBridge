import dev.metrobridge.poc.MusicLinkFields;
public class MusicLinkFieldsTest {
 static int count;
 static void check(boolean ok){count++;if(!ok)throw new AssertionError("link case "+count);}
 static MusicLinkFields read(String link){return MusicLinkFields.read(link);}
 public static void main(String[] args){
  check(read(null).searchQuery.isEmpty());
  check(read("https://music.youtube.com/search?q=rap").searchQuery.equals("rap"));
  check(read("https://www.youtube.com/results?search_query=my+Israeli+playlist").searchQuery.equals("my Israeli playlist"));
  check(read("https://music.youtube.com/playlist?list=PL_PUBLIC").playlistOnly);
  check(!read("https://music.youtube.com/watch?v=VIDEO&list=PL_PUBLIC").playlistOnly);
  check(read("https://music.youtube.com/watch?q=rap").searchQuery.isEmpty());
  check(read("https://evil.example/search?q=rap").searchQuery.isEmpty());
  check(!read("https://music.youtube.com.evil.example/playlist?list=PL_X").playlistOnly);
  check(read("https://music.youtube.com/search?q=rap&q=pop").searchQuery.isEmpty());
  check(read("https://music.youtube.com/search?q=rap&query=pop").searchQuery.isEmpty());
  String sanitized=read("https://music.youtube.com/playlist?list=PL_X&access_token=SECRET&code=SECRET").summary;
  check(!sanitized.contains("SECRET")&&!sanitized.contains("access_token")&&!sanitized.contains("code="));
  check(!read("https://music.youtube.com/listen/SECRET?code=SECRET").summary.contains("SECRET"));
  check(read("https://music.youtube.com/search?q=%XX").searchQuery.isEmpty());
  check(!read("https://music.youtube.com/playlist?list=contains%20space").playlistOnly);
  check(!read("ytmusic://playlist?list=PL_X").playlistOnly);
  check(read("https://music.youtube.com/watch?list=PLtYQKRARBjqGLAYA-Y6KYFWHBSoxDjial").playlistOnly);
  check(read("https://music.youtube.com/watch?list=PL_X&v=").playlistOnly);
  check(!read("https://music.youtube.com/watch?list=PL_X&v=invalid%20video").playlistOnly);
  check(!read("https://music.youtube.com/watch?v=VIDEO").playlistOnly);
  System.out.println(count+" allowlisted music-link checks passed");
 }
}
