package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class LucasDroneRendererTest {
    @Test
    void exposesTheConfiguredRenderDefaults() throws Exception {
        Class<?> renderMathClass;
        try {
            renderMathClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.LucasDroneRenderMath");
        } catch (ClassNotFoundException exception) {
            fail("LucasDroneRenderMath should exist so the LUCAS drone render defaults can be tested without client-only renderer dependencies.");
            return;
        }

        Method scaleMethod = renderMathClass.getDeclaredMethod("droneVisualScale");
        Method translationMethod = renderMathClass.getDeclaredMethod("bodyTranslationY");
        Method yawOffsetMethod = renderMathClass.getDeclaredMethod("baseYawOffsetDegrees");
        scaleMethod.setAccessible(true);
        translationMethod.setAccessible(true);
        yawOffsetMethod.setAccessible(true);

        assertEquals(60.0F, ((Number) scaleMethod.invoke(null)).floatValue(), 0.0F);
        assertEquals(0.002F, ((Number) translationMethod.invoke(null)).floatValue(), 0.0F);
        assertEquals(180.0F, ((Number) yawOffsetMethod.invoke(null)).floatValue(), 0.0F);
    }

    @Test
    void appliesTheLucasFixedWingOrientationRules() throws Exception {
        Class<?> renderMathClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.LucasDroneRenderMath");

        Method yawMethod = renderMathClass.getDeclaredMethod("renderYawDegrees", float.class);
        Method pitchMethod = renderMathClass.getDeclaredMethod("renderBodyPitchDegrees", float.class);
        Method rollMethod = renderMathClass.getDeclaredMethod("renderRollDegrees", float.class);
        Method visibilityMethod = renderMathClass.getDeclaredMethod("shouldHideControlledDroneBody", boolean.class, boolean.class);
        yawMethod.setAccessible(true);
        pitchMethod.setAccessible(true);
        rollMethod.setAccessible(true);
        visibilityMethod.setAccessible(true);

        assertEquals(150.0F, ((Number) yawMethod.invoke(null, 30.0F)).floatValue(), 0.0F,
                "The LUCAS drone body should render with a 180 degree yaw offset.");
        assertEquals(0.0F, ((Number) pitchMethod.invoke(null, 12.5F)).floatValue(), 0.0F,
                "The LUCAS fixed-wing body should stay level instead of inheriting quadcopter-style pitch while flying.");
        assertEquals(8.0F, ((Number) rollMethod.invoke(null, -8.0F)).floatValue(), 0.0F,
                "The LUCAS drone roll should follow the flipped FPV body convention.");
        assertEquals(true, visibilityMethod.invoke(null, true, true));
        assertEquals(false, visibilityMethod.invoke(null, true, false));
        assertEquals(false, visibilityMethod.invoke(null, false, true));
    }

    @Test
    void keepsAttachmentRenderingInTheStandardDroneFrame() throws Exception {
        Class<?> renderMathClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.LucasDroneRenderMath");

        Method attachmentScaleMethod = renderMathClass.getDeclaredMethod("attachmentScaleCompensation");
        Method attachmentOffsetMethod = renderMathClass.getDeclaredMethod("attachmentBaseYOffset");
        Method attachmentYawMethod = renderMathClass.getDeclaredMethod("renderAttachmentYawDegrees", float.class);
        Method attachmentPitchMethod = renderMathClass.getDeclaredMethod("renderAttachmentPitchDegrees", float.class);
        Method attachmentRollMethod = renderMathClass.getDeclaredMethod("renderAttachmentRollDegrees", float.class);
        attachmentScaleMethod.setAccessible(true);
        attachmentOffsetMethod.setAccessible(true);
        attachmentYawMethod.setAccessible(true);
        attachmentPitchMethod.setAccessible(true);
        attachmentRollMethod.setAccessible(true);

        assertEquals(1.0F, ((Number) attachmentScaleMethod.invoke(null)).floatValue(), 0.0F);
        assertEquals(0.12F, ((Number) attachmentOffsetMethod.invoke(null)).floatValue(), 0.0001F);
        assertEquals(-30.0F, ((Number) attachmentYawMethod.invoke(null, 30.0F)).floatValue(), 0.0F);
        assertEquals(0.0F, ((Number) attachmentPitchMethod.invoke(null, 12.5F)).floatValue(), 0.0F,
                "The LUCAS attachments should stay aligned with the level fixed-wing fuselage.");
        assertEquals(-8.0F, ((Number) attachmentRollMethod.invoke(null, -8.0F)).floatValue(), 0.0F);
    }
}
