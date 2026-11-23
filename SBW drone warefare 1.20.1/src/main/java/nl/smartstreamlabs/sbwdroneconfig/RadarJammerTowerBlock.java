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
            removeMultiblock(level, pos, false, state.getValue(HorizontalDirectionalBlock.FACING));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Rj01Shapes.cell(state.getValue(HorizontalDirectionalBlock.FACING), BlockPos.ZERO);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Rj01Shapes.cell(state.getValue(HorizontalDirectionalBlock.FACING), BlockPos.ZERO);
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
        return createTickerHelper(blockEntityType, AddonBlockEntities.RADAR_JAMMER_TOWER.get(), RadarJammerTowerBlockEntity::rj01Tick);
    }

    static boolean isRemovingMultiblock() {
        return REMOVING_MULTIBLOCK.get();
    }

    static void removeMultiblock(Level level, BlockPos masterPos, boolean dropMaster) {
        if (level.isClientSide() || isRemovingMultiblock()) {
            return;
        }

        var current=level.getBlockState(masterPos);
        removeMultiblock(level,masterPos,dropMaster,current.hasProperty(HorizontalDirectionalBlock.FACING)?current.getValue(HorizontalDirectionalBlock.FACING):Direction.NORTH);
    }
    static void removeMultiblock(Level level,BlockPos masterPos,boolean dropMaster,Direction facing){
        if(level.isClientSide()||isRemovingMultiblock())return;
        REMOVING_MULTIBLOCK.set(true);
        try {
            BlockState masterState = level.getBlockState(masterPos);
            if (masterState.is(AddonBlocks.RADAR_JAMMER_TOWER.get())) {
                level.destroyBlock(masterPos, dropMaster);
            }

            for (RadarJammerTowerPart part : RadarJammerTowerPart.values()) {
                BlockPos partPos = part.partPos(masterPos, facing);
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(AddonBlocks.RADAR_JAMMER_TOWER_PART.get())
                        && RadarJammerTowerPartBlock.findMasterPos(partState,partPos).equals(masterPos)) {
                    level.destroyBlock(partPos, false);
                }
            }
        } finally {
            REMOVING_MULTIBLOCK.set(false);
        }
    }

    // Idempotent old-layout migration. Never overwrite a foreign block or load chunks.
    static boolean reconcileParts(Level level,BlockPos masterPos,BlockState state){
        boolean complete=true;Direction facing=state.getValue(HorizontalDirectionalBlock.FACING);
        REMOVING_MULTIBLOCK.set(true);
        try{for(var part:RadarJammerTowerPart.values()){
            BlockPos pos=part.partPos(masterPos,facing);if(!level.hasChunkAt(pos)){complete=false;continue;}
            BlockState found=level.getBlockState(pos);boolean ours=found.is(AddonBlocks.RADAR_JAMMER_TOWER_PART.get())&&RadarJammerTowerPartBlock.findMasterPos(found,pos).equals(masterPos);
            if(!part.required()){if(ours)level.removeBlock(pos,false);continue;}
            if(ours)continue;
            if(found.isAir())level.setBlock(pos,AddonBlocks.RADAR_JAMMER_TOWER_PART.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,facing).setValue(RadarJammerTowerPartBlock.PART,part),3);
            // Occupied cells are deliberately left intact; no player data is deleted.
        }}finally{REMOVING_MULTIBLOCK.set(false);}return complete;
    }

    private boolean canPlaceMultiblock(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos masterPos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection().getOpposite();
        if(!RadarJammerTowerBlockEntity.stationBelow(level,masterPos)){if(context.getPlayer()!=null)context.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Place the radar tower on a Charging Station."),true);return false;}
        for (RadarJammerTowerPart part : RadarJammerTowerPart.values()) {
            if (part.required() && (level.isOutsideBuildHeight(part.partPos(masterPos,facing)) || !level.getBlockState(part.partPos(masterPos, facing)).canBeReplaced(context))) {
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
            if(part.required())level.setBlock(part.partPos(masterPos, facing), partState.setValue(RadarJammerTowerPartBlock.PART, part), 3);
        }
    }
}
