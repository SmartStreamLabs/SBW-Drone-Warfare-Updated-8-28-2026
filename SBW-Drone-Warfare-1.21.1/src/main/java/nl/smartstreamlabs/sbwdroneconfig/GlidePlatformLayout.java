package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.world.phys.*;
/** Native GB-01 -> one Y half-turn -> (0,9,0) -> blocks -> facing -> world. */
public final class GlidePlatformLayout {
 public static final int GROUND_PARTS=9,DEPARTURE_TICKS=8,RENDER_BLEND_TICKS=8;
 public static final int PARTS;static final int[][] CELLS;private static final boolean[] SUPPORT;
 public static final double FIT_SCALE,MOUNT_PITCH,RAIL_PITCH,PIVOT_Y,OFFSET_Y;
 private static final double[] PAYLOAD,SWEEP;
 static {
  var p=readJson("lp03_placement.json");var g=readJson("lp03_bounds.json");var shapes=readJson("lp03_shapes.json");
  FIT_SCALE=p.get("payload_reference_scale").getAsDouble();OFFSET_Y=p.getAsJsonArray("payload_reference_translation_units").get(1).getAsDouble();PIVOT_Y=p.getAsJsonObject("bone_pivots_units").getAsJsonArray("mount_pitch").get(1).getAsDouble();MOUNT_PITCH=p.getAsJsonArray("mount_pitch_rotation_degrees").get(0).getAsDouble();
  if(p.get("units_per_block").getAsInt()!=16||FIT_SCALE!=1||OFFSET_Y!=9||MOUNT_PITCH!=0||p.getAsJsonArray("payload_reference_rotation_degrees").get(1).getAsInt()!=180||!p.get("platform_forward_axis").getAsString().equals("+Z"))throw new IllegalStateException("Unsupported LP-03 R2 transform");
  // Preserve the existing guided departure physics. This is NOT the mount bind pitch.
  RAIL_PITCH=-8;PAYLOAD=read(g.getAsJsonArray("payload_bounds_blocks"));SWEEP=read(g.getAsJsonArray("platform_swept_bounds_blocks"));
  var cells=shapes.getAsJsonArray("cells");PARTS=cells.size();CELLS=new int[PARTS][3];SUPPORT=new boolean[PARTS];for(int i=0;i<PARTS;i++)for(int a=0;a<3;a++)CELLS[i][a]=cells.get(i).getAsJsonArray().get(a).getAsInt();for(var i:shapes.getAsJsonArray("support"))SUPPORT[i.getAsInt()]=true;
 }
 static com.google.gson.JsonObject readJson(String name){try(var in=GlidePlatformLayout.class.getResourceAsStream("/assets/sbwdroneconfig/"+name)){if(in==null)throw new IllegalStateException("Missing "+name);return com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();}catch(java.io.IOException ex){throw new ExceptionInInitializerError(ex);}}
 private static double[] read(com.google.gson.JsonArray a){double[] r=new double[6];for(int i=0;i<6;i++)r[i]=a.get(i).getAsDouble();return r;}
 public static final double MOUNT_HEIGHT=fitPoint(Vec3.ZERO).y,LAUNCH_SPEED=.55,LAUNCH_RISE=Math.tan(Math.toRadians(-RAIL_PITCH))*LAUNCH_SPEED;
 private GlidePlatformLayout(){}
 public static boolean needsSupport(int part){return SUPPORT[part];}
 public static Vec3 fitPoint(Vec3 source){return new Vec3(-source.x*FIT_SCALE,source.y*FIT_SCALE+OFFSET_Y,-source.z*FIT_SCALE).scale(1.0/16);}
 public static Direction right(Direction d){return d.getCounterClockWise();}
 public static Vec3 forward(Direction d){return new Vec3(d.getStepX(),0,d.getStepZ());}
 public static Vec3 orient(Vec3 p,Direction d){var r=right(d);return new Vec3(r.getStepX()*p.x+d.getStepX()*p.z,p.y,r.getStepZ()*p.x+d.getStepZ()*p.z);}
 public static BlockPos cell(BlockPos p,Direction d,int i){return p.relative(right(d),CELLS[i][0]).relative(d,CELLS[i][1]).above(CELLS[i][2]);}
 public static BlockPos anchor(BlockPos p,Direction d,int i){return p.relative(right(d),-CELLS[i][0]).relative(d,-CELLS[i][1]).below(CELLS[i][2]);}
 public static Vec3 center(BlockPos p,Direction d){return Vec3.atBottomCenterOf(p).add(right(d).getStepX(),0,right(d).getStepZ());}
 public static Vec3 mount(BlockPos p,Direction d){return center(p,d).add(orient(fitPoint(Vec3.ZERO),d));}
 public static float renderBlend(long now,long end,float partial){return net.minecraft.util.Mth.clamp((now-end+partial)/RENDER_BLEND_TICKS,0,1);}
 private static AABB bounds(BlockPos p,Direction d,double[] a){var c=center(p,d);AABB b=null;for(double x:new double[]{a[0],a[3]})for(double y:new double[]{a[1],a[4]})for(double z:new double[]{a[2],a[5]}){var v=c.add(orient(new Vec3(x,y,z),d));var point=new AABB(v,v);b=b==null?point:b.minmax(point);}return b.inflate(.025);}
 public static AABB droneBounds(BlockPos p,Direction d){return bounds(p,d,PAYLOAD);}
 public static AABB openingBounds(BlockPos p,Direction d){var b=bounds(p,d,SWEEP);return new AABB(b.minX,p.getY()+.001,b.minZ,b.maxX,b.maxY,b.maxZ);}
}
