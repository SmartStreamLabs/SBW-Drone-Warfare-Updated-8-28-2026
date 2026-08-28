package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public final class CubedFpvInputClient {
    private static final double DEFAULT_DRONE_MOUSE_SENSITIVITY = 0.4D;
    private static final double LERP_FACTOR = 0.3D;
    private static final float MAX_MOUSE_DELTA = 256.0F;

    private static int sequence;
    private static Vec2 previousMousePos;
    private static double lerpSpeedX;
    private static double lerpSpeedY;

    private CubedFpvInputClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(CubedFpvInputClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(CubedFpvInputClient.class);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            resetMouse();
            return;
        }

        ItemStack monitor = DroneClientViewContext.activeMonitor(minecraft);
        UUID linkedDroneId = SbwCompat.getLinkedDroneUuid(monitor);
        if (linkedDroneId == null || !SbwCompat.isUsingLinkedMonitor(monitor)) {
            resetMouse();
            return;
        }

        Entity activeDrone = DroneClientViewContext.activeDroneEntity(minecraft);
        if (activeDrone instanceof GlideBombEntity) {
            resetMouse();
            return;
        }
        if (activeDrone != null && !(activeDrone instanceof CubedFpvDroneEntity)) {
            resetMouse();
            return;
        }

        MouseDelta mouse = notInGame(minecraft)
                ? MouseDelta.ZERO
                : readMouseDelta(minecraft, activeDrone);
        AddonNetwork.sendCubedFpvInput(new CubedFpvInputMessage(
                linkedDroneId,
                readMovementKeys(minecraft),
                mouse.x,
                mouse.y,
                sequence++
        ));
    }

    private static short readMovementKeys(Minecraft minecraft) {
        short keys = 0;
        var options = minecraft.options;
        if (options.keyLeft.isDown()) {
            keys |= 0b000000001;
        }
        if (options.keyRight.isDown()) {
            keys |= 0b000000010;
        }
        if (options.keyUp.isDown()) {
            keys |= 0b000000100;
        }
        if (options.keyDown.isDown()) {
            keys |= 0b000001000;
        }
        if (options.keyJump.isDown()) {
            keys |= 0b000010000;
        }
        if (options.keyShift.isDown()) {
            keys |= 0b000100000;
        }
        if (options.keySprint.isDown()) {
            keys |= 0b100000000;
        }
        return keys;
    }

    private static MouseDelta readMouseDelta(Minecraft minecraft, Entity activeDrone) {
        Vec2 currentMousePos = currentMousePos(minecraft);
        if (previousMousePos == null) {
            previousMousePos = currentMousePos;
            return MouseDelta.ZERO;
        }

        float moveSpeedX = Mth.clamp(currentMousePos.x - previousMousePos.x, -MAX_MOUSE_DELTA, MAX_MOUSE_DELTA);
        float moveSpeedY = Mth.clamp(currentMousePos.y - previousMousePos.y, -MAX_MOUSE_DELTA, MAX_MOUSE_DELTA);
        previousMousePos = currentMousePos;

        double fov = ClientEventHandler.droneFovLerp <= 0.0D ? 1.0D : ClientEventHandler.droneFovLerp;
        double sensitivity = activeDrone instanceof CubedFpvDroneEntity drone
                ? drone.getMouseSensitivity()
                : DEFAULT_DRONE_MOUSE_SENSITIVITY;
        double speedX = (sensitivity / fov) * moveSpeedX;
        double speedY = (sensitivity / fov) * moveSpeedY;

        lerpSpeedX = Mth.lerp(LERP_FACTOR, lerpSpeedX, speedX);
        lerpSpeedY = Mth.lerp(LERP_FACTOR, lerpSpeedY, speedY);
        return new MouseDelta(lerpSpeedX, lerpSpeedY);
    }

    private static boolean notInGame(Minecraft minecraft) {
        return minecraft.getOverlay() != null
                || minecraft.screen != null
                || !minecraft.mouseHandler.isMouseGrabbed()
                || !minecraft.isWindowActive();
    }

    private static Vec2 currentMousePos(Minecraft minecraft) {
        MouseHandler mouseHandler = minecraft.mouseHandler;
        if (mouseHandler.isMouseGrabbed()) {
            return new Vec2((float) mouseHandler.xpos(), (float) mouseHandler.ypos());
        }

        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(minecraft.getWindow().getWindow(), x, y);
        return new Vec2((float) x[0], (float) y[0]);
    }

    private static void resetMouse() {
        previousMousePos = null;
        lerpSpeedX = 0.0D;
        lerpSpeedY = 0.0D;
    }

    private record MouseDelta(double x, double y) {
        private static final MouseDelta ZERO = new MouseDelta(0.0D, 0.0D);
    }
}
