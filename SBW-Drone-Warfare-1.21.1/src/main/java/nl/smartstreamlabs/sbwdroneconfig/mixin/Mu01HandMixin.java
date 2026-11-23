package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.Mu01Client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=PlayerRenderer.class,remap=false)
public abstract class Mu01HandMixin extends net.minecraft.client.renderer.entity.LivingEntityRenderer<AbstractClientPlayer,net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> {
 protected Mu01HandMixin(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context,net.minecraft.client.model.PlayerModel<AbstractClientPlayer> model,float shadow){super(context,model,shadow);}
 @Inject(method="renderHand",at=@At("TAIL"),require=1)
 private void mu01$hand(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,ModelPart arm,ModelPart sleeve,CallbackInfo ci){Mu01Client.hand(pose,buffers,light,player,arm,arm==getModel().rightArm);nl.smartstreamlabs.sbwdroneconfig.Us01Client.hand(pose,buffers,light,player,arm,arm==getModel().rightArm);}
}
