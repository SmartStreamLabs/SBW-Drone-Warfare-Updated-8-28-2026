package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class ShaderUniformLocationsTest {
 @Test void cachesAnExistingAndMissingUniform(){var c=new ShaderUniformLocations();assertNull(c.lookup(3,"Sampler0"));c.remember(3,"Sampler0",7);c.remember(3,"Missing",-1);assertEquals(7,c.lookup(3,"Sampler0"));assertEquals(-1,c.lookup(3,"Missing"));}
 @Test void programSwitchInvalidatesEvenWhenSwitchingBack(){var c=new ShaderUniformLocations();c.remember(3,"x",4);assertNull(c.lookup(5,"x"));c.remember(5,"x",8);assertNull(c.lookup(3,"x"));}
 @Test void relinkingSameProgramInvalidatesLocations(){var c=new ShaderUniformLocations();c.remember(3,"x",4);c.clear();assertNull(c.lookup(3,"x"));c.remember(3,"x",9);assertEquals(9,c.lookup(3,"x"));}
 @Test void instancesDoNotShareLocations(){var a=new ShaderUniformLocations();var b=new ShaderUniformLocations();a.remember(3,"x",1);assertNull(b.lookup(3,"x"));}
}
