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
    public static final int MAX_RANGE = 650;
    private static final int DEFAULT_RANGE = 650;
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
        return level!=null && stationBelow(level,worldPosition) && getEnergy() >= RadarJammerTowerSettings.MINIMUM_ENERGY_TO_ACTIVATE;
    }

    public boolean isOperational() {
        return canOpenPoweredGui();
    }

    @Override
    protected void tickBeforeJammerScan(ServerLevel serverLevel, BlockPos pos, long gameTime) {
        tickStationBattery(serverLevel);
    }

    private void tickStationBattery(ServerLevel serverLevel) {
        int maxEnergy = getMaxEnergy();
        if (energy > maxEnergy) {
            energy = maxEnergy;
            setChanged();
        }

        boolean wasCharging = charging;
        int oldEnergy = energy;
        int chargeRate = 0;
        if(stationBelow(serverLevel,worldPosition)){
            var below=serverLevel.getBlockEntity(worldPosition.below());
            if(below!=null){var storage=below.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY,net.minecraft.core.Direction.UP).orElse(null);if(storage!=null)chargeRate=storage.extractEnergy(Math.min(maxEnergy-energy,RadarJammerTowerSettings.CHARGING_STATION_CHARGE_RATE),false);}
        }
        charging=chargeRate>0;
        if (chargeRate > 0) {
            energy = Math.min(maxEnergy, energy + chargeRate);
        }
        if (energy != oldEnergy || charging != wasCharging) {
            setChanged();
        }
    }

    public static boolean stationBelow(net.minecraft.world.level.Level level,BlockPos pos){
        if(!level.hasChunkAt(pos.below()))return false;
        var id=BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos.below()).getBlock());
        return "superbwarfare".equals(id.getNamespace())&&("charging_station".equals(id.getPath())||"creative_charging_station".equals(id.getPath()));
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
    // RJ01_VISUAL_BEGIN: transient presentation only; never included in saved machine NBT.
    private boolean rj01Active;
    private long rj01VisualTicks;
    public boolean rj01Active() { return rj01Active; }
    public float rj01Time(float partial) { return (rj01VisualTicks % 1200 + (rj01Active ? partial : 0)) / 20f; }
    private boolean rj01Powered() { return AddonConfig.enableDroneJammer() && isOperational(); }
    private boolean rj01LayoutReady;
    private long rj01NextLayoutCheck;
    public static void rj01Tick(net.minecraft.world.level.Level level,BlockPos pos,BlockState state,RadarJammerTowerBlockEntity be) {
        if (level.isClientSide()) { if (be.rj01Active) be.rj01VisualTicks++; return; }
        if(!be.rj01LayoutReady&&level.getGameTime()>=be.rj01NextLayoutCheck){be.rj01LayoutReady=RadarJammerTowerBlock.reconcileParts(level,pos,state);be.rj01NextLayoutCheck=level.getGameTime()+40;}
        JammerBlockEntity.serverTick(level,pos,state,be);
        boolean active=be.rj01Powered();
        if (active!=be.rj01Active) { be.rj01Active=active; level.sendBlockUpdated(pos,state,state,Block.UPDATE_CLIENTS); }
    }
    @Override public CompoundTag getUpdateTag() { CompoundTag t=new CompoundTag();t.putBoolean("RJ01VisualActive",rj01Powered());return t; }
    @Override public void handleUpdateTag(CompoundTag tag) { rj01Active=tag.getBoolean("RJ01VisualActive"); }
    @Override public void onDataPacket(net.minecraft.network.Connection connection,net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket packet) { handleUpdateTag(packet.getTag()); }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() { return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this); }
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox() { return new net.minecraft.world.phys.AABB(worldPosition).inflate(1,0,1).expandTowards(0,3,0); }
    // RJ01_VISUAL_END
}
