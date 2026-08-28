package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SbwVersionCompatSourceTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    @Test
    void compatLayerClassesArePresent() {
        assertTrue(Files.exists(source("SbwCompat.java")), "SbwCompat wrapper must exist");
        assertTrue(Files.exists(source("SbwVersionDetector.java")), "runtime version detector must exist");
        assertTrue(Files.exists(source("SbwDroneAccess.java")), "drone access adapter interface must exist");
        assertTrue(Files.exists(source("Sbw088FinalCompat.java")), "0.8.8 final adapter must exist");
        assertTrue(Files.exists(source("Sbw089FinalCompat.java")), "0.8.9 final adapter must exist");
        assertTrue(Files.exists(source("SbwEntityResolver.java")), "entity resolver must exist");
        assertTrue(Files.exists(source("SbwItemResolver.java")), "item resolver must exist");
    }

    @Test
    void sbwCompatExposesStableWrapperApi() throws IOException {
        String compat = Files.readString(source("SbwCompat.java"));
        assertTrue(compat.contains("SbwVersionDetector.selectAdapter()"), "SbwCompat must select an adapter once");
        assertTrue(compat.contains("isSbwDrone("), "wrapper API must expose isSbwDrone");
        assertTrue(compat.contains("isSupportedDrone("), "wrapper API must expose isSupportedDrone");
        assertTrue(compat.contains("getDroneOwner("), "wrapper API must expose getDroneOwner");
        assertTrue(compat.contains("getDronePosition("), "wrapper API must expose getDronePosition");
        assertTrue(compat.contains("getDroneDisplayName("), "wrapper API must expose getDroneDisplayName");
        assertTrue(compat.contains("resolveSbwItem("), "wrapper API must expose resolveSbwItem");
        assertTrue(compat.contains("resolveSbwEntityType("), "wrapper API must expose resolveSbwEntityType");
    }

    @Test
    void sbw089MovedClassesAreKnownToMixins() throws IOException {
        String plugin = Files.readString(ROOT.resolve("src/main/java/nl/smartstreamlabs/sbwdroneconfig/mixin/SbwDroneConfigMixinPlugin.java"));
        assertTrue(plugin.contains("com.atsuishio.superbwarfare.item.Monitor"), "0.8.8 monitor target must remain supported");
        assertTrue(plugin.contains("com.atsuishio.superbwarfare.item.misc.MonitorItem"), "0.8.9 monitor target must be supported");
        assertTrue(plugin.contains("com.atsuishio.superbwarfare.item.Drone"), "0.8.8 drone item target must remain supported");
        assertTrue(plugin.contains("com.atsuishio.superbwarfare.item.misc.DroneItem"), "0.8.9 drone item target must be supported");
        assertTrue(plugin.contains("com.atsuishio.superbwarfare.item.VehicleDeployerBlockItem"), "0.8.8 deployer target must remain supported");
        assertTrue(plugin.contains("com.atsuishio.superbwarfare.item.misc.AbstractDeployerItem"), "0.8.9 deployer target must be supported");
        assertTrue(plugin.contains("com.atsuishio.superbwarfare.client.overlay.CommonOverlay"), "0.8.9 HUD overlay target must be supported");
        assertTrue(plugin.contains("!classExists(SBW_COMMON_OVERLAY_089_CLASS)"), "0.8.8 HUD mixin must not run against the 0.8.9 RenderContext HUD");

        String mixins = Files.readString(ROOT.resolve("src/main/resources/sbwdroneconfig.mixins.json"));
        assertTrue(mixins.contains("MonitorItem089Mixin"), "0.8.9 monitor mixin must be listed");
        assertTrue(mixins.contains("DroneItem089OwnerMixin"), "0.8.9 drone item owner mixin must be listed");
        assertTrue(mixins.contains("AbstractDeployerItem089OwnerMixin"), "0.8.9 deployer owner mixin must be listed");
        assertTrue(mixins.contains("CommonOverlay089Mixin"), "0.8.9 HUD cancel mixin must be listed");
    }

    @Test
    void addonDronesDoNotImportOldMonitorClassDirectly() throws IOException {
        String cubed = Files.readString(source("CubedFpvDroneEntity.java"));
        String lucas = Files.readString(source("LucasDroneEntity.java"));
        assertFalse(cubed.contains("com.atsuishio.superbwarfare.item.Monitor"), "Cubed FPV must use SbwCompat instead of old Monitor class");
        assertFalse(lucas.contains("com.atsuishio.superbwarfare.item.Monitor"), "LUCAS must use SbwCompat instead of old Monitor class");
        assertTrue(cubed.contains("SbwCompat.unlinkMonitorFromDrone"), "Cubed FPV pickup unlink must use wrapper");
        assertTrue(lucas.contains("SbwCompat.unlinkMonitorFromDrone"), "LUCAS pickup unlink must use wrapper");
    }

    @Test
    void superbWarfareDependencyAllows088And089() throws IOException {
        String build = Files.readString(ROOT.resolve("build.gradle"));
        String properties = Files.readString(ROOT.resolve("gradle.properties"));
        String mods = Files.readString(ROOT.resolve("src/main/resources/META-INF/neoforge.mods.toml"));
        assertTrue(build.contains("sbw_version_range"), "build resources must expose SBW version range");
        assertTrue(properties.contains("sbw_version_range=[0.8.8,0.9.0)"), "range should include 0.8.8 and 0.8.9 final only");
        assertTrue(mods.contains("${sbw_version_range}"), "mods.toml must use expanded SBW range");
    }

    private static Path source(String fileName) {
        return ROOT.resolve("src/main/java/nl/smartstreamlabs/sbwdroneconfig").resolve(fileName);
    }
}
