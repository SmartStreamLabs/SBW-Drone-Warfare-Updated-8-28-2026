package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.phys.Vec3;import java.lang.reflect.*;
/** Resolve the actual 0.8.9 / 0.8.9.1 fire descriptor once; native gun logic stays authoritative. */
public final class VatagNative {
 private static final Method FIRE=resolve();
 private static Method resolve(){try{return VehicleEntity.class.getMethod("vehicleShoot",LivingEntity.class,String.class);}catch(NoSuchMethodException old){try{return VehicleEntity.class.getMethod("vehicleShoot",LivingEntity.class,String.class,Vec3.class);}catch(NoSuchMethodException absent){throw new IllegalStateException("VATAG requires a supported SBW vehicleShoot API",absent);}}}
 public static void validate(){}
 public static void fire(VatagEntity v,LivingEntity operator){try{if(FIRE.getParameterCount()==2)FIRE.invoke(v,operator,"Cannon");else FIRE.invoke(v,operator,"Cannon",null);}catch(ReflectiveOperationException e){throw new IllegalStateException("VATAG native SBW firing failed",e);}}
}
