package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class ExplosionAudioDistanceBoostTest {
 @Test void preservesCloseRecordingAndMakesFarFieldMoreThanTwiceAsLoud(){assertEquals(1,ExplosionAudioMath.distanceBoost(0));assertEquals(1,ExplosionAudioMath.distanceBoost(45));assertEquals(4,ExplosionAudioMath.distanceBoost(160));assertTrue(ExplosionAudioMath.distanceBoost(500)/1.8>2);}
 @Test void transitionIsContinuousBoundedAndDoesNotBypassRange(){double last=1;for(int d=0;d<=1100;d++){double b=ExplosionAudioMath.distanceBoost(d);assertTrue(b>=last&&b<=4);last=b;}assertEquals(0,ExplosionAudioMath.gain(1024,1024)*ExplosionAudioMath.distanceBoost(1024));assertEquals(ExplosionAudioMath.distanceBoost(80-.001),ExplosionAudioMath.distanceBoost(80+.001),.001);}
}
