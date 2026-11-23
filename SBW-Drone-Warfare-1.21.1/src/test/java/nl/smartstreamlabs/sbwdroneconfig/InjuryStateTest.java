package nl.smartstreamlabs.sbwdroneconfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InjuryStateTest {
    private void hit(InjuryState s,float n){s.hit(n,2,10,14,200,false);}
    @Test void splitImpactDoesNotDuplicateAccumulatedTrauma(){var s=new InjuryState();s.hit(6,6,2,10,14,200,false);s.hit(6,12,2,10,14,200,false);assertEquals(12,s.trauma);assertTrue(s.downed());}
    @Test void minorHitsDoNotAutomaticallyDown(){var s=new InjuryState();hit(s,2);assertEquals(InjuryState.Stage.LIGHT,s.stage);assertEquals(1,s.bleeding);assertFalse(s.downed());}
    @Test void sustainedHitsEscalate(){var s=new InjuryState();hit(s,5);hit(s,5);assertFalse(s.downed());hit(s,4);assertTrue(s.downed());assertEquals(2,s.bleeding);}
    @Test void protectionBelowThresholdCannotCreateAnImmediateSevereWound(){var s=new InjuryState();hit(s,.2f);assertEquals(InjuryState.Stage.HEALTHY,s.stage);assertEquals(0,s.bleeding);}
    @Test void accumulationExpires(){var s=new InjuryState();hit(s,7);for(int i=0;i<200;i++)s.tick();hit(s,7);assertFalse(s.downed());}
    @Test void bandageStopsBloodButNeverStandsASeverePatientUp(){var s=new InjuryState();hit(s,11);assertTrue(s.bandage(100));assertEquals(0,s.bleeding);assertEquals(InjuryState.Stage.STABILIZED,s.stage);for(int i=0;i<1000;i++)s.tick();assertTrue(s.downed());}
    @Test void furtherTraumaReopensAStabilizedWound(){var s=new InjuryState();hit(s,12);s.bandage(100);hit(s,2);assertEquals(InjuryState.Stage.SEVERE,s.stage);assertEquals(2,s.bleeding);}
    @Test void medkitAllowsRecoveryWithoutRearmingFatalProtection(){var s=new InjuryState();s.hit(20,2,10,14,200,true);assertTrue(s.rescueUsed);assertTrue(s.medkit(30));assertFalse(s.downed());for(int i=0;i<30;i++)s.tick();assertEquals(InjuryState.Stage.HEALTHY,s.stage);assertTrue(s.rescueUsed);}
    @Test void healthRegenerationHasNoMedicalTransition(){var s=new InjuryState();hit(s,12);for(int i=0;i<6000;i++)s.tick();assertEquals(InjuryState.Stage.SEVERE,s.stage);assertEquals(2,s.bleeding);}
    @Test void malformedStoredNumbersAreBounded(){var s=new InjuryState();s.bleeding=99;s.trauma=Float.NaN;s.recovery=-2;s.normalize();assertEquals(2,s.bleeding);assertEquals(0,s.trauma);assertEquals(0,s.recovery);}
    @Test void stabilizedPatientDoesNotConsumeRedundantBandages(){var s=new InjuryState();hit(s,12);assertTrue(s.bandage(20));assertFalse(s.bandage(20));}
}
