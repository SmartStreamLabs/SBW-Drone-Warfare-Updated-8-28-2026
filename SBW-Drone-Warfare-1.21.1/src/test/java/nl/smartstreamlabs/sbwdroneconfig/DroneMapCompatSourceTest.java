package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneMapCompatSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");
    private static final Path MIXIN = MAIN.resolve("mixin");
    private static final Path LANG = Path.of("src/main/resources/assets/sbwdroneconfig/lang/en_us.json");

    @Test
    void minimapVisibilityHasConfigAndGuiControls() throws Exception {
        String config = Files.readString(MAIN.resolve("AddonConfig.java"));
        String draft = Files.readString(MAIN.resolve("DroneConfigDraft.java"));
        String screen = Files.readString(MAIN.resolve("DroneConfigScreen.java"));
        String lang = Files.readString(LANG);

        assertTrue(config.contains("HIDE_PLAYER_ON_MINIMAP_DURING_DRONE_CONTROL_VALUE"));
        assertTrue(config.contains("define(\"hidePlayerOnMinimapDuringDroneControl\", true)"));
        assertTrue(config.contains("SHOW_DRONE_MARKER_INSTEAD_VALUE"));
        assertTrue(config.contains("define(\"showDroneMarkerInstead\", true)"));
        assertTrue(config.contains("hidePlayerOnMinimapDuringDroneControl()"));
        assertTrue(config.contains("showDroneMarkerInstead()"));

        assertTrue(draft.contains("hidePlayerOnMinimapDuringDroneControl"));
        assertTrue(draft.contains("showDroneMarkerInstead"));
        assertTrue(draft.contains("HIDE_PLAYER_ON_MINIMAP_DURING_DRONE_CONTROL_VALUE.set"));
        assertTrue(draft.contains("SHOW_DRONE_MARKER_INSTEAD_VALUE.set"));

        assertTrue(screen.contains("screen.sbwdroneconfig.config.label.hide_player_on_minimap"));
        assertTrue(screen.contains("screen.sbwdroneconfig.config.label.show_drone_marker"));
        String fpvCameraPage = screen.substring(screen.indexOf("private void addFpvCameraPage"));
        fpvCameraPage = fpvCameraPage.substring(0, fpvCameraPage.indexOf("private void addLucasFlightPage"));
        assertTrue(fpvCameraPage.contains("screen.sbwdroneconfig.config.label.hide_player_on_minimap"));
        assertTrue(fpvCameraPage.contains("screen.sbwdroneconfig.config.label.show_drone_marker"));
        String lucasFuelAndAudioPage = screen.substring(screen.indexOf("private void addLucasFuelAndAudioPage"));
        lucasFuelAndAudioPage = lucasFuelAndAudioPage.substring(0, lucasFuelAndAudioPage.indexOf("private void saveAndClose"));
        assertTrue(lucasFuelAndAudioPage.contains("screen.sbwdroneconfig.config.label.hide_player_on_minimap"));
        assertTrue(lucasFuelAndAudioPage.contains("screen.sbwdroneconfig.config.label.show_drone_marker"));
        assertTrue(lang.contains("screen.sbwdroneconfig.config.label.hide_player_on_minimap"));
        assertTrue(lang.contains("screen.sbwdroneconfig.config.label.show_drone_marker"));
    }

    @Test
    void mapCompatUsesActiveFpvAndLucasDroneViews() throws Exception {
        String compat = Files.readString(MAIN.resolve("DroneMapCompatClient.java"));

        assertTrue(compat.contains("AddonConfig.hidePlayerOnMinimapDuringDroneControl()"));
        assertTrue(compat.contains("DroneClientViewContext.activeDroneEntity"));
        assertTrue(compat.contains("activeCubedFpvDrone"));
        assertTrue(compat.contains("activeLucasDrone"));
        assertTrue(compat.contains("shouldShowDroneMarkerInstead"));
        assertTrue(compat.contains("localPlayer.getUUID().equals(mapPlayer.getUUID())"));
    }

    @Test
    void xaeroAndJourneyMapHooksStayOptional() throws Exception {
        String mixinConfig = Files.readString(Path.of("src/main/resources/sbwdroneconfig.mixins.json"));
        String plugin = Files.readString(MIXIN.resolve("SbwDroneConfigMixinPlugin.java"));
        String xaeroMinimap = Files.readString(MIXIN.resolve("XaeroMinimapRendererMixin.java"));
        String xaeroWorldMap = Files.readString(MIXIN.resolve("XaeroWorldMapGuiMapMixin.java"));
        String journeyMap = Files.readString(MAIN.resolve("JourneyMapCompatClient.java"));

        assertTrue(mixinConfig.contains("XaeroMinimapRendererMixin"));
        assertTrue(mixinConfig.contains("XaeroWorldMapGuiMapMixin"));
        assertTrue(plugin.contains("XAERO_MINIMAP_RENDERER_CLASS"));
        assertTrue(plugin.contains("XAERO_WORLD_MAP_GUI_MAP_CLASS"));
        assertTrue(xaeroMinimap.contains("@Pseudo"));
        assertTrue(xaeroWorldMap.contains("@Pseudo"));
        assertTrue(journeyMap.contains("isJourneyMapLoaded"));
        assertTrue(journeyMap.contains("journeymap.client.api.event.forge.EntityRadarUpdateEvent"));
        assertTrue(journeyMap.contains("NeoForge.EVENT_BUS.addListener"));
        assertTrue(journeyMap.contains("setDisableMethod"));
        assertTrue(journeyMap.contains("shouldHidePlayerMarker"));
        assertTrue(journeyMap.contains("shouldShowDroneMarker"));
    }
}
