package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.phys.*;import net.minecraft.world.level.chunk.LevelChunk;import java.util.*;
/** Bounded collision-only settling. No entity, velocity, world mutation or chunk tickets. */
public final class Sk01Placement {
 private static final double[][]OFFSETS={{0,0},{.75,0},{-.75,0},{0,.75},{0,-.75},{1.5,0},{-1.5,0},{0,1.5},{0,-1.5},{.75,.75},{-.75,.75},{.75,-.75},{-.75,-.75}};
 private final Sk01Entry original;private int column,y;public Vec3 result;public boolean waiting;
 public Sk01Placement(Sk01Entry e,ServerLevel l){original=e;y=Math.min(l.getMaxBuildHeight()-1,Math.max(l.getMinBuildHeight(),(int)Math.floor(e.y())+1));}
 private static LevelChunk loaded(ServerLevel l,int x,int z){return l.getChunkSource().getChunkNow(x>>4,z>>4);}
 public static net.minecraft.world.level.BlockGetter view(ServerLevel l){return new net.minecraft.world.level.BlockGetter(){
  public net.minecraft.world.level.block.state.BlockState getBlockState(BlockPos p){var c=loaded(l,p.getX(),p.getZ());return c==null?net.minecraft.world.level.block.Blocks.AIR.defaultBlockState():c.getBlockState(p);}
  public net.minecraft.world.level.material.FluidState getFluidState(BlockPos p){return getBlockState(p).getFluidState();}
  public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(BlockPos p){var c=loaded(l,p.getX(),p.getZ());return c==null?null:c.getBlockEntity(p,LevelChunk.EntityCreationType.CHECK);}
  public int getHeight(){return l.getHeight();}public int getMinBuildHeight(){return l.getMinBuildHeight();}
 };}
 public boolean step(ServerLevel l){waiting=false;if(column>=OFFSETS.length){result=new Vec3(original.x(),Math.max(l.getMinBuildHeight()-.5,original.y()),original.z());return false;}
  double x=original.x()+OFFSETS[column][0],z=original.z()+OFFSETS[column][1];int bx=(int)Math.floor(x),bz=(int)Math.floor(z);var chunk=loaded(l,bx,bz);if(chunk==null){if(column==0){waiting=true;return true;}next(l);return true;}
  y=Math.min(y,Math.max(l.getMinBuildHeight(),chunk.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR,bx&15,bz&15)));
  var at=new BlockPos(bx,y,bz);var readOnly=view(l);var shape=chunk.getBlockState(at).getCollisionShape(readOnly,at);double top=Double.NEGATIVE_INFINITY;
  for(var a:shape.toAabbs())if(x>=bx+a.minX&&x<=bx+a.maxX&&z>=bz+a.minZ&&z<=bz+a.maxZ)top=Math.max(top,y+a.maxY);
  if(Double.isFinite(top)){var proposed=new Vec3(x,top+.012,z);var body=original.settled(proposed).bounds();double raise=0;boolean available=true;
   for(int xx=(int)Math.floor(body.minX);xx<=Math.floor(body.maxX);xx++)for(int zz=(int)Math.floor(body.minZ);zz<=Math.floor(body.maxZ);zz++)for(int yy=(int)Math.floor(body.minY);yy<=Math.floor(body.maxY);yy++){
    var c=loaded(l,xx,zz);if(c==null){available=false;continue;}var p=new BlockPos(xx,yy,zz);for(var a:c.getBlockState(p).getCollisionShape(readOnly,p).toAabbs())if(a.move(p).intersects(body))raise=Math.max(raise,yy+a.maxY-body.minY+.012);
   }
   if(available&&raise<=.61){proposed=proposed.add(0,raise,0);if(clear(l,original.settled(proposed).bounds())){result=proposed;return false;}}
   next(l);return true;
  }
  if(--y<l.getMinBuildHeight())next(l);return true;
 }
 private boolean clear(ServerLevel l,AABB body){var readOnly=view(l);for(int x=(int)Math.floor(body.minX);x<=Math.floor(body.maxX);x++)for(int z=(int)Math.floor(body.minZ);z<=Math.floor(body.maxZ);z++)for(int y=(int)Math.floor(body.minY);y<=Math.floor(body.maxY);y++){var c=loaded(l,x,z);if(c==null)return false;var p=new BlockPos(x,y,z);for(var a:c.getBlockState(p).getCollisionShape(readOnly,p).toAabbs())if(a.move(p).intersects(body))return false;}return true;}
 private void next(ServerLevel l){column++;y=Math.min(l.getMaxBuildHeight()-1,Math.max(l.getMinBuildHeight(),(int)Math.floor(original.y())+1));}
}
