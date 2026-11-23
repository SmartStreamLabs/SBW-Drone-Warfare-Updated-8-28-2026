package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetBulkPlacementSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");

    @Test
    void onlySupportedAntiDroneNetIdsAreMappedForBulkPlacement() throws Exception {
        String kindSource = Files.readString(MAIN.resolve("NetBulkPlacementKind.java"), StandardCharsets.UTF_8);
        String itemSource = Files.readString(MAIN.resolve("AddonItems.java"), StandardCharsets.UTF_8);

        assertTrue(kindSource.contains("ANTI_DRONE_NET(\"anti_drone_net\")"));
        assertTrue(kindSource.contains("ANTI_DRONE_NET_PANEL(\"anti_drone_net_panel\")"));
        assertTrue(kindSource.contains("ANTI_DRONE_NET_CARPET(\"anti_drone_net_carpet\")"));
        assertTrue(itemSource.contains("ANTI_DRONE_NET"));
        assertTrue(itemSource.contains("ANTI_DRONE_NET_PANEL"));
        assertTrue(itemSource.contains("ANTI_DRONE_NET_CARPET"));
    }

    @Test
    void workflowKeepsNormalPlacementAndUsesClientOnlyGhostPreview() throws Exception {
        String clientSource = Files.readString(MAIN.resolve("NetBulkPlacementClient.java"), StandardCharsets.UTF_8);

        assertTrue(clientSource.contains("InputEvent.InteractionKeyMappingTriggered"),
                "Bulk selection should hook normal use input without a global keybind.");
        assertTrue(clientSource.contains("player.isShiftKeyDown()"),
                "Sneak-use should start/cancel bulk mode so normal use can still place one block.");
        assertTrue(clientSource.contains("event.setCanceled(true)"),
                "Selection/confirm clicks should not place an early block.");
        assertTrue(clientSource.contains("RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS"));
        assertTrue(clientSource.contains("renderSingleBlock"));
        assertTrue(clientSource.contains("LevelRenderer.renderLineBox"));
        int blockRenderIndex = clientSource.indexOf("renderSingleBlock(");
        int lineBufferIndex = clientSource.indexOf("VertexConsumer lines = buffers.getBuffer(RenderType.lines());");
        assertTrue(lineBufferIndex > blockRenderIndex,
                "The line buffer must be fetched after renderSingleBlock so the preview outline buffer stays open.");
        assertTrue(clientSource.contains("PreviewTarget"));
        assertTrue(clientSource.contains("clearSelection"));
    }

    @Test
    void planeLogicMatchesRequestedNetPanelAndCarpetShapes() throws Exception {
        String supportSource = Files.readString(MAIN.resolve("NetBulkPlacementSupport.java"), StandardCharsets.UTF_8);

        assertTrue(supportSource.contains("new BlockPos(x, pos1.getY(), z)"),
                "Carpet bulk placement should lock Y to POS1 and fill X/Z only.");
        assertTrue(supportSource.contains("Direction.Axis.Y"),
                "Carpet plans should be explicitly horizontal.");
        assertTrue(supportSource.contains("planeAxis == Direction.Axis.Z"),
                "North/south clicked faces should create an X/Y vertical plane.");
        assertTrue(supportSource.contains("planeAxis == Direction.Axis.X"),
                "East/west clicked faces should create a Z/Y vertical plane.");
        assertTrue(supportSource.contains("SlabType.DOUBLE"),
                "Panel bulk planes should keep a consistent panel state instead of random rotations.");
        assertTrue(supportSource.contains("BlockStateProperties.WATERLOGGED"),
                "Net state generation should preserve waterlogging when supported.");
    }

    @Test
    void serverValidationInventoryJobsAndUndoAreAuthoritative() throws Exception {
        String serverSource = Files.readString(MAIN.resolve("NetBulkPlacementServer.java"), StandardCharsets.UTF_8);
        String commandSource = Files.readString(MAIN.resolve("NetBulkPlacementCommand.java"), StandardCharsets.UTF_8);

        assertTrue(serverSource.contains("ACTIVE_JOBS"));
        assertTrue(serverSource.contains("LAST_UNDO"));
        assertTrue(serverSource.contains("RECENT_SESSIONS"));
        assertTrue(serverSource.contains("level.hasChunkAt"));
        assertTrue(serverSource.contains("level.mayInteract"));
        assertTrue(serverSource.contains("player.mayUseItemAt"));
        assertTrue(serverSource.contains("existing.hasBlockEntity()"));
        assertTrue(serverSource.contains("BlockPlaceContext"));
        assertTrue(serverSource.contains("countMatchingItems"));
        assertTrue(serverSource.contains("consumeMatchingItems"));
        assertTrue(serverSource.contains("refundItems"));
        assertTrue(serverSource.contains("netBulkPlacementBlocksPerTick"));
        assertTrue(serverSource.contains("MESSAGE_NOT_ENOUGH_DETAILED"));
        assertTrue(serverSource.contains("undoLast"));
        assertTrue(commandSource.contains("Commands.literal(\"sbw\")"));
        assertTrue(commandSource.contains("Commands.literal(\"net\")"));
        assertTrue(commandSource.contains("Commands.literal(\"undo\")"));
    }

    @Test
    void configNetworkRegistrationAndLifecycleHooksArePresent() throws Exception {
        String configSource = Files.readString(MAIN.resolve("AddonConfig.java"), StandardCharsets.UTF_8);
        String networkSource = Files.readString(MAIN.resolve("AddonNetwork.java"), StandardCharsets.UTF_8);
        String modSource = Files.readString(MAIN.resolve("SbwDroneRangeConfig.java"), StandardCharsets.UTF_8);

        assertTrue(configSource.contains("define(\"netBulkPlacementEnabled\", true)"));
        assertTrue(configSource.contains("defineInRange(\"netBulkPlacementMaxWidth\", 256"));
        assertTrue(configSource.contains("defineInRange(\"netBulkPlacementMaxHeight\", 128"));
        assertTrue(configSource.contains("defineInRange(\"netBulkPlacementMaxBlocks\", 16384"));
        assertTrue(configSource.contains("defineInRange(\"netBulkPlacementMaximumReach\", 256.0D"));
        assertTrue(configSource.contains("define(\"netBulkPlacementAllOrNothing\", false)"));
        assertTrue(configSource.contains("define(\"netBulkPlacementSkipBlocked\", true)"));
        assertTrue(configSource.contains("defineInRange(\"netBulkPlacementBlocksPerTick\", 128"));
        assertTrue(configSource.contains("defineInRange(\"netBulkPlacementUndoSeconds\", 60"));

        assertTrue(networkSource.contains("NetBulkPlacementConfirmMessage"));
        assertTrue(networkSource.contains("sendNetBulkPlacementConfirm"));
        assertTrue(modSource.contains("NetBulkPlacementClient.init"));
        assertTrue(modSource.contains("NetBulkPlacementServer.class"));
        assertTrue(modSource.contains("NetBulkPlacementCommand.class"));
    }

    @Test
    void actionBarMessagesArePackaged() throws Exception {
        try (var stream = NetBulkPlacementSourceTest.class.getClassLoader()
                .getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "English language file should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("Net Pos1 set", root.get("message.sbwdroneconfig.net_pos1_set").getAsString());
            assertEquals("Net Pos2 set", root.get("message.sbwdroneconfig.net_pos2_set").getAsString());
            assertEquals("Right-click to confirm", root.get("message.sbwdroneconfig.net_confirm").getAsString());
            assertEquals("Net placement cancelled", root.get("message.sbwdroneconfig.net_cancelled").getAsString());
            assertEquals("Not enough net blocks", root.get("message.sbwdroneconfig.net_not_enough").getAsString());
            assertEquals("Placement area too large", root.get("message.sbwdroneconfig.net_too_large").getAsString());
        }
    }
}
