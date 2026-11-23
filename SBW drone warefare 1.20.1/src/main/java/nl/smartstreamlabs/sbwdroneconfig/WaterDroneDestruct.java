package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.entity.Entity;
/** Authenticated server-clock hold. Duplicate commands cannot accelerate the fuse. */
public final class WaterDroneDestruct{
 private record Hold(long start,long tick){}private static final Map<Entity,Hold>HOLDS=new WeakHashMap<>();
 public static void clear(Entity e){HOLDS.remove(e);}
 public static void command(ServerPlayer p,VatagEntity v,boolean pressed){
  if(v instanceof Ndc01Entity||!(v instanceof Sd01Entity||v instanceof Wd01Entity)||!pressed){clear(v);return;}
  if(v.isRemoved()||!v.isAlive()||v.isWreck()||v.operator()!=p||!v.permitted(p)||!VatagSessions.controlling(p,v)||v.getEnergy()<=0||DroneJamStateManager.isHardJammed(v)){clear(v);return;}
  long now=v.level().getGameTime();var old=HOLDS.get(v);if(old!=null&&old.tick()==now)return;long start=old!=null&&now-old.tick()<=10?old.start():now;HOLDS.put(v,new Hold(start,now));if(now-start<20)return;clear(v);
  DroneAttackDamage.mark(v,v,"sbwdroneconfig:water_drone_self_destruct");var at=v.position();VatagSessions.stop(p);var inventory=v.getInventory();for(int slot=0;slot<inventory.getSlots();slot++)inventory.setStackInSlot(slot,net.minecraft.world.item.ItemStack.EMPTY);
  try(var scope=DroneAttackDamage.scope(v)){new com.atsuishio.superbwarfare.tools.CustomExplosion.Builder(v).directSource(v).source(v).attacker(p).position(at).damage(160).radius(4).keepBlock().fireTime(0).explode();}finally{v.discard();}
 }
 private WaterDroneDestruct(){}
}
