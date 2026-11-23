package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.data.gun.GunData;import java.lang.reflect.*;
/** SBW .1 copies custom components only after its state version changes. Final has no version object. */
public final class Dil01GunDataCompat {
 private static final Field VERSION;private static final Method INVALIDATE;
 static{Field field=null;Method method=null;try{field=GunData.class.getField("nbtVersion");method=field.getType().getMethod("invalidateState");}catch(NoSuchFieldException olderFinal){field=null;}catch(NoSuchMethodException changedApi){throw new ExceptionInInitializerError(changedApi);}VERSION=field;INVALIDATE=method;}
 public static void changed(GunData data){if(VERSION!=null)try{INVALIDATE.invoke(VERSION.get(data));}catch(ReflectiveOperationException failure){throw new IllegalStateException("DIL-01 cannot synchronize SBW gun state",failure);}data.save();}
 private Dil01GunDataCompat(){}
}
