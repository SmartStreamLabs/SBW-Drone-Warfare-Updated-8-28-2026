package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BayraktarTb2NamingCompatibilityTest {
    private static final Path MAIN = Path.of("src/main");

    @Test
    void keepsTheLegacyRegistryIdWhileRemovingTheOldVisibleName() throws Exception {
        String definition = Files.readString(MAIN.resolve("java/nl/smartstreamlabs/sbwdroneconfig/BayraktarTb2Definition.java"), StandardCharsets.UTF_8);
        String language = Files.readString(MAIN.resolve("resources/assets/sbwdroneconfig/lang/en_us.json"), StandardCharsets.UTF_8);
        String vehicle = Files.readString(MAIN.resolve("resources/data/sbwdroneconfig/sbw/vehicles/ukrainian_bayraktar_tb2.json"), StandardCharsets.UTF_8);

        assertTrue(definition.contains("ID = \"ukrainian_bayraktar_tb2\""));
        assertTrue(definition.contains("DISPLAY_NAME = \"Bayraktar TB2\""));
        assertTrue(vehicle.contains("\"ID\": \"sbwdroneconfig:ukrainian_bayraktar_tb2\""));
        assertTrue(language.contains("\"entity.sbwdroneconfig.ukrainian_bayraktar_tb2\": \"Bayraktar TB2\""));
        assertTrue(language.contains("\"item.sbwdroneconfig.ukrainian_bayraktar_tb2\": \"Bayraktar TB2\""));
        assertFalse(language.contains("Ukrainian Bayraktar TB2"));
    }
}
