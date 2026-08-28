package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.fml.ModList;

public final class JourneyMapCompatClient {
    private JourneyMapCompatClient() {
    }

    public static boolean isJourneyMapLoaded() {
        return ModList.get().isLoaded("journeymap");
    }
}
