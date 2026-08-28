package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCrashRecoverySourceTest {
    @Test
    void crashSystemHandsOffFiberRecoveryToTheAddon() throws Exception {
        Path crashSystemSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "DroneCrashExplosionSystem.java"
        );
        String source = Files.readString(crashSystemSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("FiberOpticLinkSystem.handleDroneCrashRecovery(level, drone);"),
                "A destroyed fiber drone should hand off recovery spawning before the crash cleanup finishes.");
    }

    @Test
    void addonRegistersRecoverableFiberCableEntityAndRenderer() throws Exception {
        Path entitiesSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "AddonEntities.java"
        );
        String entities = Files.readString(entitiesSource, StandardCharsets.UTF_8);
        assertTrue(entities.contains("RECOVERABLE_FIBER_OPTIC_CABLE"),
                "The addon should register a dedicated recoverable fiber cable entity type.");

        Path renderersSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "AddonEntityRenderers.java"
        );
        String renderers = Files.readString(renderersSource, StandardCharsets.UTF_8);
        assertTrue(renderers.contains("RecoverableFiberOpticCableRenderer::new"),
                "The recoverable fiber cable entity should have its own renderer registration.");
    }

    @Test
    void recoverableCableEntitySupportsShiftRightClickPickupOfDamagedSpool() throws Exception {
        Path entitySource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "RecoverableFiberOpticCableEntity.java"
        );
        String source = Files.readString(entitySource, StandardCharsets.UTF_8);

        assertTrue(source.contains("player.isCrouching()"),
                "Recoverable fiber cable pickup should require crouching.");
        assertTrue(source.contains("InteractionHand hand"),
                "Recoverable fiber cable pickup should use a normal right-click interaction flow.");
        assertTrue(source.contains("ItemHandlerHelper.giveItemToPlayer(player, recoveredSpool.copy())"),
                "Shift-right-clicking the recoverable cable should return the stored damaged spool item to the player.");
        assertTrue(source.contains("this.discard();"),
                "After the spool is recovered, the crash cable entity should remove itself from the world.");
    }

    @Test
    void fiberLinkSystemExtractsAndSpawnsCrashRecoverySpool() throws Exception {
        Path linkSystemSource = Path.of(
                "src",
                "main",
                "java",
                "nl",
                "smartstreamlabs",
                "sbwdroneconfig",
                "FiberOpticLinkSystem.java"
        );
        String source = Files.readString(linkSystemSource, StandardCharsets.UTF_8);

        assertTrue(source.contains("public static void handleDroneCrashRecovery(ServerLevel level, Entity drone)"),
                "Fiber optic recovery should be handled through a dedicated server-side crash recovery method.");
        assertTrue(source.contains("new DroneInventoryContainer(drone)"),
                "Crash recovery should inspect the existing drone inventory instead of inventing a separate spool store.");
        assertTrue(source.contains("AddonItems.FIBER_OPTIC_SPOOL_UPGRADE.get()"),
                "Crash recovery should only spawn salvage from the installed Fiber Optic Spool Upgrade.");
        assertTrue(source.contains("AddonEntities.RECOVERABLE_FIBER_OPTIC_CABLE.get().create(level)"),
                "Crash recovery should spawn a dedicated recoverable cable entity on the crash site.");
    }
}
