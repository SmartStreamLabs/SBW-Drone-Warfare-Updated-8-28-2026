package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RadarJammerTowerPartBlock extends Block {
    public static final EnumProperty<RadarJammerTowerPart> PART = EnumProperty.create("part", RadarJammerTowerPart.class);
    private static final VoxelShape PART_SHAPE = Shapes.block();

    public RadarJammerTowerPartBlock(Properties properties) {
        super(properties.randomTicks());
        registerDefaultState(stateDefinition.any()
                .setValue(HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.NORTH)
                .setValue(PART, RadarJammerTowerPart.WEST));
    }

    @Override public void randomTick(BlockState state,net.minecraft.server.level.ServerLevel level,BlockPos pos,net.minecraft.util.RandomSource random){var master=findMasterPos(state,pos);if(level.hasChunkAt(master)&&!level.getBlockState(master).is(AddonBlocks.RADAR_JAMMER_TOWER.get()))level.removeBlock(pos,false);}

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockPos master=findMasterPos(state,pos);
        return Rj01Shapes.cell(state.getValue(HorizontalDirectionalBlock.FACING),pos.subtract(master));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockPos master=findMasterPos(state,pos);
        return Rj01Shapes.cell(state.getValue(HorizontalDirectionalBlock.FACING),pos.subtract(master));
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return JammerBlock.openJammer(level, findMasterPos(state, pos), player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !RadarJammerTowerBlock.isRemovingMultiblock()) {
            RadarJammerTowerBlock.removeMultiblock(level, findMasterPos(state, pos), true, state.getValue(HorizontalDirectionalBlock.FACING));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING, PART);
    }

    static BlockPos findMasterPos(BlockState state, BlockPos pos) {
        return state.getValue(PART).masterPos(pos, state.getValue(HorizontalDirectionalBlock.FACING));
    }
}
