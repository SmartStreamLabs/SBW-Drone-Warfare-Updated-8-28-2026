package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class AntiVehicleMortarPayloadEntity extends Entity {
    private static final String TAG_OWNER_UUID = "sbwdroneconfigMortarOwnerUuid";
    private static final String TAG_SOURCE_DRONE_UUID = "sbwdroneconfigMortarSourceDroneUuid";
    private static final String TAG_ACTIVE_FLIGHT_TICKS = "sbwdroneconfigMortarActiveFlightTicks";
    private static final int MAX_LIFETIME_TICKS = 200;

    private UUID ownerUuid;
    private UUID sourceDroneUuid;
    private int activeFlightTicks;
    private boolean detonated;

    public AntiVehicleMortarPayloadEntity(EntityType<? extends AntiVehicleMortarPayloadEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = false;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ownerUuid = tag.hasUUID(TAG_OWNER_UUID) ? tag.getUUID(TAG_OWNER_UUID) : null;
        this.sourceDroneUuid = tag.hasUUID(TAG_SOURCE_DRONE_UUID) ? tag.getUUID(TAG_SOURCE_DRONE_UUID) : null;
        this.activeFlightTicks = tag.getInt(TAG_ACTIVE_FLIGHT_TICKS);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.putUUID(TAG_OWNER_UUID, this.ownerUuid);
        }
        if (this.sourceDroneUuid != null) {
            tag.putUUID(TAG_SOURCE_DRONE_UUID, this.sourceDroneUuid);
        }
        tag.putInt(TAG_ACTIVE_FLIGHT_TICKS, this.activeFlightTicks);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        this.noPhysics = false;
        setNoGravity(false);
        activeFlightTicks++;

        Vec3 velocity = getDeltaMovement().add(0.0D, -0.055D, 0.0D);
        setDeltaMovement(velocity);
        move(MoverType.SELF, velocity);
        setDeltaMovement(getDeltaMovement().scale(0.985D));

        if (tickCount > 3 && hasImpactEntity()) {
            detonate();
            return;
        }

        if (horizontalCollision || verticalCollision || onGround() || activeFlightTicks > MAX_LIFETIME_TICKS) {
            detonate();
        }
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public void setSourceDroneUuid(UUID sourceDroneUuid) {
        this.sourceDroneUuid = sourceDroneUuid;
    }

    public void releaseFromDrone(Vec3 velocity) {
        this.activeFlightTicks = 0;
        this.noPhysics = false;
        setNoGravity(false);
        setDeltaMovement(velocity);
    }

    private boolean hasImpactEntity() {
        AABB box = getBoundingBox().inflate(0.35D);
        for (Entity entity : level().getEntities(this, box, this::canTriggerImpactWith)) {
            return true;
        }
        return false;
    }

    private boolean canTriggerImpactWith(Entity entity) {
        if (entity == null || !entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        UUID uuid = entity.getUUID();
        return !uuid.equals(ownerUuid) && !uuid.equals(sourceDroneUuid);
    }

    private void detonate() {
        if (detonated || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        detonated = true;

        double radius = AddonConfig.antiVehicleMortarExplosionRadius();
        AABB damageBox = getBoundingBox().inflate(radius);
        var targets=serverLevel.getEntities(this, damageBox, this::canDamage);var source=damageSources().explosion(this,this);Vc01Damage.component(serverLevel,source,this,targets);
        for (Entity target : targets) {
            boolean vehicleLike = SbwCompat.isSbwVehicle(target);
            float damage = (float) (vehicleLike
                    ? AddonConfig.antiVehicleMortarVehicleDamage()
                    : AddonConfig.antiVehicleMortarEntityDamage());
            if (damage > 0.0F) {
                target.hurt(source, damage);
                
            }
        }

        float blockDamageRadius = (float) AddonConfig.antiVehicleMortarBlockDamageRadius();
        try (var scope=DroneExplosionVfx.explosionScope(serverLevel,this,getX(),getY(),getZ(),blockDamageRadius>0?blockDamageRadius:1.5F,true)) {
        if (blockDamageRadius > 0.0F) {
            DroneExplosionVfx.mortar(serverLevel, this, blockDamageRadius);
            serverLevel.explode(this, getX(), getY(), getZ(), blockDamageRadius, Level.ExplosionInteraction.TNT);
        } else {
            DroneExplosionVfx.mortar(serverLevel, this, 1.5F);
            serverLevel.explode(this, getX(), getY(), getZ(), 1.5F, Level.ExplosionInteraction.NONE);
        }
        }

        discard();
    }

    private boolean canDamage(Entity entity) {
        return canTriggerImpactWith(entity);
    }
}
