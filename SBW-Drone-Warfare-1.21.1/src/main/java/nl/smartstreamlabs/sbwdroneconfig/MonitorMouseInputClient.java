package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.event.ClientEventHandler;
import com.atsuishio.superbwarfare.network.message.send.MouseMoveMessage;
import com.atsuishio.superbwarfare.tools.EntityFindUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class MonitorMouseInputClient {
    private static final double DEFAULT_DRONE_MOUSE_SENSITIVITY = 0.4D;
    private static final double LERP_FACTOR = 0.3D;
    private static final float MAX_MOUSE_DELTA = 256.0F;

    private static Vec2 previousMousePos;
    private static double lerpSpeedX;
    private static double lerpSpeedY;

    private MonitorMouseInputClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(MonitorMouseInputClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(MonitorMouseInputClient.class);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            reset();
            return;
        }

        ItemStack monitor = player.getMainHandItem();
        if (!SbwCompat.isLegacyUsingMonitor(monitor)) {
            reset();
            return;
        }

        CompoundTag tag = AddonStackTags.get(monitor);
        String linkedDrone = tag.getString(SbwCompat.TAG_LINKED_DRONE);
        if (linkedDrone.isBlank()) {
            reset();
            return;
        }

        if (EntityFindUtil.findDrone(level, linkedDrone) != null) {
            reset();
            return;
        }

        if (notInGame(minecraft)) {
            reset();
            PacketDistributor.sendToServer(new MouseMoveMessage(0.0D, 0.0D));
            return;
        }

        Vec2 currentMousePos = currentMousePos(minecraft);
        if (previousMousePos == null) {
            previousMousePos = currentMousePos;
            return;
        }

        float moveSpeedX = Mth.clamp(currentMousePos.x - previousMousePos.x, -MAX_MOUSE_DELTA, MAX_MOUSE_DELTA);
        float moveSpeedY = Mth.clamp(currentMousePos.y - previousMousePos.y, -MAX_MOUSE_DELTA, MAX_MOUSE_DELTA);
        previousMousePos = currentMousePos;

        double fov = ClientEventHandler.droneFovLerp <= 0.0D ? 1.0D : ClientEventHandler.droneFovLerp;
        double speedX = (DEFAULT_DRONE_MOUSE_SENSITIVITY / fov) * moveSpeedX;
        double speedY = (DEFAULT_DRONE_MOUSE_SENSITIVITY / fov) * moveSpeedY;

        lerpSpeedX = Mth.lerp(LERP_FACTOR, lerpSpeedX, speedX);
        lerpSpeedY = Mth.lerp(LERP_FACTOR, lerpSpeedY, speedY);
        PacketDistributor.sendToServer(new MouseMoveMessage(lerpSpeedX, lerpSpeedY));
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

    private static void reset() {
        previousMousePos = null;
        lerpSpeedX = 0.0D;
        lerpSpeedY = 0.0D;
    }
}
