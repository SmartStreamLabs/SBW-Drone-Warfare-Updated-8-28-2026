package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class FiberOpticCableSegmentEntity extends Entity {
    private static final String TAG_DRONE_UUID = "sbwdroneconfigFiberCableDroneUuid";
    private static final String TAG_SEGMENT_INDEX = "sbwdroneconfigFiberCableSegmentIndex";
    private static final String TAG_SEGMENT_HEALTH = "sbwdroneconfigFiberCableSegmentHealth";
    private static final EntityDataAccessor<Float> DATA_SEGMENT_LENGTH =
            SynchedEntityData.defineId(FiberOpticCableSegmentEntity.class, EntityDataSerializers.FLOAT);

    private UUID droneUuid;
    private int segmentIndex;
    private float segmentHealth = (float) AddonConfig.fiberCableSegmentHealth();
    private float lastAppliedHitboxSize = -1.0F;
    private Vec3 segmentDirection = new Vec3(0.0D, 0.0D, 1.0D);
    private String lastDamageSourceId = "none";
    private String lastDirectEntityType = "none";
    private String lastDirectEntityUuid = "none";
    private String lastAttackerType = "none";
    private String lastAttackerUuid = "none";
    private float lastDamageAmount = 0.0F;

    public FiberOpticCableSegmentEntity(EntityType<? extends FiberOpticCableSegmentEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SEGMENT_LENGTH, 0.25F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(TAG_DRONE_UUID)) {
            this.droneUuid = tag.getUUID(TAG_DRONE_UUID);
        }
        this.segmentIndex = tag.getInt(TAG_SEGMENT_INDEX);
        this.segmentHealth = tag.contains(TAG_SEGMENT_HEALTH)
                ? tag.getFloat(TAG_SEGMENT_HEALTH)
                : (float) AddonConfig.fiberCableSegmentHealth();
        this.entityData.set(DATA_SEGMENT_LENGTH, Math.max(0.18F, tag.getFloat("SegmentLength")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.droneUuid != null) {
            tag.putUUID(TAG_DRONE_UUID, this.droneUuid);
        }
        tag.putInt(TAG_SEGMENT_INDEX, this.segmentIndex);
        tag.putFloat(TAG_SEGMENT_HEALTH, this.segmentHealth);
        tag.putFloat("SegmentLength", this.getSegmentLength());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.discard();
            return;
        }

        this.setNoGravity(true);
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
        refreshHitboxIfNeeded();
        this.setBoundingBox(makeBoundingBox());

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity drone = getTrackedDrone(serverLevel);
        if (drone == null || !drone.isAlive() || !FiberOpticLinkSystem.isFiberSessionActive(drone)) {
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) {
            return true;
        }
        if (!(this.level() instanceof ServerLevel serverLevel) || !canTakeDamageFrom(serverLevel, source)) {
            return false;
        }

        float appliedDamage = Math.max(0.1F, amount);
        recordDamageContext(source, appliedDamage);
        this.segmentHealth -= appliedDamage;

        if (AddonConfig.debugFiberCableSegments()) {
            SbwDroneRangeConfig.LOGGER.info(
                    "Fiber cable segment damaged drone={} segment={} source={} amount={} remainingHealth={}",
                    this.droneUuid,
                    this.segmentIndex,
                    source.getMsgId(),
                    String.format(java.util.Locale.ROOT, "%.2f", appliedDamage),
                    String.format(java.util.Locale.ROOT, "%.2f", this.segmentHealth)
            );
        }

        if (this.segmentHealth <= 0.0F) {
            if (AddonConfig.fiberCableBreaksWhenAnySegmentDestroyed()) {
                FiberOpticLinkSystem.breakCableFromSegment(serverLevel, this);
            } else {
                this.discard();
            }
        }

        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity drone = getTrackedDrone(serverLevel);
            if (drone != null && FiberOpticLinkSystem.isProtectedCableBreaker(serverLevel, drone, attacker)) {
                return true;
            }
        }
        return super.skipAttackInteraction(attacker);
    }

    @Override
    public float getPickRadius() {
        return 0.0F;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float hitboxSize = (float) Math.max(0.1D, AddonConfig.fiberCableSegmentHitboxSize());
        return EntityDimensions.scalable(hitboxSize, hitboxSize);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void applySegmentPose(UUID ownerDroneUuid, int newSegmentIndex, FiberOpticCableSegmentLayout.SegmentPose pose) {
        this.droneUuid = ownerDroneUuid;
        this.segmentIndex = newSegmentIndex;
        this.segmentHealth = Math.min(this.segmentHealth, (float) AddonConfig.fiberCableSegmentHealth());
        this.entityData.set(DATA_SEGMENT_LENGTH, Math.max(0.18F, pose.length()));
        this.segmentDirection = pose.direction();

        Vec3 center = pose.center();
        Vec3 direction = pose.direction();
        float yaw = (float) Math.toDegrees(Mth.atan2(direction.x, direction.z));
        float horizontalDistance = (float) Math.sqrt((direction.x * direction.x) + (direction.z * direction.z));
        float pitch = (float) -Math.toDegrees(Mth.atan2(direction.y, horizontalDistance));

        this.moveTo(center.x, center.y, center.z, yaw, pitch);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
        this.setBoundingBox(makeBoundingBox());
    }

    public UUID getDroneUuid() {
        return this.droneUuid;
    }

    public int getSegmentIndex() {
        return this.segmentIndex;
    }

    public float getSegmentLength() {
        return this.entityData.get(DATA_SEGMENT_LENGTH);
    }

    public String describeLastDamageContext() {
        return "source=" + this.lastDamageSourceId
                + ", amount=" + String.format(java.util.Locale.ROOT, "%.2f", this.lastDamageAmount)
                + ", directType=" + this.lastDirectEntityType
                + ", directUuid=" + this.lastDirectEntityUuid
                + ", attackerType=" + this.lastAttackerType
                + ", attackerUuid=" + this.lastAttackerUuid;
    }

    @Override
    protected AABB makeBoundingBox() {
        Vec3 direction = getSegmentDirection();
        double halfLength = Math.max(0.09D, this.getSegmentLength() * 0.5D);
        Vec3 center = this.position();
        Vec3 start = center.subtract(direction.scale(halfLength));
        Vec3 end = center.add(direction.scale(halfLength));
        double halfThickness = Math.max(0.08D, AddonConfig.fiberCableSegmentHitboxSize() * 0.5D);

        return new AABB(
                Math.min(start.x, end.x) - halfThickness,
                Math.min(start.y, end.y) - halfThickness,
                Math.min(start.z, end.z) - halfThickness,
                Math.max(start.x, end.x) + halfThickness,
                Math.max(start.y, end.y) + halfThickness,
                Math.max(start.z, end.z) + halfThickness
        );
    }

    private boolean canTakeDamageFrom(ServerLevel level, DamageSource source) {
        Entity drone = getTrackedDrone(level);
        Entity responsibleAttacker = FiberOpticLinkSystem.resolveResponsibleCableAttacker(source);
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            if (drone != null && FiberOpticLinkSystem.isProtectedCableBreaker(level, drone, responsibleAttacker)) {
                return false;
            }
            return AddonConfig.fiberCableCanBeDamagedByExplosions();
        }

        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof Projectile) {
            if (drone != null && FiberOpticLinkSystem.isProtectedProjectileCableBreaker(level, drone, responsibleAttacker)) {
                return false;
            }
            return responsibleAttacker != null && AddonConfig.fiberCableCanBeDamagedByProjectiles();
        }

        if (drone != null && FiberOpticLinkSystem.isProtectedCableBreaker(level, drone, responsibleAttacker)) {
            return false;
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof Player) {
            return AddonConfig.fiberCableCanBeDamagedByPlayers();
        }

        return false;
    }

    private void recordDamageContext(DamageSource source, float appliedDamage) {
        this.lastDamageSourceId = source != null ? source.getMsgId() : "none";
        this.lastDamageAmount = appliedDamage;

        Entity directEntity = source != null ? source.getDirectEntity() : null;
        this.lastDirectEntityType = describeEntityType(directEntity);
        this.lastDirectEntityUuid = directEntity != null ? directEntity.getUUID().toString() : "none";

        Entity attacker = source != null ? FiberOpticLinkSystem.resolveResponsibleCableAttacker(source) : null;
        this.lastAttackerType = describeEntityType(attacker);
        this.lastAttackerUuid = attacker != null ? attacker.getUUID().toString() : "none";
    }

    private static String describeEntityType(Entity entity) {
        if (entity == null) {
            return "none";
        }

        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null ? key.toString() : entity.getClass().getName();
    }

    private Entity getTrackedDrone(ServerLevel level) {
        if (this.droneUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(this.droneUuid);
        return entity != null && SbwCompat.isDrone(entity) ? entity : null;
    }

    private void refreshHitboxIfNeeded() {
        float desiredHitboxSize = (float) Math.max(0.1D, AddonConfig.fiberCableSegmentHitboxSize());
        if (Math.abs(desiredHitboxSize - this.lastAppliedHitboxSize) > 1.0E-4F) {
            this.lastAppliedHitboxSize = desiredHitboxSize;
            this.refreshDimensions();
            this.setBoundingBox(makeBoundingBox());
        }
    }

    private Vec3 getSegmentDirection() {
        if (this.segmentDirection != null && this.segmentDirection.lengthSqr() > 1.0E-6D) {
            return this.segmentDirection.normalize();
        }

        float yawRadians = (float) Math.toRadians(this.getYRot());
        float pitchRadians = (float) Math.toRadians(this.getXRot());
        double x = -Mth.sin(yawRadians) * Mth.cos(pitchRadians);
        double y = -Mth.sin(pitchRadians);
        double z = Mth.cos(yawRadians) * Mth.cos(pitchRadians);
        Vec3 derived = new Vec3(x, y, z);
        return derived.lengthSqr() > 1.0E-6D ? derived.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }
}
