package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LucasConfigPaginationSourceTest {
    @Test
    void lucasFlightConfigIsSplitAcrossPagesInsteadOfOverflowingOnePanel() throws Exception {
        String screen = Files.readString(
                Path.of("src", "main", "java", "nl", "smartstreamlabs", "sbwdroneconfig", "DroneConfigScreen.java"),
                StandardCharsets.UTF_8
        );
        String english = Files.readString(
                Path.of("src", "main", "resources", "assets", "sbwdroneconfig", "lang", "en_us.json"),
                StandardCharsets.UTF_8
        );

        assertTrue(screen.contains("categoryPage"), "The config screen should track the current category page.");
        assertTrue(screen.contains("case LUCAS_FLIGHT -> 2"), "LUCAS Flight needs two pages to avoid overflowing the panel.");
        assertTrue(screen.contains("addPageButtons(contentX, top, contentWidth)"), "Paged categories should expose page navigation.");
        assertTrue(screen.contains("addLucasFlightPage("), "The first LUCAS page should contain flight controls.");
        assertTrue(screen.contains("addLucasFuelAndAudioPage("), "The second LUCAS page should contain fuel and audio controls.");
        assertTrue(screen.contains("new DroneConfigScreen(parent, category, draft, 0)"), "Switching categories should start on page one.");
        assertTrue(screen.contains("new DroneConfigScreen(parent, selectedCategory, draft, categoryPage)"), "Resetting should keep the visible page.");
        assertTrue(english.contains("\"screen.sbwdroneconfig.config.page\": \"Page %s/%s\""));

        assertFalse(screen.contains("lucasSpacing * 10"), "No LUCAS page should need eleven vertical rows.");
        assertFalse(screen.contains("lucasSpacing * 11"), "No LUCAS page should need twelve vertical rows.");
    }
}
