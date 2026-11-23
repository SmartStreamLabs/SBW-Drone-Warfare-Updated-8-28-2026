package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
public final class Ps01Renderer implements BlockEntityRenderer<Ps01BlockEntity>{
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(Ps01BlockEntity b){return b.getRenderBoundingBox();}
 @Override public boolean shouldRenderOffScreen(Ps01BlockEntity b){return true;}@Override public int getViewDistance(){return 192;}@Override public boolean shouldRender(Ps01BlockEntity b,net.minecraft.world.phys.Vec3 camera){return b.getRenderBoundingBox().getCenter().distanceToSqr(camera)<192*192;}
 @Override public void render(Ps01BlockEntity b,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){if(b.getLevel()==null)return;p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(-90*Ps01Geometry.quarter(b.facing())));Ps01Model.get().draw(p,buffers,LevelRenderer.getLightColor(b.getLevel(),b.getBlockPos().above(4)),LevelRenderer.getLightColor(b.getLevel(),b.getBlockPos().above()),overlay,b.progress(partial),b.doorHealth<=0,b.lights,(float)b.integrity(),(float)b.doorIntegrity(),false,true);p.popPose();}
}
