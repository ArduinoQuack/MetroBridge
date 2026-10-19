package dev.metrobridge.poc;

import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

/** Only allowlisted music fields; never retains or logs the full incoming URL. */
public final class MusicLinkFields {
 public final String summary,searchQuery;
 public final boolean playlistOnly;
 private MusicLinkFields(String summary,String query,boolean playlistOnly){this.summary=summary;this.searchQuery=query;this.playlistOnly=playlistOnly;}
 public static MusicLinkFields read(String raw){
  if(raw==null)return new MusicLinkFields("none","",false);
  if(raw.length()>4096)return new MusicLinkFields("over-length","",false);
  try{
   URI uri=new URI(raw);String scheme=uri.getScheme(),host=uri.getHost();
   if(scheme==null||host==null||!(scheme.equalsIgnoreCase("https")||scheme.equalsIgnoreCase("http")))return new MusicLinkFields("non-web-music-link","",false);
   host=host.toLowerCase(Locale.ROOT);
   if(!Arrays.asList("music.youtube.com","youtube.com","www.youtube.com","youtu.be").contains(host))return new MusicLinkFields("non-youtube-host","",false);
   String path=uri.getPath();String route=Arrays.asList("/playlist","/watch","/search","/results","/browse").contains(path)?path:"other";
   Map<String,String> fields=new HashMap<>();String query=uri.getRawQuery();
   if(query!=null)for(String part:query.split("&",-1)){
    String[] pair=part.split("=",2);String key=URLDecoder.decode(pair[0],"UTF-8");
    if(Arrays.asList("q","query","search_query","list","v").contains(key)){
     if(fields.containsKey(key))return new MusicLinkFields("duplicate-music-field","",false);
     fields.put(key,pair.length<2?"":URLDecoder.decode(pair[1],"UTF-8"));
    }
   }
   String search="";
   if(route.equals("/search")||route.equals("/results"))for(String key:new String[]{"q","query","search_query"}){
    String value=fields.get(key);if(value!=null&&!value.isEmpty()){
     if(value.length()>300||(!search.isEmpty()&&!search.equals(value)))return new MusicLinkFields("ambiguous-or-long-search-field","",false);
     search=value;
    }
   }
   String list=id(fields.get("list")),video=id(fields.get("v"));
   return new MusicLinkFields("host="+host+" route="+route+" list="+list+" video="+video+" search="+search,search,!list.isEmpty()&&(route.equals("/playlist")||(route.equals("/watch")&&(!fields.containsKey("v")||fields.get("v").isEmpty()))));
  }catch(Exception e){return new MusicLinkFields("unparseable-link","",false);}
 }
 private static String id(String value){return value!=null&&value.matches("[A-Za-z0-9_-]{1,160}")?value:"";}
 private MusicLinkFields(){this("","",false);}
}
