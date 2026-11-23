package nl.smartstreamlabs.sbwdroneconfig;

/**
 * Compatibility entry point retained for existing config lifecycle callers.
 * Per-player drone tracking is now applied precisely by ChunkMapTrackedEntityMixin.
 */
public final class DroneTrackingRangeOverride {
    private DroneTrackingRangeOverride() {
    }

    public static void applyConfiguredRange() {
        // Do not mutate EntityType internals. The tracking mixin selects a bounded range per observer.
    }
}
