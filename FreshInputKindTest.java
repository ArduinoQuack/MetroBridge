package dev.metrobridge.poc;
public final class FreshInputKindTest {
 static class DefaultInput{}static class RegenerateInput{}static class EditInput{}
 public static void main(String[] args){
  GoogleSchema.profile=SpeechProfile.select("17.68.17.ve.arm64",301824130L);
  if(!GoogleSchema.name("anjl").equals("anlk"))throw new AssertionError("Default mapping");
  if(!GoogleSchema.fresh(new DefaultInput(),DefaultInput.class))throw new AssertionError("fresh Default rejected");
  if(GoogleSchema.fresh(new RegenerateInput(),DefaultInput.class))throw new AssertionError("regeneration accepted");
  if(GoogleSchema.fresh(new EditInput(),DefaultInput.class))throw new AssertionError("edit accepted");
  if(GoogleSchema.fresh(null,DefaultInput.class))throw new AssertionError("null accepted");
  GoogleSchema.profile=SpeechProfile.select("17.67.17.ve.arm64",301822338L);if(!GoogleSchema.name("anjl").equals("anjn"))throw new AssertionError("old Default mapping");
  System.out.println("6 input-kind mapping and regeneration/edit exclusion checks passed");
 }
}
