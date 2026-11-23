package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class FpvImpactDamageSystem {
    private static final String TAG_PRE_IMPACT_SPEED = "sbwdroneconfigFpvPreImpactSpeed";
    private static final String TAG_PRE_IMPACT_Y_SPEED = "sbwdroneconfigFpvPreImpactYSpeed";
    private static final String TAG_LAST_IMPACT_TIME = "sbwdroneconfigFpvLastImpactTime";
    private static final double MIN_IMPACT_SPEED = 0.08D;
    private static final double DESTROY_IMPACT_SPEED = 0.26D;
    private static final float MIN_IMPACT_DAMAGE = 5.0F;
    private static final float IMPACT_DAMAGE_MULTIPLIER = 180.0F;
    private static final int IMPACT_COOLDOWN_TICKS = 6;

    private FpvImpactDamageSystem() {
    }

    public static void beforeDroneTravel(Entity entity) {
        if (!(entity instanceof CubedFpvDroneEntity) || entity instanceof DeliveryDroneEntity || entity.level().isClientSide()) {
            return;
        }

        entity.getPersistentData().putDouble(TAG_PRE_IMPACT_SPEED, entity.getDeltaMovement().length());
        entity.getPersistentData().putDouble(TAG_PRE_IMPACT_Y_SPEED, entity.getDeltaMovement().y);
    }

    public static void afterDroneTravel(Entity entity) {
        if (!(entity instanceof CubedFpvDroneEntity drone) || entity instanceof DeliveryDroneEntity || !(drone.level() instanceof ServerLevel serverLevel) || !drone.isAlive()) {
            return;
        }

        CompoundTag data = drone.getPersistentData();
        double preImpactYSpeed = data.getDouble(TAG_PRE_IMPACT_Y_SPEED);
        boolean horizontalImpact = drone.horizontalCollision;
        boolean verticalImpact = drone.verticalCollision && (!drone.onGround() || preImpactYSpeed < -MIN_IMPACT_SPEED);
        if (!horizontalImpact && !verticalImpact) {
            return;
        }

        double impactSpeed = data.getDouble(TAG_PRE_IMPACT_SPEED);
        if (impactSpeed < MIN_IMPACT_SPEED || isOnCooldown(data, serverLevel.getGameTime())) {
            return;
        }

        data.putLong(TAG_LAST_IMPACT_TIME, serverLevel.getGameTime());
        float damage = impactSpeed >= DESTROY_IMPACT_SPEED
                ? 1000.0F
                : calculateImpactDamage(impactSpeed);
        drone.hurt(drone.damageSources().generic(), damage);
    }

    static float calculateImpactDamage(double impactSpeed) {
        return Math.max(MIN_IMPACT_DAMAGE, (float) ((impactSpeed - MIN_IMPACT_SPEED) * IMPACT_DAMAGE_MULTIPLIER));
    }

    private static boolean isOnCooldown(CompoundTag data, long gameTime) {
        return data.contains(TAG_LAST_IMPACT_TIME)
                && gameTime - data.getLong(TAG_LAST_IMPACT_TIME) < IMPACT_COOLDOWN_TICKS;
    }
}
