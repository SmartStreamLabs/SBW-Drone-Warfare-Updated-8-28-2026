package nl.smartstreamlabs.sbwdroneconfig;

final class LucasDroneRenderMath {
    // The imported LUCAS GLB uses a much smaller poly-mesh unit scale than the Cubed FPV drone,
    // so we compensate here to keep the aircraft readable at a normal in-game size.
    private static final float DRONE_VISUAL_SCALE = 60.0F;
    private static final float CONTROLLED_CAMERA_DRONE_VISUAL_SCALE = 18.0F;
    private static final float BODY_TRANSLATION_Y = 0.002F;

    private LucasDroneRenderMath() {
    }

    static float droneVisualScale() {
        return DRONE_VISUAL_SCALE;
    }

    static float droneVisualScale(boolean controllingLinkedDrone, boolean firstPersonCamera) {
        return controllingLinkedDrone && !firstPersonCamera ? CONTROLLED_CAMERA_DRONE_VISUAL_SCALE : DRONE_VISUAL_SCALE;
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
        return 0.0F;
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
        return 0.0F;
    }

    static float renderAttachmentRollDegrees(float entityRollDegrees) {
        return entityRollDegrees;
    }

    static float attachmentBaseYOffset() {
        return droneVisualScale() * bodyTranslationY();
    }

    static float nameplateOffsetY() {
        return 0.35F;
    }
}
