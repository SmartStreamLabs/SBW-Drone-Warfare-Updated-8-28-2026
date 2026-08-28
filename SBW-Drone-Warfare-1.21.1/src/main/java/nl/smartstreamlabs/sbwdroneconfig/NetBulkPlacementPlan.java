package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

record NetBulkPlacementPlan(
        NetBulkPlacementKind kind,
        BlockPos pos1,
        BlockPos pos2,
        Direction.Axis planeAxis,
        Direction facing,
        int width,
        int height,
        List<Target> targets
) {
    int blockCount() {
        return targets.size();
    }

    record Target(BlockPos pos, BlockState state) {
    }
}
