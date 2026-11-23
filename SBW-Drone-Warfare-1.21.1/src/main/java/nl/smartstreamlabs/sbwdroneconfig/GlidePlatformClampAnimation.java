package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;
/** Cached vector channels from the supplied R2 clips; positions remain native model units. */
final class GlidePlatformClampAnimation {
 static final int TICKS,LID_TICKS;
 private record Track(float duration,float[] time,float[][] values){float sample(int axis,float progress){float seconds=Math.max(0,Math.min(1,progress))*duration;if(seconds<=time[0])return values[0][axis];for(int i=1;i<time.length;i++)if(seconds<=time[i])return values[i-1][axis]+(values[i][axis]-values[i-1][axis])*(seconds-time[i-1])/(time[i]-time[i-1]);return values[values.length-1][axis];}}
 private static final java.util.Map<String,Track> TRACKS=new java.util.HashMap<>();
 static {
  var a=GlidePlatformLayout.readJson("animations/glide_bomb_platform_lp03.animation.json").getAsJsonObject("animations");var release=a.getAsJsonObject("animation.glide_platform.prepare_release");var cover=a.getAsJsonObject("animation.glide_platform.open_safety_cover");TICKS=(int)Math.round(release.get("animation_length").getAsDouble()*20);LID_TICKS=(int)Math.round(cover.get("animation_length").getAsDouble()*20);
  for(var clip:new JsonObject[]{release,cover})for(var bone:clip.getAsJsonObject("bones").entrySet())for(var channel:bone.getValue().getAsJsonObject().entrySet()){
   var entries=new java.util.ArrayList<>(channel.getValue().getAsJsonObject().entrySet());entries.sort(java.util.Comparator.comparingDouble(e->Double.parseDouble(e.getKey())));float[] t=new float[entries.size()];float[][] v=new float[t.length][3];for(int i=0;i<t.length;i++){t[i]=Float.parseFloat(entries.get(i).getKey());for(int axis=0;axis<3;axis++)v[i][axis]=entries.get(i).getValue().getAsJsonArray().get(axis).getAsFloat();}TRACKS.put(bone.getKey()+"/"+channel.getKey(),new Track(clip.get("animation_length").getAsFloat(),t,v));
  }
  if(TICKS!=18||LID_TICKS!=9)throw new IllegalStateException("Unexpected LP-03 clip duration");
 }
 static float position(String bone,int axis,float progress){var t=TRACKS.get(bone+"/position");return t==null?0:t.sample(axis,progress);}
 static float rotation(String bone,int axis,float progress){var t=TRACKS.get(bone+"/rotation");return t==null?0:(float)Math.toRadians(t.sample(axis,progress));}
 static float cover(float open){return rotation("safety_cover",2,open);}
}
