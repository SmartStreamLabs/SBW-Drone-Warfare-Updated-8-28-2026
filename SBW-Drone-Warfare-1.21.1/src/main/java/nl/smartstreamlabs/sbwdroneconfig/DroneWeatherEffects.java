package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;import net.minecraft.server.level.ServerPlayer;
/** Compatibility entry points. Central moisture simulation is ticked exactly once through Entity.baseTick. */
public final class DroneWeatherEffects {
 public static void onDroneBaseTick(Entity e){}
 public static void applyRainEffects(Entity e){}
 public static void applyStormEffects(Entity e){}
 public static int getAdditionalEnergyUse(Entity e,boolean active){return 0;}
 public static int getEffectiveControlRange(ServerPlayer p,Entity e,int range){return e instanceof Hx2Entity?Math.min(range,Hx2Config.RANGE.get()):range;}
 private DroneWeatherEffects(){}
}
