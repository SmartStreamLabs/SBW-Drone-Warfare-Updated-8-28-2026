package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JammerWhitelistSourceIntegrationTest {
    @Test
    void jammerBlockEntityStoresAndAppliesGamertagWhitelistServerSide() throws Exception {
        String source = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "JammerBlockEntity.java"), StandardCharsets.UTF_8);

        assertTrue(source.contains("whitelistedGamertags"), "Jammer block entity should persist a per-block whitelist.");
        assertTrue(source.contains("addWhitelistedGamertag"), "Jammer block entity should allow adding gamertags.");
        assertTrue(source.contains("removeWhitelistedGamertag"), "Jammer block entity should allow removing gamertags.");
        assertTrue(source.contains("findWhitelistedGamertag(level, entity)"), "Jammer scan should check the whitelist before applying influence.");
        assertTrue(source.contains("return false;"), "Whitelisted drones/controllers should be skipped by shouldAffectDrone.");
        assertTrue(source.contains("SbwCompat.getDroneControllerUuid(drone)"), "Whitelist must work for the active controller gamertag.");
        assertTrue(source.contains("SbwCompat.resolveDroneOwner(level, drone)"), "Whitelist should also fall back to stored drone owner identity.");
        assertTrue(source.contains("WhitelistedGamertags"), "Whitelist must be saved to NBT.");
    }

    @Test
    void jammerGuiAndNetworkExposeWhitelistEditing() throws Exception {
        String screen = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "JammerScreen.java"), StandardCharsets.UTF_8);
        String network = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonNetwork.java"), StandardCharsets.UTF_8);
        String openMessage = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "OpenJammerConfigMessage.java"), StandardCharsets.UTF_8);
        String updateMessage = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "UpdateJammerWhitelistMessage.java"), StandardCharsets.UTF_8);

        assertTrue(screen.contains("EditBox"), "Jammer GUI should expose a gamertag input field.");
        assertTrue(screen.contains("UpdateJammerWhitelistMessage"), "Jammer GUI should send whitelist add/remove packets.");
        assertTrue(screen.contains("screen.sbwdroneconfig.jammer.whitelist"), "Jammer GUI should label the whitelist.");
        assertTrue(openMessage.contains("List<String> whitelistedGamertags"), "Open packet should sync the current whitelist to the client.");
        assertTrue(network.contains("UpdateJammerWhitelistMessage.class"), "Whitelist update packet should be registered.");
        assertTrue(network.contains("updateJammerWhitelist"), "Client helper should send whitelist updates.");
        assertTrue(updateMessage.contains("blockEntity.addWhitelistedGamertag(trimmed)"), "Server packet should add entries on the block entity.");
        assertTrue(updateMessage.contains("blockEntity.removeWhitelistedGamertag(trimmed)"), "Server packet should remove entries on the block entity.");
        assertTrue(updateMessage.contains("blockEntity.canConfigure(player)"), "Whitelist editing should respect jammer ownership.");
    }

    @Test
    void englishTranslationsExposeJammerWhitelistLabels() throws Exception {
        try (var stream = JammerWhitelistSourceIntegrationTest.class.getClassLoader().getResourceAsStream("assets/sbwdroneconfig/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json should be packaged.");

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("Whitelisted players", root.get("screen.sbwdroneconfig.jammer.whitelist").getAsString());
            assertEquals("Gamertag whitelist", root.get("screen.sbwdroneconfig.jammer.whitelist_input").getAsString());
            assertEquals("Added jammer whitelist entry: %s", root.get("message.sbwdroneconfig.jammer_whitelist_added").getAsString());
            assertEquals("Removed jammer whitelist entry: %s", root.get("message.sbwdroneconfig.jammer_whitelist_removed").getAsString());
        }
    }

    @Test
    void jammerGuiUsesSharedLayoutConstantsSoWhitelistTextCannotOverlapButtons() throws Exception {
        String screen = Files.readString(Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "JammerScreen.java"), StandardCharsets.UTF_8);

        assertTrue(screen.contains("JammerScreenLayout.PANEL_TOP"), "Widget placement and text rendering should share the same panel top.");
        assertTrue(screen.contains("JammerScreenLayout.WHITELIST_LIST_LABEL_Y"), "Whitelist list label should have an explicit non-overlapping Y position.");
        assertTrue(screen.contains("JammerScreenLayout.SAVE_BUTTON_Y"), "Save and cancel buttons should sit below the whitelist list area.");
        assertFalse(screen.contains("int top = 50;"), "Old widget top offset caused text and buttons to drift out of sync.");
        assertFalse(screen.contains("int top = 40;"), "Old render top offset caused whitelist entries to overlap Add/Remove buttons.");

        assertTrue(JammerScreenLayout.WHITELIST_LIST_LABEL_Y > JammerScreenLayout.WHITELIST_BUTTON_Y + JammerScreenLayout.BUTTON_HEIGHT,
                "Whitelist list text should render below the Add/Remove buttons.");
        assertTrue(JammerScreenLayout.SAVE_BUTTON_Y > JammerScreenLayout.WHITELIST_LIST_Y + 50,
                "Save row should leave room for visible whitelist entries.");
    }
}
