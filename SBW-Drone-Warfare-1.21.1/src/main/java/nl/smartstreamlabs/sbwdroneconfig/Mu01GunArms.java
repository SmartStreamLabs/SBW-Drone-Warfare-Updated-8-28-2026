package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.api.event.RenderPlayerArmEvent;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.*;
import java.util.*;
/** SBW supplies the animated hand bone and its exact transform for its separate gun-arm renderer. */
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class Mu01GunArms {
 private static final ModelPart ARM=new ModelPart(List.of(),Map.of());
 @net.neoforged.bus.api.SubscribeEvent
 public static void render(RenderPlayerArmEvent e){
  var wearer=e.getLocalPlayer();if(wearer==null||!Mu01Client.wearing(wearer,EquipmentSlot.CHEST)||!e.getTransformType().firstPerson())return;
  boolean right=e.getArm()==HumanoidArm.RIGHT;var poses=e.getStack();poses.pushPose();
  try{
   // Match SBW's own left/right offsets and both supported hand-bone conventions once.
   poses.translate(right?1d/16d:-1d/16d,2d/16d,0);
   // Final exposes Java static helpers; 0.8.9.1 changes their Kotlin receiver ABI.
   // These are the exact two native SBW pose conventions, verified from both distributions.
   boolean legacy=e.isUseOldHandRender();var bone=e.getBone();
   ARM.setPos(bone.getPivotX(),bone.getPivotY()+(legacy?0:7),bone.getPivotZ());
   ARM.xRot=0;ARM.yRot=legacy?0:(float)Math.PI;ARM.zRot=legacy?0:(float)Math.PI;
   Mu01Client.hand(poses,e.getCurrentBuffer(),e.getPackedLightIn(),wearer,ARM,right);
   e.setCanceled(true);
  }finally{poses.popPose();}
 }
}
