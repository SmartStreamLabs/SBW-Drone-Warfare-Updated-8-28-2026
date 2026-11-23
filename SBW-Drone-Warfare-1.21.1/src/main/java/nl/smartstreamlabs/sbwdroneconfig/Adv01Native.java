package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.*;

/** Same verified SBW Final/.1 descriptor split as VatagNative, without changing that public API. */
final class Adv01Native {
    private static final Method FIRE=resolve();
    private static Method resolve() {
        try{return VehicleEntity.class.getMethod("vehicleShoot",LivingEntity.class,String.class);}
        catch(NoSuchMethodException older) {
            try{return VehicleEntity.class.getMethod("vehicleShoot",LivingEntity.class,String.class,Vec3.class);}
            catch(NoSuchMethodException absent){throw new IllegalStateException("ADV-01 requires the supported native SBW vehicle weapon API",absent);}
        }
    }
    static void fire(Adv01Entity vehicle,LivingEntity actor) {
        try {if(FIRE.getParameterCount()==2)FIRE.invoke(vehicle,actor,"Cannon");else FIRE.invoke(vehicle,actor,"Cannon",null);}
        catch(ReflectiveOperationException failure){throw new IllegalStateException("ADV-01 native gun invocation failed",failure);}
    }
}
