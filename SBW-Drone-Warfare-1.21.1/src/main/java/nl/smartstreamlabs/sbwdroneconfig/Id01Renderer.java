package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
public final class Id01Renderer extends CachedEntityRenderer<Id01Entity>{
    public static final class Spin {double last=-1,angle;float speed;}
    public Id01Renderer(EntityRendererProvider.Context c){super(c,new Id01Model());shadowRadius=1.1f;}
    @Override protected void applyRotations(Id01Entity e,PoseStack p,float age,float yaw,float partial){p.mulPose(Axis.YP.rotationDegrees(180));}
    @Override public void render(Id01Entity e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
        p.pushPose();p.translate(0,Id01Pose.ROOT_HEIGHT-.01,0);p.mulPose(Axis.YP.rotationDegrees(-net.minecraft.util.Mth.rotLerp(partial,e.yRotO,e.getYRot())));p.mulPose(Axis.XP.rotationDegrees(e.posePitch(partial)));
        super.render(e,yaw,partial,p,buffers,light);p.popPose();
        if(net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes())for(var box:Id01Geometry.parts(e))net.minecraft.client.renderer.LevelRenderer.renderLineBox(p,buffers.getBuffer(net.minecraft.client.renderer.RenderType.lines()),box.move(-e.getX(),-e.getY(),-e.getZ()),0f,1f,1f,1f);
    }
}
