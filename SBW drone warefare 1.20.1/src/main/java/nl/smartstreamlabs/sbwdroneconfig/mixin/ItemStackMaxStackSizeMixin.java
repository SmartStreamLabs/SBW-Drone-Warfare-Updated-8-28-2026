package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.world.item.ItemStack;
import nl.smartstreamlabs.sbwdroneconfig.SbwItemStackingRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMaxStackSizeMixin {
    @Inject(method = {"getMaxStackSize", "m_41741_"}, at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void sbwdroneconfig$overrideSbwMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        int maxStackSize = SbwItemStackingRules.getMaxStackSize((ItemStack) (Object) this);
        if (maxStackSize > 0) {
            cir.setReturnValue(maxStackSize);
        }
    }
}
