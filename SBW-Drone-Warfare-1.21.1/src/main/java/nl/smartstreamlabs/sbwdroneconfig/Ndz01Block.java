package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/** Vanilla double-height lifecycle: only the lower half owns loot; no block entity or ticker. */
public class Ndz01Block extends DoublePlantBlock {
    public static final com.mojang.serialization.MapCodec<Ndz01Block> CODEC=simpleCodec(Ndz01Block::new);
    @Override public com.mojang.serialization.MapCodec<? extends Ndz01Block> codec(){return CODEC;}
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape[][] SHAPES=new VoxelShape[4][2];
    static {
        // Native model units; centred on (8,0,8), front -Z. Physical detail stays deliberately bounded.
        double[][] boxes={{-1.52,0,-.352,1.52,.85,2.688},{-.576,.24,.592,.576,30.32,1.744},{-5.648,16.464,-.352,5.648,31.224,.28},{-1.15,19.45,.18,1.15,20.25,2.05},{-1.15,27.45,.18,1.15,28.2,2.05}};
        for(Direction f:Direction.Plane.HORIZONTAL) for(int half=0;half<2;half++) {
            VoxelShape result=Shapes.empty();int turn=switch(f){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
            for(double[] box:boxes){double y0=Math.max(box[1],half*16),y1=Math.min(box[4],(half+1)*16);if(y1<=y0)continue;
                double minX=32,minZ=32,maxX=-32,maxZ=-32;
                for(double x:new double[]{box[0],box[3]})for(double z:new double[]{box[2],box[5]}){double rx=x,rz=z;for(int n=0;n<turn;n++){double t=rx;rx=-rz;rz=t;}minX=Math.min(minX,rx);maxX=Math.max(maxX,rx);minZ=Math.min(minZ,rz);maxZ=Math.max(maxZ,rz);}
                result=Shapes.or(result,Block.box(8+minX,y0-half*16,8+minZ,8+maxX,y1-half*16,8+maxZ));
            } SHAPES[f.get2DDataValue()][half]=result.optimize();
        }
    }
    public Ndz01Block(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(HALF,DoubleBlockHalf.LOWER).setValue(FACING,Direction.NORTH));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(FACING);}
    @Override protected boolean mayPlaceOn(BlockState ground,BlockGetter level,BlockPos pos){return ground.isFaceSturdy(level,pos,Direction.UP);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){BlockState s=super.getStateForPlacement(c);return s==null?null:s.setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){level.setBlock(pos.above(),state.setValue(HALF,DoubleBlockHalf.UPPER),3);}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPES[s.getValue(FACING).get2DDataValue()][s.getValue(HALF)==DoubleBlockHalf.UPPER?1:0];}
    @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return getShape(s,l,p,c);}
    @Override public VoxelShape getOcclusionShape(BlockState s,BlockGetter l,BlockPos p){return Shapes.empty();}
    @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
}
