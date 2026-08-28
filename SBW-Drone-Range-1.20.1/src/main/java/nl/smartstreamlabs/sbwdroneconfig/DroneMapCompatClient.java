package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

public final class DroneMapCompatClient {
    private DroneMapCompatClient() {
    }

    public static boolean isLocalDroneControlActive() {
        if (!AddonConfig.hidePlayerOnMinimapDuringDroneControl() || !ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }

        Entity activeDrone = DroneClientViewContext.activeDroneEntity(minecraft);
        if (activeDrone == null) {
            return false;
        }

        return DroneClientViewContext.activeCubedFpvDrone(minecraft) != null
                || DroneClientViewContext.activeLucasDrone(minecraft) != null;
    }

    public static boolean shouldHideXaeroMinimapSelfMarker() {
        return isLocalDroneControlActive();
    }

    public static boolean shouldHideXaeroWorldMapSelfMarker(Entity mapPlayer) {
        return shouldHidePlayerMarker(mapPlayer);
    }

    public static boolean shouldHidePlayerMarker(Entity mapPlayer) {
        if (!isLocalDroneControlActive()) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        return localPlayer != null && mapPlayer != null && localPlayer.getUUID().equals(mapPlayer.getUUID());
    }

    public static boolean shouldShowDroneMarkerInstead() {
        return AddonConfig.showDroneMarkerInstead() && isLocalDroneControlActive() && activeDroneForMarker() != null;
    }

    public static Entity activeDroneForMarker() {
        if (!AddonConfig.showDroneMarkerInstead()) {
            return null;
        }
        return DroneClientViewContext.activeDroneEntity(Minecraft.getInstance());
    }
}
