package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class DroneMapCompatClient {
    private DroneMapCompatClient() {
    }

    public static boolean isLocalDroneControlActive() {
        if (!ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();
        return SbwCompat.isUsingLinkedMonitor(stack);
    }

    public static boolean shouldHideXaeroMinimapSelfMarker() {
        return isLocalDroneControlActive();
    }

    public static boolean shouldHideXaeroWorldMapSelfMarker(Entity mapPlayer) {
        if (!isLocalDroneControlActive()) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        return localPlayer != null && mapPlayer != null && localPlayer.getUUID().equals(mapPlayer.getUUID());
    }
}
