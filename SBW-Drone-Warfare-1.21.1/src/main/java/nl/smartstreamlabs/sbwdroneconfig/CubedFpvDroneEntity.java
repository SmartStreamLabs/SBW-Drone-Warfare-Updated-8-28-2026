package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.data.CustomData;
import com.atsuishio.superbwarfare.data.drone_attachment.DroneAttachmentData;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import com.atsuishio.superbwarfare.tools.TagDataParser;
import com.mojang.math.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Locale;

import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_DATA;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.DISPLAY_ENTITY_TAG;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.IS_KAMIKAZE;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.MAX_AMMO;
import static com.atsuishio.superbwarfare.entity.vehicle.DroneEntity.AMMO;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.L_WHEEL_HEALTH;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.MAIN_ENGINE_HEALTH;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.R_WHEEL_HEALTH;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.SUB_ENGINE_HEALTH;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.TURRET_HEALTH;

public class CubedFpvDroneEntity extends DroneEntity {
    private static final String TAG_INSTALLED_PAYLOAD = "SBWDroneWarfareInstalledPayload";
    private static final float RAD_TO_DEG = 180.0F / (float) Math.PI;
    private static final float MAX_HEALTH = 1.0F;
    private static final float COMPONENT_MAX_HEALTH = 1.0F;
    private static final float INCOMING_DAMAGE_MULTIPLIER = 8.0F;

