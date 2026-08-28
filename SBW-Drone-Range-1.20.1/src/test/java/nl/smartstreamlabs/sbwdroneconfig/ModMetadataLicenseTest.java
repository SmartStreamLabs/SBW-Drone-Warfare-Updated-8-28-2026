package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModMetadataLicenseTest {
    @Test
    void forgeModMetadataUsesGnuGeneralPublicLicense() throws Exception {
        String gradleProperties = Files.readString(Path.of("gradle.properties"));
        String modMetadata = Files.readString(Path.of("src/main/resources/META-INF/neoforge.mods.toml"));

        assertTrue(
                gradleProperties.contains("mod_license=GNU GENERAL PUBLIC LICENSE")
                        && modMetadata.contains("license=\"${mod_license}\""),
                "Forge mod metadata should display GNU GENERAL PUBLIC LICENSE in the mod list."
        );
    }
}
