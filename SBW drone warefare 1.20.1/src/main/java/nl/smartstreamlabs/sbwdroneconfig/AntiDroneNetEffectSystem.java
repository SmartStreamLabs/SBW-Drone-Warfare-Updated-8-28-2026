package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class AntiDroneNetEffectSystem {
    private static final String TAG_NET_COOLDOWN_UNTIL = "sbwdroneconfigAntiDroneNetCooldownUntil";
    private static final int NET_COOLDOWN_TICKS = 12;

    private AntiDroneNetEffectSystem() {
    }

    public static boolean isOnCooldown(ServerLevel serverLevel, Entity drone) {
        return serverLevel != null
                && drone != null
                && drone.getPersistentData().getLong(TAG_NET_COOLDOWN_UNTIL) > serverLevel.getGameTime();
    }

    public static boolean jamDrone(ServerLevel serverLevel, Entity entity, Vec3 effectPosition, String sourceDescription) {
        if (serverLevel == null || entity == null || !entity.isAlive() || isOnCooldown(serverLevel, entity)) {
            return false;
        }

        Vec3 position = effectPosition == null ? entity.position() : effectPosition;
        String notificationKey = FiberOpticLinkSystem.handleAntiDroneNetCut(serverLevel, entity);
        boolean disconnected = notificationKey != null || DroneWaterDisconnectSystem.forceControlDisconnect(serverLevel, entity);
        SbwCompat.resetDroneInput(entity);
        entity.getPersistentData().putLong(TAG_NET_COOLDOWN_UNTIL, serverLevel.getGameTime() + NET_COOLDOWN_TICKS);
        SbwDroneRangeConfig.LOGGER.info("Anti-Drone Net jammed drone from {}", sourceDescription == null ? "unknown source" : sourceDescription);
        if (!entity.onGround()) {
            entity.getPersistentData().putBoolean("down", true);
        }
        entity.setDeltaMovement(0.0D, Math.min(0.0D, entity.getDeltaMovement().y), 0.0D);
        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                entity.getX(),
                entity.getY() + 0.35D,
                entity.getZ(),
                8,
                0.18D,
                0.18D,
                0.18D,
                0.02D
        );
        serverLevel.playSound(
                null,
                BlockPos.containing(position),
                SoundEvents.REDSTONE_TORCH_BURNOUT,
                SoundSource.BLOCKS,
                1.0F,
                0.8F
        );

        if (disconnected) {
            notifyController(
                    serverLevel,
                    entity,
                    notificationKey != null ? notificationKey : "message.sbwdroneconfig.drone_net_connection_lost"
            );
        }
        return true;
    }

    private static void notifyController(ServerLevel serverLevel, Entity drone, String messageKey) {
        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            if ((controllerUuid != null && controllerUuid.equals(player.getUUID()))
                    || SbwCompat.isUsingLinkedMonitorForDrone(player, drone)) {
                player.displayClientMessage(
                        Component.translatable(messageKey).withStyle(ChatFormatting.RED),
                        true
                );
            }
        }
    }
}
