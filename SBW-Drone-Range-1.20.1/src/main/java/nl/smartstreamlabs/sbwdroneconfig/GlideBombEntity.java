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
        GlideBombFixedWingFlightController.travel(this);
        FiberOpticLinkSystem.afterDroneTravel(this);
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
        tag.putFloat(TAG_THROTTLE, getGlideBombThrottle());
        tag.putBoolean(TAG_STALLING, isGlideBombStalling());
        tag.putFloat(TAG_AIRSPEED, getGlideBombAirspeed());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
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
        if (this.getCurrentItem().isEmpty()) {
            return;
        }

        int ammo = Math.max(0, SbwCompat.getDroneAmmo(this));
        for (int index = 0; index < ammo; index++) {
            ItemStack payload = this.getCurrentItem().copy();
            payload.setCount(1);
            ItemHandlerHelper.giveItemToPlayer(player, payload);
        }
    }
}
