package nl.smartstreamlabs.sbwdroneconfig.mixin;
import java.util.*;import java.math.BigInteger;
import net.minecraft.world.item.ItemStack;
import nl.smartstreamlabs.sbwdroneconfig.*;
import com.refinedmods.refinedstorage.api.network.INetwork;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Keep the ordinary RS disk/external contribution separate from the synthetic
 * bounded DWF view. Otherwise an ordinary chest of the same item would be
 * mistaken for part of the DWF allowance and disappear from the Grid count. */
@Pseudo @Mixin(targets="com.refinedmods.refinedstorage.apiimpl.storage.cache.ItemStorageCache",remap=false)
public abstract class DwfRs1CacheMixin implements DwfRs1ForeignStock {
 @Shadow(remap=false) @Final private INetwork network;
 @Unique private final Map<DwfRs1Key,BigInteger>dwf$other=new HashMap<>();
 @Unique private final Set<DwfRs1Key>dwf$clipped=new HashSet<>();
 @Override public void dwf$limit(ItemStack stack,boolean limited){if(limited)dwf$clipped.add(new DwfRs1Key(stack,true));else dwf$clipped.remove(new DwfRs1Key(stack,false));}
 @Override public boolean dwf$limited(){return !dwf$clipped.isEmpty();}
 @Override public BigInteger dwf$foreign(ItemStack stack){return dwf$other.getOrDefault(new DwfRs1Key(stack,false),BigInteger.ZERO);}
 @Unique private void dwf$delta(ItemStack stack,int amount){if(DwfRs1.viewMutation()||amount==0||stack.isEmpty())return;var probe=new DwfRs1Key(stack,false);var old=dwf$other.get(probe);var next=(old==null?BigInteger.ZERO:old).add(BigInteger.valueOf(amount)).max(BigInteger.ZERO);if(next.signum()==0)dwf$other.remove(probe);else dwf$other.put(old==null?new DwfRs1Key(stack,true):probe,next);DwfRs1.foreignChanged(network,stack);}
 @Inject(method="invalidate",at=@At("HEAD"),require=1)private void dwf$reset(com.refinedmods.refinedstorage.api.storage.cache.InvalidateCause cause,CallbackInfo ci){((com.refinedmods.refinedstorage.api.storage.cache.IStorageCache<ItemStack>)(Object)this).flush();dwf$other.clear();dwf$clipped.clear();}
 @Inject(method="add(Lnet/minecraft/world/item/ItemStack;IZZ)V",at=@At("HEAD"),require=1)private void dwf$add(ItemStack stack,int count,boolean rebuilding,boolean batched,CallbackInfo ci){dwf$delta(stack,count);}
 @Inject(method="remove(Lnet/minecraft/world/item/ItemStack;IZ)V",at=@At("HEAD"),require=1)private void dwf$remove(ItemStack stack,int count,boolean batched,CallbackInfo ci){dwf$delta(stack,-count);}
}
