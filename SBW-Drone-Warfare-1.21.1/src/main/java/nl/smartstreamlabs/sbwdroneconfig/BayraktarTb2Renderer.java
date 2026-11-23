package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BayraktarTb2Renderer extends CachedEntityRenderer<BayraktarTb2Entity> {
    private final Bt01PayloadRenderer payloadRenderer=new Bt01PayloadRenderer();
    public BayraktarTb2Renderer(EntityRendererProvider.Context c){super(c,new BayraktarTb2Model());this.shadowRadius=6;}
    @Override public RenderType getRenderType(BayraktarTb2Entity d,ResourceLocation t,MultiBufferSource b,float partial){return RenderType.entityCutoutNoCull(t);}
    @Override protected void applyRotations(BayraktarTb2Entity d,PoseStack p,float age,float yaw,float partial,float nativeScale){}
    public static void applyVisualPose(BayraktarTb2Entity d,float partial,PoseStack p){var m=Bt01VisualPose.matrix(d.getYaw(partial),d.getBodyPitch(partial),d.getRoll(partial));p.translate(m.m30(),m.m31(),m.m32());p.mulPose(new org.joml.Quaternionf(m.getNormalizedRotation(new org.joml.Quaterniond())));}
    @Override public void render(BayraktarTb2Entity d,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
        if(DroneClientViewContext.isReadOnlyCameraIntercept(d))return;
        var mc=Minecraft.getInstance();boolean controlled=SbwCompat.isUsingLinkedMonitorForDrone(mc.player,d);boolean active=DroneClientViewContext.activeDroneEntity(mc)==d;
        if(!controlled&&!active){
            p.pushPose();applyVisualPose(d,partial,p);
            p.pushPose();p.translate(0,-.01,0);super.render(d,yaw,partial,p,buffers,light);p.popPose();
            payloadRenderer.render(d,p,buffers,light);p.popPose();
        }
    }
    @Override public boolean shouldRender(BayraktarTb2Entity d,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){
        double r=Bt01VisualPose.CULL_RADIUS,cy=d.getY()+Bt01VisualPose.CENTER_Y;
        return d.isAlive()&&!d.isRemoved()&&f.isVisible(new AABB(d.getX()-r,cy-r,d.getZ()-r,d.getX()+r,cy+r,d.getZ()+r));
    }
}
