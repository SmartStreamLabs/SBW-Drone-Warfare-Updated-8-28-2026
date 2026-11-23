package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/** Bounded, loaded-only collision/fluid query. No height-map column loading or world ray cast. */
public final class DroneOsdGround implements BlockGetter {
    private Level level;private ChunkAccess centre;private int cx,cz;private boolean missing;
    private final BlockPos.MutableBlockPos cursor=new BlockPos.MutableBlockPos();
    public double surface(Level world,Vec3 position){
        level=world;missing=false;cx=((int)Math.floor(position.x))>>4;cz=((int)Math.floor(position.z))>>4;
        centre=world.getChunkSource().getChunk(cx,cz,ChunkStatus.FULL,false);
        if(centre==null)return Double.NaN;
        int x=(int)Math.floor(position.x),z=(int)Math.floor(position.z),start=Math.min(world.getMaxBuildHeight()-1,(int)Math.floor(position.y));
        int bottom=Math.max(world.getMinBuildHeight(),start-255);double lx=position.x-x,lz=position.z-z;
        for(int y=start;y>=bottom;y--){
            cursor.set(x,y,z);BlockState state=centre.getBlockState(cursor);if(state.isAir())continue;
            double top=Double.NaN;
            var shape=state.getCollisionShape(this,cursor);
            for(var box:shape.toAabbs())if(lx>=box.minX&&lx<=box.maxX&&lz>=box.minZ&&lz<=box.maxZ&&y+box.maxY<=position.y+.001)top=Double.isNaN(top)?y+box.maxY:Math.max(top,y+box.maxY);
            var fluid=state.getFluidState();if(!fluid.isEmpty()){double fy=y+fluid.getHeight(this,cursor);if(fy<=position.y+.001)top=Double.isNaN(top)?fy:Math.max(top,fy);}
            if(missing)return Double.NaN;
            if(Double.isFinite(top))return top;
        }
        return Double.NaN;
    }
    private ChunkAccess chunk(BlockPos p){int x=p.getX()>>4,z=p.getZ()>>4;var c=x==cx&&z==cz?centre:level.getChunkSource().getChunk(x,z,ChunkStatus.FULL,false);if(c==null)missing=true;return c;}
    @Override public BlockState getBlockState(BlockPos p){var c=chunk(p);return c==null?Blocks.AIR.defaultBlockState():c.getBlockState(p);}
    @Override public FluidState getFluidState(BlockPos p){return getBlockState(p).getFluidState();}
    @Override public BlockEntity getBlockEntity(BlockPos p){var c=chunk(p);return c instanceof LevelChunk l?l.getBlockEntity(p):null;}
    @Override public int getHeight(){return level.getHeight();}
    @Override public int getMinBuildHeight(){return level.getMinBuildHeight();}
    public void clear(){level=null;centre=null;}
}
