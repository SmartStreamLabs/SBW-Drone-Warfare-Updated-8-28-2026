package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

public class AntiVehicleMortarPayloadEntity extends Entity {
    private static final String TAG_OWNER_UUID = "sbwdroneconfigMortarOwnerUuid";
    private static final String TAG_SOURCE_DRONE_UUID = "sbwdroneconfigMortarSourceDroneUuid";
    private static final String TAG_ACTIVE_FLIGHT_TICKS = "sbwdroneconfigMortarActiveFlightTicks";
    private static final int MAX_LIFETIME_TICKS = 200;
    private static final double VEHICLE_DAMAGE = 85.0D;
    private static final double ENTITY_DAMAGE = 12.0D;
    private static final double DAMAGE_RADIUS = 7.0D;
    private static final float BLOCK_DAMAGE_RADIUS = 2.0F;

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
    protected void defineSynchedData() {
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
        if (this.level().isClientSide()) {
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

        AABB damageBox = getBoundingBox().inflate(DAMAGE_RADIUS);
        var targets=serverLevel.getEntities(this, damageBox, this::canDamage);var source=damageSources().explosion(this,this);Vc01Damage.component(serverLevel,source,this,targets);
        for (Entity target : targets) {
            boolean vehicleLike = SbwCompat.isSbwVehicle(target);
            float damage = (float) (vehicleLike ? VEHICLE_DAMAGE : ENTITY_DAMAGE);
            if (damage > 0.0F) {
                target.hurt(source, damage);
                
            }
        }

        DroneExplosionVfx.mortar(serverLevel, this, BLOCK_DAMAGE_RADIUS);
        try (var scope=DroneExplosionVfx.explosionScope(serverLevel,this,getX(),getY(),getZ(),BLOCK_DAMAGE_RADIUS,true)) {
        serverLevel.explode(this, getX(), getY(), getZ(), BLOCK_DAMAGE_RADIUS, Level.ExplosionInteraction.TNT);
        }
        discard();
    }

    private boolean canDamage(Entity entity) {
        return canTriggerImpactWith(entity);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
