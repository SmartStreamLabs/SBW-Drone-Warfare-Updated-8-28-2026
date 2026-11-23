package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.ItemStack;
/** Read-only model state. Rendering must never clean or rewrite a monitor link. */
public final class GroundMonitorVisual {
 public static boolean linked(ItemStack stack){
  if(!SbwCompat.isMonitor(stack))return false;
  var n=stack.getTag();if(n==null)return false;
  if(n.getBoolean(SbwCompat.TAG_LINKED))return true;
  if(!n.getBoolean(SbwCompat.TAG_USING)&&n.hasUUID("VatagUUID"))return true;
  if(n.hasUUID("PadLoad")&&n.contains("PadPosition",99)&&!n.getString("PadDimension").isEmpty())return true;
  return n.hasUUID("NdcCarrier")&&n.hasUUID("NdcLoad")&&n.contains("NdcSlot",99)&&n.getInt("NdcSlot")>=0&&n.getInt("NdcSlot")<18&&!n.getString("NdcDimension").isEmpty();
 }
 private GroundMonitorVisual(){}
}
