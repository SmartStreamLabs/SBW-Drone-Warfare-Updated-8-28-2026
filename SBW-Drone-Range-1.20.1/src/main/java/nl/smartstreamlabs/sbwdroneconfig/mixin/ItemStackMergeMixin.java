package nl.smartstreamlabs.sbwdroneconfig.mixin;

import nl.smartstreamlabs.sbwdroneconfig.SbwItemStackingRules;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMergeMixin {
    @Inject(method = {"isSameItemSameTags", "isSameItemSameComponents", "m_150942_"}, at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void sbwdroneconfig$allowSafeSbwItemStackMerging(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (SbwItemStackingRules.canMergeStackableSbwItems(first, second)) {
            cir.setReturnValue(true);
        }
    }
}
