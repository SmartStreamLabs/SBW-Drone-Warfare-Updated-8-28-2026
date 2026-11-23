package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=net.minecraft.client.KeyMapping.class,remap=false)
public abstract class Rd01KeyMappingMixin {
    // Consume the original queued click before suppressing it, so it cannot open a GUI on monitor exit.
    @Inject(method="consumeClick",at=@At("RETURN"),cancellable=true)
    private void rd01$reservedClick(CallbackInfoReturnable<Boolean> c){if((nl.smartstreamlabs.sbwdroneconfig.Rd01Client.blocksMapping((net.minecraft.client.KeyMapping)(Object)this)||nl.smartstreamlabs.sbwdroneconfig.VatagClient.blocksMapping((net.minecraft.client.KeyMapping)(Object)this)))c.setReturnValue(false);}
    @Inject(method="isDown",at=@At("RETURN"),cancellable=true)
    private void rd01$reservedHeld(CallbackInfoReturnable<Boolean> c){if((nl.smartstreamlabs.sbwdroneconfig.Rd01Client.blocksMapping((net.minecraft.client.KeyMapping)(Object)this)||nl.smartstreamlabs.sbwdroneconfig.VatagClient.blocksMapping((net.minecraft.client.KeyMapping)(Object)this)))c.setReturnValue(false);}
}
