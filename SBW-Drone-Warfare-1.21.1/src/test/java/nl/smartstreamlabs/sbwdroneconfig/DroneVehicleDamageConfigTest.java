package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DroneVehicleDamageConfigTest {
 @Test void validRegistryOverrides(){assertTrue(DroneAttackDamage.validOverride("sbwdroneconfig:lucas_drone=2.5"));assertTrue(DroneAttackDamage.validOverride("superbwarfare:rpg_rocket_tbg=0"));}
 @Test void invalidFactorsCannotEnterConfig(){for(String s:new String[]{"x:foo=-1","x:foo=101","x:foo=NaN","x:foo=Infinity","no equals","INVALID ID=2"})assertFalse(DroneAttackDamage.validOverride(s),s);}
}
