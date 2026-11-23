package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import java.util.*;
public final class Dc01Docking {
 static final String SLOT="DC01Slot",PROFILE="DC01Profile",PARENT="DC01LaunchParent",UNTIL="DC01LaunchUntil";
 public static Dc01Entity carrier(Entity e){return e!=null&&e.getVehicle() instanceof Dc01Entity c?c:null;}
 public static boolean docked(Entity e){return carrier(e)!=null;}
 public static int profile(Entity e){if(e.getType()==AddonEntities.HEAVY_BOMBER_DRONE.get()&&e instanceof HeavyBomberDroneEntity)return 3;if(e.getType()==AddonEntities.CUBED_FPV_DRONE.get()&&e instanceof CubedFpvDroneEntity){if(!SbwCompat.getDroneCurrentItem(e).isEmpty())return 0;var inv=new DroneInventoryContainer(e);for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(AddonItems.FIBER_OPTIC_SPOOL_UPGRADE.get()))return 2;return 1;}return 0;}
 public static void neutralize(Entity e){if(!(e instanceof DroneEntity d))return;SbwCompat.resetDroneInput(e);SbwCompat.setDroneFire(e,false);d.setForwardInputDown(false);d.setBackInputDown(false);d.setLeftInputDown(false);d.setRightInputDown(false);d.setUpInputDown(false);d.setDownInputDown(false);d.setBodyXRot(0);d.setZRot(0);e.setXRot(0);e.setDeltaMovement(Vec3.ZERO);e.setOnGround(true);}
 public static boolean owned(ServerPlayer p,Entity e){var owner=SbwCompat.resolveDroneOwner(p.serverLevel(),e);var controller=SbwCompat.getDroneControllerPlayer(e);return (controller==null||controller==p)&&(p.hasPermissions(2)||(owner.uuid()!=null?p.getUUID().equals(owner.uuid()):owner.name()==null||p.getGameProfile().getName().equals(owner.name())));}
 public static boolean sameCarrier(Entity a,Entity b){if(a==null||b==null||a==b)return false;if(Ndc01Monitor.grace(a,b)||Ndc01Monitor.grace(b,a))return true;var ca=a instanceof Dc01Entity c?c:carrier(a);var cb=b instanceof Dc01Entity c?c:carrier(b);if(ca!=null&&ca==cb)return true;return grace(a,b)||grace(b,a)||cb!=null&&grace(a,cb)||ca!=null&&grace(b,ca);}
 private static boolean grace(Entity drone,Entity parent){var n=drone.getPersistentData();return parent instanceof Dc01Entity&&n.hasUUID(PARENT)&&parent.getUUID().equals(n.getUUID(PARENT))&&drone.level().getGameTime()<=n.getLong(UNTIL);}
 public static void departure(Entity d,Dc01Entity c){d.getPersistentData().putUUID(PARENT,c.getUUID());d.getPersistentData().putLong(UNTIL,c.level().getGameTime()+12);}
 public static void departureInput(Entity e){Ndc01Monitor.departureInput(e);if(!e.getPersistentData().contains(UNTIL))return;long left=e.getPersistentData().getLong(UNTIL)-e.level().getGameTime();if(left<0){e.getPersistentData().remove(UNTIL);e.getPersistentData().remove(PARENT);}else if(left>=6&&e instanceof DroneEntity d)d.setUpInputDown(true);}
 public static boolean carrierMonitor(ItemStack s){return s.hasTag()&&s.getTag().getBoolean("DC01CarrierMonitor");}
 public static void markMonitor(ItemStack s,boolean value){s.getOrCreateTag().putBoolean("DC01CarrierMonitor",value);}
 private Dc01Docking(){}
}
