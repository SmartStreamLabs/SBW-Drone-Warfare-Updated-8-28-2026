package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Vc01ProtectionTest {
 @Test void fullHealthPassesFortyPercent(){assertEquals(.4,Vc01Panels.factor(.6,1),1e-12);}
 @Test void halfHealthPassesSeventyPercent(){assertEquals(.7,Vc01Panels.factor(.6,.5),1e-12);}
 @Test void brokenPanelCannotProtect(){assertEquals(1,Vc01Panels.factor(.6,0),1e-12);}
 @Test void settingsAreBounded(){assertEquals(0,Vc01Panels.factor(7,4),1e-12);assertEquals(1,Vc01Panels.factor(-1,1));}
}
