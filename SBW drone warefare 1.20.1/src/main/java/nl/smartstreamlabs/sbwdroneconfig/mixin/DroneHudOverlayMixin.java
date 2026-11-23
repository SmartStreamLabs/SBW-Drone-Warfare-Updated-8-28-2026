package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import nl.smartstreamlabs.sbwdroneconfig.CubedFpvReferenceGuiOverlay;
import nl.smartstreamlabs.sbwdroneconfig.DeliveryDroneHudOverlay;
import nl.smartstreamlabs.sbwdroneconfig.DroneThermalVisionClient;
import nl.smartstreamlabs.sbwdroneconfig.LucasDroneHudOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(targets = "com.atsuishio.superbwarfare.client.overlay.DroneHudOverlay")
public abstract class DroneHudOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sbwdroneconfig$replaceCubedFpvHud(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci) {
        if (nl.smartstreamlabs.sbwdroneconfig.DroneOsdClient.ownsFullView() || CubedFpvReferenceGuiOverlay.shouldReplaceDefaultSbwHud()
                || LucasDroneHudOverlay.shouldReplaceDefaultSbwHud()
                || DeliveryDroneHudOverlay.shouldReplaceDefaultSbwHud()) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/atsuishio/superbwarfare/tools/SeekTool;seekLivingEntities(Lnet/minecraft/world/entity/Entity;DD)Ljava/util/List;"
            ),
            remap = false,
            require = 0
    )
    private List<Entity> sbwdroneconfig$hideDefaultEntityBoxes(Entity source, double range, double angle) {
        if (DroneThermalVisionClient.shouldHideDefaultTargetBoxes()) {
            return Collections.emptyList();
        }
        return com.atsuishio.superbwarfare.tools.SeekTool.seekLivingEntities(source, range, angle);
    }
}
