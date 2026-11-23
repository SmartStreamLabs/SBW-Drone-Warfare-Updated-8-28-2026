package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import nl.smartstreamlabs.sbwdroneconfig.DroneJamStateManager;
import nl.smartstreamlabs.sbwdroneconfig.DroneTrackingHooks;
import nl.smartstreamlabs.sbwdroneconfig.SbwCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.atsuishio.superbwarfare.item.misc.MonitorItem")
public abstract class MonitorItem089Mixin {
    @Inject(method = {"use", "m_7203_"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sbwdroneconfig$preventMonitorEnableWhileHardJammed(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if(nl.smartstreamlabs.sbwdroneconfig.InjuryHooks.downed(player)){cir.setReturnValue(InteractionResultHolder.fail(stack));return;}
        if (!SbwCompat.isLinkedMonitor(stack) || SbwCompat.isUsingLinkedMonitor(stack)) {
            return;
        }

        if (!SbwCompat.hasEquippedFpvGoggles(player)) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.sbwdroneconfig.fpv_goggles_required"),
                        true
                );
            }
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Entity drone = SbwCompat.findLinkedDrone(serverPlayer, stack);
        if (drone == null || !DroneJamStateManager.isHardJammed(drone)) {
            return;
        }

        serverPlayer.displayClientMessage(
                Component.translatable("message.sbwdroneconfig.drone_jammed_hard"),
                true
        );
        cir.setReturnValue(InteractionResultHolder.fail(stack));
    }

    @Redirect(
            method = "inventoryTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;putBoolean(Ljava/lang/String;Z)V",
                    ordinal = 1
            ),
            remap = false,
            require = 0
    )
    private void sbwdroneconfig$avoidClientSignalDropDev(
            CompoundTag tag,
            String key,
            boolean value,
            ItemStack itemstack,
            Level world,
            Entity entity,
            int slot,
            boolean selected
    ) {
        sbwdroneconfig$avoidClientSignalDrop(tag, key, value, itemstack, world, entity, slot, selected);
    }

    @Redirect(
            method = "m_6883_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;m_128379_(Ljava/lang/String;Z)V",
                    ordinal = 1
            ),
            remap = false,
            require = 0
    )
    private void sbwdroneconfig$avoidClientSignalDropSrg(
            CompoundTag tag,
            String key,
            boolean value,
            ItemStack itemstack,
            Level world,
            Entity entity,
            int slot,
            boolean selected
    ) {
        sbwdroneconfig$avoidClientSignalDrop(tag, key, value, itemstack, world, entity, slot, selected);
    }

    private void sbwdroneconfig$avoidClientSignalDrop(
            CompoundTag tag,
            String key,
            boolean value,
            ItemStack itemstack,
            Level world,
            Entity entity,
            int slot,
            boolean selected
    ) {
        if (DroneTrackingHooks.shouldSuppressClientMonitorDisconnect(itemstack, world, entity, selected)) {
            return;
        }

        tag.putBoolean(key, value);
    }
}
