package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.fml.ModList;

public final class SbwVersionDetector {
    private static final String UNKNOWN_VERSION = "missing";

    private SbwVersionDetector() {
    }

    public static String detectedVersion() {
        return ModList.get()
                .getModContainerById(SbwDroneRangeConfig.SBW_MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse(UNKNOWN_VERSION);
    }

    public static SbwDroneAccess selectAdapter() {
        String version = detectedVersion();
        SbwDroneAccess adapter;
        if (version.startsWith("0.8.9")) {
            adapter = new Sbw089FinalCompat();
        } else if (version.startsWith("0.8.8")) {
            adapter = new Sbw088FinalCompat();
        } else {
            adapter = new Sbw089FinalCompat();
            SbwDroneRangeConfig.LOGGER.warn(
                    "Detected unsupported Superb Warfare version '{}'. Falling back to {}. Some SBW-specific hooks may be disabled.",
                    version,
                    adapter.adapterName()
            );
        }

        SbwDroneRangeConfig.LOGGER.info(
                "Detected Superb Warfare version '{}'; selected compat adapter '{}'",
                version,
                adapter.adapterName()
        );
        logResolvedClasses(adapter);
        return adapter;
    }

    private static void logResolvedClasses(SbwDroneAccess adapter) {
        SbwDroneRangeConfig.LOGGER.info(
                "SBW compat classes: monitor={}, droneItem={}, deployer={}",
                adapter.hasAnyClass(adapter.monitorClassNames()),
                adapter.hasAnyClass(adapter.droneItemClassNames()),
                adapter.hasAnyClass(adapter.deployerItemClassNames())
        );
    }
}
