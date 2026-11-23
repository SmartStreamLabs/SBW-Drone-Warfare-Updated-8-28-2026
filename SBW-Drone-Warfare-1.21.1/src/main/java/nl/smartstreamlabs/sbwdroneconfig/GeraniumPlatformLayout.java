package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.world.phys.*;
public final class GeraniumPlatformLayout {
 public static final int GROUND_PARTS=15,PARTS=30;
 static final int[][] CELLS=new int[PARTS][3];
 public static final double FIT_SCALE,RAIL_PITCH,PIVOT_Y,OFFSET_Y,CONSOLE_LID_X;
 static {
  try(var stream=GeraniumPlatformLayout.class.getResourceAsStream("/assets/sbwdroneconfig/lp02_placement.json")){
   if(stream==null)throw new IllegalStateException("LP-02 placement data missing");var p=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
   FIT_SCALE=p.get("reference_drone_scale").getAsDouble();RAIL_PITCH=p.getAsJsonArray("rail_rotation_degrees").get(0).getAsDouble();PIVOT_Y=p.getAsJsonArray("rail_pivot").get(1).getAsDouble();OFFSET_Y=22;CONSOLE_LID_X=p.getAsJsonArray("default_console_lid_rotation").get(0).getAsDouble();
   if(p.get("units_per_block").getAsInt()!=16 || FIT_SCALE!=1 || !p.get("forward_axis").getAsString().equals("+Z"))throw new IllegalStateException("Invalid LP-02 coordinate convention");
  }catch(java.io.IOException e){throw new ExceptionInInitializerError(e);}
  int n=0;for(int y=0;y<2;y++)for(int z:new int[]{0,-1,1,-2,2})for(int x=0;x<3;x++)CELLS[n++]=new int[]{x,z,y};
 }
 public static final double MOUNT_HEIGHT=fitPoint(Vec3.ZERO).y,LAUNCH_SPEED=1.15,LAUNCH_RISE=Math.tan(Math.toRadians(-RAIL_PITCH))*LAUNCH_SPEED;
 private GeraniumPlatformLayout(){}
 /** Original BB geometry, Y180, offset, rail pivot rotation, then divide by 16. */
 public static Vec3 fitPoint(Vec3 source){double x=-source.x*FIT_SCALE,y=source.y*FIT_SCALE+OFFSET_Y-PIVOT_Y,z=-source.z*FIT_SCALE,a=Math.toRadians(RAIL_PITCH),c=Math.cos(a),s=Math.sin(a);return new Vec3(x,PIVOT_Y+y*c-z*s,y*s+z*c).scale(1.0/16);}
 public static Direction right(Direction forward){return forward.getCounterClockWise();}
 public static Vec3 forward(Direction d){return new Vec3(d.getStepX(),0,d.getStepZ());}
 public static Vec3 orient(Vec3 p,Direction d){var r=right(d);return new Vec3(r.getStepX()*p.x+d.getStepX()*p.z,p.y,r.getStepZ()*p.x+d.getStepZ()*p.z);}
 public static BlockPos cell(BlockPos p,Direction d,int i){return p.relative(right(d),CELLS[i][0]).relative(d,CELLS[i][1]).above(CELLS[i][2]);}
 public static BlockPos anchor(BlockPos p,Direction d,int i){return p.relative(right(d),-CELLS[i][0]).relative(d,-CELLS[i][1]).below(CELLS[i][2]);}
 public static Vec3 center(BlockPos p,Direction d){return Vec3.atBottomCenterOf(p).add(right(d).getStepX(),0,right(d).getStepZ());}
 public static Vec3 mount(BlockPos p,Direction d){return center(p,d).add(orient(fitPoint(Vec3.ZERO),d));}
 public static AABB droneBounds(BlockPos p,Direction d){var c=center(p,d);AABB b=null;for(double x:new double[]{-1.30,1.30})for(double z:new double[]{-2.10,1.45}){var v=c.add(orient(new Vec3(x,0,z),d));var column=new AABB(v.x,c.y+.60,v.z,v.x,c.y+1.95,v.z);b=b==null?column:b.minmax(column);}return b.inflate(.04);}
}
