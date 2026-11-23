package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModMetadataLicenseTest {
    @Test
    void modMetadataUsesExistingAgplCodeLicense() throws Exception {
        String buildScript = Files.readString(Path.of("build.gradle"));

        assertTrue(
                buildScript.contains("mod_license            : 'GNU AGPL version 3 (LICENSE)"),
                "Mod metadata must preserve AGPL version 3 and its license reference."
        );
    }
}
