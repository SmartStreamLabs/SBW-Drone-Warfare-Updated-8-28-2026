package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

public final class CubedFpvCameraEffectsClient {
    private static final double FPV_ZOOM_FOV = 45.0D;
    private static final double FPV_ZOOM_LERP = 0.32D;
    private static final KeyMapping ZOOM_FPV_CAMERA = new KeyMapping(
            "key.sbwdroneconfig.zoom_fpv_camera",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.sbwdroneconfig"
    );

    private static int tickCounter;
    private static double previousSpeed;
    private static float vibrationStrength;
    private static double currentFov = Double.NaN;

    private CubedFpvCameraEffectsClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(CubedFpvCameraEffectsClient::onClientSetup);
        modBus.addListener(CubedFpvCameraEffectsClient::onRegisterKeyMappings);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(CubedFpvCameraEffectsClient.class);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ZOOM_FPV_CAMERA);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        tickCounter++;
        CubedFpvDroneEntity drone = DroneClientViewContext.activeCubedFpvDrone(Minecraft.getInstance());
        if (drone == null) {
            previousSpeed = 0.0D;
            vibrationStrength = 0.0F;
            currentFov = Double.NaN;
            return;
        }

        updateFpvZoom();
        double speed = drone.getDeltaMovement().length();
        double acceleration = Math.abs(speed - previousSpeed);
        previousSpeed = speed;
        float throttleShake = (float) Math.min(1.0D, speed / 0.45D);
        float accelerationShake = (float) Math.min(1.0D, acceleration * 8.0D);
        vibrationStrength = (vibrationStrength * 0.78F) + ((throttleShake * 0.18F + accelerationShake * 0.42F) * 0.22F);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (DroneClientViewContext.activeCubedFpvDrone(Minecraft.getInstance()) == null) {
            return;
        }

        event.setFOV(currentFpvFov());
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (DroneClientViewContext.activeCubedFpvDrone(Minecraft.getInstance()) == null) {
            return;
        }

        float vibration = AddonConfig.enableFpvCameraVibration() ? vibrationStrength : 0.0F;
        double time = tickCounter + event.getPartialTick();
        event.setPitch(event.getPitch() + (float) AddonConfig.fpvCameraTiltDegrees() + (float) (Math.sin(time * 1.65D) * vibration * 0.85F));
        event.setYaw(event.getYaw() + (float) (Math.sin(time * 2.20D) * vibration * 0.45F));
        event.setRoll(event.getRoll() + (float) (Math.sin(time * 1.15D) * vibration * 0.80F));
    }

    private static void updateFpvZoom() {
        double baseFov = baseFpvFov();
        if (Double.isNaN(currentFov)) {
            currentFov = baseFov;
        }

        double targetFov = ZOOM_FPV_CAMERA.isDown() ? FPV_ZOOM_FOV : baseFov;
        currentFov += (targetFov - currentFov) * FPV_ZOOM_LERP;
    }

    private static double currentFpvFov() {
        return Double.isNaN(currentFov) ? baseFpvFov() : currentFov;
    }

    private static double baseFpvFov() {
        return CubedFpvCameraViewMath.clampFov(AddonConfig.fpvCameraFov());
    }
}
