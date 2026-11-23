package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.world.phys.*;import net.minecraft.world.phys.shapes.*;
/** Per-component frame collision, clipped to occupied cells. The suspended centre stays open. */
final class GlidePlatformShapes {
 private static final VoxelShape[][] SELECT=build("selection",false),COLLIDE=build("collision",true);
 static VoxelShape selection(Direction d,int part){return SELECT[d.get2DDataValue()][part];}
 static VoxelShape collision(Direction d,int part){return COLLIDE[d.get2DDataValue()][part];}
 private static VoxelShape[][] build(String name,boolean physical){
  var boxes=GlidePlatformLayout.readJson("lp03_shapes.json").getAsJsonArray(name);VoxelShape[][] shapes=new VoxelShape[4][GlidePlatformLayout.PARTS];
  for(Direction d:new Direction[]{Direction.SOUTH,Direction.WEST,Direction.NORTH,Direction.EAST})for(int part=0;part<GlidePlatformLayout.PARTS;part++){
   int[] cell=GlidePlatformLayout.CELLS[part];VoxelShape shape=Shapes.empty();AABB envelope=null;
   for(var raw:boxes){var b=raw.getAsJsonArray();double loX=Math.max(0,b.get(0).getAsDouble()+1.5-cell[0]),hiX=Math.min(1,b.get(3).getAsDouble()+1.5-cell[0]),loY=Math.max(0,b.get(1).getAsDouble()-cell[2]),hiY=Math.min(1,b.get(4).getAsDouble()-cell[2]),loZ=Math.max(0,b.get(2).getAsDouble()+.5-cell[1]),hiZ=Math.min(1,b.get(5).getAsDouble()+.5-cell[1]);
    if(hiX<=loX||hiY<=loY||hiZ<=loZ)continue;
    if(physical){loX=Math.floor(loX*16)/16;loY=Math.floor(loY*16)/16;loZ=Math.floor(loZ*16)/16;hiX=Math.ceil(hiX*16)/16;hiY=Math.ceil(hiY*16)/16;hiZ=Math.ceil(hiZ*16)/16;}
    double minX=10,maxX=-10,minZ=10,maxZ=-10;var right=GlidePlatformLayout.right(d);
    for(double x:new double[]{loX-.5,hiX-.5})for(double z:new double[]{loZ-.5,hiZ-.5}){double xx=.5+right.getStepX()*x+d.getStepX()*z,zz=.5+right.getStepZ()*x+d.getStepZ()*z;minX=Math.min(minX,xx);maxX=Math.max(maxX,xx);minZ=Math.min(minZ,zz);maxZ=Math.max(maxZ,zz);}
    var box=new AABB(minX,loY,minZ,maxX,hiY,maxZ);if(physical)shape=Shapes.or(shape,Shapes.create(box));else envelope=envelope==null?box:envelope.minmax(box);
   }
   shapes[d.get2DDataValue()][part]=physical?shape.optimize():envelope==null?Shapes.empty():Shapes.create(envelope);
  }return shapes;
 }
}
