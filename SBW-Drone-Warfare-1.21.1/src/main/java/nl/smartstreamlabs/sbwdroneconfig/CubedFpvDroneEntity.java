package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.init.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.L_WHEEL_HEALTH;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.MAIN_ENGINE_HEALTH;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.R_WHEEL_HEALTH;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.SUB_ENGINE_HEALTH;
import static com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.TURRET_HEALTH;

public class CubedFpvDroneEntity extends DroneEntity {
    private static final net.minecraft.network.syncher.EntityDataAccessor<ItemStack> HG_VISUAL=net.minecraft.network.syncher.SynchedEntityData.defineId(CubedFpvDroneEntity.class,net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK);
    public final java.util.List<ItemStack> hgDeathDrops=new java.util.ArrayList<>();
    public ItemStack hgVisual(){return entityData.get(HG_VISUAL);}
    public void hgVisual(ItemStack stack){if(!ItemStack.matches(stack,hgVisual()))entityData.set(HG_VISUAL,stack);}
    private static final float MAX_HEALTH = 1.0F;
    private static final float COMPONENT_MAX_HEALTH = 1.0F;
    private static final float INCOMING_DAMAGE_MULTIPLIER = 8.0F;

    public CubedFpvDroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super((EntityType<DroneEntity>) type, level);
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(HG_VISUAL,ItemStack.EMPTY);}
    @Override
    public float getMaxHealth() {
        return VehicleProfiles.health(this,getDroneMaxHealthLimit());
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

    /** SBW's proximity crash query uses the entire carrier AABB, including empty space below its mast. */
    @Override public void hitEntityCrash(Player pilot,net.minecraft.world.entity.Entity target){
        if(target instanceof Ndc01Entity carrier){
            if(Ndc01Monitor.grace(this,carrier))return;
            if(carrier.getOBBs().stream().noneMatch(o->com.atsuishio.superbwarfare.tools.OBB.isColliding(o,getBoundingBox().inflate(.04))))return;
        }
        super.hitEntityCrash(pilot,target);
    }

    @Override
    public void baseTick() {
        Hg01Payload.beforeTick(this);
        if(Dc01Docking.docked(this))Dc01Docking.neutralize(this);
        super.baseTick();

        if (!this.level().isClientSide()) {
            CubedFpvInputState.applyRecentInput(this);
            clampDurabilityToFpvLimit();
        }
    }

    @Override public void destroy(){Sk01Deaths.destroyed(this);Hg01Payload.destroyed(this);super.destroy();Hg01Payload.finishDrops(this);}
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getMainHandItem();
        if ((player.isCrouching() || player.isShiftKeyDown()) && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                ItemHandlerHelper.giveItemToPlayer(player, PlatformInventory.packDrone(this, MoistureSystem.pack(this, new ItemStack(AddonItems.CUBED_FPV_DRONE.get()))));

                // Packed entity data contains the real payload; no separate duplicate drop.

                player.getInventory().items.stream()
                        .filter(SbwCompat::isMonitor)
                        .forEach(itemStack -> SbwCompat.unlinkMonitorFromDrone(itemStack, player, this));

                if (this.level() instanceof ServerLevel serverLevel) {
                    SbwCompat.disconnectControllersForDrone(serverLevel, this);
                }

                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.interact(player, hand);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if(tag.contains("PlatformInventoryPayload",10)){var s=ItemStack.parseOptional(registryAccess(),tag.getCompound("PlatformInventoryPayload"));if(platformPayloadAllowed(s))platformPayload(s);}
        clampDurabilityToFpvLimit();
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(AddonItems.CUBED_FPV_DRONE.get());
    }

    private boolean platformManagedPayload;
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(platformManagedPayload&&!SbwCompat.getDroneCurrentItem(this).isEmpty())n.put("PlatformInventoryPayload",SbwCompat.getDroneCurrentItem(this).save(registryAccess()));}
    public static boolean platformPayloadAllowed(ItemStack stack){return stack.isEmpty()||Hg01Payload.is(stack)||com.atsuishio.superbwarfare.data.CustomData.DRONE_ATTACHMENT.get(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())!=null;}
    public void platformPayload(ItemStack stack){platformManagedPayload=true;
        if(!platformPayloadAllowed(stack))throw new IllegalArgumentException("Unsupported FPV payload");
        SbwCompat.setDroneCurrentItem(this,stack.isEmpty()?ItemStack.EMPTY:stack.copyWithCount(1));
        entityData.set(AMMO,stack.isEmpty()?0:1);entityData.set(MAX_AMMO,1);entityData.set(IS_KAMIKAZE,false);entityData.set(DISPLAY_ENTITY,"");hgVisual(ItemStack.EMPTY);
        if(stack.isEmpty())return;
        if(Hg01Payload.is(stack)){hgVisual(stack.copyWithCount(1));return;}
        var a=com.atsuishio.superbwarfare.data.CustomData.DRONE_ATTACHMENT.get(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        entityData.set(IS_KAMIKAZE,a.isKamikaze);entityData.set(DISPLAY_ENTITY,a.displayEntity());
        entityData.set(DISPLAY_ENTITY_TAG,a.displayData()==null?new CompoundTag():com.atsuishio.superbwarfare.tools.TagDataParser.parse(a.displayData(),v->net.minecraft.nbt.StringTag.valueOf(v)));
        var sc=a.scale();var of=a.offset();var ro=a.rotation();entityData.set(DISPLAY_DATA,java.util.List.of(sc[0],sc[1],sc[2],of[0],of[1],of[2],ro[0],ro[1],ro[2],a.xLength,a.zLength,(float)a.tickCount));
    }
    private void clampDurabilityToFpvLimit() {
        clampHealthToFpvLimit();
        clampComponentHealthToFpvLimit();
    }

    private void clampHealthToFpvLimit() {
        float maxHealth = getMaxHealth();
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
}
