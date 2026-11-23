package nl.smartstreamlabs.sbwdroneconfig.mixin;
import com.atsuishio.superbwarfare.data.gun.GunData;import nl.smartstreamlabs.sbwdroneconfig.Dil01Item;import net.minecraft.world.entity.Entity;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Creative mode and native infinite-ammo sources cannot manufacture a DIL round. Normal SBW guns are untouched. */
@Mixin(value=GunData.class,remap=false)
public abstract class Dil01AmmoMixin {
 @Inject(method="hasInfiniteBackupAmmo",at=@At("HEAD"),cancellable=true)private void dil01$realAmmo(Entity entity,CallbackInfoReturnable<Boolean> ci){if(((GunData)(Object)this).item instanceof Dil01Item)ci.setReturnValue(false);}
 @Inject(method="countBackupAmmo(Lnet/minecraft/world/entity/Entity;)I",at=@At("HEAD"),cancellable=true)private void dil01$count(Entity entity,CallbackInfoReturnable<Integer> ci){var d=(GunData)(Object)this;if(d.item instanceof Dil01Item&&entity instanceof net.minecraft.world.entity.player.Player p)ci.setReturnValue(d.selectedAmmoConsumer().count(d,new net.minecraftforge.items.wrapper.InvWrapper(p.getInventory())));}
 @Inject(method="consumeBackupAmmo(Lnet/minecraft/world/entity/Entity;I)V",at=@At("HEAD"),cancellable=true)private void dil01$consume(Entity entity,int count,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){var d=(GunData)(Object)this;if(d.item instanceof Dil01Item&&entity instanceof net.minecraft.world.entity.player.Player p){if(count>0)d.selectedAmmoConsumer().consume(d,new net.minecraftforge.items.wrapper.InvWrapper(p.getInventory()),count);ci.cancel();}}
}
