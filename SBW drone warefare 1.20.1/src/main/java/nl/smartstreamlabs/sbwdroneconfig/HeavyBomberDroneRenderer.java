package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.*;
import org.joml.Vector3d;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HeavyBomberDroneRenderer extends CachedEntityRenderer<HeavyBomberDroneEntity> {
    private final Hb01PayloadRenderer payloadRenderer=new Hb01PayloadRenderer();
    public HeavyBomberDroneRenderer(EntityRendererProvider.Context c){super(c,new HeavyBomberDroneModel());this.shadowRadius=.65f;}
    @Override public RenderType getRenderType(HeavyBomberDroneEntity d,ResourceLocation texture,MultiBufferSource buffers,float partial){return RenderType.entityCutoutNoCull(texture);}
    @Override protected void applyRotations(HeavyBomberDroneEntity d,PoseStack p,float age,float yaw,float partial){} // one authored attitude below
    public static void applyVisualPose(HeavyBomberDroneEntity d,float partial,PoseStack p){
        var m=Hb01VisualPose.matrix(d.getYaw(partial),d.getBodyPitch(partial),d.getRoll(partial));
        p.translate(m.m30(),m.m31(),m.m32());p.mulPose(new org.joml.Quaternionf(m.getNormalizedRotation(new org.joml.Quaterniond())));
    }
    @Override public void render(HeavyBomberDroneEntity d,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){p.pushPose();try{Dc01DockRender.apply(d,partial,p);renderDrone(d,yaw,partial,p,buffers,light);}finally{p.popPose();}}
     private void renderDrone(HeavyBomberDroneEntity d,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
        if(DroneClientViewContext.isLocalFpvViewOf(d))return;
        var mc=Minecraft.getInstance();boolean first=mc.options.getCameraType()==CameraType.FIRST_PERSON;
        boolean controlled=SbwCompat.isUsingLinkedMonitorForDrone(mc.player,d);
        if(!CubedFpvDroneRenderMath.shouldHideControlledDroneBody(controlled,first)){
            p.pushPose();applyVisualPose(d,partial,p);
            p.pushPose();p.translate(0,-.01,0);super.render(d,yaw,partial,p,buffers,light);p.popPose();
            payloadRenderer.render(d,p,buffers,light);
            p.popPose();
        }
    }
    @Override public boolean shouldRender(HeavyBomberDroneEntity d,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){
        if(!d.isAlive()||d.isRemoved())return false;
        var carrier=Dc01Docking.carrier(d);if(carrier!=null)return frustum.isVisible(carrier.getBoundingBoxForCulling());
        var m=Hb01VisualPose.matrix(d.getYaw(1),d.getBodyPitch(1),d.getRoll(1));AABB bounds=null;
        for(int i=0;i<8;i++){
            var v=m.transformPosition(new Vector3d((i&1)==0?Hb01VisualPose.MIN_X:Hb01VisualPose.MAX_X,(i&2)==0?Hb01VisualPose.MIN_Y:Hb01VisualPose.MAX_Y,(i&4)==0?Hb01VisualPose.MIN_Z:Hb01VisualPose.MAX_Z));
            var point=new Vec3(d.getX()+v.x,d.getY()+v.y,d.getZ()+v.z);var corner=new AABB(point,point);bounds=bounds==null?corner:bounds.minmax(corner);
        }
        return frustum.isVisible(bounds.inflate(.05));
    }
}
