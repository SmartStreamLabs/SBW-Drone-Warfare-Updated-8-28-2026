package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DistanceWarPlaybackGateTest {
    @Test void pendingOrPlayingVoiceRejectsEveryAdditionalEvent(){var g=new DistanceWarPlaybackGate();assertTrue(g.reserve(100));assertFalse(g.reserve(101));assertFalse(g.reserve(10000));assertTrue(g.busy());}
    @Test void quietMinuteStartsAfterActualTailNotExpectedStart(){var g=new DistanceWarPlaybackGate();g.reserve(10);g.finish(500);assertFalse(g.reserve(1699));assertTrue(g.reserve(1700));}
    @Test void earlyOrCancelledVoiceStillCannotCreateABurst(){var g=new DistanceWarPlaybackGate();g.reserve(10);g.finish(12);assertFalse(g.reserve(13));assertFalse(g.reserve(1211));assertTrue(g.reserve(1212));}
    @Test void repeatedFinishDoesNotRestartTheQuietTimer(){var g=new DistanceWarPlaybackGate();g.reserve(0);g.finish(100);g.finish(500);assertEquals(1300,g.quietUntil());assertTrue(g.reserve(1300));}
    @Test void onlyAWorldResetClearsThePlaybackReservation(){var g=new DistanceWarPlaybackGate();g.reserve(200);g.resetWorld();assertFalse(g.busy());assertTrue(g.reserve(0));}
}
