package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RadarJammerTowerAnimationAssetTest {
    @Test
    void placedRadarTowerUsesOneStaticFullRadarModel() throws Exception {
        ClassLoader loader = RadarJammerTowerAnimationAssetTest.class.getClassLoader();
        String blockObj = readResource(loader, "assets/sbwdroneconfig/models/block/radar_jammer_tower.obj");
        assertTrue(blockObj.contains("usemtl build"), "The placed Radar Jammer Tower should render the base/build mesh.");
        assertTrue(blockObj.contains("usemtl antene"), "The placed Radar Jammer Tower should render the antenna/radar mesh statically.");

        String itemObj = readResource(loader, "assets/sbwdroneconfig/models/block/radar_jammer_tower_item.obj");
        assertTrue(itemObj.contains("usemtl build"), "The item model should keep the base/build mesh.");
        assertTrue(itemObj.contains("usemtl antene"), "The item model should preview the full tower.");
    }

    @Test
    void radarTowerDoesNotRegisterAnimatedBlockEntityRenderer() throws Exception {
        String renderersSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonEntityRenderers.java"
        ), StandardCharsets.UTF_8);

        assertFalse(renderersSource.contains("RadarJammerTowerRenderer::new"),
                "Radar Jammer Tower should not use the experimental animated/debug block entity renderer.");
        assertFalse(renderersSource.contains("registerBlockEntityRenderer(AddonBlockEntities.RADAR_JAMMER_TOWER"),
                "Radar Jammer Tower should render from its static block model only.");
    }

    @Test
    void radarTowerUsesMarkedFourByTwoMultiblockFootprint() throws Exception {
        String blockSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "RadarJammerTowerBlock.java"
        ), StandardCharsets.UTF_8);
        String partEnumSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "RadarJammerTowerPart.java"
        ), StandardCharsets.UTF_8);
        String partSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "RadarJammerTowerPartBlock.java"
        ), StandardCharsets.UTF_8);
        String registrySource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonBlocks.java"
        ), StandardCharsets.UTF_8);

        assertTrue(registrySource.contains("RADAR_JAMMER_TOWER_PART"),
                "Radar Jammer Tower should register internal part blocks for the marked footprint.");
        assertTrue(blockSource.contains("placeMultiblockParts"),
                "The master radar block should place the marked footprint helper blocks.");
        assertTrue(blockSource.contains("removeMultiblock"),
                "Breaking any radar tower block should remove the entire footprint.");
        assertTrue(blockSource.contains("RadarJammerTowerPart.values()"),
                "The master radar block should iterate every non-master multiblock part.");
        assertTrue(partEnumSource.contains("FOOTPRINT_WIDTH_BLOCKS = 4") && partEnumSource.contains("FOOTPRINT_DEPTH_BLOCKS = 2"),
                "The radar tower footprint should cover the marked 4x2 base around the actual model.");
        assertTrue(partEnumSource.contains("partPos(BlockPos masterPos, Direction facing)")
                        && partEnumSource.contains("masterPos(BlockPos partPos, Direction facing)"),
                "The radar tower part offsets should rotate with the placed facing.");
        assertTrue(partEnumSource.contains("EAST_NORTH") && partEnumSource.contains("WEST_TWO_NORTH"),
                "The radar tower should include the extra blocks needed for the full marked 4x2 footprint.");
        assertFalse(blockSource.contains("BASE_FOOTPRINT_SHAPE"),
                "The radar should not fake the footprint with one oversized voxel shape.");
        assertTrue(partSource.contains("RenderShape.INVISIBLE"),
                "The helper blocks should not render their own model.");
        assertTrue(partSource.contains("findMasterPos"),
                "The helper blocks should route interaction/breaking back to the master block.");
    }

    @Test
    void radarTowerPartResourcesAndBlockEntityStayInternal() throws Exception {
        ClassLoader loader = RadarJammerTowerAnimationAssetTest.class.getClassLoader();
        String partBlockstate = readResource(loader, "assets/sbwdroneconfig/blockstates/radar_jammer_tower_part.json");
        String partModel = readResource(loader, "assets/sbwdroneconfig/models/block/radar_jammer_tower_part.json");
        String blockEntitiesSource = Files.readString(Path.of(
                "src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "AddonBlockEntities.java"
        ), StandardCharsets.UTF_8);

        assertTrue(partBlockstate.contains("radar_jammer_tower_part"),
                "The invisible helper block should have a valid blockstate to avoid missing-model warnings.");
        assertTrue(partModel.contains("\"elements\": []"),
                "The invisible helper block should use an empty model.");
        assertFalse(blockEntitiesSource.contains("RADAR_JAMMER_TOWER_PART.get()"),
                "Only the master radar block should own the GUI/ticking block entity.");
    }

    private static String readResource(ClassLoader loader, String path) throws Exception {
        try (var stream = loader.getResourceAsStream(path)) {
            assertNotNull(stream, path + " should be packaged.");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
