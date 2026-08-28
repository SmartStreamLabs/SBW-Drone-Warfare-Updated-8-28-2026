package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LucasDroneScaleSourceTest {
    private static final Path MAIN = Path.of("src/main/java/nl/smartstreamlabs/sbwdroneconfig");

    @Test
    void lucasDroneUsesLargeFixedWingVisualScaleAndHitbox() throws Exception {
        String renderMath = Files.readString(MAIN.resolve("LucasDroneRenderMath.java"));
        String renderer = Files.readString(MAIN.resolve("LucasDroneRenderer.java"));
        String entities = Files.readString(MAIN.resolve("AddonEntities.java"));

        assertTrue(renderMath.contains("DRONE_VISUAL_SCALE = 60.0F"),
                "LUCAS should render roughly 3x larger than its previous 20.0 scale.");
        assertTrue(renderMath.contains("BODY_TRANSLATION_Y = 0.002F"),
                "The larger LUCAS model should sit slightly lower in the hitbox without changing its overall scale.");
        assertTrue(renderer.contains("this.shadowRadius = 1.2F"),
                "The LUCAS renderer shadow should match the larger fixed-wing footprint.");
        assertTrue(entities.contains(".sized(3.2F, 0.35F)"),
                "The LUCAS hitbox should stay flatter and closer to the airframe without changing the visual model scale.");
        assertTrue(entities.contains(".sized(0.6F, 0.2F)"),
                "The normal FPV drone hitbox must remain unchanged.");
    }

    @Test
    void lucasDroneCameraAndNameplateAreRaisedForTheLargerAirframe() throws Exception {
        String entity = Files.readString(MAIN.resolve("LucasDroneEntity.java"));
        String renderer = Files.readString(MAIN.resolve("LucasDroneRenderer.java"));

        assertTrue(entity.contains("LUCAS_EYE_HEIGHT = 0.45F"),
                "The LUCAS camera eye height should be raised for the larger UAV body.");
        assertTrue(entity.contains("protected float getEyeHeight"),
                "LUCAS should override camera eye height instead of using the tiny inherited drone value.");
        assertTrue(renderer.contains("renderNameTag"),
                "The LUCAS nameplate should float above the larger fixed-wing body.");
        assertTrue(renderer.contains("LucasDroneRenderMath.nameplateOffsetY()"),
                "The renderer should use a single LUCAS scale helper for nameplate height.");
    }
}
