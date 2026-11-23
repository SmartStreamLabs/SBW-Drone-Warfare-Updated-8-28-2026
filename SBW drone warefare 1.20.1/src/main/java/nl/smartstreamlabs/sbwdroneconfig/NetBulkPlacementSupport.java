package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class NetBulkPlacementSupport {
    static final String MESSAGE_POS1_SET = "message.sbwdroneconfig.net_pos1_set";
    static final String MESSAGE_POS2_SET = "message.sbwdroneconfig.net_pos2_set";
    static final String MESSAGE_CONFIRM = "message.sbwdroneconfig.net_confirm";
    static final String MESSAGE_CANCELLED = "message.sbwdroneconfig.net_cancelled";
    static final String MESSAGE_NOT_ENOUGH = "message.sbwdroneconfig.net_not_enough";
    static final String MESSAGE_NOT_ENOUGH_DETAILED = "message.sbwdroneconfig.net_not_enough_detailed";
    static final String MESSAGE_TOO_LARGE = "message.sbwdroneconfig.net_too_large";
    static final String MESSAGE_INVALID_PLANE = "message.sbwdroneconfig.net_invalid_plane";
    static final String MESSAGE_BLOCKED = "message.sbwdroneconfig.net_blocked";
    static final String MESSAGE_SUCCESS = "message.sbwdroneconfig.net_success";
    static final String MESSAGE_STARTED = "message.sbwdroneconfig.net_started";
    static final String MESSAGE_DISABLED = "message.sbwdroneconfig.net_disabled";
    static final String MESSAGE_BUSY = "message.sbwdroneconfig.net_busy";
    static final String MESSAGE_UNDO_SUCCESS = "message.sbwdroneconfig.net_undo_success";
    static final String MESSAGE_UNDO_EMPTY = "message.sbwdroneconfig.net_undo_empty";

    private NetBulkPlacementSupport() {
    }

    static Optional<NetBulkPlacementKind> kindForItem(ItemStack stack) {
        return NetBulkPlacementKind.fromItemStack(stack);
    }

    static NetBulkPlacementPlan buildPlan(
            NetBulkPlacementKind kind,
            BlockPos pos1,
            BlockPos pos2,
            Direction.Axis requestedPlaneAxis,
            Direction facing,
            BlockGetter level,
            int maxWidth,
            int maxHeight,
            int maxBlocks
    ) throws InvalidSelectionException {
        if (kind == null || pos1 == null || pos2 == null || facing == null) {
            throw new InvalidSelectionException(MESSAGE_INVALID_PLANE);
        }

        Direction.Axis planeAxis = resolvePlaneAxis(kind, pos1, pos2, requestedPlaneAxis, facing);
        int width;
        int height;
        List<NetBulkPlacementPlan.Target> targets = new ArrayList<>();

        if (kind.isCarpet()) {
            int minX = Math.min(pos1.getX(), pos2.getX());
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            width = maxX - minX + 1;
            height = maxZ - minZ + 1;
            validateSize(width, height, maxWidth, maxHeight, maxBlocks);
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos targetPos = new BlockPos(x, pos1.getY(), z);
                    targets.add(new NetBulkPlacementPlan.Target(targetPos, stateFor(kind, level, targetPos, planeAxis, facing)));
                }
            }
            return new NetBulkPlacementPlan(kind, pos1.immutable(), pos2.immutable(), Direction.Axis.Y, facing, width, height, List.copyOf(targets));
        }

        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        height = maxY - minY + 1;
        if (planeAxis == Direction.Axis.Z) {
            int minX = Math.min(pos1.getX(), pos2.getX());
            int maxX = Math.max(pos1.getX(), pos2.getX());
            width = maxX - minX + 1;
            validateSize(width, height, maxWidth, maxHeight, maxBlocks);
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos targetPos = new BlockPos(x, y, pos1.getZ());
                    targets.add(new NetBulkPlacementPlan.Target(targetPos, stateFor(kind, level, targetPos, planeAxis, facing)));
                }
            }
            return new NetBulkPlacementPlan(kind, pos1.immutable(), pos2.immutable(), planeAxis, facing, width, height, List.copyOf(targets));
        }

        if (planeAxis == Direction.Axis.X) {
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            width = maxZ - minZ + 1;
            validateSize(width, height, maxWidth, maxHeight, maxBlocks);
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos targetPos = new BlockPos(pos1.getX(), y, z);
                    targets.add(new NetBulkPlacementPlan.Target(targetPos, stateFor(kind, level, targetPos, planeAxis, facing)));
                }
            }
            return new NetBulkPlacementPlan(kind, pos1.immutable(), pos2.immutable(), planeAxis, facing, width, height, List.copyOf(targets));
        }

        throw new InvalidSelectionException(MESSAGE_INVALID_PLANE);
    }

    static Direction.Axis resolvePlaneAxis(
            NetBulkPlacementKind kind,
            BlockPos pos1,
            BlockPos pos2,
            Direction.Axis requestedPlaneAxis,
            Direction facing
    ) throws InvalidSelectionException {
        if (kind.isCarpet()) {
            return Direction.Axis.Y;
        }
        if (requestedPlaneAxis == Direction.Axis.X || requestedPlaneAxis == Direction.Axis.Z) {
            return requestedPlaneAxis;
        }
        if (facing.getAxis() == Direction.Axis.X || facing.getAxis() == Direction.Axis.Z) {
            return facing.getAxis();
        }
        int dx = Math.abs(pos2.getX() - pos1.getX());
        int dz = Math.abs(pos2.getZ() - pos1.getZ());
        if (dx == 0 && dz == 0) {
            throw new InvalidSelectionException(MESSAGE_INVALID_PLANE);
        }
        return dx >= dz ? Direction.Axis.Z : Direction.Axis.X;
    }

    static Direction.Axis preferredPlaneAxis(NetBulkPlacementKind kind, BlockPos pos1, BlockPos pos2, Direction facing) throws InvalidSelectionException {
        return resolvePlaneAxis(kind, pos1, pos2, null, facing);
    }

    private static BlockState stateFor(NetBulkPlacementKind kind, BlockGetter level, BlockPos pos, Direction.Axis planeAxis, Direction facing) {
        BlockState state = kind.block().defaultBlockState();
        if (kind == NetBulkPlacementKind.ANTI_DRONE_NET) {
            state = setIfPresent(state, BlockStateProperties.NORTH, planeAxis == Direction.Axis.X);
            state = setIfPresent(state, BlockStateProperties.SOUTH, planeAxis == Direction.Axis.X);
            state = setIfPresent(state, BlockStateProperties.EAST, planeAxis == Direction.Axis.Z);
            state = setIfPresent(state, BlockStateProperties.WEST, planeAxis == Direction.Axis.Z);
        } else if (kind == NetBulkPlacementKind.ANTI_DRONE_NET_PANEL && state.hasProperty(AntiDroneNetPanelBlock.TYPE)) {
            SlabType slabType = switch (facing) {
                case DOWN -> SlabType.TOP;
                case UP -> SlabType.BOTTOM;
                default -> SlabType.DOUBLE;
            };
            state = state.setValue(AntiDroneNetPanelBlock.TYPE, slabType);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && level != null) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos).is(Fluids.WATER));
        }
        return state;
    }

    private static BlockState setIfPresent(BlockState state, net.minecraft.world.level.block.state.properties.BooleanProperty property, boolean value) {
        return state.hasProperty(property) ? state.setValue(property, value) : state;
    }

    private static void validateSize(int width, int height, int maxWidth, int maxHeight, int maxBlocks) throws InvalidSelectionException {
        if (width < 1 || height < 1) {
            throw new InvalidSelectionException(MESSAGE_INVALID_PLANE);
        }
        if (width > maxWidth
                || height > maxHeight
                || (long) width * (long) height > maxBlocks) {
            throw new InvalidSelectionException(MESSAGE_TOO_LARGE);
        }
    }

    static final class InvalidSelectionException extends Exception {
        private final String messageKey;

        InvalidSelectionException(String messageKey) {
            super(messageKey);
            this.messageKey = messageKey;
        }

        String messageKey() {
            return messageKey;
        }
    }
}
