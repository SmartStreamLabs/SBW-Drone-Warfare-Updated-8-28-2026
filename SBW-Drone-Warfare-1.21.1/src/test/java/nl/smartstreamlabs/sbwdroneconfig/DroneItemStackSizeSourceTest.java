package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneItemStackSizeSourceTest {
    @Test
    void fpvAndLucasDroneItemsStackToSixtyFour() throws Exception {
        Path itemsSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonItems.java");
        String source = Files.readString(itemsSource, StandardCharsets.UTF_8);

        assertTrue(
                Pattern.compile("CUBED_FPV_DRONE[\\s\\S]*?CubedFpvDroneItem\\(new Item\\.Properties\\(\\)\\.stacksTo\\(64\\)")
                        .matcher(source)
                        .find(),
                "The FPV Drone item should stack to 64 so players can carry multiple deployable drones in one slot."
        );
        assertTrue(
                Pattern.compile("LUCAS_DRONE[\\s\\S]*?LucasDroneItem\\(new Item\\.Properties\\(\\)\\.stacksTo\\(64\\)")
                        .matcher(source)
                        .find(),
                "The LUCAS Drone item should stack to 64 so players can carry multiple deployable drones in one slot."
        );
    }

    @Test
    void monitorReadChecksDoNotCreateTagsOnUnlinkedStacks() throws Exception {
        Path compatSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "SbwCompat.java");
        String source = Files.readString(compatSource, StandardCharsets.UTF_8);

        assertFalse(methodSection(source, "public static boolean isLinkedMonitor").contains("getOrCreateTag()"));
        assertFalse(methodSection(source, "public static boolean isLegacyLinkedMonitor").contains("getOrCreateTag()"));
        assertFalse(methodSection(source, "public static boolean isUsingLinkedMonitor").contains("getOrCreateTag()"));
        assertFalse(methodSection(source, "public static UUID getLinkedDroneUuid").contains("getOrCreateTag()"));
        assertTrue(
                source.contains("stack.setTag(null);"),
                "Unlinked monitor stacks with only stale monitor state should have their empty tags removed so they can stack again."
        );
    }

    @Test
    void monitorInventoryTickCleansInactiveStackTags() throws Exception {
        Path mixinSource = Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "mixin", "MonitorItemMixin.java");
        String source = Files.readString(mixinSource, StandardCharsets.UTF_8);

        assertTrue(
                source.contains("SbwCompat.normalizeInactiveMonitorStack(itemstack);"),
                "The SBW monitor inventory tick should remove stale inactive NBT after vanilla/SBW writes Using=false."
        );
    }

    private static String methodSection(String source, String signatureStart) {
        int start = source.indexOf(signatureStart);
        assertTrue(start >= 0, "Missing method section for " + signatureStart);

        int nextMethod = source.indexOf("\n    public static ", start + signatureStart.length());
        return nextMethod >= 0 ? source.substring(start, nextMethod) : source.substring(start);
    }
}
