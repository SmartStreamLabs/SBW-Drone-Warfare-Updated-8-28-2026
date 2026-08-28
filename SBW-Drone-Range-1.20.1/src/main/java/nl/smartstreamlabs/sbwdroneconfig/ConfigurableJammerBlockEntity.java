package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface ConfigurableJammerBlockEntity {
    int getJammerRange();

    void setJammerRange(int jammerRange);

    int getMinJammerRange();

    int getMaxJammerRange();

    boolean canConfigure(ServerPlayer player);

    List<String> getWhitelistedGamertags();

    boolean addWhitelistedGamertag(String gamertag);

    boolean removeWhitelistedGamertag(String gamertag);

    default boolean isLongRangeTower() {
        return false;
    }
}
