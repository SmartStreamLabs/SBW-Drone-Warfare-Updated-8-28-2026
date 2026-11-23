package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.Us01Client;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.*;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Keep the real skin's outer layer visible, but inside the approved clothing shells. */
@Mixin(value=PlayerModel.class,remap=false)
public abstract class Us01SkinFitMixin extends HumanoidModel<LivingEntity> {
 @Shadow public ModelPart rightSleeve,leftSleeve,rightPants,leftPants,jacket;
 protected Us01SkinFitMixin(ModelPart root){super(root);}
 @Inject(method="setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",at=@At("TAIL"),require=1)
 private void us01$fit(LivingEntity e,float swing,float amount,float age,float yaw,float pitch,CallbackInfo ci){
  if(Us01Client.wearing(e,EquipmentSlot.HEAD))fit(hat,0,-4,0,8.01f/9,8.01f/9,8.01f/9);
  if(Us01Client.wearing(e,EquipmentSlot.CHEST)){
   fit(jacket,0,6,0,8.01f/8.5f,12.01f/12.5f,4.01f/4.5f);
   boolean slim=Us01Client.slim(e);float center=slim?.5f:1f,width=slim?3.5f:4.5f,scale=slim?3.01f/width:4.01f/width;
   fit(rightSleeve,-center,4,0,scale,12.01f/12.5f,4.01f/4.5f);fit(leftSleeve,center,4,0,scale,12.01f/12.5f,4.01f/4.5f);
  }
  if(Us01Client.wearing(e,EquipmentSlot.LEGS)||Us01Client.wearing(e,EquipmentSlot.FEET)){fit(rightPants,0,6,0,4.01f/4.5f,12.01f/12.5f,4.01f/4.5f);fit(leftPants,0,6,0,4.01f/4.5f,12.01f/12.5f,4.01f/4.5f);}
 }
 private static void fit(ModelPart p,float x,float y,float z,float sx,float sy,float sz){var offset=new Vector3f(x*(1-sx)*p.xScale,y*(1-sy)*p.yScale,z*(1-sz)*p.zScale).rotate(new Quaternionf().rotationZYX(p.zRot,p.yRot,p.xRot));p.x+=offset.x;p.y+=offset.y;p.z+=offset.z;p.xScale*=sx;p.yScale*=sy;p.zScale*=sz;}
}
