package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineInfo;
import com.google.gson.Gson;
/** One detached engine copy per changed profile/engine instance; native/shared engine data is never mutated. */
public final class VehicleDriveTuning {
 private static final Gson GSON=new Gson();
 private static float scale(float value,double multiplier){return VehicleProfileValues.scaled(value,multiplier);}
 public static EngineInfo copy(EngineInfo original,VehicleDriveConfig.Factors factors){
  if(original==null||factors.equals(VehicleDriveConfig.IDENTITY))return original;
  if(!(original instanceof EngineInfo.Wheel)&&!(original instanceof EngineInfo.Ship))return original;
  EngineInfo engine=GSON.fromJson(GSON.toJson(original),original.getClass());
  engine.setIncrement(scale(original.getIncrement(),factors.acceleration()));engine.setDecrement(scale(original.getDecrement(),factors.braking()));
  engine.setEnergyCostRate(Math.min(Double.MAX_VALUE,original.getEnergyCostRate()*factors.energy()));
  if(engine instanceof EngineInfo.Wheel wheel&&original instanceof EngineInfo.Wheel source){wheel.setMaxForwardSpeedRate(scale(source.getMaxForwardSpeedRate(),factors.forward()));wheel.setMaxBackwardSpeedRate(scale(source.getMaxBackwardSpeedRate(),factors.reverse()));wheel.setSteeringSpeed(scale(source.getSteeringSpeed(),factors.turning()));}
  if(engine instanceof EngineInfo.Ship ship&&original instanceof EngineInfo.Ship source){ship.setMaxForwardSpeedRate(scale(source.getMaxForwardSpeedRate(),factors.forward()));ship.setMaxBackwardSpeedRate(scale(source.getMaxBackwardSpeedRate(),factors.reverse()));ship.setSteeringSpeed(scale(source.getSteeringSpeed(),factors.turning()));}
  return engine;
 }
 private VehicleDriveTuning(){}
}
