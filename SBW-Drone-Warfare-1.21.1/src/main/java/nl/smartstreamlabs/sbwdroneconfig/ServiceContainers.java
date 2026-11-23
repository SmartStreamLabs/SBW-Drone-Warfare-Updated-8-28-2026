package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;import net.minecraft.nbt.CompoundTag;
public final class ServiceContainers{
 public static boolean is(ItemStack s,String name){var c=s.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);var n=c==null?null:c.copyTag(); return n!=null&&("sbwdroneconfig:"+name).equals(n.getString("EntityType"));}
}
