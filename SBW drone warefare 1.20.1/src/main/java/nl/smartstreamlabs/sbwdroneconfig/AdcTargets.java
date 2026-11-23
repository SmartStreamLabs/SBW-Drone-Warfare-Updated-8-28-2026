package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.phys.Vec3;import java.util.UUID;
/** Central small-aircraft filter; relationship rules remain the existing SBW team/owner adapter. */
public final class AdcTargets{
 public static int category(Entity e){if(!Mpl01Entity.airDrone(e)||e instanceof BayraktarTb2Entity||e instanceof Geranium2Entity)return -1;if(e instanceof DeliveryDroneEntity||e instanceof HeavyBomberDroneEntity)return 1;if(e instanceof LucasDroneEntity||e instanceof Hx2Entity||e instanceof GlideBombEntity)return 2;return 0;}
 public static boolean hostile(ServerLevel l,UUID owner,String name,Entity e,int priorities){int c=category(e);return c>=0&&((priorities>>(c*2))&3)>0&&Adv01Targets.relation(l,owner,name,e)==Adv01Targets.HOSTILE;}
 public static boolean protectedBy(java.util.Set<UUID> list,ServerLevel l,Entity e){if(e==null)return false;if(list.contains(e.getUUID()))return true;var o=SbwCompat.resolveDroneOwner(l,e);if(o.hasUuid()&&list.contains(o.uuid())||list.contains(SbwCompat.getDroneControllerUuid(e)))return true;for(var passenger:e.getPassengers())if(list.contains(passenger.getUUID()))return true;return false;}
 public static Vec3 lead(Vec3 from,Entity e){var center=e.getBoundingBox().getCenter();double t=java.lang.Math.min(25,from.distanceTo(center)/4.5);return center.add(e.getDeltaMovement().scale(t));}
 public static double score(Vec3 from,Entity e,int priorities){int priority=(priorities>>(category(e)*2))&3;boolean closing=e.getDeltaMovement().dot(from.subtract(e.position()))>0;return from.distanceToSqr(e.getBoundingBox().getCenter())-priority*10000-(closing&&category(e)!=1?4000:0);}
}
