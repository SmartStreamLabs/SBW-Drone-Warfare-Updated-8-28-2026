package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

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
}
