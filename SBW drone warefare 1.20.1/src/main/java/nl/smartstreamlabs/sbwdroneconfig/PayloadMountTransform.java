package nl.smartstreamlabs.sbwdroneconfig;

final class PayloadMountTransform {
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float rotationPitch;
    private final float rotationRoll;
    private final float rotationYaw;
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final boolean customMount;
    private final boolean useLocalRenderYaw;

    private PayloadMountTransform(
            float offsetX,
            float offsetY,
            float offsetZ,
            float rotationPitch,
            float rotationRoll,
            float rotationYaw,
            float scaleX,
            float scaleY,
            float scaleZ,
            boolean customMount,
            boolean useLocalRenderYaw
    ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.rotationPitch = rotationPitch;
        this.rotationRoll = rotationRoll;
        this.rotationYaw = rotationYaw;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.customMount = customMount;
        this.useLocalRenderYaw = useLocalRenderYaw;
    }

    static PayloadMountTransform custom(float offsetX, float offsetY, float offsetZ, float rotationPitch, float rotationRoll, float rotationYaw, float scale) {
        return new PayloadMountTransform(offsetX, offsetY, offsetZ, rotationPitch, rotationRoll, rotationYaw, scale, scale, scale, true, true);
    }

    static PayloadMountTransform fromDisplayData(float[] offset, float[] rotation, float[] scale) {
        return new PayloadMountTransform(
                offset[0], offset[1], offset[2],
                rotation[0], rotation[1], rotation[2],
                scale[0], scale[1], scale[2],
                false, false
        );
    }

    float offsetX() {
        return offsetX;
    }

    float offsetY() {
        return offsetY;
    }

    float offsetZ() {
        return offsetZ;
    }

    float rotationPitch() {
        return rotationPitch;
    }

    float rotationRoll() {
        return rotationRoll;
    }

    float rotationYaw() {
        return rotationYaw;
    }

    float scaleX() {
        return scaleX;
    }

    float scaleY() {
        return scaleY;
    }

    float scaleZ() {
        return scaleZ;
    }

    boolean customMount() {
        return customMount;
    }

    boolean useLocalRenderYaw() {
        return useLocalRenderYaw;
    }
}
