package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import nl.smartstreamlabs.sbwdroneconfig.WhiteHotRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=EntityRenderDispatcher.class,remap=false)
public abstract class WhiteHotEntityMixin {
    @ModifyVariable(method="render",at=@At("HEAD"),argsOnly=true) private MultiBufferSource drone$thermalVertices(MultiBufferSource source,net.minecraft.world.entity.Entity entity,double x,double y,double z,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,MultiBufferSource original,int light){return entity==nl.smartstreamlabs.sbwdroneconfig.DroneOsdClient.fullView()?source:WhiteHotRender.wrap(source);}
}
