package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleAntiDroneNetUpgradeSourceTest {
    @Test
    void vehicleAntiDroneNetUpgradeItemAndResourcesAreRegistered() throws IOException {
        String items = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonItems.java");
        String tabs = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonCreativeTabs.java");
        String lang = read("src/main/resources/assets/sbwdroneconfig/lang/en_us.json");

        assertTrue(items.contains("VEHICLE_ANTI_DRONE_NET_UPGRADE"));
        assertTrue(items.contains("\"vehicle_anti_drone_net_upgrade\""));
        assertTrue(items.contains("stacksTo(1)"));
        assertTrue(tabs.contains("VEHICLE_ANTI_DRONE_NET_UPGRADE"));
        assertTrue(lang.contains("item.sbwdroneconfig.vehicle_anti_drone_net_upgrade"));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/sbwdroneconfig/models/item/vehicle_anti_drone_net_upgrade.json")));
        assertTrue(Files.exists(Path.of("src/main/resources/data/sbwdroneconfig/recipes/vehicle_anti_drone_net_upgrade.json")));
    }

    @Test
    void activeUpgradeIsDetectedThroughCentralVehicleCompatOnly() throws IOException {
        String compat = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/SbwCompat.java");
        String method = methodBody(compat, "public static boolean hasVehicleAntiDroneNetUpgrade");

        assertTrue(method.contains("isVehicleNetHost(vehicle)"));
        assertTrue(method.contains("for (int slot = 0; slot < size; slot++)"));
        assertTrue(method.contains("getVehicleItem(vehicle, slot)"));
        assertFalse(method.contains("getInventory()"));
        assertFalse(method.contains("Player"));
    }

    @Test
    void blockAndVehicleNetsShareTheSameJamEffectSystem() throws IOException {
        String block = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AntiDroneNetBlock.java");
        String collision = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/VehicleAntiDroneNetCollisionSystem.java");

        assertTrue(block.contains("AntiDroneNetEffectSystem.jamDrone"));
        assertTrue(block.contains("VehicleAntiDroneNetCollisionSystem.tryJamVehicleNet"));
        assertTrue(collision.contains("SbwCompat::hasVehicleAntiDroneNetUpgrade"));
        assertTrue(collision.contains("Vc01Sweep.hit"));
        assertTrue(read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/Vc01Intercept.java").contains("AntiDroneNetEffectSystem.jamDrone"));
    }

    @Test
    void clientPreviewIsRenderOnlyAndUsesTheSharedGeometry() throws IOException {
        String client = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/VehicleAntiDroneNetClient.java");

        assertTrue(client.contains("RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES"));
        assertTrue(client.contains("ACTIVE_VEHICLE_NETS"));
        assertTrue(client.contains("handleVehicleNetState"));
        assertTrue(client.contains("RenderType.entityCutoutNoCull(NET)"));
        assertTrue(client.contains("Vc01Geometry.matrix"));
        assertTrue(client.contains("textures/vc01/vc01_net_welded.png"));
        assertTrue(client.contains("Vc01Geometry.profile"));
        assertTrue(client.contains("clientLevel.entitiesForRendering()"));
        assertFalse(client.contains("RenderType.lines()"));
        assertFalse(client.contains("renderLineBox"));
        assertFalse(client.contains("FULL_BRIGHT"));
        assertFalse(client.contains("addFreshEntity"));
        assertFalse(client.contains("setBlock"));
        assertFalse(client.contains("ArmorStand"));
    }

    @Test
    void vehicleNetRenderStateIsSyncedFromTheServer() throws IOException {
        String network = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/AddonNetwork.java");
        String mod = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/SbwDroneRangeConfig.java");
        String sync = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/VehicleAntiDroneNetSyncSystem.java");
        String message = read("src/main/java/nl/smartstreamlabs/sbwdroneconfig/VehicleAntiDroneNetStateMessage.java");

        assertTrue(network.contains("VehicleAntiDroneNetStateMessage"));
        assertTrue(network.contains("syncVehicleAntiDroneNetState"));
        assertTrue(mod.contains("VehicleAntiDroneNetSyncSystem.class"));
        assertTrue(sync.contains("TickEvent.LevelTickEvent"));
        assertTrue(sync.contains("SbwCompat.hasVehicleAntiDroneNetUpgrade"));
        assertTrue(sync.contains("AddonNetwork.sendVehicleAntiDroneNetState"));
        assertTrue(sync.contains("player.distanceToSqr(vehicle)"));
        assertTrue(message.contains("PLAY_TO_CLIENT") || network.contains("NetworkDirection.PLAY_TO_CLIENT"));
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }

    private static String methodBody(String source, String signature) {
        int start = source.indexOf(signature);
        if (start < 0) {
            return "";
        }
        int brace = source.indexOf('{', start);
        int depth = 0;
        for (int index = brace; index < source.length(); index++) {
            char c = source.charAt(index);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(start, index + 1);
                }
            }
        }
        return source.substring(start);
    }
}
