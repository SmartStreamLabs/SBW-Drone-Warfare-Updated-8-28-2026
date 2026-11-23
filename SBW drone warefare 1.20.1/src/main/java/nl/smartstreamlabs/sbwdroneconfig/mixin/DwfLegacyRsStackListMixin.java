package nl.smartstreamlabs.sbwdroneconfig.mixin;
import java.util.*;import com.google.common.collect.ArrayListMultimap;import net.minecraft.world.item.*;import net.minecraftforge.items.ItemHandlerHelper;
import com.refinedmods.refinedstorage.api.util.*;import nl.smartstreamlabs.sbwdroneconfig.DwfRs1Key;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;

/** Verified RS 1.12.4 hot path. Its native per-item linear scan is quadratic for
 * many NBT variants. Keep native collections/UUIDs/fuzzy queries, index exact
 * variants, and report the ACTUAL clipped delta so Grid integers cannot wrap. */
@Pseudo @Mixin(targets="com.refinedmods.refinedstorage.apiimpl.util.ItemStackList",remap=false)
public abstract class DwfLegacyRsStackListMixin{
 @Shadow(remap=false) @Final private ArrayListMultimap<Item,StackListEntry<ItemStack>> stacks;
 @Shadow(remap=false) @Final private Map<UUID,ItemStack> index;
 @Unique private Map<DwfRs1Key,StackListEntry<ItemStack>> dwf$exact;
 @Unique private Map<DwfRs1Key,StackListEntry<ItemStack>> dwf$map(){if(dwf$exact==null){dwf$exact=new HashMap<>();for(var entry:stacks.values())dwf$exact.put(new DwfRs1Key(entry.getStack(),true),entry);}return dwf$exact;}
 @Inject(method="add(Lnet/minecraft/world/item/ItemStack;I)Lcom/refinedmods/refinedstorage/api/util/StackListResult;",at=@At("HEAD"),cancellable=true,require=0)
 private void dwf$add(ItemStack stack,int size,CallbackInfoReturnable<StackListResult<ItemStack>> ci){
  if(stack.isEmpty()||size<=0)throw new IllegalArgumentException("Cannot accept empty stack");var map=dwf$map();var entry=map.get(new DwfRs1Key(stack,false));
  if(entry!=null){var other=entry.getStack();int old=other.getCount(),next=(int)Math.min(Integer.MAX_VALUE,(long)old+size);other.setCount(next);ci.setReturnValue(new StackListResult<>(other,entry.getId(),next-old));}
  else{var created=new StackListEntry<>(ItemHandlerHelper.copyStackWithSize(stack,size));stacks.put(stack.getItem(),created);index.put(created.getId(),created.getStack());map.put(new DwfRs1Key(created.getStack(),true),created);ci.setReturnValue(new StackListResult<>(created.getStack(),created.getId(),size));}
 }
 @Inject(method="remove(Lnet/minecraft/world/item/ItemStack;I)Lcom/refinedmods/refinedstorage/api/util/StackListResult;",at=@At("HEAD"),cancellable=true,require=0)
 private void dwf$remove(ItemStack stack,int size,CallbackInfoReturnable<StackListResult<ItemStack>> ci){
  if(size<=0)return;if(stack.isEmpty()){ci.setReturnValue(null);return;}var key=new DwfRs1Key(stack,false);var map=dwf$map();var entry=map.get(key);if(entry==null){ci.setReturnValue(null);return;}var other=entry.getStack();int old=other.getCount();
  if(size>=old){stacks.remove(other.getItem(),entry);index.remove(entry.getId());map.remove(key);ci.setReturnValue(new StackListResult<>(other,entry.getId(),-old));}
  else{other.shrink(size);ci.setReturnValue(new StackListResult<>(other,entry.getId(),-size));}
 }
 @Inject(method="get(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;",at=@At("HEAD"),cancellable=true,require=0)
 private void dwf$get(ItemStack stack,int flags,CallbackInfoReturnable<ItemStack> ci){if(flags==1){var entry=stack.isEmpty()?null:dwf$map().get(new DwfRs1Key(stack,false));ci.setReturnValue(entry==null?null:entry.getStack());}}
 @Inject(method="getEntry(Lnet/minecraft/world/item/ItemStack;I)Lcom/refinedmods/refinedstorage/api/util/StackListEntry;",at=@At("HEAD"),cancellable=true,require=0)
 private void dwf$entry(ItemStack stack,int flags,CallbackInfoReturnable<StackListEntry<ItemStack>> ci){if(flags==1)ci.setReturnValue(stack.isEmpty()?null:dwf$map().get(new DwfRs1Key(stack,false)));}
 @Inject(method="clear",at=@At("RETURN"),require=0)private void dwf$clear(CallbackInfo ci){dwf$exact=null;}
}
