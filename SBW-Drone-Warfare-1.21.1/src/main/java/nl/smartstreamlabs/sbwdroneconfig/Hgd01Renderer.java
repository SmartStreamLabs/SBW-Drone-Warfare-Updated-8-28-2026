package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;import net.minecraft.client.renderer.*;
public class Hgd01Renderer extends CachedVehicleRenderer<Hgd01Entity> {
 public Hgd01Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context c){super(c,new Hgd01Model());shadowRadius=2.3f;}
 @Override public void vehicleAxis(Hgd01Entity e,PoseStack p,float yaw,float partial){var frame=e.getVehicleTransform(partial);var v=frame.transformPosition(new org.joml.Vector3d()).sub(e.getPosition(partial).x,e.getPosition(partial).y,e.getPosition(partial).z);p.translate(v.x,v.y,v.z);p.mulPose(frame.getUnnormalizedRotation(new org.joml.Quaterniond()).get(new org.joml.Quaternionf()));}
 @Override protected void applyRotations(Hgd01Entity e,PoseStack p,float age,float yaw,float partial,float scale){}
 @Override public RenderType getRenderType(Hgd01Entity e,net.minecraft.resources.ResourceLocation t,MultiBufferSource b,float partial){return RenderType.entityCutoutNoCull(getTextureLocation(e));}
 @Override public void renderCube(PoseStack p,software.bernie.geckolib.cache.object.GeoCube c,VertexConsumer v,int light,int overlay,int color){super.renderCube(p,c,v,light,overlay,color);}
 @Override public void render(Hgd01Entity e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){super.render(e,yaw,partial,p,buffers,light);var v=buffers.getBuffer(RenderType.lines());for(int side=0;side<2;side++){var end=e.beamEnd(side);if(end==null)continue;var start=e.laserPosition(side,partial).subtract(e.getPosition(partial));end=end.subtract(e.getPosition(partial));var n=end.subtract(start).normalize();v.addVertex(p.last().pose(),(float)start.x,(float)start.y,(float)start.z).setColor(100,235,255,235).setNormal(p.last(),(float)n.x,(float)n.y,(float)n.z);v.addVertex(p.last().pose(),(float)end.x,(float)end.y,(float)end.z).setColor(200,255,255,235).setNormal(p.last(),(float)n.x,(float)n.y,(float)n.z);}}
}
