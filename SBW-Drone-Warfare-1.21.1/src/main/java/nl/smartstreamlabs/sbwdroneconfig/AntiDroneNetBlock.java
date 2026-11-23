package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AntiDroneNetBlock extends IronBarsBlock {
    private static final double JAM_RANGE = 1.5D;

    public AntiDroneNetBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Players should still be able to walk through the anti-drone net.
        return Shapes.empty();
    }

    public static void onDroneBaseTick(Entity drone) {
        if (drone == null || drone.level().isClientSide() || !drone.isAlive() || !SbwCompat.isDrone(drone)) {
            return;
        }

        if (!(drone.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (AntiDroneNetEffectSystem.isOnCooldown(serverLevel, drone)) {
            return;
        }

        if (VehicleAntiDroneNetCollisionSystem.tryJamVehicleNet(serverLevel, drone)) {
            return;
        }

        AABB droneBox = drone.getBoundingBox();
        AABB jammerRangeBox = droneBox.inflate(JAM_RANGE);
        BlockPos min = BlockPos.containing(jammerRangeBox.minX, jammerRangeBox.minY, jammerRangeBox.minZ);
        BlockPos max = BlockPos.containing(jammerRangeBox.maxX, jammerRangeBox.maxY, jammerRangeBox.maxZ);

        int previousChunkX = Integer.MIN_VALUE;
        int previousChunkZ = Integer.MIN_VALUE;
        LevelChunk availableChunk = null;
        for (BlockPos scanPos : BlockPos.betweenClosed(min, max)) {
            int chunkX = scanPos.getX() >> 4;
            int chunkZ = scanPos.getZ() >> 4;
            if (chunkX != previousChunkX || chunkZ != previousChunkZ) {
                previousChunkX = chunkX;
                previousChunkZ = chunkZ;
                availableChunk = serverLevel.getChunkSource().getChunkNow(chunkX, chunkZ);
            }
            // The detection margin can reach an unfinished neighbour before the drone does.
            // Inspect it on a later tick; a trap scan must never generate or wait for terrain.
            if (availableChunk == null) {
                continue;
            }
            BlockState scanState = availableChunk.getBlockState(scanPos);
            if (!isAntiDroneNetTrapBlock(scanState)) {
                continue;
            }

            AABB trapBox = getTrapTriggerBox(scanState, scanPos);
            AABB detectionBox = isThinAntiDroneNetTrap(scanState)
                    ? droneBox.inflate(0.2D)
                    : jammerRangeBox;
            if (!detectionBox.intersects(trapBox)) {
                continue;
            }

            AntiDroneNetEffectSystem.jamDrone(
                    serverLevel,
                    drone,
                    Vec3.atCenterOf(scanPos),
                    "block " + scanPos.toShortString()
            );
            return;
        }
    }

    public static boolean isAntiDroneNetTrapBlock(BlockState state) {
        return state.getBlock() instanceof AntiDroneNetBlock
                || state.getBlock() instanceof AntiDroneNetCarpetBlock
                || state.getBlock() instanceof AntiDroneNetPanelBlock;
    }

    private static boolean isThinAntiDroneNetTrap(BlockState state) {
        return state.getBlock() instanceof AntiDroneNetCarpetBlock || state.getBlock() instanceof AntiDroneNetPanelBlock;
    }

    private static AABB getTrapTriggerBox(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AntiDroneNetCarpetBlock) {
            return AntiDroneNetCarpetBlock.getDroneTriggerBox(pos);
        }
        if (state.getBlock() instanceof AntiDroneNetPanelBlock) {
            return AntiDroneNetPanelBlock.getDroneTriggerBox(state, pos);
        }
        return new AABB(pos);
    }
}
