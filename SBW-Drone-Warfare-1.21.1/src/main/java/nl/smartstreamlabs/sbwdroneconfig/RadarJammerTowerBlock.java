package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RadarJammerTowerBlock extends JammerBlock {
    private static final VoxelShape MASTER_SHAPE = Shapes.block();
    private static final ThreadLocal<Boolean> REMOVING_MULTIBLOCK = ThreadLocal.withInitial(() -> false);

    public RadarJammerTowerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
        return canPlaceMultiblock(context) ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            placeMultiblockParts(level, pos, level.getBlockState(pos));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isRemovingMultiblock()) {
            removeMultiblock(level, pos, false);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return MASTER_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return MASTER_SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadarJammerTowerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide()
                ? null
                : createTickerHelper(blockEntityType, AddonBlockEntities.RADAR_JAMMER_TOWER.get(), JammerBlockEntity::serverTick);
    }

    static boolean isRemovingMultiblock() {
        return REMOVING_MULTIBLOCK.get();
    }

    static void removeMultiblock(Level level, BlockPos masterPos, boolean dropMaster) {
        if (level.isClientSide() || isRemovingMultiblock()) {
            return;
        }

        REMOVING_MULTIBLOCK.set(true);
        try {
            BlockState masterState = level.getBlockState(masterPos);
            if (masterState.is(AddonBlocks.RADAR_JAMMER_TOWER.get())) {
                level.destroyBlock(masterPos, dropMaster);
            }

            Direction facing = masterState.hasProperty(HorizontalDirectionalBlock.FACING)
                    ? masterState.getValue(HorizontalDirectionalBlock.FACING)
                    : Direction.NORTH;
            for (RadarJammerTowerPart part : RadarJammerTowerPart.values()) {
                BlockPos partPos = part.partPos(masterPos, facing);
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(AddonBlocks.RADAR_JAMMER_TOWER_PART.get())
                        && part.masterPos(partPos, partState.getValue(HorizontalDirectionalBlock.FACING)).equals(masterPos)) {
                    level.destroyBlock(partPos, false);
                }
            }
        } finally {
            REMOVING_MULTIBLOCK.set(false);
        }
    }

    private boolean canPlaceMultiblock(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos masterPos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection().getOpposite();
        for (RadarJammerTowerPart part : RadarJammerTowerPart.values()) {
            if (!level.getBlockState(part.partPos(masterPos, facing)).canBeReplaced(context)) {
                return false;
            }
        }
        return true;
    }

    private void placeMultiblockParts(Level level, BlockPos masterPos, BlockState masterState) {
        Direction facing = masterState.getValue(HorizontalDirectionalBlock.FACING);
        BlockState partState = AddonBlocks.RADAR_JAMMER_TOWER_PART.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing);
        for (RadarJammerTowerPart part : RadarJammerTowerPart.values()) {
            level.setBlock(part.partPos(masterPos, facing), partState.setValue(RadarJammerTowerPartBlock.PART, part), 3);
        }
    }
}
