package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RecoverableFiberOpticCableRenderer extends EntityRenderer<RecoverableFiberOpticCableEntity> {
    public RecoverableFiberOpticCableRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(RecoverableFiberOpticCableEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ItemStack recoveredSpool = entity.getRecoveredSpool();
        if (recoveredSpool.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.03D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.scale(1.1F, 1.1F, 1.1F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                recoveredSpool,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RecoverableFiberOpticCableEntity entity) {
        return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
    }
}
