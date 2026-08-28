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
    private static final float MAX_HEALTH = 1.0F;
    private static final float COMPONENT_MAX_HEALTH = 1.0F;
    private static final float INCOMING_DAMAGE_MULTIPLIER = 8.0F;

    public CubedFpvDroneEntity(EntityType<? extends DroneEntity> type, Level level) {
        super((EntityType<DroneEntity>) type, level);
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
        super.baseTick();

        if (!this.level().isClientSide()) {
            CubedFpvInputState.applyRecentInput(this);
            clampDurabilityToFpvLimit();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getMainHandItem();
        if (player.isCrouching() && (stack.isEmpty() || stack.is(ModTags.Items.TOOLS_CROWBAR))) {
            if (!this.level().isClientSide()) {
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(AddonItems.CUBED_FPV_DRONE.get()));

                for (int index = 0; index < SbwCompat.getDroneAmmo(this); index++) {
                    if (!this.getCurrentItem().isEmpty()) {
                        ItemHandlerHelper.giveItemToPlayer(player, this.getCurrentItem().copy());
                    }
                }

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
        clampDurabilityToFpvLimit();
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return new ItemStack(AddonItems.CUBED_FPV_DRONE.get());
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
}
