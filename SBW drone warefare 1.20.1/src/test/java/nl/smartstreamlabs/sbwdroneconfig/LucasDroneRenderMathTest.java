package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LucasDroneRenderMathTest {
    @Test
    void lucasDroneBodyPitchUsesTheImportedAircraftPitchDirection() {
        assertEquals(0.0F, LucasDroneRenderMath.renderBodyPitchDegrees(12.5F), 0.0001F,
                "The LUCAS fixed-wing body should stay visually level instead of inheriting quadcopter-style forward pitch.");
        assertEquals(0.0F, LucasDroneRenderMath.renderBodyPitchDegrees(-8.0F), 0.0001F,
                "The LUCAS fixed-wing body should also ignore reverse pitch so the fuselage does not lean backward.");
    }
}
