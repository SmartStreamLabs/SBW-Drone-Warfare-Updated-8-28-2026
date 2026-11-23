package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneConfigUiMathTest {
    @Test
    void normalizedIntMappingClampsToBounds() {
        assertEquals(0.0D, DroneConfigUiMath.normalizedFromInt(-50, 0, 100));
        assertEquals(1.0D, DroneConfigUiMath.normalizedFromInt(250, 0, 100));
        assertEquals(0.5D, DroneConfigUiMath.normalizedFromInt(50, 0, 100));
    }

    @Test
    void intMappingRoundsBackIntoConfiguredRange() {
        assertEquals(8, DroneConfigUiMath.intFromNormalized(-0.25D, 8, 64));
        assertEquals(64, DroneConfigUiMath.intFromNormalized(1.25D, 8, 64));
        assertEquals(36, DroneConfigUiMath.intFromNormalized(0.5D, 8, 64));
    }

    @Test
    void doubleMappingClampsAndRoundsToTwoDecimals() {
        assertEquals(0.0D, DroneConfigUiMath.doubleFromNormalized(-0.5D, 0.0D, 2.0D));
        assertEquals(2.0D, DroneConfigUiMath.doubleFromNormalized(1.5D, 0.0D, 2.0D));
        assertEquals(1.5D, DroneConfigUiMath.doubleFromNormalized(0.75D, 0.0D, 2.0D));
    }

    @Test
    void audioPercentLabelUsesWholePercentFormatting() {
        assertEquals("0%", DroneConfigUiMath.toPercentLabel(0.0D));
        assertEquals("100%", DroneConfigUiMath.toPercentLabel(1.0D));
        assertEquals("135%", DroneConfigUiMath.toPercentLabel(1.35D));
    }

    @Test
    void audioConfigLimitsAllowFiveHundredPercentForEngineAndAirborneBoost() {
        assertEquals(5.0D, DroneAudioConfigLimits.MAX_ENGINE_VOLUME_MULTIPLIER);
        assertEquals(5.0D, DroneAudioConfigLimits.MAX_AIRBORNE_VOLUME_MULTIPLIER);
        assertEquals("500%", DroneConfigUiMath.toPercentLabel(DroneAudioConfigLimits.MAX_ENGINE_VOLUME_MULTIPLIER));
        assertEquals("500%", DroneConfigUiMath.toPercentLabel(DroneAudioConfigLimits.MAX_AIRBORNE_VOLUME_MULTIPLIER));
    }

    @Test
    void configHeaderLayoutSeparatesSubtitleDescriptionAndControls() {
        assertEquals(30, DroneConfigScreen.subtitleTop(0));
        assertEquals(68, DroneConfigScreen.categoryDescriptionTop(0));
        assertEquals(92, DroneConfigScreen.contentAreaTop(0));

        assertTrue(
                DroneConfigScreen.categoryDescriptionTop(0) >= DroneConfigScreen.subtitleTop(0) + 20,
                "The category description should start well below the main subtitle so the two text blocks never overlap."
        );
        assertTrue(
                DroneConfigScreen.contentAreaTop(0) >= DroneConfigScreen.categoryDescriptionTop(0) + 18,
                "The first control row should start below the category description block."
        );
    }
}
