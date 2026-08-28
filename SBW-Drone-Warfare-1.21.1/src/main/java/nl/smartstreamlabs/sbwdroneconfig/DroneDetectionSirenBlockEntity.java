package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DroneDetectionSirenBlockEntity extends BlockEntity {
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final int SIREN_SOUND_REPEAT_TICKS = 280;

    private UUID ownerUuid;
    private int activeTicksRemaining;
    private long nextAlarmSoundTick;
    private final Set<String> whitelistedGamertags = new LinkedHashSet<>();

    public DroneDetectionSirenBlockEntity(BlockPos pos, BlockState blockState) {
        super(AddonBlockEntities.DRONE_DETECTION_SIREN.get(), pos, blockState);
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    public boolean canConfigure(ServerPlayer player) {
        return ownerUuid == null || ownerUuid.equals(player.getUUID()) || player.hasPermissions(2);
    }

    public List<String> getBlacklistedGamertags() {
        return new ArrayList<>(whitelistedGamertags);
    }

    public boolean addBlacklistedGamertag(String gamertag) {
        String normalized = normalizeGamertag(gamertag);
        if (normalized.isEmpty()) {
            return false;
        }
        boolean changed = whitelistedGamertags.add(normalized);
        if (changed) {
            setChanged();
        }
        return changed;
    }

    public boolean removeBlacklistedGamertag(String gamertag) {
        String normalized = normalizeGamertag(gamertag);
        if (normalized.isEmpty()) {
            return false;
        }
        boolean changed = whitelistedGamertags.remove(normalized);
        if (changed) {
            setChanged();
        }
        return changed;
    }

    public void refreshDetectionState(ServerLevel level) {
        BlockState state = getBlockState();
        if (hasDetectedDrone(level, getBlockPos())) {
            activeTicksRemaining = Math.max(1, AddonConfig.droneSirenCooldownTicks());
            if (!state.getValue(DroneDetectionSirenBlock.ACTIVE)) {
                setActive(level, state, true);
            }
            return;
        }

        activeTicksRemaining = 0;
        nextAlarmSoundTick = 0L;
        if (state.getValue(DroneDetectionSirenBlock.ACTIVE)) {
            setActive(level, state, false);
        } else {
            setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DroneDetectionSirenBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();

        if (!AddonConfig.enableDroneSiren()) {
            blockEntity.activeTicksRemaining = 0;
            blockEntity.setActive(serverLevel, state, false);
            return;
        }

        if (Math.floorMod(gameTime + pos.asLong(), SCAN_INTERVAL_TICKS) == 0L && blockEntity.hasDetectedDrone(serverLevel, pos)) {
            blockEntity.activeTicksRemaining = Math.max(1, AddonConfig.droneSirenCooldownTicks());
            if (!state.getValue(DroneDetectionSirenBlock.ACTIVE)) {
                blockEntity.setActive(serverLevel, state, true);
                state = serverLevel.getBlockState(pos);
            }
        }

        if (blockEntity.activeTicksRemaining > 0) {
            blockEntity.activeTicksRemaining--;

            if (!state.getValue(DroneDetectionSirenBlock.ACTIVE)) {
                blockEntity.setActive(serverLevel, state, true);
                state = serverLevel.getBlockState(pos);
            }

            if (gameTime >= blockEntity.nextAlarmSoundTick) {
                blockEntity.playAlarm(serverLevel, pos);
                blockEntity.nextAlarmSoundTick = gameTime + SIREN_SOUND_REPEAT_TICKS;
            }
        } else if (state.getValue(DroneDetectionSirenBlock.ACTIVE)) {
            blockEntity.setActive(serverLevel, state, false);
        }
    }

    private boolean hasDetectedDrone(ServerLevel level, BlockPos pos) {
        if (!net.minecraftforge.fml.ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            return false;
        }

        double range = AddonConfig.droneSirenRange();
        AABB searchBox = new AABB(pos).inflate(range);
        return !level.getEntities((Entity) null, searchBox, entity -> shouldDetectDrone(level, entity)).isEmpty();
    }

    private boolean shouldDetectDrone(ServerLevel level, Entity entity) {
        if (entity == null || !entity.isAlive() || !SbwCompat.isDrone(entity)) {
            return false;
        }

        SbwCompat.DroneOwnerIdentity ownerIdentity = SbwCompat.resolveDroneOwner(level, entity);
        if (findWhitelistedGamertag(level, entity, ownerIdentity) != null || isIgnoredOwner(ownerIdentity)) {
            return false;
        }

        if (!AddonConfig.detectFriendlyDrones()
                && ownerUuid != null
                && ownerIdentity.hasUuid()
                && ownerUuid.equals(ownerIdentity.uuid())) {
            return false;
        }

        if (!ownerIdentity.isKnown() && AddonConfig.ignoreOwnerlessDrones()) {
            return false;
        }

        String droneName = SbwCompat.getDroneCustomName(entity);
        if (droneName == null) {
            return !AddonConfig.ignoreUnnamedDrones();
        }

        if (isIgnoredDroneName(droneName)) {
            return false;
        }

        return true;
    }

    private String findWhitelistedGamertag(ServerLevel level, Entity drone, SbwCompat.DroneOwnerIdentity ownerIdentity) {
        if (whitelistedGamertags.isEmpty()) {
            return null;
        }

        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        if (controllerUuid != null) {
            ServerPlayer controller = level.getServer().getPlayerList().getPlayer(controllerUuid);
            if (controller != null && isWhitelistedGamertag(controller.getGameProfile().getName())) {
                return controller.getGameProfile().getName();
            }
        }

        ServerPlayer linkedController = SbwCompat.findLinkedMonitorController(level, drone);
        if (linkedController != null && isWhitelistedGamertag(linkedController.getGameProfile().getName())) {
            SbwCompat.assignDroneOwner(drone, linkedController);
            return linkedController.getGameProfile().getName();
        }

        if (ownerIdentity.hasName() && isWhitelistedGamertag(ownerIdentity.name())) {
            return ownerIdentity.name();
        }

        if (ownerIdentity.hasUuid()) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerIdentity.uuid());
            if (owner != null && isWhitelistedGamertag(owner.getGameProfile().getName())) {
                return owner.getGameProfile().getName();
            }
        }

        return null;
    }

    private boolean isIgnoredOwner(SbwCompat.DroneOwnerIdentity ownerIdentity) {
        if (ownerIdentity.hasUuid() && AddonConfig.ignoredDroneOwnerUUIDs().contains(ownerIdentity.uuid())) {
            return true;
        }
        return ownerIdentity.hasName()
                && AddonConfig.ignoredDroneOwnerNames().contains(normalizeGamertag(ownerIdentity.name()));
    }

    private boolean isWhitelistedGamertag(String gamertag) {
        String normalized = normalizeGamertag(gamertag);
        return !normalized.isEmpty() && whitelistedGamertags.contains(normalized);
    }

    private boolean isIgnoredDroneName(String droneName) {
        String normalizedName = normalizeDroneName(droneName);
        if (normalizedName.isEmpty()) {
            return false;
        }

        return AddonConfig.ignoredDroneNames().contains(normalizedName);
    }

    private void playAlarm(ServerLevel level, BlockPos pos) {
        SoundEvent sound = AddonSounds.DRONE_DETECTION_SIREN.get();
        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSoundPacket(
                    net.minecraft.core.Holder.direct(sound),
                    SoundSource.BLOCKS,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    2.2F,
                    1.0F,
                    level.random.nextLong()
            ));
        }
    }

    private void setActive(ServerLevel level, BlockState state, boolean active) {
        if (state.getValue(DroneDetectionSirenBlock.ACTIVE) == active) {
            return;
        }

        BlockState updatedState = state.setValue(DroneDetectionSirenBlock.ACTIVE, active);
        level.setBlock(worldPosition, updatedState, 3);
        level.updateNeighborsAt(worldPosition, updatedState.getBlock());
        setChanged();

        if (!active) {
            nextAlarmSoundTick = 0L;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        tag.putInt("ActiveTicksRemaining", activeTicksRemaining);
        tag.putLong("NextAlarmSoundTick", nextAlarmSoundTick);
        String joinedWhitelist = String.join("\n", whitelistedGamertags);
        tag.putString("WhitelistedGamertags", joinedWhitelist);
        tag.putString("BlacklistedGamertags", joinedWhitelist);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        activeTicksRemaining = tag.getInt("ActiveTicksRemaining");
        nextAlarmSoundTick = tag.getLong("NextAlarmSoundTick");
        whitelistedGamertags.clear();
        String rawNames = tag.contains("WhitelistedGamertags") ? tag.getString("WhitelistedGamertags") : tag.getString("BlacklistedGamertags");
        if (!rawNames.isBlank()) {
            for (String name : rawNames.split("\\R")) {
                String normalized = normalizeGamertag(name);
                if (!normalized.isEmpty()) {
                    whitelistedGamertags.add(normalized);
                }
            }
        }
    }

    private static String normalizeDroneName(String droneName) {
        return AddonConfig.normalizeName(droneName, AddonConfig.blacklistMatchCaseInsensitive());
    }

    private static String normalizeGamertag(String gamertag) {
        return AddonConfig.normalizeName(gamertag, AddonConfig.blacklistMatchCaseInsensitive());
    }
}
