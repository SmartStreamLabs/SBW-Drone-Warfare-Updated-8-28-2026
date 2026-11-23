package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;
/** Reads an immutable baked snapshot; no spec traversal, file parsing or per-entity cache. */
public final class VehicleProfiles {
 private static VehicleProfileValues.Factors factors(Entity e){return VehicleProfilesConfig.factors(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()));}
 public static float health(Entity e,float nativeHealth){return VehicleProfileValues.scaled(nativeHealth,factors(e).health());}
 public static float incoming(Entity e,float nativeDamage){return VehicleProfileValues.scaled(nativeDamage,factors(e).incomingDamage());}
 private VehicleProfiles(){}
}
