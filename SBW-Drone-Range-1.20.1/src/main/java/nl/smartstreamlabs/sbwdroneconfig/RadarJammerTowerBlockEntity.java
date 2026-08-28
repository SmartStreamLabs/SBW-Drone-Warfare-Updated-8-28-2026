package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RadarJammerTowerBlockEntity extends JammerBlockEntity {
    public static final int MAX_RANGE = 1200;
    private static final int DEFAULT_RANGE = 1200;
    private static final int RADAR_SOUND_INTERVAL_TICKS = 40;
    private static final int OWNER_ALERT_INTERVAL_TICKS = 100;
    private static final String ENERGY_TAG = "RadarTowerEnergy";
    private static final String CHARGING_TAG = "RadarTowerCharging";

    private int energy;
    private boolean charging;
    private long nextOwnerAlertTick;

    public RadarJammerTowerBlockEntity(BlockPos pos, BlockState blockState) {
        super(AddonBlockEntities.RADAR_JAMMER_TOWER.get(), pos, blockState, MIN_RANGE, MAX_RANGE, DEFAULT_RANGE);
    }

    @Override
    public boolean isLongRangeTower() {
        return true;
    }

    public int getEnergy() {
        return Math.max(0, Math.min(energy, getMaxEnergy()));
    }

    public int getMaxEnergy() {
        return AddonConfig.radarTowerMaxBatteryCapacity();
    }

    public boolean isCharging() {
        return charging;
    }

    public boolean canOpenPoweredGui() {
        return getEnergy() >= AddonConfig.radarTowerMinimumEnergyToActivate();
    }

    public boolean isOperational() {
        return canOpenPoweredGui();
    }

    @Override
    protected void tickBeforeJammerScan(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        tickSolarBattery(serverLevel);
    }

    private void tickSolarBattery(ServerLevel serverLevel) {
        int maxEnergy = getMaxEnergy();
        if (energy > maxEnergy) {
            energy = maxEnergy;
            setChanged();
        }

        boolean wasCharging = charging;
        int oldEnergy = energy;
        boolean solarCharging = canSolarCharge(serverLevel);
        boolean chargingStationCharging = canChargeFromChargingStationBelow(serverLevel);
        int chargeRate = 0;
        if (solarCharging) {
            chargeRate += AddonConfig.radarTowerSolarChargeRate();
        }
        if (chargingStationCharging) {
            chargeRate += AddonConfig.radarTowerChargingStationChargeRate();
        }
        charging = solarCharging || chargingStationCharging;
        if (chargeRate > 0) {
            energy = Math.min(maxEnergy, energy + chargeRate);
        }
        if (energy != oldEnergy || charging != wasCharging) {
            setChanged();
        }
    }

    private boolean canSolarCharge(ServerLevel serverLevel) {
        return serverLevel.isDay()
                && !serverLevel.isRaining()
                && !serverLevel.isThundering()
                && serverLevel.canSeeSky(solarPanelSkyPos());
    }

    private BlockPos solarPanelSkyPos() {
        return worldPosition.above(4);
    }

    private boolean canChargeFromChargingStationBelow(ServerLevel serverLevel) {
        BlockPos baseBelow = worldPosition.below();
        for (int x = -1; x <= 2; x++) {
            for (int z = -1; z <= 2; z++) {
                if (isSbwChargingStation(serverLevel.getBlockState(baseBelow.offset(x, 0, z)).getBlock())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSbwChargingStation(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null || !"superbwarfare".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return "charging_station".equals(path) || "creative_charging_station".equals(path);
    }

    @Override
    protected boolean canJammerOperate(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        return canOpenPoweredGui();
    }

    @Override
    protected void afterActiveJammerScan(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        notifyOwnerOfNearbyDrone(serverLevel, pos, gameTime);
        drainActiveJammerEnergy();
    }

    private void notifyOwnerOfNearbyDrone(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        UUID ownerUuid = getOwnerUuid();
        if (ownerUuid == null || gameTime < nextOwnerAlertTick) {
            return;
        }

        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);
        if (owner == null || owner.level() != serverLevel) {
            return;
        }

        int nearestDistance = findNearestDroneDistance(serverLevel, pos);
        if (nearestDistance < 0) {
            return;
        }

        owner.displayClientMessage(Component.translatable(
                "message.sbwdroneconfig.radar_jammer_tower.drone_nearby",
                nearestDistance), true);
        nextOwnerAlertTick = gameTime + OWNER_ALERT_INTERVAL_TICKS;
    }

    private int findNearestDroneDistance(ServerLevel serverLevel, BlockPos pos) {
        double range = getJammerRange();
        AABB searchBox = new AABB(pos).inflate(range);
        Vec3 towerCenter = Vec3.atCenterOf(pos);
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : serverLevel.getEntities((Entity) null, searchBox, this::isTowerAlertDrone)) {
            double distance = entity.position().distanceTo(towerCenter);
            if (distance <= range && distance < nearestDistance) {
                nearestDistance = distance;
            }
        }

        return nearestDistance == Double.MAX_VALUE ? -1 : Math.max(1, (int) Math.round(nearestDistance));
    }

    private boolean isTowerAlertDrone(Entity entity) {
        return entity != null
                && entity.isAlive()
                && !entity.isRemoved()
                && (SbwCompat.isDrone(entity)
                || entity instanceof CubedFpvDroneEntity
                || entity instanceof LucasDroneEntity);
    }

    public void drainActiveJammerEnergy() {
        int oldEnergy = energy;
        energy = Math.max(0, energy - AddonConfig.radarTowerJammerEnergyDrainRate());
        if (energy != oldEnergy) {
            setChanged();
        }
    }

    @Override
    protected void playActiveJammerSound(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        if (Math.floorMod(gameTime + pos.asLong(), RADAR_SOUND_INTERVAL_TICKS) != 0L) {
            return;
        }
        serverLevel.playSound(null, pos, AddonSounds.RADAR_JAMMER_TOWER_LOOP.get(), SoundSource.BLOCKS, 0.9F, 1.0F);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(ENERGY_TAG, getEnergy());
        tag.putBoolean(CHARGING_TAG, charging);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = Math.max(0, Math.min(tag.getInt(ENERGY_TAG), getMaxEnergy()));
        charging = tag.getBoolean(CHARGING_TAG);
    }
}
