package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class CubedFpvDroneRendererTest {
    @Test
    void exposesHalfTurnModelYawOffset() throws Exception {
        Class<?> rendererClass;
        try {
            rendererClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneRenderMath");
        } catch (ClassNotFoundException exception) {
            fail("CubedFpvDroneRenderMath should exist so the cubed drone yaw offset can be tested without client-only renderer dependencies.");
            return;
        }

        Method offsetMethod;
        try {
            offsetMethod = rendererClass.getDeclaredMethod("baseYawOffsetDegrees");
        } catch (NoSuchMethodException exception) {
            fail("CubedFpvDroneRenderMath should expose a dedicated base yaw offset so the cubed drone can face the correct direction.");
            return;
        }

        offsetMethod.setAccessible(true);
        Object result = offsetMethod.invoke(null);
        assertEquals(180.0F, ((Number) result).floatValue(), 0.0F,
                "The cubed drone model should render with a 180 degree yaw offset.");
    }

    @Test
    void invertsPitchAndRollForTheFlippedCubedModel() throws Exception {
        Class<?> rendererClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneRenderMath");

        Method pitchMethod = rendererClass.getDeclaredMethod("renderBodyPitchDegrees", float.class);
        Method rollMethod = rendererClass.getDeclaredMethod("renderRollDegrees", float.class);
        pitchMethod.setAccessible(true);
        rollMethod.setAccessible(true);

        assertEquals(-12.5F, ((Number) pitchMethod.invoke(null, 12.5F)).floatValue(), 0.0F,
                "The cubed drone pitch should be inverted so forward/backward flight does not tilt the wrong way.");
        assertEquals(6.75F, ((Number) pitchMethod.invoke(null, -6.75F)).floatValue(), 0.0F,
                "The cubed drone pitch inversion should also work for reverse tilt.");
        assertEquals(-8.0F, ((Number) rollMethod.invoke(null, 8.0F)).floatValue(), 0.0F,
                "The cubed drone roll should be inverted so banking stays aligned after the 180 degree model flip.");
        assertEquals(3.5F, ((Number) rollMethod.invoke(null, -3.5F)).floatValue(), 0.0F,
                "The cubed drone roll inversion should also work for the opposite bank direction.");
    }

    @Test
    void hidesTheControlledDroneBodyOnlyInPureFirstPersonFpv() throws Exception {
        Class<?> rendererClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneRenderMath");

        Method visibilityMethod = rendererClass.getDeclaredMethod("shouldHideControlledDroneBody", boolean.class, boolean.class);
        visibilityMethod.setAccessible(true);

        assertEquals(true, visibilityMethod.invoke(null, true, true));
        assertEquals(false, visibilityMethod.invoke(null, true, false));
        assertEquals(false, visibilityMethod.invoke(null, false, true));
    }

    @Test
    void cancelsTheDroneVisualScaleBeforeRenderingAttachedEntities() throws Exception {
        Class<?> rendererClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneRenderMath");

        Method droneScaleMethod = rendererClass.getDeclaredMethod("droneVisualScale");
        Method attachmentCompensationMethod = rendererClass.getDeclaredMethod("attachmentScaleCompensation");
        droneScaleMethod.setAccessible(true);
        attachmentCompensationMethod.setAccessible(true);

        float droneScale = ((Number) droneScaleMethod.invoke(null)).floatValue();
        float attachmentCompensation = ((Number) attachmentCompensationMethod.invoke(null)).floatValue();

        assertEquals(1.9F, droneScale, 0.0F,
                "The cubed drone should keep its 1.9x visual scale.");
        assertEquals(1.0F, attachmentCompensation, 0.0F,
                "Attachments should render in the standard drone frame instead of inheriting the cubed body's visual scale.");
    }

    @Test
    void compensatesAttachmentPlacementForTheDroneVisualScale() throws Exception {
        Class<?> rendererClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneRenderMath");

        Method placementMethod = rendererClass.getDeclaredMethod("compensateAttachmentCoordinate", float.class);
        placementMethod.setAccessible(true);

        assertEquals(1.0F, ((Number) placementMethod.invoke(null, 1.0F)).floatValue(), 0.0F,
                "Attachment offsets should stay in the standard drone coordinate frame.");
        assertEquals(-1.0F, ((Number) placementMethod.invoke(null, -1.0F)).floatValue(), 0.0F,
                "Negative attachment offsets should also stay in the standard drone coordinate frame.");
    }

    @Test
    void keepsAttachmentsInTheStandardDroneOrientationFrame() throws Exception {
        Class<?> rendererClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneRenderMath");

        Method yawMethod = rendererClass.getDeclaredMethod("renderAttachmentYawDegrees", float.class);
        Method pitchMethod = rendererClass.getDeclaredMethod("renderAttachmentPitchDegrees", float.class);
        Method rollMethod = rendererClass.getDeclaredMethod("renderAttachmentRollDegrees", float.class);
        yawMethod.setAccessible(true);
        pitchMethod.setAccessible(true);
        rollMethod.setAccessible(true);

        assertEquals(-30.0F, ((Number) yawMethod.invoke(null, 30.0F)).floatValue(), 0.0F,
                "Attachments should follow the normal drone yaw instead of inheriting the cubed model's 180 degree flip.");
        assertEquals(12.5F, ((Number) pitchMethod.invoke(null, 12.5F)).floatValue(), 0.0F,
                "Attachments should keep the standard pitch direction.");
        assertEquals(-8.0F, ((Number) rollMethod.invoke(null, -8.0F)).floatValue(), 0.0F,
                "Attachments should keep the standard roll direction.");
    }

    @Test
    void appliesTheCubedDroneBodyLiftToAttachmentRendering() throws Exception {
        Class<?> rendererClass = Class.forName("nl.smartstreamlabs.sbwdroneconfig.CubedFpvDroneRenderMath");

        Method bodyLiftMethod = rendererClass.getDeclaredMethod("attachmentBaseYOffset");
        bodyLiftMethod.setAccessible(true);

        assertEquals(0.095F, ((Number) bodyLiftMethod.invoke(null)).floatValue(), 0.0001F,
                "Attachments should keep the cubed drone's upward body alignment so they do not float below the drone.");
    }
}
