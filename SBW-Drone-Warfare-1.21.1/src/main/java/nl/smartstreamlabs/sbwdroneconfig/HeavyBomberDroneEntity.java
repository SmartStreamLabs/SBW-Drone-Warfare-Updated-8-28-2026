package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.projectile.C4Entity;
import com.atsuishio.superbwarfare.entity.projectile.Tm62Entity;
import com.atsuishio.superbwarfare.entity.projectile.FastThrowableProjectile;
import com.atsuishio.superbwarfare.entity.projectile.JavelinMissileEntity;
import com.atsuishio.superbwarfare.entity.projectile.MortarShellEntity;
import com.atsuishio.superbwarfare.entity.projectile.RpgRocketStandardEntity;
import com.atsuishio.superbwarfare.entity.projectile.RpgRocketTBGEntity;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModEntities;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class HeavyBomberDroneEntity extends CubedFpvDroneEntity {
    public static final int MAX_BOMBS = 4;
    private static final String TM_62_ITEM_ID = "superbwarfare:tm_62";
    private static final String ANTI_VEHICLE_MORTAR_PAYLOAD_ITEM_ID = "sbwdroneconfig:anti_vehicle_mortar_payload";
    private static final PayloadDefinition[] SUPPORTED_PAYLOADS = new PayloadDefinition[]{
            new PayloadDefinition(TM_62_ITEM_ID, "tm_62"),
            new PayloadDefinition(ANTI_VEHICLE_MORTAR_PAYLOAD_ITEM_ID, "anti_vehicle_mortar_payload"),
            new PayloadDefinition("superbwarfare:mortar_shell", "mortar_shell"),
            new PayloadDefinition("superbwarfare:mortar_shell_wp", "mortar_shell_wp"),
            new PayloadDefinition("superbwarfare:rpg_rocket_standard", "rpg_rocket_standard"),
            new PayloadDefinition("superbwarfare:rpg_rocket_tbg", "rpg_rocket_tbg"),
            new PayloadDefinition("superbwarfare:c4_bomb", "c4_bomb"),
            new PayloadDefinition("superbwarfare:javelin_missile", "javelin_missile")
    };
    private static final float HEAVY_BOMBER_MAX_HEALTH = 160.0F;
    private static final float HEAVY_BOMBER_COMPONENT_MAX_HEALTH = 80.0F;
    private static final float HEAVY_BOMBER_INCOMING_DAMAGE_MULTIPLIER = 0.35F;
    private static final double SLOW_FLIGHT_HORIZONTAL_DAMPING = 0.90D;
    private static final double SLOW_FLIGHT_VERTICAL_DAMPING = 0.97D;
    private static final float JAVELIN_DEFAULT_DAMAGE = 650.0F;
    private static final float JAVELIN_DEFAULT_EXPLOSION_DAMAGE = 120.0F;
    private static final float JAVELIN_DEFAULT_EXPLOSION_RADIUS = 9.0F;
    private static final String TAG_BOMBS_REMAINING = "SBWHeavyBomberBombs";
    private static final EntityDataAccessor<Integer> BOMBS_REMAINING =
            SynchedEntityData.defineId(HeavyBomberDroneEntity.class, EntityDataSerializers.INT);

    public HeavyBomberDroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super(type, level);
        this.setHealth(HEAVY_BOMBER_MAX_HEALTH);
    }

    @Override
    protected float getDroneMaxHealthLimit() {
        return HEAVY_BOMBER_MAX_HEALTH;
    }

    @Override
    protected float getDroneComponentMaxHealthLimit() {
        return HEAVY_BOMBER_COMPONENT_MAX_HEALTH;
    }

    @Override
    protected float getIncomingDamageMultiplier() {
        return HEAVY_BOMBER_INCOMING_DAMAGE_MULTIPLIER;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BOMBS_REMAINING, MAX_BOMBS);
    }

    @Override
    public void baseTick() {
        if (!this.level().isClientSide() && this.getFire()) {
            tryDropPayload();
            this.setFire(false);
        }

        super.baseTick();

        if (!this.level().isClientSide() && !this.onGround()) {
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(
                    movement.x * SLOW_FLIGHT_HORIZONTAL_DAMPING,
                    movement.y * SLOW_FLIGHT_VERTICAL_DAMPING,
                    movement.z * SLOW_FLIGHT_HORIZONTAL_DAMPING
            );
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                pickupHeavyBomber(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.interact(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_BOMBS_REMAINING, getBombsRemaining());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_BOMBS_REMAINING, Tag.TAG_INT)) {
            setBombsRemaining(tag.getInt(TAG_BOMBS_REMAINING));
        }
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return createStackWithBombs(getBombsRemaining());
    }

    public int getBombsRemaining() {
        return Mth.clamp(this.entityData.get(BOMBS_REMAINING), 0, MAX_BOMBS);
    }

    public void setBombsRemaining(int bombsRemaining) {
        this.entityData.set(BOMBS_REMAINING, Mth.clamp(bombsRemaining, 0, MAX_BOMBS));
    }

    public void loadFromItem(ItemStack stack) {
        setBombsRemaining(bombsFromStack(stack));
    }

    public static int bombsFromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_BOMBS_REMAINING, Tag.TAG_INT)) {
            return Mth.clamp(tag.getInt(TAG_BOMBS_REMAINING), 0, MAX_BOMBS);
        }
        return MAX_BOMBS;
    }

    public static ItemStack createStackWithBombs(int bombsRemaining) {
        ItemStack stack = new ItemStack(AddonItems.HEAVY_BOMBER_DRONE.get());
        int clamped = Mth.clamp(bombsRemaining, 0, MAX_BOMBS);
        if (clamped != MAX_BOMBS) {
            stack.getOrCreateTag().putInt(TAG_BOMBS_REMAINING, clamped);
        }
        return stack;
    }

    private boolean tryDropPayload() {
        Player controller = this.getController();
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }

        DroneInventoryContainer inventory = new DroneInventoryContainer(this);
        LoadedPayload loadedPayload = findLoadedPayload(inventory);
        if (loadedPayload == null) {
            if (controller != null) {
                controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.heavy_bomber_empty"), true);
            }
            return false;
        }

        Player owner = this.getController();
        Vec3 dropPos = this.position().add(0.0D, -0.55D, 0.0D);
        Vec3 releaseVelocity = this.getDeltaMovement().add(0.0D, -0.75D, 0.0D);
        Entity payload = createNativePayload(loadedPayload.definition(), owner, level, dropPos, releaseVelocity);
        if (payload == null) {
            return false;
        }

        ItemStack loadedStack = inventory.getItem(loadedPayload.slot());
        loadedStack.shrink(1);
        inventory.setItem(loadedPayload.slot(), loadedStack.isEmpty() ? ItemStack.EMPTY : loadedStack);
        inventory.saveToDrone();

        level.addFreshEntity(payload);
        level.playSound(null, dropPos.x, dropPos.y, dropPos.z, SoundEvents.DISPENSER_LAUNCH, SoundSource.NEUTRAL, 0.9F, 0.65F);

        if (controller != null) {
            controller.displayClientMessage(Component.translatable("message.sbwdroneconfig.heavy_bomber_bombs", countLoadedPayloads(inventory)), true);
        }
        return true;
    }

    private @Nullable Entity createNativePayload(PayloadDefinition definition, LivingEntity owner, ServerLevel level, Vec3 dropPos, Vec3 releaseVelocity) {
        Entity payload = switch (definition.kind()) {
            case "tm_62" -> new Tm62Entity(owner, level, true);
            case "anti_vehicle_mortar_payload" -> {
                AntiVehicleMortarPayloadEntity mortarPayload = new AntiVehicleMortarPayloadEntity(AddonEntities.ANTI_VEHICLE_MORTAR_PAYLOAD.get(), level);
                if (owner != null) {
                    mortarPayload.setOwnerUuid(owner.getUUID());
                }
                mortarPayload.setSourceDroneUuid(this.getUUID());
                mortarPayload.releaseFromDrone(releaseVelocity);
                yield mortarPayload;
            }
            case "mortar_shell" -> new MortarShellEntity(ModEntities.MORTAR_SHELL.get(), level);
            case "mortar_shell_wp" -> new MortarShellEntity(ModEntities.MORTAR_SHELL.get(), level);
            case "rpg_rocket_standard" -> new RpgRocketStandardEntity(ModEntities.RPG_ROCKET_STANDARD.get(), level);
            case "rpg_rocket_tbg" -> new RpgRocketTBGEntity(ModEntities.RPG_ROCKET_TBG.get(), level);
            case "c4_bomb" -> new C4Entity(owner, level, false);
            case "javelin_missile" -> new JavelinMissileEntity(
                    this,
                    level,
                    JAVELIN_DEFAULT_DAMAGE,
                    JAVELIN_DEFAULT_EXPLOSION_DAMAGE,
                    JAVELIN_DEFAULT_EXPLOSION_RADIUS,
                    1,
                    dropPos.add(releaseVelocity.normalize().scale(64.0D))
            );
            default -> null;
        };

        if (payload == null) {
            return null;
        }

        payload.moveTo(dropPos.x, dropPos.y, dropPos.z, this.getYRot(), 0.0F);
        payload.setYRot(this.getYRot());
        if (payload instanceof FastThrowableProjectile projectile) {
            if (owner != null) {
                projectile.setOwner(owner);
            }
            Vec3 shot = releaseVelocity.lengthSqr() < 0.01D ? new Vec3(0.0D, -1.0D, 0.0D) : releaseVelocity;
            projectile.shoot(shot.x, shot.y, shot.z, Math.max(0.65F, (float) shot.length()), 0.0F);
        } else if (payload instanceof C4Entity c4) {
            c4.look(releaseVelocity);
            c4.setDeltaMovement(releaseVelocity);
        } else {
            payload.setDeltaMovement(releaseVelocity);
        }
        return payload;
    }

    private LoadedPayload findLoadedPayload(DroneInventoryContainer inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            for (PayloadDefinition definition : SUPPORTED_PAYLOADS) {
                if (stackMatches(stack, definition.itemId())) {
                    return new LoadedPayload(slot, definition);
                }
            }
        }
        return null;
    }

    private int countLoadedPayloads(DroneInventoryContainer inventory) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            for (PayloadDefinition definition : SUPPORTED_PAYLOADS) {
                if (stackMatches(stack, definition.itemId())) {
                    count += stack.getCount();
                    break;
                }
            }
        }
        return count;
    }

    private static boolean stackMatches(ItemStack stack, String itemId) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && itemId.equals(key.toString());
    }

    private void pickupHeavyBomber(Player player) {
        ItemHandlerHelper.giveItemToPlayer(player, createStackWithBombs(getBombsRemaining()));

        player.getInventory().items.stream()
                .filter(SbwCompat::isMonitor)
                .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

        if (this.level() instanceof ServerLevel serverLevel) {
            SbwCompat.disconnectControllersForDrone(serverLevel, this);
        }

        this.discard();
    }

    private record PayloadDefinition(String itemId, String kind) {
    }

    private record LoadedPayload(int slot, PayloadDefinition definition) {
    }
}
