package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.world.phys.*;import net.minecraft.world.phys.shapes.*;import java.util.*;
/** Reserved cells have no tickers or per-cell inventories. Local +Z is the front. */
public final class DwfGeometry{
 public static final int[][] CELLS;private static final VoxelShape[][]SHAPES;
 static{var cells=new ArrayList<int[]>();cells.add(new int[]{0,0,0});for(int y=0;y<6;y++)for(int x=-7;x<=7;x++)for(int z=-3;z<=3;z++)if(x!=0||y!=0||z!=0)cells.add(new int[]{x,y,z});CELLS=cells.toArray(int[][]::new);SHAPES=new VoxelShape[4][CELLS.length];for(int d=0;d<4;d++)for(int i=0;i<CELLS.length;i++){var c=CELLS[i];double minX=Math.max(c[0]-.5,-7.45),maxX=Math.min(c[0]+.5,7.45),minZ=Math.max(c[2]-.5,-2.75),maxZ=Math.min(c[2]+.5,2.75);double top=Math.abs(c[0])>4?3.05:5.45;double maxY=Math.min(1,top-c[1]);var shape=maxY<=0||maxX<=minX||maxZ<=minZ?Shapes.empty():Shapes.box(minX-c[0]+.5,0,minZ-c[2]+.5,maxX-c[0]+.5,maxY,maxZ-c[2]+.5);for(int r=0;r<d;r++){var next=new ArrayList<AABB>();for(var b:shape.toAabbs())next.add(new AABB(1-b.maxZ,b.minY,b.minX,1-b.minZ,b.maxY,b.maxX));shape=Shapes.empty();for(var b:next)shape=Shapes.or(shape,Shapes.create(b));}SHAPES[d][i]=shape;}}
 public static BlockPos cell(BlockPos root,Direction facing,int i){var c=CELLS[i];return root.offset(c[0]*facing.getStepZ()+c[2]*facing.getStepX(),c[1],-c[0]*facing.getStepX()+c[2]*facing.getStepZ());}
 public static BlockPos root(BlockPos at,Direction facing,int i){return at.subtract(cell(BlockPos.ZERO,facing,i));}
 public static int chest(int i){var c=CELLS[i];return c[1]==0&&c[2]==0&&Math.abs(c[0])==7?(c[0]<0?0:1):-1;}
 public static Vec3 local(BlockPos root,Direction f,Vec3 world){var d=world.subtract(root.getX()+.5,root.getY(),root.getZ()+.5);return new Vec3(d.x*f.getStepZ()-d.z*f.getStepX(),d.y,d.x*f.getStepX()+d.z*f.getStepZ());}
 public static VoxelShape shape(Direction f,int i){int turn=f==Direction.SOUTH?0:f==Direction.WEST?1:f==Direction.NORTH?2:3;return SHAPES[turn][i];}
 public static AABB bounds(BlockPos root,Direction f){return f.getAxis()==Direction.Axis.Z?new AABB(root.getX()-7,root.getY(),root.getZ()-3,root.getX()+8,root.getY()+6,root.getZ()+4):new AABB(root.getX()-3,root.getY(),root.getZ()-7,root.getX()+4,root.getY()+6,root.getZ()+8);}
 private DwfGeometry(){}
}
