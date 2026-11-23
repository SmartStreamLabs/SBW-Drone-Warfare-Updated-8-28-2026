package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class GlideBombEntity extends CubedFpvDroneEntity {
    private static final net.minecraft.network.syncher.EntityDataAccessor<java.util.Optional<net.minecraft.core.BlockPos>> LP03_MOUNT=net.minecraft.network.syncher.SynchedEntityData.defineId(GlideBombEntity.class,net.minecraft.network.syncher.EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Long> LP03_END=net.minecraft.network.syncher.SynchedEntityData.defineId(GlideBombEntity.class,net.minecraft.network.syncher.EntityDataSerializers.LONG);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Float> LP03_YAW=net.minecraft.network.syncher.SynchedEntityData.defineId(GlideBombEntity.class,net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
    private String lp03Dimension="";
    public boolean isLp03Fit(){return entityData.get(LP03_MOUNT).isPresent();}
    public float lp03RenderBlend(float partial){return isLp03Fit()?GlidePlatformLayout.renderBlend(level().getGameTime(),entityData.get(LP03_END),partial):1;}
    public boolean isLp03Mount(net.minecraft.core.BlockPos anchor){return entityData.get(LP03_MOUNT).filter(anchor::equals).isPresent() && (level().isClientSide || level().dimension().location().toString().equals(lp03Dimension));}
    public boolean isLp03Departing(){return isLp03Fit() && level().getGameTime()<entityData.get(LP03_END) && (level().isClientSide || level().dimension().location().toString().equals(lp03Dimension));}
    public void setLp03Mount(net.minecraft.core.BlockPos anchor,float yaw){entityData.set(LP03_MOUNT,java.util.Optional.of(anchor.immutable()));entityData.set(LP03_END,level().getGameTime()+GlidePlatformLayout.DEPARTURE_TICKS+1);entityData.set(LP03_YAW,yaw);lp03Dimension=level().dimension().location().toString();}
    /** Render-only horizontal clearance under R2 beams; native guided velocity stays unchanged. */
    public double lp03VisualRise(float partial){if(!isLp03Fit())return 0;double travel=net.minecraft.util.Mth.clamp(GlidePlatformLayout.DEPARTURE_TICKS-(entityData.get(LP03_END)-level().getGameTime())+partial,0,GlidePlatformLayout.DEPARTURE_TICKS);return travel*GlidePlatformLayout.LAUNCH_RISE*(1-lp03RenderBlend(partial));}
    private boolean travelLp03Departure(){
        if(!isLp03Departing())return false;
        float yaw=entityData.get(LP03_YAW);double radians=Math.toRadians(yaw);
        setYRot(yaw);setXRot((float)GlidePlatformLayout.RAIL_PITCH);setBodyXRot((float)GlidePlatformLayout.RAIL_PITCH);setRoll(0);
        // SBW applies its 0.06 vertical tick bias after travel, independently of vanilla NoGravity.
        // Cancel it only during the eight-tick rack guide; ordinary flight and gravity flags stay untouched.
        setDeltaMovement(new net.minecraft.world.phys.Vec3(-Math.sin(radians)*GlidePlatformLayout.LAUNCH_SPEED,GlidePlatformLayout.LAUNCH_RISE+.06,Math.cos(radians)*GlidePlatformLayout.LAUNCH_SPEED));
        setGlideBombAirspeed((float)GlidePlatformLayout.LAUNCH_SPEED);setGlideBombStalling(false);hurtMarked=true;return true;
    }
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(LP03_MOUNT,java.util.Optional.empty());builder.define(LP03_END,0L);builder.define(LP03_YAW,0F);}
    private static final String TAG_THROTTLE = "sbwdroneconfigGlideBombThrottle";
    private static final String TAG_STALLING = "sbwdroneconfigGlideBombStalling";
    private static final String TAG_AIRSPEED = "sbwdroneconfigGlideBombAirspeed";
    private float glideBombThrottle;
    private boolean glideBombStalling;
    private float glideBombAirspeed;
    private int glideBombTakeoffAssistTicksRemaining;
    private boolean glideBombControlSessionActive;

    public GlideBombEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void travel() {
        DroneJammerSystem.beforeDroneTravel(this);
        DroneWeatherEffects.applyStormEffects(this);
        FiberOpticLinkSystem.beforeDroneTravel(this);
        if(!MissionManager.beforeTravel(this) && !travelLp03Departure())GlideBombFixedWingFlightController.travel(this);
        FiberOpticLinkSystem.afterDroneTravel(this);
        MissionManager.afterTravel(this);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                pickupGlideBomb(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (SbwCompat.isMonitor(stack)) {
            InteractionResult result = super.interact(player, hand);
            if (!this.level().isClientSide() && result.consumesAction()) {
                SbwCompat.assignDroneOwner(this, player);
            }
            return result;
        }

        return super.interact(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if(isLp03Fit()){tag.putLong("sbwdroneconfigLp03Mount",entityData.get(LP03_MOUNT).orElseThrow().asLong());tag.putInt("sbwdroneconfigLp03Remaining",(int)Math.max(-GlidePlatformLayout.RENDER_BLEND_TICKS,Math.min(GlidePlatformLayout.DEPARTURE_TICKS+1,entityData.get(LP03_END)-level().getGameTime())));tag.putFloat("sbwdroneconfigLp03Yaw",entityData.get(LP03_YAW));tag.putString("sbwdroneconfigLp03Dimension",lp03Dimension);}

        tag.putFloat(TAG_THROTTLE, getGlideBombThrottle());
        tag.putBoolean(TAG_STALLING, isGlideBombStalling());
        tag.putFloat(TAG_AIRSPEED, getGlideBombAirspeed());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if(tag.contains("sbwdroneconfigLp03Mount")){entityData.set(LP03_MOUNT,java.util.Optional.of(net.minecraft.core.BlockPos.of(tag.getLong("sbwdroneconfigLp03Mount"))));entityData.set(LP03_END,level().getGameTime()+net.minecraft.util.Mth.clamp(tag.getInt("sbwdroneconfigLp03Remaining"),-GlidePlatformLayout.RENDER_BLEND_TICKS,GlidePlatformLayout.DEPARTURE_TICKS+1));entityData.set(LP03_YAW,tag.getFloat("sbwdroneconfigLp03Yaw"));lp03Dimension=tag.getString("sbwdroneconfigLp03Dimension");}else{entityData.set(LP03_MOUNT,java.util.Optional.empty());entityData.set(LP03_END,0L);}

        if (tag.contains(TAG_THROTTLE)) {
            setGlideBombThrottle(tag.getFloat(TAG_THROTTLE));
        }
        if (tag.contains(TAG_STALLING)) {
            setGlideBombStalling(tag.getBoolean(TAG_STALLING));
        }
        if (tag.contains(TAG_AIRSPEED)) {
            setGlideBombAirspeed(tag.getFloat(TAG_AIRSPEED));
        }
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(AddonItems.GLIDE_BOMB.get());
    }

    public float getGlideBombThrottle() {
        return glideBombThrottle;
    }

    public void setGlideBombThrottle(float throttle) {
        glideBombThrottle = LucasFlightPhysicsMath.clampThrottleNormalized(throttle);
    }

    public boolean isGlideBombStalling() {
        return glideBombStalling;
    }

    public void setGlideBombStalling(boolean stalling) {
        glideBombStalling = stalling;
    }

    public float getGlideBombAirspeed() {
        return glideBombAirspeed;
    }

    public void setGlideBombAirspeed(float airspeed) {
        glideBombAirspeed = Math.max(0.0F, airspeed);
    }

    public boolean beginGlideBombTakeoffAssist() {
        if (glideBombControlSessionActive) {
            return false;
        }
        glideBombControlSessionActive = true;
        glideBombTakeoffAssistTicksRemaining = Math.max(0, AddonConfig.lucasTakeoffAssistTicks());
        return true;
    }

    public void endGlideBombTakeoffAssist() {
        glideBombControlSessionActive = false;
        glideBombTakeoffAssistTicksRemaining = 0;
    }

    public boolean hasGlideBombTakeoffAssist() {
        return glideBombTakeoffAssistTicksRemaining > 0;
    }

    public void tickGlideBombTakeoffAssist() {
        if (glideBombTakeoffAssistTicksRemaining > 0) {
            glideBombTakeoffAssistTicksRemaining--;
        }
    }

    private void pickupGlideBomb(Player player) {
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(AddonItems.GLIDE_BOMB.get()));
        returnInheritedPayloadIfPresent(player);

        player.getInventory().items.stream()
                .filter(SbwCompat::isMonitor)
                .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

        if (this.level() instanceof ServerLevel serverLevel) {
            SbwCompat.disconnectControllersForDrone(serverLevel, this);
        }

        this.discard();
    }

    private void returnInheritedPayloadIfPresent(Player player) {
        if (SbwCompat.getDroneCurrentItem(this).isEmpty()) {
            return;
        }

        int ammo = Math.max(0, SbwCompat.getDroneAmmo(this));
        for (int index = 0; index < ammo; index++) {
            ItemStack payload = SbwCompat.getDroneCurrentItem(this).copy();
            payload.setCount(1);
            ItemHandlerHelper.giveItemToPlayer(player, payload);
        }
    }
}
