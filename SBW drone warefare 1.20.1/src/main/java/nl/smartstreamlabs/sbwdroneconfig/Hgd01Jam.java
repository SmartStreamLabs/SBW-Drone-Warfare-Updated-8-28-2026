package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.ItemStack;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.entity.Entity;import java.util.*;
/** Adapter for the existing jammer accumulator, deliberately not a native flying DroneEntity. */
public final class Hgd01Jam {
 public static boolean monitor(ItemStack s){return s.hasTag()&&s.getTag().getBoolean("HGD01Monitor");}
 public static void mark(ItemStack s,boolean value){s.getOrCreateTag().putBoolean("HGD01Monitor",value);}
 public static boolean affects(ServerLevel l,Hgd01Entity e,UUID owner,Set<String> whitelist){
  if(!e.isAlive()||e.isWreck())return false;var operator=e.operator();
  for(UUID id:new UUID[]{e.ownerId(),operator==null?null:operator.getUUID()}){if(id==null)continue;var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null&&whitelist.contains(AddonConfig.normalizeName(p.getGameProfile().getName(),AddonConfig.blacklistMatchCaseInsensitive())))return false;}
  return AddonConfig.affectFriendlyDrones()||owner==null||!owner.equals(e.ownerId());
 }
 private Hgd01Jam(){}
}
