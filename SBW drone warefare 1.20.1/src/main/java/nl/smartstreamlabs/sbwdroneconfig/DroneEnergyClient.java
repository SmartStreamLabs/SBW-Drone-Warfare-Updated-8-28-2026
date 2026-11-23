package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Locale;
import java.util.UUID;

public final class DroneEnergyClient {
    private static EnergyState state;

    private DroneEnergyClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DroneEnergyClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneEnergyClient.class);
    }

    public static void handleSync(DroneEnergySyncMessage message) {
        state = new EnergyState(
                message.droneId(),
                message.currentEnergy(),
                message.maxEnergy(),
                message.outOfEnergy(),
                message.lowEnergy()
        );
    }

    public static double getBatteryVoltageFor(UUID droneId) {
        if (state == null || droneId == null || !droneId.equals(state.droneId) || state.maxEnergy <= 0) {
            return Double.NaN;
        }

        double charge = Math.max(0.0D, Math.min(1.0D, state.currentEnergy / (double) state.maxEnergy));
        return 14.0D + (charge * 2.8D);
    }

    public static double getBatteryPercentFor(UUID droneId) {
        if (state == null || droneId == null || !droneId.equals(state.droneId) || state.maxEnergy <= 0) {
            return Double.NaN;
        }

        return Math.max(0.0D, Math.min(100.0D, state.currentEnergy * 100.0D / state.maxEnergy));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !isActiveDroneView(minecraft.player)) {
            return;
        }
        if (state != null && state.maxEnergy > 0 && state.currentEnergy > state.maxEnergy) {
            state = new EnergyState(state.droneId, state.maxEnergy, state.maxEnergy, state.outOfEnergy, state.lowEnergy);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) { if(DroneOsdClient.ownsFullView())return;
        if (!shouldRenderHud() || event.getOverlay() == null || !"hotbar".equals(event.getOverlay().id().getPath())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Component base = Component.translatable("overlay.sbwdroneconfig.energy", state.currentEnergy, state.maxEnergy);
        String suffix = state.outOfEnergy
                ? Component.translatable("overlay.sbwdroneconfig.energy_empty").getString()
                : state.lowEnergy ? Component.translatable("overlay.sbwdroneconfig.energy_low").getString() : "";
        Component text = suffix.isBlank() ? base : Component.literal(base.getString() + " " + suffix);
        int x = 10;
        int y = 10;
        int color = state.outOfEnergy ? 0xFF3B30 : state.lowEnergy ? 0xFFB000 : 0x8CFF5A;
        guiGraphics.drawString(minecraft.font, text, x, y, color, true);

        Component distanceText = getDistanceText(minecraft.player.getMainHandItem(), minecraft.player);
        if (distanceText != null) {
            guiGraphics.drawString(minecraft.font, distanceText, x, y + 12, 0xFFFFFF, true);
        }
    }

    private static boolean shouldRenderHud() {
        if (!AddonConfig.enableBatterySystem() || !ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID) || state == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !isActiveDroneView(player)) {
            return false;
        }
        if (DroneClientViewContext.isActiveCubedFpvView() || DroneClientViewContext.isActiveLucasView()) {
            return false;
        }
        UUID linkedDrone = SbwCompat.getLinkedDroneUuid(player.getMainHandItem());
        return linkedDrone != null && linkedDrone.equals(state.droneId);
    }

    private static boolean isActiveDroneView(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        return SbwCompat.isUsingLinkedMonitor(stack);
    }

    private static Component getDistanceText(ItemStack stack, LocalPlayer player) {
        if (!stack.hasTag()) {
            return null;
        }

        if (!stack.getTag().contains("PosX") || !stack.getTag().contains("PosY") || !stack.getTag().contains("PosZ")) {
            return null;
        }

        double dx = stack.getTag().getDouble("PosX") - player.getX();
        double dy = stack.getTag().getDouble("PosY") - player.getY();
        double dz = stack.getTag().getDouble("PosZ") - player.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return Component.translatable(
                "overlay.sbwdroneconfig.distance",
                String.format(Locale.ROOT, "%.1f", distance)
        );
    }

    private record EnergyState(UUID droneId, int currentEnergy, int maxEnergy, boolean outOfEnergy, boolean lowEnergy) {
    }
}
