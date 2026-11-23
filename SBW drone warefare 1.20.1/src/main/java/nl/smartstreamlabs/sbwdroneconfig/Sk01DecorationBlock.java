package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.level.block.state.properties.*;import net.minecraft.world.item.context.BlockPlaceContext;import net.minecraft.world.phys.shapes.*;
/** A normal decorative building block, independent of generated death remains. */
public final class Sk01DecorationBlock extends BaseEntityBlock {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 private static final VoxelShape ALONG_Z=Shapes.box(-.006,0,-.451,1.006,.436,1.451),ALONG_X=Shapes.box(-.451,0,-.006,1.451,.436,1.006);
 public Sk01DecorationBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
 @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return state.getValue(FACING).getAxis()==Direction.Axis.Z?ALONG_Z:ALONG_X;}
 @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.empty();}
 @Override public VoxelShape getOcclusionShape(BlockState state,BlockGetter level,BlockPos pos){return Shapes.empty();}
 @Override public RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
 @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new Sk01DecorationBlockEntity(pos,state);}
 @Override public BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
 @Override public BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(FACING)));}
}
