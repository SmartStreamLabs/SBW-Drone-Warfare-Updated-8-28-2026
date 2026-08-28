package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

public final class AbandonedFiberCableRenderer {
    private AbandonedFiberCableRenderer() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(AbandonedFiberCableRenderer::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(AbandonedFiberCableRenderer.class);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || AbandonedFiberCableClient.cables().isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        double maxRenderDistance = AddonConfig.fiberCableMaxRenderDistance();
        int maxVisibleCables = AddonConfig.maxVisibleFiberCables();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.leash());
        Matrix4f matrix = poseStack.last().pose();

        int rendered = 0;
        for (AbandonedFiberCableData.CableRecord cable : AbandonedFiberCableClient.cables()) {
            if (rendered >= maxVisibleCables) {
                break;
            }
            if (FiberOpticCableRenderer.renderStaticCable(consumer, matrix, level, cameraPos, cable.points(), maxRenderDistance)) {
                rendered++;
            }
        }

        poseStack.popPose();
        buffers.endBatch(RenderType.leash());
    }
}
