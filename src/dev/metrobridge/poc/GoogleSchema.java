package dev.metrobridge.poc;
import android.content.Context;import android.content.pm.PackageInfo;
final class GoogleSchema {
 static SpeechProfile profile;
 static boolean configure(Context c){try{PackageInfo p=c.getPackageManager().getPackageInfo(BridgeStore.GOOGLE,0);profile=SpeechProfile.select(p.versionName,p.getLongVersionCode());CurrentTurnReader.displayField=profile!=null&&profile.modern?"m":"l";return profile!=null;}catch(Throwable e){return false;}}
 static Class<?> type(String old,ClassLoader loader)throws ClassNotFoundException{return Class.forName(name(old),false,loader);}
 static boolean fresh(Object kind,Class<?> primary){return kind!=null&&(primary.isInstance(kind)||profile!=null&&kind.getClass().getName().equals(profile.modern?"anlo":"anjr"));}
 static String name(String old){
  if(profile==null||!profile.modern)return "anjl".equals(old)?"anjn":old;
  switch(old){
   case "arku":return "arma";case "gdka":return "gdqi";case "artg":return "arut";
   case "aofa":return "aohd";case "aofk":return "aohn";case "aoft":return "aohw";
   case "aofe":return "aohh";case "aofx":return "aoia";case "appq":return "apqo";
   case "apvs":return "apwy";case "hent":return "hesz";case "gbqj":return "gbwc";
   case "gbuc":return "gbzw";case "axno":return "axsa";case "aobd":return "aodg";
   case "aobk":return "aodn";case "appx":return "apqv";case "anjl":return "anlk";
   case "anjy":return "anlv";case "aofw":return "aohz";case "aofp":return "aohs";
   case "apso":return "aptm";default:return profile.name(old);
  }
 }
 private GoogleSchema(){}
}
