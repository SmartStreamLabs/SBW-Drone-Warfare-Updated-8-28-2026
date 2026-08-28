package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public final class DroneWeatherEffects {
    private DroneWeatherEffects() {
    }

    /**
     * Applies lightweight weather state to the drone every tick.
     * Rain is primarily visual/client-side, while storms add real flight instability.
     */
    public static void onDroneBaseTick(Entity drone) {
        if (!AddonConfig.weatherEffectsEnabled() || drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return;
        }

        applyRainEffects(drone);
    }

    /**
     * Rain slightly weakens the usable signal range and enables the client HUD effect.
     * The visual side reads the world weather directly; this method keeps the weather
     * application path explicit and easy to extend later.
     */
    public static void applyRainEffects(Entity drone) {
        drone.getPersistentData().putBoolean("sbwdroneconfigRainActive", drone.level().isRaining());
    }

    /**
     * Storms make the drone harder to control by nudging its velocity with small random
     * gusts. This hooks into the existing movement flow instead of replacing it.
     */
    public static void applyStormEffects(Entity drone) {
        if (!AddonConfig.weatherEffectsEnabled() || drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return;
        }
        if (!drone.level().isThundering() || drone.onGround()) {
            return;
        }

        double strength = AddonConfig.stormInstabilityStrength();
        if (strength <= 0.0D) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double windX = (random.nextDouble() - 0.5D) * 0.06D * strength;
        double windY = (random.nextDouble() - 0.5D) * 0.02D * strength;
        double windZ = (random.nextDouble() - 0.5D) * 0.06D * strength;

        Vec3 adjusted = drone.getDeltaMovement().add(windX, windY, windZ);
        double maxHorizontal = 1.6D;
        double horizontal = Math.sqrt(adjusted.x * adjusted.x + adjusted.z * adjusted.z);
        if (horizontal > maxHorizontal) {
            double scale = maxHorizontal / horizontal;
            adjusted = new Vec3(adjusted.x * scale, adjusted.y, adjusted.z * scale);
        }

        drone.setDeltaMovement(
                adjusted.x,
                Mth.clamp(adjusted.y, -1.0D, 1.0D),
                adjusted.z
        );
    }

    public static int getAdditionalEnergyUse(Entity drone, boolean active) {
        if (!AddonConfig.weatherEffectsEnabled() || !active || drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return 0;
        }
        return drone.level().isThundering() ? 1 : 0;
    }

    public static int getEffectiveControlRange(ServerPlayer player, Entity drone, int baseRange) {
        if (!AddonConfig.weatherEffectsEnabled() || player == null || drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return baseRange;
        }
        if (!player.level().isRaining()) {
            return baseRange;
        }

        double multiplier = AddonConfig.rainVisibilityMultiplier();
        return Math.max(1, Mth.floor(baseRange * multiplier));
    }
}
