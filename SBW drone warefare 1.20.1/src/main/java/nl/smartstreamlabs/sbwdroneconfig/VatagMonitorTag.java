package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.ItemStack;import net.minecraft.core.BlockPos;import java.util.UUID;
public final class VatagMonitorTag {
 public static UUID linked(ItemStack s){if(!SbwCompat.isMonitor(s))return null;var n=s.getTag();if(n!=null&&n.getBoolean(SbwCompat.TAG_LINKED)){n.remove("VatagUUID");n.remove("VatagPosition");n.remove("VatagDimension");return null;}return n!=null&&!n.getBoolean(SbwCompat.TAG_LINKED)&&!n.getBoolean(SbwCompat.TAG_USING)&&n.hasUUID("VatagUUID")?n.getUUID("VatagUUID"):null;}
 public static boolean matches(ItemStack s,UUID id){return id.equals(linked(s));}
 public static void link(ItemStack s,UUID id){link(s,id,null);}
 public static void link(ItemStack s,UUID id,BlockPos pos){link(s,id,pos,null);}
 public static void link(ItemStack s,UUID id,BlockPos pos,String dimension){if(!SbwCompat.isMonitor(s))return;var n=s.getOrCreateTag();n.remove(SbwCompat.TAG_LINKED_DRONE);n.putBoolean(SbwCompat.TAG_USING,false);n.putBoolean(SbwCompat.TAG_LINKED,false);n.putUUID("VatagUUID",id);n.remove("Id01Linked");n.remove("Rd01Linked");if(pos!=null)n.putLong("VatagPosition",pos.asLong());if(dimension!=null)n.putString("VatagDimension",dimension);}
 public static String dimension(ItemStack s){var n=s.getTag();return n!=null&&n.contains("VatagDimension")?n.getString("VatagDimension"):null;}
 public static BlockPos position(ItemStack s){var n=s.getTag();return n!=null&&n.contains("VatagPosition")?BlockPos.of(n.getLong("VatagPosition")):null;}
}
