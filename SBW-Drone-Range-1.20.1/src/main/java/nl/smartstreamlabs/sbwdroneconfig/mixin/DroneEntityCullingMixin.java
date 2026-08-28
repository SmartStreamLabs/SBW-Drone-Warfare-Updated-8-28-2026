package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import nl.smartstreamlabs.sbwdroneconfig.SbwCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class DroneEntityCullingMixin {
    @Inject(
            method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void sbwdroneconfig$expandDroneVisualCullingBounds(Entity drone, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> callback) {
        if (!SbwCompat.isDrone(drone)) {
            return;
        }

        // This only covers the renderer's scaled model and payloads, not collision.
        AABB visualBounds = drone.getBoundingBox().inflate(1.25D, 0.75D, 1.25D);
        // Keep normal frustum culling, but do not use the tiny collision box to
        // distance-cull a model that is deliberately rendered much larger.
        callback.setReturnValue(drone.isAlive() && !drone.isRemoved() && frustum.isVisible(visualBounds));
    }
}
