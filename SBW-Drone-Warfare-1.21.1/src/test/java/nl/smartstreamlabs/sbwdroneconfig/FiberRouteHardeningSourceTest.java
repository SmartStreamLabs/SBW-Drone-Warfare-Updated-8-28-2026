package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberRouteHardeningSourceTest {
    @Test
    void routeHistoryDoesNotSpawnCableOrRouteHelperEntities() throws Exception {
        String linkSource = readMainSource("FiberOpticLinkSystem.java");
        String allMainSources = readMainSource("FiberOpticLinkSystem.java") + readMainSource("FiberCableRouteState.java") + readMainSource("DroneFiberOpticClient.java");

        assertFalse(linkSource.contains("AddonEntities.FIBER_OPTIC_CABLE_SEGMENT"));
        assertFalse(linkSource.contains("FIBER_OPTIC_CABLE_SEGMENT.get().create"));
        assertFalse(linkSource.contains("new FiberOpticCableSegmentEntity"));
        assertFalse(linkSource.contains("addFreshEntity(segment"));
        assertFalse(linkSource.contains("addFreshEntity(cableSegment"));
        assertFalse(allMainSources.contains("RoutePointEntity"));
        assertFalse(allMainSources.contains("FiberRouteEntity"));
        assertFalse(allMainSources.contains("ArmorStand"));
        assertFalse(allMainSources.contains("BlockDisplay"));
    }

    @Test
    void legacySegmentMaintenancePathsOnlyDiscardTrackedSegments() throws Exception {
        String linkSource = readMainSource("FiberOpticLinkSystem.java");

        assertTrue(linkSource.contains("private static void maybeMaintainCableSegments(ServerLevel level, Entity drone)"));
        assertTrue(linkSource.contains("private static void syncCableSegments(ServerLevel level, Entity drone)"));
        assertTrue(linkSource.contains("discardLegacyCableSegments(level, drone);"));
        assertEquals(0, countOccurrences(linkSource, "level.addFreshEntity(segment"));
        assertEquals(0, countOccurrences(linkSource, "level.addFreshEntity(cableSegment"));
    }

    @Test
    void routePacketsAreGatedBySparseRouteStateUpdates() throws Exception {
        String linkSource = readMainSource("FiberOpticLinkSystem.java");

        assertTrue(linkSource.contains("FiberCableRouteState.ensureInitialized("));
        assertTrue(linkSource.contains("FiberCableRouteState.appendIfNeeded(drone, resolveServerSpoolPoint(drone))"));
        assertTrue(linkSource.contains("if (update == null || update.kind() == FiberCableRouteState.Kind.NONE)"));
        assertEquals(1, countOccurrences(linkSource, "AddonNetwork.sendFiberRouteAppend"));
        assertEquals(2, countOccurrences(linkSource, "AddonNetwork.sendFiberRouteSnapshot(player, new FiberRouteSnapshotMessage"));
    }

    @Test
    void debugRouteLoggingIsOptInAndRegisteredWithNetworkMessages() throws Exception {
        String configSource = readMainSource("AddonConfig.java");
        String routeStateSource = readMainSource("FiberCableRouteState.java");
        String clientSource = readMainSource("DroneFiberOpticClient.java");
        String linkSource = readMainSource("FiberOpticLinkSystem.java");
        String networkSource = readMainSource("AddonNetwork.java");

        assertTrue(configSource.contains(".define(\"fiberRouteDebugLogging\", false)"));
        assertTrue(configSource.contains("public static boolean fiberRouteDebugLogging()"));
        assertTrue(routeStateSource.contains("if (!AddonConfig.fiberRouteDebugLogging())"));
        assertTrue(clientSource.contains("if (!AddonConfig.fiberRouteDebugLogging())"));
        assertTrue(linkSource.contains("if (!AddonConfig.fiberRouteDebugLogging())"));
        assertTrue(networkSource.contains("FiberRouteSnapshotMessage.TYPE"));
        assertTrue(networkSource.contains("FiberRouteAppendMessage.TYPE"));
        assertTrue(networkSource.contains("FiberRouteClearMessage.TYPE"));
    }

    private static String readMainSource(String fileName) throws Exception {
        return Files.readString(Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                fileName
        ), StandardCharsets.UTF_8);
    }

    private static String readAllMainSources() throws Exception {
        try (var paths = Files.walk(Path.of("src", "main", "java"))) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> {
                        try {
                            return Files.readString(path, StandardCharsets.UTF_8);
                        } catch (Exception exception) {
                            throw new IllegalStateException(exception);
                        }
                    })
                    .collect(Collectors.joining("\n"));
        }
    }

    private static int countOccurrences(String source, String needle) {
        int count = 0;
        int fromIndex = 0;
        while (fromIndex >= 0) {
            int index = source.indexOf(needle, fromIndex);
            if (index < 0) {
                return count;
            }
            count++;
            fromIndex = index + needle.length();
        }
        return count;
    }
}
