package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;
import net.minecraft.world.phys.shapes.*;
import java.util.*;
/** RJ01 component bounds in the same centred block space as the baked OBJ/BER.
 * The dish uses a fixed swept envelope so its cosmetic rotation cannot push players.
 * Shapes are clipped to each occupied cell, including upper cells; no ghost full cubes.
 */
public final class Rj01Shapes {
 private static final Map<Direction,Map<BlockPos,VoxelShape>> CELLS=build();
 private static Map<Direction,Map<BlockPos,VoxelShape>> build(){
  VoxelShape north=Shapes.or(Shapes.box(-.25,0,-.064,1.25,.178,1.063),Shapes.box(-.178,.147,.011,.453,1.077,.958),Shapes.box(.616,.144,.303,1.009,1.828,.697));
  north=Shapes.or(north,Shapes.box(-0.10866025,1.0625,0.14625,-0.09133975,1.128125,0.16625));
  north=Shapes.or(north,Shapes.box(-0.1046831,1.12084018,0.14625,-0.0515669,1.16353482,0.16625));
  north=Shapes.or(north,Shapes.box(-0.05625,1.14758975,0.14625,0.3375,1.16491025,0.16625));
  north=Shapes.or(north,Shapes.box(0.33230385,1.1211968,0.14625,0.38019615,1.1631782,0.16625));
  north=Shapes.or(north,Shapes.box(0.36633975,1.0625,0.14625,0.38366025,1.128125,0.16625));
  north=Shapes.or(north,Shapes.box(-0.10866025,1.0625,0.83375,-0.09133975,1.128125,0.85375));
  north=Shapes.or(north,Shapes.box(-0.1046831,1.12084018,0.83375,-0.0515669,1.16353482,0.85375));
  north=Shapes.or(north,Shapes.box(-0.05625,1.14758975,0.83375,0.3375,1.16491025,0.85375));
  north=Shapes.or(north,Shapes.box(0.33230385,1.1211968,0.83375,0.38019615,1.1631782,0.85375));
  north=Shapes.or(north,Shapes.box(0.36633975,1.0625,0.83375,0.38366025,1.128125,0.85375));
  north=Shapes.or(north,Shapes.box(0.56489502,0.146875,0.226875,0.58510498,0.34375,0.248125));
  north=Shapes.or(north,Shapes.box(0.55125,0.14625,0.21375,0.59875,0.16,0.26125));
  north=Shapes.or(north,Shapes.box(1.07114502,0.146875,0.226875,1.09135498,0.34375,0.248125));
  north=Shapes.or(north,Shapes.box(1.0575,0.14625,0.21375,1.105,0.16,0.26125));
  north=Shapes.or(north,Shapes.box(0.56925186,0.33656482,0.226875,0.61199814,0.37593518,0.248125));
  north=Shapes.or(north,Shapes.box(0.60625,0.35954848,0.226875,1.05,0.37795152,0.248125));
  north=Shapes.or(north,Shapes.box(1.04425186,0.33656482,0.226875,1.08699814,0.37593518,0.248125));
  north=Shapes.or(north,Shapes.box(0.564375,0.33364502,0.2375,0.585625,0.35385498,0.39375));
  north=Shapes.or(north,Shapes.box(1.070625,0.33364502,0.2375,1.091875,0.35385498,0.39375));
  // Conservative circular envelope of the supplied 360-degree yaw / +/-8-degree tilt.
  for(int i=0;i<16;i++){double a=i*Math.PI/8,b=(i+1)*Math.PI/8;double x0=.8125+.60*Math.cos(a),x1=.8125+.60*Math.cos(b),z0=.5+.60*Math.sin(a),z1=.5+.60*Math.sin(b);north=Shapes.or(north,Shapes.box(Math.min(.8125,Math.min(x0,x1)),1.813,Math.min(.5,Math.min(z0,z1)),Math.max(.8125,Math.max(x0,x1)),2.81,Math.max(.5,Math.max(z0,z1))));}
  var out=new EnumMap<Direction,Map<BlockPos,VoxelShape>>(Direction.class);
  for(Direction d:Direction.Plane.HORIZONTAL){VoxelShape rotated=Shapes.empty();for(var box:north.toAabbs()){double[] xs={box.minX,box.maxX},zs={box.minZ,box.maxZ};double minX=10,minZ=10,maxX=-10,maxZ=-10;for(double x:xs)for(double z:zs){double rx=x,rz=z;switch(d){case EAST:rx=1-z;rz=x;break;case SOUTH:rx=1-x;rz=1-z;break;case WEST:rx=z;rz=1-x;break;default:break;}minX=Math.min(minX,rx);maxX=Math.max(maxX,rx);minZ=Math.min(minZ,rz);maxZ=Math.max(maxZ,rz);}rotated=Shapes.or(rotated,Shapes.box(minX,box.minY,minZ,maxX,box.maxY,maxZ));}
   Map<BlockPos,VoxelShape> cells=new HashMap<>();for(int x=-2;x<=2;x++)for(int y=0;y<3;y++)for(int z=-2;z<=2;z++){var cell=new BlockPos(x,y,z);var clipped=Shapes.join(rotated,Shapes.block().move(x,y,z),BooleanOp.AND).move(-x,-y,-z).optimize();if(!clipped.isEmpty())cells.put(cell,clipped);}out.put(d,Map.copyOf(cells));
  }return Map.copyOf(out);
 }
 public static VoxelShape cell(Direction d,BlockPos offset){return CELLS.get(d).getOrDefault(offset,Shapes.empty());}
}
