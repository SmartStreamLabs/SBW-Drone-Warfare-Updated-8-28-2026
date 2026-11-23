package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class FpvGogglesOverlayClient {
    private static final String LINK_LABEL = "FPV LINK";

    private FpvGogglesOverlayClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(FpvGogglesOverlayClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(FpvGogglesOverlayClient.class);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) { if(DroneOsdClient.ownsFullView())return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (!shouldRender(player)) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        renderVignette(graphics, width, height);
        renderVisorFrame(graphics, width, height);
        renderScanlines(graphics, width, height);
        renderLinkLabel(graphics, minecraft, width);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static boolean shouldRender(LocalPlayer player) {
        if (player == null || !player.isAlive()) {
            return false;
        }

        ItemStack goggles = SbwCompat.getEquippedFpvGoggles(player);
        if (goggles.isEmpty()) {
            return false;
        }
        if (DroneClientViewContext.isActiveDeliveryView()) {
            return false;
        }

        ItemStack mainHand = player.getMainHandItem();
        return SbwCompat.isLegacyUsingMonitor(mainHand);
    }

    private static void renderVignette(GuiGraphics graphics, int width, int height) {
        int edge = Math.max(20, Math.min(width, height) / 13);
        graphics.fill(0, 0, width, edge, 0x44000000);
        graphics.fill(0, height - edge, width, height, 0x4A000000);
        graphics.fill(0, 0, edge, height, 0x36000000);
        graphics.fill(width - edge, 0, width, height, 0x36000000);

        int soft = Math.max(10, edge / 2);
        graphics.fill(edge, 0, width - edge, soft, 0x16000000);
        graphics.fill(edge, height - soft, width - edge, height, 0x18000000);
        graphics.fill(0, edge, soft, height - edge, 0x12000000);
        graphics.fill(width - soft, edge, width, height - edge, 0x12000000);
    }

    private static void renderVisorFrame(GuiGraphics graphics, int width, int height) {
        int frame = Math.max(8, Math.min(width, height) / 44);
        int insetX = Math.max(10, width / 52);
        int insetY = Math.max(8, height / 48);
        int left = insetX;
        int top = insetY;
        int right = width - insetX;
        int bottom = height - insetY;

        graphics.fill(0, 0, width, frame, 0x66000000);
        graphics.fill(0, height - frame, width, height, 0x70000000);
        graphics.fill(0, 0, frame, height, 0x66000000);
        graphics.fill(width - frame, 0, width, height, 0x66000000);

        int borderColor = 0x3E7FFFB4;
        int corner = Math.max(26, Math.min(width, height) / 11);
        drawCorner(graphics, left, top, corner, 1, 1, borderColor);
        drawCorner(graphics, right, top, corner, -1, 1, borderColor);
        drawCorner(graphics, left, bottom, corner, 1, -1, borderColor);
        drawCorner(graphics, right, bottom, corner, -1, -1, borderColor);

        int bridgeWidth = Math.max(28, width / 18);
        int bridgeHeight = Math.max(12, height / 42);
        int centerX = width / 2;
        graphics.fill(centerX - bridgeWidth, top, centerX + bridgeWidth, top + 2, 0x2A7FFFB4);
        graphics.fill(centerX - 1, top + 2, centerX + 1, top + bridgeHeight, 0x207FFFB4);
    }

    private static void drawCorner(GuiGraphics graphics, int x, int y, int length, int directionX, int directionY, int color) {
        int horizontalEnd = x + (length * directionX);
        int verticalEnd = y + (length * directionY);
        graphics.fill(Math.min(x, horizontalEnd), y - 1, Math.max(x, horizontalEnd), y + 1, color);
        graphics.fill(x - 1, Math.min(y, verticalEnd), x + 1, Math.max(y, verticalEnd), color);
    }

    private static void renderScanlines(GuiGraphics graphics, int width, int height) {
        int color = 0x0E8AFFC4;
        for (int y = 6; y < height; y += 6) {
            graphics.fill(0, y, width, y + 1, color);
        }
    }

    private static void renderLinkLabel(GuiGraphics graphics, Minecraft minecraft, int width) {
        int x = (width - minecraft.font.width(LINK_LABEL)) / 2;
        graphics.drawString(minecraft.font, LINK_LABEL, x, 8, 0x9C82FFC0, true);
    }
}
