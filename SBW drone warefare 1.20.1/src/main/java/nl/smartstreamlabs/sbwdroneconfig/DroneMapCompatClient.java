package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
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
        var player = Minecraft.getInstance().player;
        return player != null && PilotMapClient.hidden(player.getUUID());
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
    private DroneMapCompatClient() {}
}
