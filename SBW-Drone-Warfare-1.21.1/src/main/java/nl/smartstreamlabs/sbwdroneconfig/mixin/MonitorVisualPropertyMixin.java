package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Preserve SBW's registered property, independently of client setup registration order. */
@Mixin(value=ItemProperties.class,remap=false)
public abstract class MonitorVisualPropertyMixin {
 @Unique private static ItemPropertyFunction dronewarfare$original,dronewarfare$wrapped;
 @Inject(method="getProperty",at=@At("RETURN"),cancellable=true,require=1)
 private static void dronewarfare$monitor(ItemStack stack,ResourceLocation key,CallbackInfoReturnable<ItemPropertyFunction> cir){
  if(!key.getNamespace().equals("superbwarfare")||!key.getPath().equals("monitor_linked"))return;
  var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
  if(!id.getNamespace().equals("superbwarfare")||!id.getPath().equals("monitor"))return;
  var original=cir.getReturnValue();
  if(dronewarfare$wrapped==null||original!=dronewarfare$original){
   dronewarfare$original=original;
   dronewarfare$wrapped=(monitor,level,entity,seed)->nl.smartstreamlabs.sbwdroneconfig.GroundMonitorVisual.linked(monitor)?1:original==null?0:original.call(monitor,level,entity,seed);
  }
  cir.setReturnValue(dronewarfare$wrapped);
 }
}
