package nl.smartstreamlabs.sbwdroneconfig;

final class CubedFpvDroneRenderMath {
    private static final float DRONE_VISUAL_SCALE = 1.9F;
    private static final float BODY_TRANSLATION_Y = 0.05F;

    private CubedFpvDroneRenderMath() {
    }

    static float droneVisualScale() {
        return DRONE_VISUAL_SCALE;
    }

    static float bodyTranslationY() {
        return BODY_TRANSLATION_Y;
    }

    static float baseYawOffsetDegrees() {
        return 180.0F;
    }

    static float renderYawDegrees(float entityYawDegrees) {
        return baseYawOffsetDegrees() - entityYawDegrees;
    }

    static float renderBodyPitchDegrees(float entityBodyPitchDegrees) {
        return -entityBodyPitchDegrees;
    }

    static float renderRollDegrees(float entityRollDegrees) {
        return -entityRollDegrees;
    }

    static boolean shouldHideControlledDroneBody(boolean controllingLinkedDrone, boolean firstPersonCamera) {
        return controllingLinkedDrone && firstPersonCamera;
    }

    static float attachmentScaleCompensation() {
        return 1.0F;
    }

    static float compensateAttachmentCoordinate(float coordinate) {
        return coordinate;
    }

    static float renderAttachmentYawDegrees(float entityYawDegrees) {
        return -entityYawDegrees;
    }

    static float renderAttachmentPitchDegrees(float entityBodyPitchDegrees) {
        return entityBodyPitchDegrees;
    }

    static float renderAttachmentRollDegrees(float entityRollDegrees) {
        return entityRollDegrees;
    }

    static float attachmentBaseYOffset() {
        return droneVisualScale() * bodyTranslationY();
    }
}
