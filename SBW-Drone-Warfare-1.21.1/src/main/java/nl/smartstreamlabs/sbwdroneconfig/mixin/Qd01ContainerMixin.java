package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.*;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.*;import net.minecraft.world.item.*;import net.minecraft.world.level.Level;import net.minecraft.world.entity.player.Player;
/** Real SBW crowbar containers retain their full entity NBT and deploy onto validated ground. Other containers are untouched. */
@Mixin(value=com.atsuishio.superbwarfare.item.container.ContainerBlockItem.class,remap=false)
public abstract class Qd01ContainerMixin {
 @Inject(method="use",remap=false,at=@At("HEAD"),cancellable=true) private void qd01$ground(Level l,Player p,InteractionHand h,CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci){var n=Qd01Item.container(p.getItemInHand(h));if(n!=null)ci.setReturnValue(Qd01Item.deploy(l,p,h,n));}
 @Inject(method="useOn",remap=false,at=@At("HEAD"),cancellable=true) private void qd01$groundOn(net.minecraft.world.item.context.UseOnContext c,CallbackInfoReturnable<InteractionResult> ci){if(c.getPlayer()==null)return;var n=Qd01Item.container(c.getItemInHand());if(n!=null)ci.setReturnValue(Qd01Item.deploy(c.getLevel(),c.getPlayer(),c.getHand(),n).getResult());}
}
