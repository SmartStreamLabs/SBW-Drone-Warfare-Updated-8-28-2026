package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AntiDroneNetPanelBlock extends Block {
    public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;

    private static final VoxelShape BOTTOM_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    private static final VoxelShape TOP_SHAPE = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public AntiDroneNetPanelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, SlabType.BOTTOM));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos());
        if (clickedState.is(this)) {
            return clickedState.setValue(TYPE, SlabType.DOUBLE);
        }

        double clickY = context.getClickLocation().y - context.getClickedPos().getY();
        boolean placeTop = context.getClickedFace() == net.minecraft.core.Direction.DOWN
                || (context.getClickedFace().getAxis().isHorizontal() && clickY > 0.5D);
        return defaultBlockState().setValue(TYPE, placeTop ? SlabType.TOP : SlabType.BOTTOM);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (state.getValue(TYPE) == SlabType.DOUBLE || !context.getItemInHand().is(asItem())) {
            return false;
        }
        if (!context.replacingClickedOnBlock()) {
            return true;
        }

        double clickY = context.getClickLocation().y - context.getClickedPos().getY();
        boolean clickedUpperHalf = clickY > 0.5D;
        net.minecraft.core.Direction clickedFace = context.getClickedFace();
        SlabType type = state.getValue(TYPE);
        if (type == SlabType.BOTTOM) {
            return clickedFace == net.minecraft.core.Direction.UP
                    || (clickedFace.getAxis().isHorizontal() && clickedUpperHalf);
        }
        return clickedFace == net.minecraft.core.Direction.DOWN
                || (clickedFace.getAxis().isHorizontal() && !clickedUpperHalf);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getSlabShape(state.getValue(TYPE));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getSlabShape(state.getValue(TYPE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    public static AABB getDroneTriggerBox(BlockState state, BlockPos pos) {
        return createSlabBox(pos, state.getValue(TYPE)).inflate(0.1D);
    }

    private static VoxelShape getSlabShape(SlabType type) {
        return switch (type) {
            case BOTTOM -> BOTTOM_SHAPE;
            case TOP -> TOP_SHAPE;
            case DOUBLE -> Shapes.block();
        };
    }

    private static AABB createSlabBox(BlockPos pos, SlabType type) {
        return switch (type) {
            case BOTTOM -> new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 0.5D, pos.getZ() + 1.0D);
            case TOP -> new AABB(pos.getX(), pos.getY() + 0.5D, pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D);
            case DOUBLE -> new AABB(pos);
        };
    }
}
