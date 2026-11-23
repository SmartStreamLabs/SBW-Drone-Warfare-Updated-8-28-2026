package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;import net.minecraft.client.Minecraft;import nl.smartstreamlabs.sbwdroneconfig.VatagClient;
@Mixin(value=Minecraft.class,remap=true)
public abstract class VatagPlayerActionsMixin {
 @Inject(method="handleKeybinds",at=@At("HEAD"),cancellable=true) private void vatag$remoteKeys(CallbackInfo c){if(VatagClient.active()){var o=Minecraft.getInstance().options;for(var key:o.keyHotbarSlots)while(key.consumeClick()){}while(o.keyAdvancements.consumeClick()){}while(o.keyInventory.consumeClick()){}c.cancel();}}

 @Inject(method="startAttack",at=@At("HEAD"),cancellable=true,remap=true) private void vatag$attack(CallbackInfoReturnable<Boolean> c){if((VatagClient.active()||nl.smartstreamlabs.sbwdroneconfig.Rd01Client.active()||nl.smartstreamlabs.sbwdroneconfig.Id01Client.active()))c.setReturnValue(false);}
 @Inject(method="continueAttack",at=@At("HEAD"),cancellable=true,remap=true) private void vatag$dig(boolean down,CallbackInfo c){if((VatagClient.active()||nl.smartstreamlabs.sbwdroneconfig.Rd01Client.active()||nl.smartstreamlabs.sbwdroneconfig.Id01Client.active()))c.cancel();}
 @Inject(method="startUseItem",at=@At("HEAD"),cancellable=true,remap=true) private void vatag$use(CallbackInfo c){if((VatagClient.blocksUse()||nl.smartstreamlabs.sbwdroneconfig.Rd01Client.active()||nl.smartstreamlabs.sbwdroneconfig.Id01Client.active()))c.cancel();}
}
