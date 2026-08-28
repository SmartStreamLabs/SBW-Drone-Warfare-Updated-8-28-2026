package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
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
        return RadarJammerTowerSettings.MAX_BATTERY_CAPACITY;
    }

    public boolean isCharging() {
        return charging;
    }

    public boolean canOpenPoweredGui() {
        return getEnergy() >= RadarJammerTowerSettings.MINIMUM_ENERGY_TO_ACTIVATE;
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
        int chargeRate = 0;
        boolean solarCharging = serverLevel.isDay() && !serverLevel.isRaining() && !serverLevel.isThundering()
                && serverLevel.canSeeSky(worldPosition.above(4));
        boolean chargingStationCharging = canChargeFromChargingStationBelow(serverLevel);
        if (solarCharging) {
            chargeRate += RadarJammerTowerSettings.SOLAR_CHARGE_RATE;
        }
        if (chargingStationCharging) {
            chargeRate += RadarJammerTowerSettings.CHARGING_STATION_CHARGE_RATE;
        }
        charging = solarCharging || chargingStationCharging;
        if (chargeRate > 0) {
            energy = Math.min(maxEnergy, energy + chargeRate);
        }
        if (energy != oldEnergy || charging != wasCharging) {
            setChanged();
        }
    }

    private boolean canChargeFromChargingStationBelow(ServerLevel serverLevel) {
        BlockPos baseBelow = worldPosition.below();
        for (int x = -1; x <= 2; x++) {
            for (int z = -1; z <= 2; z++) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(serverLevel.getBlockState(baseBelow.offset(x, 0, z)).getBlock());
                if (id != null && "superbwarfare".equals(id.getNamespace())
                        && ("charging_station".equals(id.getPath()) || "creative_charging_station".equals(id.getPath()))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean canJammerOperate(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        return canOpenPoweredGui();
    }

    @Override
    protected void afterActiveJammerScan(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        notifyOwnerOfNearbyDrone(serverLevel, pos, gameTime);
        int oldEnergy = energy;
        energy = Math.max(0, energy - RadarJammerTowerSettings.JAMMER_ENERGY_DRAIN_RATE);
        if (energy != oldEnergy) {
            setChanged();
        }
    }

    private void notifyOwnerOfNearbyDrone(ServerLevel level, BlockPos pos, long gameTime) {
        UUID ownerUuid = getOwnerUuid();
        if (ownerUuid == null || gameTime < nextOwnerAlertTick) {
            return;
        }
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUuid);
        if (owner == null || owner.level() != level) {
            return;
        }

        AABB searchBox = new AABB(pos).inflate(getJammerRange());
        Vec3 towerCenter = Vec3.atCenterOf(pos);
        double nearest = Double.MAX_VALUE;
        for (Entity entity : level.getEntities((Entity) null, searchBox, this::isTowerAlertDrone)) {
            double distance = entity.position().distanceTo(towerCenter);
            if (distance <= getJammerRange() && distance < nearest) {
                nearest = distance;
            }
        }
        if (nearest != Double.MAX_VALUE) {
            owner.displayClientMessage(Component.translatable("message.sbwdroneconfig.radar_jammer_tower.drone_nearby", Math.max(1, (int) Math.round(nearest))), true);
            nextOwnerAlertTick = gameTime + OWNER_ALERT_INTERVAL_TICKS;
        }
    }

    private boolean isTowerAlertDrone(Entity entity) {
        return entity != null && entity.isAlive() && !entity.isRemoved()
                && (SbwCompat.isDrone(entity) || entity instanceof CubedFpvDroneEntity || entity instanceof LucasDroneEntity);
    }

    @Override
    protected void playActiveJammerSound(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        if (Math.floorMod(gameTime + pos.asLong(), RADAR_SOUND_INTERVAL_TICKS) == 0L) {
            serverLevel.playSound(null, pos, AddonSounds.RADAR_JAMMER_TOWER_LOOP.get(), SoundSource.BLOCKS, 0.9F, 1.0F);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(ENERGY_TAG, getEnergy());
        tag.putBoolean(CHARGING_TAG, charging);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy = Math.max(0, Math.min(tag.getInt(ENERGY_TAG), getMaxEnergy()));
        charging = tag.getBoolean(CHARGING_TAG);
    }
}
