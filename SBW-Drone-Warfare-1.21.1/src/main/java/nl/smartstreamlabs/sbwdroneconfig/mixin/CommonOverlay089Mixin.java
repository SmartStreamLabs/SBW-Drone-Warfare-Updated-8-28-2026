package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import nl.smartstreamlabs.sbwdroneconfig.CubedFpvReferenceGuiOverlay;
import nl.smartstreamlabs.sbwdroneconfig.LucasDroneHudOverlay;
import nl.smartstreamlabs.sbwdroneconfig.SbwCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.atsuishio.superbwarfare.client.overlay.CommonOverlay")
public abstract class CommonOverlay089Mixin {
    private static final String DRONE_HUD_OVERLAY_CLASS = "com.atsuishio.superbwarfare.client.overlay.DroneHudOverlay";

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void sbwdroneconfig$skipSbwCommonOverlayDuringDroneControl(
            GuiGraphics guiGraphics,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        Player player = Minecraft.getInstance().player;
        if (player != null && !SbwCompat.getActiveLinkedMonitor(player).isEmpty()) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldRender()Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void sbwdroneconfig$hideDefaultDroneHud089(CallbackInfoReturnable<Boolean> cir) {
        if (DRONE_HUD_OVERLAY_CLASS.equals(((Object) this).getClass().getName())
                && (CubedFpvReferenceGuiOverlay.shouldReplaceDefaultSbwHud() || LucasDroneHudOverlay.shouldReplaceDefaultSbwHud())) {
            cir.setReturnValue(false);
        }
    }
}
