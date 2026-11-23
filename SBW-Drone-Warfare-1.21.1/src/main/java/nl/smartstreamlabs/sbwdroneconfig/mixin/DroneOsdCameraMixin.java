package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.renderer.*;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import nl.smartstreamlabs.sbwdroneconfig.DroneOsdClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=LevelRenderer.class,remap=false)
public abstract class DroneOsdCameraMixin {
    @Unique private final Matrix4f osd$view=new Matrix4f();
    @Inject(method="renderLevel",at=@At("HEAD")) private void osd$capture(net.minecraft.client.DeltaTracker delta,boolean outline,Camera camera,GameRenderer renderer,LightTexture light,Matrix4f frustum,Matrix4f projection,CallbackInfo ci){
        osd$view.set(RenderSystem.getModelViewMatrix()).mul(frustum);DroneOsdClient.capture(osd$view,projection);nl.smartstreamlabs.sbwdroneconfig.Adv01Client.capture(osd$view,projection);
    }
}
