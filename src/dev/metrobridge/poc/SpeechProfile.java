package dev.metrobridge.poc;
/** Mappings inspected in supplied APKs; unknown builds never receive private hooks. */
final class SpeechProfile {
 final boolean modern;
 private SpeechProfile(boolean modern){this.modern=modern;}
 static SpeechProfile select(String version,long code){
  if("17.67.17.ve.arm64".equals(version)&&code==301822338L)return new SpeechProfile(false);
  if("17.68.17.ve.arm64".equals(version)&&code==301824130L)return new SpeechProfile(true);
  return null;
 }
 String name(String old){
  if(!modern)return old;
  switch(old){
   case "apsn":return "aptl";case "gnqw":return "gnwx";case "awjx":return "awnq";
   case "hdtz":return "hdze";case "awhk":return "awld";case "awiy":return "awmr";
   case "awjb":return "awmu";case "awiz":return "awms";case "avrm":return "avvd";
   case "gzhw":return "gzmw";case "awhe":return "awkx";case "awhf":return "awky";
   case "hefq":return "hekv";case "hecw":return "heib";case "heef":return "hejk";
   case "aswf":return "asxq";case "awhb":return "awku";default:return old;
  }
 }
 String stream(){return modern?"t":"s";}
 String job(){return modern?"F":"D";}
 String focus(){return modern?"E":"C";}
 String text(){return modern?"s":"r";}
 String flow(){return modern?"x":"w";}
}
