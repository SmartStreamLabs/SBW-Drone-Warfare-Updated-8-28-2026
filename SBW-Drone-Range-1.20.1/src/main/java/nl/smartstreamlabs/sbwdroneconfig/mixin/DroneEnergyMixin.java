package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import nl.smartstreamlabs.sbwdroneconfig.AntiDroneNetBlock;
import nl.smartstreamlabs.sbwdroneconfig.DroneBatterySystem;
import nl.smartstreamlabs.sbwdroneconfig.DroneInventorySystem;
import nl.smartstreamlabs.sbwdroneconfig.DroneJammerSystem;
import nl.smartstreamlabs.sbwdroneconfig.DroneSpeedSystem;
import nl.smartstreamlabs.sbwdroneconfig.DroneSpotlightSystem;
import nl.smartstreamlabs.sbwdroneconfig.DroneWaterDisconnectSystem;
import nl.smartstreamlabs.sbwdroneconfig.DroneWeatherEffects;
import nl.smartstreamlabs.sbwdroneconfig.FiberOpticLinkSystem;
import nl.smartstreamlabs.sbwdroneconfig.FpvImpactDamageSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.atsuishio.superbwarfare.entity.vehicle.DroneEntity")
public abstract class DroneEnergyMixin {
    @Inject(
            method = {"baseTick", "m_6075_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/entity/vehicle/DroneEntity;m_20069_()Z",
                    shift = At.Shift.BEFORE
            ),
            remap = false,
            require = 0,
            cancellable = true
    )
    private void sbwdroneconfig$disconnectBeforeWaterDamage(CallbackInfo ci) {
        Entity drone = (Entity) (Object) this;
        if (drone.level() instanceof ServerLevel serverLevel
                && DroneWaterDisconnectSystem.handleWaterContact(serverLevel, drone)) {
            ci.cancel();
        }
    }

    @Inject(method = {"baseTick", "m_6075_"}, at = @At("TAIL"), remap = false, require = 0)
    private void sbwdroneconfig$handleBatteryTick(CallbackInfo ci) {
        Entity drone = (Entity) (Object) this;
        AntiDroneNetBlock.onDroneBaseTick(drone);
        DroneWaterDisconnectSystem.onDroneBaseTick(drone);
        DroneBatterySystem.onDroneBaseTick(drone);
        DroneWeatherEffects.onDroneBaseTick(drone);
        DroneInventorySystem.onDroneBaseTick(drone);
        FiberOpticLinkSystem.onDroneBaseTick(drone);
        DroneSpotlightSystem.onDroneBaseTick(drone);
    }

    @Inject(method = "travel", at = @At("HEAD"), remap = false, require = 0)
    private void sbwdroneconfig$applyOutOfEnergyFlightLimits(CallbackInfo ci) {
        Entity drone = (Entity) (Object) this;
        DroneBatterySystem.beforeDroneTravel(drone);
        DroneJammerSystem.beforeDroneTravel(drone);
        DroneWeatherEffects.applyStormEffects(drone);
        FiberOpticLinkSystem.beforeDroneTravel(drone);
        FpvImpactDamageSystem.beforeDroneTravel(drone);
    }

    @Inject(method = "travel", at = @At("TAIL"), remap = false, require = 0)
    private void sbwdroneconfig$applyFiberSeveredDescent(CallbackInfo ci) {
        Entity drone = (Entity) (Object) this;
        DroneSpeedSystem.afterDroneTravel(drone);
        FiberOpticLinkSystem.afterDroneTravel(drone);
        FpvImpactDamageSystem.afterDroneTravel(drone);
    }

    @Inject(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/entity/vehicle/DroneEntity;m_20069_()Z",
                    shift = At.Shift.BEFORE
            ),
            remap = false,
            require = 0,
            cancellable = true
    )
    private void sbwdroneconfig$disconnectBeforeWaterStrike(CallbackInfo ci) {
        Entity drone = (Entity) (Object) this;
        if (drone.level() instanceof ServerLevel serverLevel
                && DroneWaterDisconnectSystem.handleWaterContact(serverLevel, drone)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = {"baseTick", "m_6075_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/entity/vehicle/DroneEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    ordinal = 0
            ),
            remap = false,
            require = 0
    )
    private boolean sbwdroneconfig$redirectBaseTickWaterDamage(@Coerce Object drone, DamageSource source, float amount) {
        return sbwdroneconfig$handlePotentialWaterDamage((Entity) drone, source, amount);
    }

    @Redirect(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/entity/vehicle/DroneEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    ordinal = 0
            ),
            remap = false,
            require = 0
    )
    private boolean sbwdroneconfig$redirectTravelWaterDamage(@Coerce Object drone, DamageSource source, float amount) {
        return sbwdroneconfig$handlePotentialWaterDamage((Entity) drone, source, amount);
    }

    private boolean sbwdroneconfig$handlePotentialWaterDamage(Entity drone, DamageSource source, float amount) {
        if (drone.level() instanceof ServerLevel serverLevel
                && DroneWaterDisconnectSystem.handleConfirmedWaterBranch(serverLevel, drone)) {
            return false;
        }
        return drone.hurt(source, amount);
    }
}
