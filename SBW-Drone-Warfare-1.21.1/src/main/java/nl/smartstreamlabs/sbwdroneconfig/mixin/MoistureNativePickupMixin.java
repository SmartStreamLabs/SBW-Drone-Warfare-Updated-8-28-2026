package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.world.entity.Entity;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.ItemStack;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import nl.smartstreamlabs.sbwdroneconfig.MoistureSystem;
@Mixin(targets="com.atsuishio.superbwarfare.entity.vehicle.DroneEntity",remap=false)
public abstract class MoistureNativePickupMixin {
 @Redirect(method={"interact","m_6096_"},at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/items/ItemHandlerHelper;giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V"),require=0)
 private void moisture$pack(Player p,ItemStack stack){if(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("superbwarfare:drone"))stack=MoistureSystem.pack((Entity)(Object)this,stack);net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer(p,stack);}
}
