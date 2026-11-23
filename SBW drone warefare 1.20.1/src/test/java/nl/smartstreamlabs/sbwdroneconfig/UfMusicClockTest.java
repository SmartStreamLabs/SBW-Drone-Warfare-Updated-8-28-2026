package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
final class UfMusicClockTest {
 @Test void repeatedPlayDoesNotRestart(){var c=new UfMusicClock();c.play(0);c.play(5_000_000_000L);assertEquals(8000,c.position(8_000_000_000L,20000,false));}
 @Test void pauseResumeExcludePausedWallTime(){var c=new UfMusicClock();c.play(0);c.pause(3_000_000_000L,10000,false);assertEquals(3000,c.position(90_000_000_000L,10000,false));c.play(100_000_000_000L);assertEquals(6500,c.position(103_500_000_000L,10000,false));}
 @Test void stopResetsAndNextStartGetsFreshOrigin(){var c=new UfMusicClock();c.play(0);c.stop();assertEquals(0,c.position(5_000_000_000L,9000,false));c.play(5_000_000_000L);assertEquals(1000,c.position(6_000_000_000L,9000,false));}
 @Test void repeatWrapsButOneShotFinishes(){var c=new UfMusicClock();c.play(0);assertEquals(500,c.position(24_500_000_000L,24000,true));assertFalse(c.finish(24_500_000_000L,24000,true));assertTrue(c.finish(24_500_000_000L,24000,false));assertEquals(UfMusicClock.STOPPED,c.status());}
 @Test void disablingRepeatDuringLaterCycleFinishesThisCycle(){var c=new UfMusicClock();c.play(0);c.rebase(52_000_000_000L,24000,true);assertEquals(4000,c.position(52_000_000_000L,24000,false));assertFalse(c.finish(71_000_000_000L,24000,false));assertTrue(c.finish(72_000_000_000L,24000,false));}
 @Test void independentFlagsDoNotShareClock(){var a=new UfMusicClock();var b=new UfMusicClock();a.play(0);b.play(2_000_000_000L);a.pause(3_000_000_000L,10000,false);assertEquals(3000,a.position(5_000_000_000L,10000,false));assertEquals(3000,b.position(5_000_000_000L,10000,false));a.stop();assertEquals(4000,b.position(6_000_000_000L,10000,false));}
 @Test void zeroVolumeCannotChangeTransport(){var c=new UfMusicClock();c.play(0);assertEquals(0,UfMusicClock.gain(10,64,0));assertEquals(12000,c.position(12_000_000_000L,24000,false));assertTrue(UfMusicClock.gain(10,64,100)>0);}
 @Test void volumeDoesNotEnlargeRadius(){for(int v:new int[]{0,1,50,100}){assertEquals(0,UfMusicClock.gain(64,64,v));assertEquals(0,UfMusicClock.gain(80,64,v));}assertEquals(.5,UfMusicClock.gain(32,64,100),.00001);}
 @Test void missingDurationAndBackwardClockAreSafe(){var c=new UfMusicClock();c.play(3_000_000_000L);assertEquals(0,c.position(2_000_000_000L,24000,false));assertEquals(0,c.position(4_000_000_000L,0,true));}
}
