package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.EntityRendererProvider;import software.bernie.geckolib.renderer.GeoEntityRenderer;
public class Hx2Renderer extends CachedEntityRenderer<Hx2Entity>{
 public Hx2Renderer(EntityRendererProvider.Context c){super(c,new Hx2Model());shadowRadius=.7f;}
 @Override protected void applyRotations(Hx2Entity e,PoseStack p,float age,float yaw,float partial,float scale){}
 @Override public void render(Hx2Entity e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
  if(SbwCompat.isUsingLinkedMonitorForDrone(net.minecraft.client.Minecraft.getInstance().player,e)&&net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson())return;
  var m=Hx2Pose.frame(e,partial);var origin=e.getPosition(partial);p.pushPose();p.translate(m.m30()-origin.x,m.m31()-origin.y-.01,m.m32()-origin.z);p.mulPose(new org.joml.Quaternionf(m.getNormalizedRotation(new org.joml.Quaterniond())));super.render(e,yaw,partial,p,buffers,light);p.popPose();
 }
 @Override public boolean shouldRender(Hx2Entity e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return f.isVisible(e.getBoundingBoxForCulling());}
}
