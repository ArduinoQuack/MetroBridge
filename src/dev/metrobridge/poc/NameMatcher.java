package dev.metrobridge.poc;
import java.text.Normalizer;
import java.util.Locale;
public final class NameMatcher {
 public static String normalize(String s) {
  return Normalizer.normalize(s, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
   .replaceAll("[^\\p{L}\\p{N}]+", " ").trim().replaceAll("\\s+", " ");
 }
 public static double score(String a,String b) {
  a=normalize(a);b=normalize(b);
  if(a.equals(b))return 1;
  if(a.isEmpty()||b.isEmpty())return 0;
  int[] row=new int[b.length()+1];for(int j=0;j<row.length;j++)row[j]=j;
  for(int i=1;i<=a.length();i++) {int prev=row[0];row[0]=i;for(int j=1;j<=b.length();j++) {
   int old=row[j];row[j]=Math.min(Math.min(row[j]+1,row[j-1]+1),prev+(a.charAt(i-1)==b.charAt(j-1)?0:1));prev=old;
  }}
  return 1.0-row[b.length()]/(double)Math.max(a.length(),b.length());
 }
}
