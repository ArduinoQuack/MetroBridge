package dev.metrobridge.poc;

import java.lang.reflect.*;
import java.util.*;

/** Fixed, version-checked schema. Reads text only after the request ID matches. */
public final class CurrentTurnReader {
 public static String displayField="l";
 public static final class Result {
  public final String turn,text,status;
  Result(String turn,String text,String status){this.turn=turn;this.text=text;this.status=status;}
 }
 public static Result read(Object execution){
  return read(execution,null);
 }
 /** New-user commit routing additionally requires the exact submitted input object. */
 public static Result read(Object execution,Object expectedInput){
  return read(execution,expectedInput,true);
 }
 public static Result readOngoing(Object execution,Object expectedInput){
  return read(execution,expectedInput,false);
 }
 private static Result read(Object execution,Object expectedInput,boolean includeCompleted){
  try{
   if(execution==null)return new Result("","","execution-context-absent");
   Object id=field(execution,"i");String wanted=id instanceof String?(String)id:"";
   if(wanted.isEmpty())return new Result("","","request-turn-id-absent");
   Object chat=field(execution,"d");if(chat==null)return new Result(wanted,"","chat-state-absent");
   Object flow=field(chat,"u"),state=flow==null?null:call(flow,"b");
   if(state==null)return new Result(wanted,"","turn-state-absent");
   Set<String> texts=new HashSet<>();Object ongoing=field(state,"d");
   if(ongoing!=null)add(texts,field(ongoing,"a"),wanted,expectedInput);
   Object all=field(state,"c");
   if(includeCompleted&&all instanceof List){List<?> complete=(List<?>)all;int end=complete.size();
    for(int i=0;i<Math.min(32,end);i++){Object pair=complete.get(i);if(pair!=null)add(texts,field(pair,"a"),wanted,expectedInput);}
    for(int i=Math.max(32,end-32);i<end;i++){Object pair=complete.get(i);if(pair!=null)add(texts,field(pair,"a"),wanted,expectedInput);}
   }
   return texts.size()==1?new Result(wanted,texts.iterator().next(),"matched-current-user-turn"):
    new Result(wanted,"",texts.isEmpty()?"no-exact-user-turn":"conflicting-matched-turn-text");
  }catch(Exception e){return new Result("","","schema-unavailable:"+e.getClass().getSimpleName());}
 }
 private static void add(Set<String> texts,Object user,String wanted,Object expectedInput)throws Exception{
  if(user==null)return;Object id=field(user,"a");
  if(id==null||!wanted.equals(call(id,"b"))||Boolean.TRUE.equals(field(user,"g")))return;
  Object input=field(user,"b");if(expectedInput!=null&&input!=expectedInput)return;
  Object text=input==null?null:field(input,"a");
  if(input!=null&&(text==null||(text instanceof String&&((String)text).trim().isEmpty())))text=field(input,displayField);
  if(text instanceof String&&!((String)text).trim().isEmpty()&&((String)text).length()<=300)texts.add((String)text);
 }
 private static Object field(Object object,String name)throws Exception{
  for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass())try{Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(object);}catch(NoSuchFieldException ignored){}
  throw new NoSuchFieldException(name);
 }
 private static Object call(Object object,String name)throws Exception{
  for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass())try{Method m=type.getDeclaredMethod(name);m.setAccessible(true);return m.invoke(object);}catch(NoSuchMethodException ignored){}
  throw new NoSuchMethodException(name);
 }
 private CurrentTurnReader(){}
}
