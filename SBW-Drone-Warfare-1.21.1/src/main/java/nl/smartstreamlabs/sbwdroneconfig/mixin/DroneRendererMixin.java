package nl.smartstreamlabs.sbwdroneconfig.mixin;

import com.atsuishio.superbwarfare.client.renderer.entity.DroneRenderer;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DroneRenderer.class)
public abstract class DroneRendererMixin {
    @Inject(
            method = "render(Lcom/atsuishio/superbwarfare/entity/vehicle/DroneEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER, remap = false),
            remap = false
    )
    private void sbwdroneconfig$scaleDrone(DroneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo ci) {
        poseStack.scale(1.9F, 1.9F, 1.9F);
        poseStack.translate(0.0D, 0.05D, 0.0D);
    }
}
