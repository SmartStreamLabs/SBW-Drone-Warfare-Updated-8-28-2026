package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;import net.minecraft.client.model.*;import net.minecraft.client.model.geom.ModelPart;import net.minecraft.client.player.AbstractClientPlayer;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.ItemRenderer;import net.minecraft.client.renderer.texture.OverlayTexture;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.entity.*;import net.minecraft.world.item.ItemStack;
/** Follows the actual already-animated parent player; the demonstration animation is never used. */
public final class Us01Client{
 private static final ResourceLocation TEXTURE=ResourceLocation.tryParse("sbwdroneconfig:us01/us01_atlas.png");
 public static boolean slim(LivingEntity entity){return Mu01Client.slim(entity);}
 public static boolean wearing(LivingEntity e,EquipmentSlot slot){return e.getItemBySlot(slot).getItem()instanceof Us01ArmorItem a&&a.usSlot==slot;}
 public static boolean render(PoseStack poses,MultiBufferSource buffers,int light,LivingEntity entity,HumanoidModel<?> model,EquipmentSlot slot){if(!wearing(entity,slot))return false;String piece=switch(slot){case HEAD->"head";case CHEST->"chest";case LEGS->"legs";case FEET->"feet";default->null;};if(piece==null)return false;draw(poses,buffers,light,entity.getItemBySlot(slot),Us01Meshes.get(piece+(Mu01Client.slim(entity)?"_slim":"_classic")),new ModelPart[]{model.head,model.body,model.rightArm,model.leftArm,model.rightLeg,model.leftLeg},-1);return true;}
 public static void hand(PoseStack poses,MultiBufferSource buffers,int light,AbstractClientPlayer entity,ModelPart arm,boolean right){if(!wearing(entity,EquipmentSlot.CHEST))return;ModelPart[]parts=new ModelPart[6];parts[right?2:3]=arm;draw(poses,buffers,light,entity.getItemBySlot(EquipmentSlot.CHEST),Us01Meshes.get("chest"+(Mu01Client.slim(entity)?"_slim":"_classic")),parts,right?2:3);}
 private static void draw(PoseStack poses,MultiBufferSource buffers,int light,ItemStack stack,Us01Meshes.Piece piece,ModelPart[]parts,int only){var type=RenderType.armorCutoutNoCull(TEXTURE);var consumer=ItemRenderer.getArmorFoilBuffer(buffers,type,false,stack.hasFoil());var batch=only<0&&!stack.hasFoil()&&buffers instanceof MultiBufferSource.BufferSource&&consumer instanceof BufferBuilder?Adv01Gpu.batch(piece.model()):null;if(batch!=null)batch.begin();
  for(var mesh:piece.meshes()){var part=parts[mesh.limb()];boolean visible=(only<0||only==mesh.limb())&&part!=null&&part.visible&&!part.skipDraw&&Us01Config.visible(mesh.optional());if(!visible&&batch==null)continue;poses.pushPose();if(visible)part.translateAndRotate(poses);else poses.last().pose().mul(new org.joml.Matrix4f().scaling(0f));if(batch!=null)batch.pose(mesh.cube(),poses);else cpu(poses,consumer,MeshRenderData.get(mesh.cube()),light);poses.popPose();}if(batch!=null)batch.draw(type,light,OverlayTexture.NO_OVERLAY,1,1,1,1);
 }
 private static void cpu(PoseStack poses,VertexConsumer consumer,MeshRenderData data,int light){
  var m=poses.last().pose();var n=poses.last().normal();var v=data.values;int overlay=OverlayTexture.NO_OVERLAY;
  if(MeshBulkWriter.draw(consumer,data,m,n,light,overlay,1,1,1,1))return;
  for(int at=0;at<v.length;at+=23){float a=v[at],b=v[at+1],c=v[at+2];float nx=n.m00()*a+n.m10()*b+n.m20()*c,ny=n.m01()*a+n.m11()*b+n.m21()*c,nz=n.m02()*a+n.m12()*b+n.m22()*c;
   for(int i=at+3;i<at+23;i+=5){float x=v[i],y=v[i+1],z=v[i+2];float px=m.m00()*x+m.m10()*y+m.m20()*z+m.m30(),py=m.m01()*x+m.m11()*y+m.m21()*z+m.m31(),pz=m.m02()*x+m.m12()*y+m.m22()*z+m.m32();consumer.vertex(px,py,pz,1,1,1,1,v[i+3],v[i+4],overlay,light,nx,ny,nz);}
  }
 }
}
