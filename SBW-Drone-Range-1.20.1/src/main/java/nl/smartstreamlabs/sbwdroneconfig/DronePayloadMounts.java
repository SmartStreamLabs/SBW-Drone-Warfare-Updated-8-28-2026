package nl.smartstreamlabs.sbwdroneconfig;

final class DronePayloadMounts {
    private DronePayloadMounts() {
    }

    static PayloadMountTransform getPayloadMountTransform(DroneKind droneKind, String payloadType, float[] displayOffset, float[] displayRotation, float[] displayScale) {
        return PayloadMountTransform.fromDisplayData(displayOffset, displayRotation, displayScale);
    }

    enum DroneKind {
        FPV,
        LUCAS
    }
}
