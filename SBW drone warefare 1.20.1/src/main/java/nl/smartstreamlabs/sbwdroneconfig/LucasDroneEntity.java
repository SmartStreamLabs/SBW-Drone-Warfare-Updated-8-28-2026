package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.AMMO;

public class LucasDroneEntity extends DroneEntity {
    private static final EntityDataAccessor<Float> LUCAS_THROTTLE = SynchedEntityData.defineId(LucasDroneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> LUCAS_STALLING = SynchedEntityData.defineId(LucasDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> LUCAS_AIRSPEED = SynchedEntityData.defineId(LucasDroneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LUCAS_FUEL = SynchedEntityData.defineId(LucasDroneEntity.class, EntityDataSerializers.FLOAT);
    private static final String TAG_LUCAS_THROTTLE = "sbwdroneconfigLucasThrottle";
    private static final String TAG_LUCAS_STALLING = "sbwdroneconfigLucasStalling";
    private static final String TAG_LUCAS_AIRSPEED = "sbwdroneconfigLucasAirspeed";
    private static final String TAG_LUCAS_FUEL = "sbwdroneconfigLucasFuel";
    private static final EntityDataAccessor<java.util.Optional<net.minecraft.core.BlockPos>> LP01_FIT=SynchedEntityData.defineId(LucasDroneEntity.class,EntityDataSerializers.OPTIONAL_BLOCK_POS);
    public boolean isLp01Fit(){return this.entityData.get(LP01_FIT).isPresent();}
    public void setLp01Mount(net.minecraft.core.BlockPos anchor){this.entityData.set(LP01_FIT,java.util.Optional.of(anchor.immutable()));}
    public boolean isLp01PropellerSafe(){
        var anchor=this.entityData.get(LP01_FIT);
        if(anchor.isEmpty())return true;
        var p=anchor.get();
        // Conservative rotor clearance around the original platform, using coordinates only. No chunk lookup.
        return ((net.minecraft.world.entity.Entity)this).distanceToSqr(p.getX()+.5,p.getY()+.5,p.getZ()+.5)>25;
    }
    private static final float LUCAS_EYE_HEIGHT = 0.45F;
    private int lucasTakeoffAssistTicksRemaining;
    private boolean lucasControlSessionActive;

    public LucasDroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super((EntityType<DroneEntity>)(EntityType<?>)type,level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LUCAS_THROTTLE, 0.0F);
        this.entityData.define(LUCAS_STALLING, false);
        this.entityData.define(LUCAS_AIRSPEED, 0.0F);
        this.entityData.define(LUCAS_FUEL, 0.0F);
        this.entityData.define(LP01_FIT,java.util.Optional.empty());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult refuelResult = LucasFuelSystem.refuel(this, player, hand);
        if (refuelResult.consumesAction()) {
            return refuelResult;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(AddonItems.LUCAS_DRONE.get()));

                for (int index = 0; index < this.entityData.get(AMMO); index++) {
                    if (!SbwCompat.getDroneCurrentItem(this).isEmpty()) {
                        ItemHandlerHelper.giveItemToPlayer(player, SbwCompat.getDroneCurrentItem(this).copy());
                    }
                }

                player.getInventory().items.stream()
                        .filter(SbwCompat::isMonitor)
                        .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

                if (this.level() instanceof ServerLevel serverLevel) {
                    SbwCompat.disconnectControllersForDrone(serverLevel, this);
                }

                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.interact(player, hand);
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(AddonItems.LUCAS_DRONE.get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("sbwdroneconfigLp01Fit",isLp01Fit());
        this.entityData.get(LP01_FIT).ifPresent(p->compound.putLong("sbwdroneconfigLp01Mount",p.asLong()));
        compound.putFloat(TAG_LUCAS_THROTTLE, getLucasThrottle());
        compound.putBoolean(TAG_LUCAS_STALLING, isLucasStalling());
        compound.putFloat(TAG_LUCAS_AIRSPEED, getLucasAirspeed());
        compound.putFloat(TAG_LUCAS_FUEL, getLucasFuel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(LP01_FIT,compound.getBoolean("sbwdroneconfigLp01Fit") && compound.contains("sbwdroneconfigLp01Mount")?java.util.Optional.of(net.minecraft.core.BlockPos.of(compound.getLong("sbwdroneconfigLp01Mount"))):java.util.Optional.empty());
        if (compound.contains(TAG_LUCAS_THROTTLE)) {
            setLucasThrottle(compound.getFloat(TAG_LUCAS_THROTTLE));
        }
        if (compound.contains(TAG_LUCAS_STALLING)) {
            setLucasStalling(compound.getBoolean(TAG_LUCAS_STALLING));
        }
        if (compound.contains(TAG_LUCAS_AIRSPEED)) {
            setLucasAirspeed(compound.getFloat(TAG_LUCAS_AIRSPEED));
        }
        if (compound.contains(TAG_LUCAS_FUEL)) {
            setLucasFuel(compound.getFloat(TAG_LUCAS_FUEL));
        }
    }

    @Override public void destroy(){if(MissionManager.suppressDestroy(this))return;MissionManager.destroyed(this);super.destroy();}

    @Override
    public void travel() {
        LucasFuelSystem.beforeLucasTravel(this);
        DroneJammerSystem.beforeDroneTravel(this);
        DroneWeatherEffects.applyStormEffects(this);
        FiberOpticLinkSystem.beforeDroneTravel(this);
        if(!MissionManager.beforeTravel(this))LucasFixedWingFlightController.travel(this);
        FiberOpticLinkSystem.afterDroneTravel(this);
        MissionManager.afterTravel(this);
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
        return LUCAS_EYE_HEIGHT;
    }

    public float getLucasThrottle() {
        return this.entityData.get(LUCAS_THROTTLE);
    }

    public void setLucasThrottle(float throttle) {
        this.entityData.set(LUCAS_THROTTLE, throttle);
    }

    public boolean isLucasStalling() {
        return this.entityData.get(LUCAS_STALLING);
    }

    public void setLucasStalling(boolean stalling) {
        this.entityData.set(LUCAS_STALLING, stalling);
    }

    public float getLucasAirspeed() {
        return this.entityData.get(LUCAS_AIRSPEED);
    }

    public void setLucasAirspeed(float airspeed) {
        this.entityData.set(LUCAS_AIRSPEED, airspeed);
    }

    public float getLucasFuel() {
        return this.entityData.get(LUCAS_FUEL);
    }

    public void setLucasFuel(float fuel) {
        this.entityData.set(LUCAS_FUEL, Math.max(0.0F, Math.min(fuel, (float) AddonConfig.lucasMaxFuel())));
    }

    public int getLucasFuelPercent() {
        int maxFuel = AddonConfig.lucasMaxFuel();
        if (maxFuel <= 0) {
            return 0;
        }
        return Math.round((getLucasFuel() * 100.0F) / maxFuel);
    }

    public boolean beginLucasTakeoffAssist() {
        if (lucasControlSessionActive) {
            return false;
        }
        lucasControlSessionActive = true;
        lucasTakeoffAssistTicksRemaining = Math.max(0, AddonConfig.lucasTakeoffAssistTicks());
        return true;
    }

    public void endLucasTakeoffAssist() {
        lucasControlSessionActive = false;
        lucasTakeoffAssistTicksRemaining = 0;
    }

    public int getLucasTakeoffAssistTicksRemaining() {
        return lucasTakeoffAssistTicksRemaining;
    }

    public boolean hasLucasTakeoffAssist() {
        return lucasTakeoffAssistTicksRemaining > 0;
    }

    public void tickLucasTakeoffAssist() {
        if (lucasTakeoffAssistTicksRemaining > 0) {
            lucasTakeoffAssistTicksRemaining--;
        }
    }
}