    public CubedFpvDroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        clampDurabilityToFpvLimit();
    }

    @Override
    public float getMaxHealth() {
        return getDroneMaxHealthLimit();
    }

    @Override
    public float getTurretMaxHealth() {
        return getDroneComponentMaxHealthLimit();
    }

    @Override
    public float getWheelMaxHealth() {
        return getDroneComponentMaxHealthLimit();
    }

    @Override
    public float getEngineMaxHealth() {
        return getDroneComponentMaxHealthLimit();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide()) {
            clampDurabilityToFpvLimit();
        }

        boolean result = super.hurt(source, amount * getIncomingDamageMultiplier());

        if (!this.level().isClientSide()) {
            clampDurabilityToFpvLimit();
        }

        return result;
    }

    protected float getDroneMaxHealthLimit() {
        return MAX_HEALTH;
    }

    protected float getDroneComponentMaxHealthLimit() {
        return COMPONENT_MAX_HEALTH;
    }

    protected float getIncomingDamageMultiplier() {
        return INCOMING_DAMAGE_MULTIPLIER;
    }

    @Override
    public void baseTick() {
        if (!this.level().isClientSide()) {
            Player controller = this.getController();
            migrateToSinglePayload(controller);

            if (this.getFire() && hasInstalledPayload()) {
                releaseSinglePayload(controller);
                this.setFire(false);
            }
        }

        super.baseTick();

        if (!this.level().isClientSide()) {
            CubedFpvInputState.applyRecentInput(this);
            clampDurabilityToFpvLimit();
        }
    }

    @Override
    public void destroy() {
        if (!this.level().isClientSide()) {
            Player controller = this.getController();
            migrateToSinglePayload(controller);
        }

        super.destroy();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getMainHandItem();

        if (!this.level().isClientSide()) {
            migrateToSinglePayload(player);
        }

        if (player.isCrouching() && stack.isEmpty() && hasInstalledPayload()) {
            if (!this.level().isClientSide()) {
                removeInstalledPayload(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        DroneAttachmentData attachment = attachmentDataFor(stack);
        if (!stack.isEmpty() && attachment != null) {
            if (!this.level().isClientSide()) {
                installPayload(player, stack, attachment);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (player.isCrouching() && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                pickupDrone(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.interact(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (hasInstalledPayload()) {
            tag.put(TAG_INSTALLED_PAYLOAD, this.getCurrentItem().save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_INSTALLED_PAYLOAD, Tag.TAG_COMPOUND)) {
            ItemStack stack = ItemStack.of(tag.getCompound(TAG_INSTALLED_PAYLOAD));
            DroneAttachmentData attachment = attachmentDataFor(stack);
            if (!stack.isEmpty() && attachment != null) {
                stack.setCount(1);
                this.setCurrentItem(stack);
                setSinglePayloadEntityData(attachment);
            } else {
                clearInstalledPayload();
            }
        }

        clampDurabilityToFpvLimit();
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(AddonItems.CUBED_FPV_DRONE.get());
    }

    private void pickupDrone(Player player) {
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(AddonItems.CUBED_FPV_DRONE.get()));

        if (hasInstalledPayload()) {
            ItemStack payload = this.getCurrentItem().copy();
            payload.setCount(1);
            ItemHandlerHelper.giveItemToPlayer(player, payload);
            clearInstalledPayload();
        }

        player.getInventory().items.stream()
                .filter(SbwCompat::isMonitor)
                .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

        if (this.level() instanceof ServerLevel serverLevel) {
            SbwCompat.disconnectControllersForDrone(serverLevel, this);
        }

        this.discard();
    }

    private void installPayload(Player player, ItemStack stack, DroneAttachmentData attachment) {
        if (hasInstalledPayload()) {
            player.displayClientMessage(Component.literal("Payload slot already occupied."), true);
            return;
        }

        ItemStack installed = stack.copy();
        installed.setCount(1);
        this.setCurrentItem(installed);
        setSinglePayloadEntityData(attachment);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.displayClientMessage(Component.literal("Payload installed."), true);
    }

    private void removeInstalledPayload(Player player) {
        if (!hasInstalledPayload()) {
            return;
        }

        ItemStack payload = this.getCurrentItem().copy();
        payload.setCount(1);
        ItemHandlerHelper.giveItemToPlayer(player, payload);
        clearInstalledPayload();
        player.displayClientMessage(Component.literal("Payload removed."), true);
    }

    private void migrateToSinglePayload(@Nullable Player owner) {
        if (this.getCurrentItem().isEmpty() || this.entityData.get(AMMO) <= 0) {
            clearInstalledPayload();
            return;
        }

        int ammo = Math.max(1, this.entityData.get(AMMO));
        int stackCount = Math.max(1, this.getCurrentItem().getCount());
        int extras = Math.max(ammo, stackCount) - 1;

        ItemStack single = this.getCurrentItem().copy();
        single.setCount(1);
        this.setCurrentItem(single);

        DroneAttachmentData attachment = attachmentDataFor(single);
        if (attachment == null) {
            clearInstalledPayload();
            return;
        }

        setSinglePayloadEntityData(attachment);

        for (int index = 0; index < extras; index++) {
            ItemStack extra = single.copy();
            extra.setCount(1);
            if (owner != null) {
                ItemHandlerHelper.giveItemToPlayer(owner, extra);
            } else {
                this.spawnAtLocation(extra);
            }
        }
    }

    private boolean hasInstalledPayload() {
        return !this.getCurrentItem().isEmpty() && this.entityData.get(AMMO) > 0;
    }

    private void releaseSinglePayload(@Nullable Player controller) {
        DroneAttachmentData attachment = attachmentDataFor(this.getCurrentItem());
        if (attachment == null) {
            clearInstalledPayload();
            return;
        }

        Entity drop = EntityType.byString(attachment.dropEntity())
                .map(type -> type.create(this.level()))
                .orElse(null);
        if (drop == null) {
            clearInstalledPayload();
            return;
        }

        if (attachment.dropData() != null) {
            drop.load(TagDataParser.parseObject(attachment.dropData(), value -> resolveDropDataValue(controller, value)));
        }

        if (drop instanceof Projectile projectile && controller != null) {
            projectile.setOwner(controller);
        }

        Vec3 releasePosition = getDanglingPayloadWorldPosition();
        Vec3 releaseVelocity = this.getDeltaMovement().add(0.0D, -0.12D, 0.0D);
        drop.moveTo(releasePosition.x, releasePosition.y, releasePosition.z, this.getYRot(), this.getXRot());
        drop.setDeltaMovement(releaseVelocity);
        if (releaseVelocity.lengthSqr() > 1.0E-7D) {
            drop.setYRot((float) (Math.atan2(releaseVelocity.x, releaseVelocity.z) * RAD_TO_DEG));
            drop.setXRot((float) (Math.atan2(releaseVelocity.y, releaseVelocity.horizontalDistance()) * RAD_TO_DEG));
        }

        this.level().addFreshEntity(drop);
        clearInstalledPayload();
    }

    private Vec3 getDanglingPayloadWorldPosition() {
        Vector3f local = new Vector3f(
                (float) AddonConfig.fpvPayloadAttachmentX(),
                (float) (AddonConfig.fpvPayloadAttachmentY() - AddonConfig.fpvPayloadCordLength()),
                (float) AddonConfig.fpvPayloadAttachmentZ()
        );
        local.rotate(Axis.YP.rotationDegrees(CubedFpvDroneRenderMath.renderAttachmentYawDegrees(this.getYaw(1.0F))));
        local.rotate(Axis.XP.rotationDegrees(CubedFpvDroneRenderMath.renderAttachmentPitchDegrees(this.getBodyPitch())));
        local.rotate(Axis.ZP.rotationDegrees(CubedFpvDroneRenderMath.renderAttachmentRollDegrees(this.getRoll())));

        return this.position().add(
                local.x(),
                local.y() + CubedFpvDroneRenderMath.attachmentBaseYOffset(),
                local.z()
        );
    }

    private void setSinglePayloadEntityData(DroneAttachmentData attachment) {
        this.getCurrentItem().setCount(1);
        this.entityData.set(AMMO, 1);
        this.entityData.set(MAX_AMMO, 1);
        this.entityData.set(IS_KAMIKAZE, attachment.isKamikaze);
        this.entityData.set(DISPLAY_ENTITY, attachment.displayEntity());
        this.entityData.set(DISPLAY_ENTITY_TAG, attachment.displayData() == null
                ? new CompoundTag()
                : TagDataParser.parseObject(attachment.displayData(), value -> StringTag.valueOf(value)));
        this.entityData.set(DISPLAY_DATA, createDisplayData(attachment));
    }

    private void clearInstalledPayload() {
        this.setCurrentItem(ItemStack.EMPTY);
        this.entityData.set(AMMO, 0);
        this.entityData.set(MAX_AMMO, 1);
        this.entityData.set(IS_KAMIKAZE, false);
        this.entityData.set(DISPLAY_ENTITY, "");
        this.entityData.set(DISPLAY_ENTITY_TAG, new CompoundTag());
        this.entityData.set(DISPLAY_DATA, List.of(1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F));
    }

    private void clampDurabilityToFpvLimit() {
        clampHealthToFpvLimit();
        clampComponentHealthToFpvLimit();
    }

    private void clampHealthToFpvLimit() {
        float maxHealth = getDroneMaxHealthLimit();
        if (this.getHealth() > maxHealth) {
            this.setHealth(maxHealth);
        }
    }

    private void clampComponentHealthToFpvLimit() {
        clampComponentHealth(TURRET_HEALTH);
        clampComponentHealth(L_WHEEL_HEALTH);
        clampComponentHealth(R_WHEEL_HEALTH);
        clampComponentHealth(MAIN_ENGINE_HEALTH);
        clampComponentHealth(SUB_ENGINE_HEALTH);
    }

    private void clampComponentHealth(EntityDataAccessor<Float> accessor) {
        float maxHealth = getDroneComponentMaxHealthLimit();
        if (this.entityData.get(accessor) > maxHealth) {
            this.entityData.set(accessor, maxHealth);
        }
    }

    private static List<Float> createDisplayData(DroneAttachmentData attachment) {
        float[] scale = attachment.scale();
        float[] offset = attachment.offset();
        float[] rotation = attachment.rotation();
        return List.of(
                scale[0], scale[1], scale[2],
                offset[0], offset[1], offset[2],
                rotation[0], rotation[1], rotation[2],
                attachment.xLength,
                attachment.zLength,
                (float) attachment.tickCount
        );
    }

    @Nullable
    private static DroneAttachmentData attachmentDataFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return CustomData.DRONE_ATTACHMENT.get(itemId);
    }

    private static Tag resolveDropDataValue(@Nullable Player owner, String value) {
        if (owner == null) {
            return StringTag.valueOf(value);
        }

        return switch (value.toLowerCase(Locale.ROOT)) {
            case "@sbw:owner" -> NbtUtils.createUUID(owner.getUUID());
            case "@sbw:owner_string_lower" -> StringTag.valueOf(owner.getStringUUID().replace("-", "").toLowerCase(Locale.ROOT));
            case "@sbw:owner_string_upper" -> StringTag.valueOf(owner.getStringUUID().replace("-", "").toUpperCase(Locale.ROOT));
            default -> StringTag.valueOf(value);
        };
    }
}
