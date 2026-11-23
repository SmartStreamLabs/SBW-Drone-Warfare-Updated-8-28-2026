package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
/** Render in the native armor layer, using the actual parent player's already posed limbs. */
public final class Mu01Client {
 private static final ResourceLocation[] TEXTURES={ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"textures/armor/mu01_ukraine_atlas.png"),ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"textures/armor/mu01_mask_cloth_v3.png")};
 public static boolean slim(LivingEntity wearer){return wearer instanceof AbstractClientPlayer p&&p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM;}
 public static boolean wearing(LivingEntity e,EquipmentSlot slot){return e.getItemBySlot(slot).getItem() instanceof Mu01ArmorItem item&&item.muSlot==slot;}
 public static boolean render(PoseStack poses,MultiBufferSource buffers,int light,LivingEntity entity,HumanoidModel<?> model,EquipmentSlot slot){
  if(!wearing(entity,slot))return false;
  String name=switch(slot){case HEAD->"helmet";case CHEST->slim(entity)?"vest_slim":"vest";case LEGS->"trousers";case FEET->"boots";default->null;};if(name==null)return false;
  draw(poses,buffers,light,entity.getItemBySlot(slot),Mu01Meshes.get(name),new ModelPart[]{model.head,model.body,model.rightArm,model.leftArm,model.rightLeg,model.leftLeg},-1);return true;
 }
 public static void hand(PoseStack poses,MultiBufferSource buffers,int light,AbstractClientPlayer entity,ModelPart arm,boolean right){
  if(!wearing(entity,EquipmentSlot.CHEST))return;
  ModelPart[] parts=new ModelPart[6];parts[right?2:3]=arm;draw(poses,buffers,light,entity.getItemBySlot(EquipmentSlot.CHEST),Mu01Meshes.get(slim(entity)?"vest_slim":"vest"),parts,right?2:3);
 }
 private static void draw(PoseStack poses,MultiBufferSource buffers,int light,ItemStack stack,Mu01Meshes.Piece piece,ModelPart[] parts,int only){
  for(var material:piece.models().entrySet()){
   int texture=material.getKey();var type=RenderType.armorCutoutNoCull(TEXTURES[texture]);var consumer=ItemRenderer.getArmorFoilBuffer(buffers,type,stack.hasFoil());
   var batch=only<0&&!stack.hasFoil()&&buffers instanceof MultiBufferSource.BufferSource&&consumer instanceof BufferBuilder?Adv01Gpu.batch(material.getValue()):null;
   if(batch!=null)batch.begin();
   for(var mesh:piece.meshes()){
    if(mesh.texture()!=texture||only>=0&&mesh.limb()!=only)continue;ModelPart part=parts[mesh.limb()];if(part==null||!part.visible||part.skipDraw)continue;
    poses.pushPose();part.translateAndRotate(poses);
    // The fitting scene's example eyes sit one pixel above real 64px Minecraft skin eyes.
    // Mount only the two V3 front-cloth meshes lower; preserve their shape, UVs and head pivot.
    if(mesh.texture()==1)poses.translate(0,1d/16d,0);
    if(batch!=null)batch.pose(mesh.cube(),poses);else cpu(poses,consumer,MeshRenderData.get(mesh.cube()),light);
    poses.popPose();
   }
   if(batch!=null)batch.draw(type,light,OverlayTexture.NO_OVERLAY,1,1,1,1);
  }
 }
 private static void cpu(PoseStack poses,VertexConsumer consumer,MeshRenderData data,int light){
  var m=poses.last().pose();var n=poses.last().normal();var v=data.values;int overlay=OverlayTexture.NO_OVERLAY;
  for(int at=0;at<v.length;at+=23){float a=v[at],b=v[at+1],c=v[at+2];float nx=n.m00()*a+n.m10()*b+n.m20()*c,ny=n.m01()*a+n.m11()*b+n.m21()*c,nz=n.m02()*a+n.m12()*b+n.m22()*c;
   for(int i=at+3;i<at+23;i+=5){float x=v[i],y=v[i+1],z=v[i+2];float px=m.m00()*x+m.m10()*y+m.m20()*z+m.m30(),py=m.m01()*x+m.m11()*y+m.m21()*z+m.m31(),pz=m.m02()*x+m.m12()*y+m.m22()*z+m.m32();consumer.addVertex(px,py,pz,-1,v[i+3],v[i+4],overlay,light,nx,ny,nz);}
  }
 }
}
