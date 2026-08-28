package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModMetadataLicenseTest {
    @Test
    void forgeModMetadataUsesGnuGeneralPublicLicense() throws Exception {
        String buildScript = Files.readString(Path.of("build.gradle"));

        assertTrue(
                buildScript.contains("mod_license            : 'GNU GENERAL PUBLIC LICENSE'"),
                "Forge mod metadata should display GNU GENERAL PUBLIC LICENSE in the mod list."
        );
    }
}
