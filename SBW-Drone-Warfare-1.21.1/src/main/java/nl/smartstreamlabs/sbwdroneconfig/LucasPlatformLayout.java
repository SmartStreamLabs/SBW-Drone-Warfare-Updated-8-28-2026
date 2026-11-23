package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** One ground anchor, twelve occupied cells; coordinates are shared by interaction and rendering. */
public final class LucasPlatformLayout {
    private static final int[][] CELLS={{0,0,0},{1,0,0},{0,-1,0},{1,-1,0},{0,1,0},{1,1,0},{0,0,1},{1,0,1},{0,-1,1},{1,-1,1},{0,1,1},{1,1,1}};
    public static final int PARTS=12;
    public static final double FIT_SCALE=32.0;
    public static final double RAIL_PITCH=-18.0;
    public static final double MOUNT_HEIGHT=fitPoint(new Vec3(0,0,0)).y;
    /** Original geometry -> BB scale once -> drone offset -> rotation about rail pivot -> blocks. */
    public static Vec3 fitPoint(Vec3 source){
        Vec3 relative=source.scale(FIT_SCALE).add(0,14.45-10,0);
        double angle=Math.toRadians(RAIL_PITCH),c=Math.cos(angle),s=Math.sin(angle);
        return new Vec3(relative.x,10+relative.y*c-relative.z*s,relative.y*s+relative.z*c).scale(1.0/16);
    }
    public static Vec3 orient(Vec3 local,Direction facing){
        var r=right(facing);return new Vec3(r.getStepX()*local.x+facing.getStepX()*local.z,local.y,r.getStepZ()*local.x+facing.getStepZ()*local.z);
    }
    public static final double LAUNCH_SPEED=1.15;
    public static final double LAUNCH_RISE=Math.tan(Math.toRadians(-RAIL_PITCH))*LAUNCH_SPEED;
    private LucasPlatformLayout() {}
    public static Direction right(Direction forward) {return forward.getCounterClockWise();}
    public static BlockPos cell(BlockPos anchor,Direction facing,int part) {
        return anchor.relative(right(facing),CELLS[part][0]).relative(facing,CELLS[part][1]).above(CELLS[part][2]);
    }
    public static BlockPos anchor(BlockPos partPos,Direction facing,int part) {
        return partPos.relative(right(facing),-CELLS[part][0]).relative(facing,-CELLS[part][1]).below(CELLS[part][2]);
    }
    public static Vec3 center(BlockPos anchor,Direction facing) {
        return Vec3.atBottomCenterOf(anchor).add(right(facing).getStepX()*.5,0,right(facing).getStepZ()*.5);
    }
    public static Vec3 mount(BlockPos anchor,Direction facing) {return center(anchor,facing).add(orient(fitPoint(Vec3.ZERO),facing));}
    public static Vec3 forward(Direction facing) {return new Vec3(facing.getStepX(),0,facing.getStepZ());}
}
