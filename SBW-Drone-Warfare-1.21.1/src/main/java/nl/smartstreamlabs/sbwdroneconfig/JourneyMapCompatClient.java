package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class JourneyMapCompatClient {
    private static final String JOURNEYMAP_RADAR_EVENT = "journeymap.client.api.event.forge.EntityRadarUpdateEvent";
    private static final String JOURNEYMAP_WRAPPED_ENTITY = "journeymap.client.api.model.WrappedEntity";
    private static Method getWrappedEntityMethod;
    private static Method getEntityLivingRefMethod;
    private static Method setDisableMethod;
    private static boolean hookRegistered;

    private JourneyMapCompatClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(JourneyMapCompatClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(JourneyMapCompatClient::registerJourneyMapEventHook);
    }

    public static boolean isJourneyMapLoaded() {
        return ModList.get().isLoaded("journeymap");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerJourneyMapEventHook() {
        if (hookRegistered || !isJourneyMapLoaded()) {
            return;
        }

        try {
            Class<?> eventClass = Class.forName(JOURNEYMAP_RADAR_EVENT);
            Class<?> wrappedEntityClass = Class.forName(JOURNEYMAP_WRAPPED_ENTITY);
            if (!Event.class.isAssignableFrom(eventClass)) {
                SbwDroneRangeConfig.LOGGER.warn("JourneyMap radar event is not a Forge Event; minimap marker hiding disabled.");
                return;
            }

            getWrappedEntityMethod = eventClass.getMethod("getWrappedEntity");
            getEntityLivingRefMethod = wrappedEntityClass.getMethod("getEntityLivingRef");
            setDisableMethod = wrappedEntityClass.getMethod("setDisable", boolean.class);
            NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, (Class) eventClass, (Consumer<Event>) JourneyMapCompatClient::onJourneyMapRadarEvent);
            hookRegistered = true;
            SbwDroneRangeConfig.LOGGER.info("JourneyMap minimap compat enabled for drone control player-marker hiding.");
        } catch (ReflectiveOperationException | LinkageError error) {
            SbwDroneRangeConfig.LOGGER.warn("JourneyMap minimap compat could not be enabled; player-marker hiding will fall back safely.", error);
        }
    }

    private static void onJourneyMapRadarEvent(Event event) {
        try {
            Object wrappedEntity = getWrappedEntityMethod.invoke(event);
            Object entityRef = getEntityLivingRefMethod.invoke(wrappedEntity);
            if (entityRef instanceof WeakReference<?> weakReference) {
                Object entity = weakReference.get();
                if (entity instanceof Entity mapEntity && shouldHidePlayerMarker(mapEntity)) {
                    setDisableMethod.invoke(wrappedEntity, true);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException error) {
            SbwDroneRangeConfig.LOGGER.debug("JourneyMap radar event handling failed safely.", error);
        }
    }

    public static boolean shouldHidePlayerMarker(Entity mapPlayer) {
        return isJourneyMapLoaded() && DroneMapCompatClient.shouldHidePlayerMarker(mapPlayer);
    }

    public static boolean shouldShowDroneMarker() {
        return isJourneyMapLoaded() && DroneMapCompatClient.shouldShowDroneMarkerInstead();
    }

    public static Entity activeDroneMarkerEntity() {
        return shouldShowDroneMarker() ? DroneMapCompatClient.activeDroneForMarker() : null;
    }
}
