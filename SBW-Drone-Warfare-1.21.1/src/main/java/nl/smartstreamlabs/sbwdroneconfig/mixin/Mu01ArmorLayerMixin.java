package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.Mu01Client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=HumanoidArmorLayer.class,remap=false)
public abstract class Mu01ArmorLayerMixin<T extends LivingEntity,M extends HumanoidModel<T>> extends RenderLayer<T,M> {
 protected Mu01ArmorLayerMixin(RenderLayerParent<T,M> parent){super(parent);}
 @Inject(method="renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",at=@At("HEAD"),cancellable=true,require=1)
 private void mu01$armor(PoseStack pose,MultiBufferSource buffers,T wearer,EquipmentSlot slot,int light,HumanoidModel<?> original,float swing,float amount,float partial,float age,float yaw,float pitch,CallbackInfo ci){if(Mu01Client.render(pose,buffers,light,wearer,getParentModel(),slot)||nl.smartstreamlabs.sbwdroneconfig.Us01Client.render(pose,buffers,light,wearer,getParentModel(),slot))ci.cancel();}
}
