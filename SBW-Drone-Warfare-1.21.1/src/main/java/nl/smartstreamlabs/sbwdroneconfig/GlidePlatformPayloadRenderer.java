package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
/** One real loaded GB-01. Undo its free-flight OBJ fit; no item display transform. */
public final class GlidePlatformPayloadRenderer {
 private GlidePlatformPayloadRenderer(){}
 private static final double OBJ_SCALE;private static final double[] OBJ_OFFSET=new double[3];
 static {var fit=GlidePlatformLayout.readJson("lp03_payload_visual.json");OBJ_SCALE=fit.get("obj_fit_scale").getAsDouble();for(int i=0;i<3;i++)OBJ_OFFSET[i]=fit.getAsJsonArray("obj_fit_translation_blocks").get(i).getAsDouble();}
 private static void attachmentPose(PoseStack pose,float yaw,float pitch,float roll){pose.mulPose(Axis.YP.rotationDegrees(-yaw));pose.mulPose(Axis.XP.rotationDegrees(pitch));pose.mulPose(Axis.ZP.rotationDegrees(-roll));float inverse=(float)(GlidePlatformLayout.FIT_SCALE/OBJ_SCALE);pose.scale(inverse,inverse,inverse);pose.translate(-OBJ_OFFSET[0],-OBJ_OFFSET[1],-OBJ_OFFSET[2]);}

 public static void render(ItemStack stack,Level level,float yaw,float pitch,float roll,PoseStack pose,MultiBufferSource buffers,int light){
  pose.pushPose();attachmentPose(pose,yaw,pitch,roll);
  // The OBJ already includes native Y=180. Only its free-flight scale/translation are compensated here.
  var renderer=Minecraft.getInstance().getItemRenderer();var model=renderer.getModel(stack,level,null,0);
  renderer.renderModelLists(model,stack,light,OverlayTexture.NO_OVERLAY,pose,buffers.getBuffer(Sheets.cutoutBlockSheet()));pose.popPose();
 }
 /** Transient visual bridge after the entire payload has cleared the rack. At the endpoint this is the existing renderer's exact matrix. */
 public static void renderBridge(ItemStack stack,Level level,float yaw,float pitch,float roll,float legacyYaw,float legacyPitch,float legacyRoll,float bodyScale,float bodyLift,float blend,PoseStack pose,MultiBufferSource buffers,int light){
  var renderer=Minecraft.getInstance().getItemRenderer();var model=renderer.getModel(stack,level,null,0);
  PoseStack target=new PoseStack();target.last().pose().set(pose.last().pose());target.last().normal().set(pose.last().normal());
  target.scale(bodyScale,bodyScale,bodyScale);target.translate(0,bodyLift,0);target.mulPose(Axis.YP.rotationDegrees(legacyYaw));target.mulPose(Axis.XP.rotationDegrees(legacyPitch));target.mulPose(Axis.ZP.rotationDegrees(legacyRoll));
  var legacy=model.applyTransform(net.minecraft.world.item.ItemDisplayContext.FIXED,target,false);target.translate(-.5,-.5,-.5);
  pose.pushPose();attachmentPose(pose,yaw,pitch,roll);pose.last().pose().lerp(target.last().pose(),blend);pose.last().normal().lerp(target.last().normal(),blend);
  renderer.renderModelLists(legacy,stack,light,OverlayTexture.NO_OVERLAY,pose,buffers.getBuffer(Sheets.cutoutBlockSheet()));pose.popPose();
 }
}
