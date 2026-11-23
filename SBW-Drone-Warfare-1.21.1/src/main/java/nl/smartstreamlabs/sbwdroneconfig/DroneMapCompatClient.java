package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.fml.ModList;
import java.lang.reflect.Method;
import java.util.UUID;

/** Only player UUIDs are filtered. Xaero retains its original rules for all visible elements. */
public final class DroneMapCompatClient {
    private static final ClassValue<java.util.Optional<Method>> IDS = new ClassValue<>() {
        @Override protected java.util.Optional<Method> computeValue(Class<?> type) {
            try {
                var method = type.getMethod("getPlayerId");
                return method.getReturnType() == UUID.class ? java.util.Optional.of(method) : java.util.Optional.empty();
            } catch (NoSuchMethodException ignored) { return java.util.Optional.empty(); }
        }
    };
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
        return PilotMapClient.hidden(Minecraft.getInstance().getCameraEntity());
    }
    public static boolean shouldHideXaeroWorldMapSelfMarker(Entity mapPlayer) { return PilotMapClient.hidden(mapPlayer); }
    public static boolean hiddenTracker(Object element) {
        if (element == null) return false;
        var getter = IDS.get(element.getClass());
        if (getter.isEmpty()) return false;
        try { return PilotMapClient.hidden((UUID)getter.get().invoke(element)); }
        catch (ReflectiveOperationException ignored) { return false; }
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
    private DroneMapCompatClient() {}
}
