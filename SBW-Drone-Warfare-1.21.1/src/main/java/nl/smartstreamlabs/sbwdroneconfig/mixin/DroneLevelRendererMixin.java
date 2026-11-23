package nl.smartstreamlabs.sbwdroneconfig.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import nl.smartstreamlabs.sbwdroneconfig.DroneClientViewContext;
import nl.smartstreamlabs.sbwdroneconfig.VatagClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Camera packets populate the cache; the render section grid must follow the same camera. */
@Mixin(value=LevelRenderer.class,remap=false)
public abstract class DroneLevelRendererMixin {
    @Redirect(remap=false,method="setupRender",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getX()D"))
    private double drone$x(LocalPlayer player) {
        Minecraft mc=Minecraft.getInstance();
        return ((nl.smartstreamlabs.sbwdroneconfig.Rd01Client.active()||VatagClient.active()||nl.smartstreamlabs.sbwdroneconfig.Id01Client.active())||DroneClientViewContext.activeDroneId(mc)!=null) ? mc.gameRenderer.getMainCamera().getPosition().x : player.getX();
    }
    @Redirect(remap=false,method="setupRender",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getY()D"))
    private double drone$y(LocalPlayer player) {
        Minecraft mc=Minecraft.getInstance();
        return ((nl.smartstreamlabs.sbwdroneconfig.Rd01Client.active()||VatagClient.active()||nl.smartstreamlabs.sbwdroneconfig.Id01Client.active())||DroneClientViewContext.activeDroneId(mc)!=null) ? mc.gameRenderer.getMainCamera().getPosition().y : player.getY();
    }
    @Redirect(remap=false,method="setupRender",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getZ()D"))
    private double drone$z(LocalPlayer player) {
        Minecraft mc=Minecraft.getInstance();
        return ((nl.smartstreamlabs.sbwdroneconfig.Rd01Client.active()||VatagClient.active()||nl.smartstreamlabs.sbwdroneconfig.Id01Client.active())||DroneClientViewContext.activeDroneId(mc)!=null) ? mc.gameRenderer.getMainCamera().getPosition().z : player.getZ();
    }
}
