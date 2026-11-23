package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;import net.minecraft.nbt.CompoundTag;
public final class ServiceContainers{
 public static boolean is(ItemStack s,String name){var n=s.getTagElement("BlockEntityTag"); return n!=null&&("sbwdroneconfig:"+name).equals(n.getString("EntityType"));}
}
