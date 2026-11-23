package nl.smartstreamlabs.sbwdroneconfig.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import nl.smartstreamlabs.sbwdroneconfig.PortableFpvDetectorRenderContext;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.nonamecrackers2.simpleclouds.client.renderer.SimpleCloudsRenderer")
public abstract class SimpleCloudsRendererMixin {
    @Inject(
            method = {
                    "renderBeforeLevel(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V",
                    "renderAfterSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V",
                    "renderBeforeWeather(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V",
                    "renderAfterLevel(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void sbwdroneconfig$skipCloudPassDuringPortableFpvCapture(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (PortableFpvDetectorRenderContext.isOffscreenRendering()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderWeather(Lnet/minecraft/client/renderer/LightTexture;FDDD)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void sbwdroneconfig$skipWeatherDuringPortableFpvCapture(LightTexture lightTexture, float partialTick, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (PortableFpvDetectorRenderContext.isOffscreenRendering()) {
            ci.cancel();
        }
    }
}
