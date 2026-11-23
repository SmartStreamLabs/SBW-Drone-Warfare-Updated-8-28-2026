package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class VehicleProfileValuesTest {
 @Test void zeroInheritsAndExplicitValueReplacesGeneral(){assertEquals(3,VehicleProfileValues.resolve(0,3));assertEquals(2,VehicleProfileValues.resolve(2,3));}
 @Test void identityPreservesNativeValuesWithoutRounding(){assertEquals(73.125f,VehicleProfileValues.scaled(73.125f,1));}
 @Test void finiteEngineLimitDoesNotOverflowToInfinity(){assertEquals(Float.MAX_VALUE,VehicleProfileValues.scaled(100,Double.MAX_VALUE));assertTrue(Float.isFinite(VehicleProfileValues.scaled(Float.MAX_VALUE,Double.MAX_VALUE)));}
 @Test void scalingDoesNotCreateHealthOrDamageFromZero(){assertEquals(0,VehicleProfileValues.scaled(0,Double.MAX_VALUE));assertEquals(25,VehicleProfileValues.scaled(100,.25));}
}
