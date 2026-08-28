package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FiberOpticCableSegmentRenderer extends EntityRenderer<FiberOpticCableSegmentEntity> {
    public FiberOpticCableSegmentRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(FiberOpticCableSegmentEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Legacy save-compatibility entity: never render a visible cable segment.
    }

    @Override
    public ResourceLocation getTextureLocation(FiberOpticCableSegmentEntity entity) {
        return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
    }
}
