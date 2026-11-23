package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.ItemStack;
import com.refinedmods.refinedstorage.apiimpl.API;
/** Optional RS-1-only key: exactly the native comparer and quantity-free hash. */
public final class DwfRs1Key{
 private final ItemStack stack;private final int hash;
 public DwfRs1Key(ItemStack stack,boolean copy){this.stack=copy?stack.copy():stack;if(copy)this.stack.setCount(1);hash=API.instance().getItemStackHashCode(this.stack);}
 @Override public int hashCode(){return hash;}
 @Override public boolean equals(Object other){return other instanceof DwfRs1Key k&&hash==k.hash&&API.instance().getComparer().isEqualNoQuantity(stack,k.stack);}
}
