package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import org.joml.Vector3d;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DeliveryDroneRenderer extends CachedEntityRenderer<DeliveryDroneEntity> {
    public DeliveryDroneRenderer(EntityRendererProvider.Context c){super(c,new DeliveryDroneModel());this.shadowRadius=.55f;}
    @Override public RenderType getRenderType(DeliveryDroneEntity d,ResourceLocation texture,MultiBufferSource buffers,float partial){return RenderType.entityCutoutNoCull(texture);}
    @Override protected void applyRotations(DeliveryDroneEntity d,PoseStack p,float age,float yaw,float partial,float nativeScale){} // attitude is applied once below
    public static void applyVisualPose(DeliveryDroneEntity d,float partial,PoseStack p){
        var m=Dd01VisualPose.matrix(d.getYaw(partial),d.getBodyPitch(partial),d.getRoll(partial));
        p.translate(m.m30(),m.m31(),m.m32());p.mulPose(new org.joml.Quaternionf(m.getNormalizedRotation(new org.joml.Quaterniond())));
    }
    @Override public void render(DeliveryDroneEntity d,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
        if(DroneClientViewContext.isLocalFpvViewOf(d))return;
        var mc=Minecraft.getInstance();boolean first=mc.options.getCameraType()==CameraType.FIRST_PERSON;
        boolean controlled=SbwCompat.isUsingLinkedMonitorForDrone(mc.player,d);
        boolean suppressCargo=first||mc.options.getCameraType()==CameraType.THIRD_PERSON_BACK;
        p.pushPose();applyVisualPose(d,partial,p);
        if(!CubedFpvDroneRenderMath.shouldHideControlledDroneBody(controlled,first)){
            // Cancel only Gecko's added .01; native feet remain at Y=0.
            p.pushPose();p.translate(0,-.01,0);super.render(d,yaw,partial,p,buffers,light);p.popPose();
        }
        if(d.hasMountedExtractionCrate()&&(!controlled||!suppressCargo))renderMountedCrate(d,p,buffers,light);
        p.popPose();
    }
    public static void applyCargoPose(PoseStack p){p.translate(0,Dd01VisualPose.CARGO_CENTER_Y,0);}
    private void renderMountedCrate(DeliveryDroneEntity d,PoseStack p,MultiBufferSource buffers,int light){
        p.pushPose();
        // Existing crate only. NONE avoids fixed-display scaling; ItemRenderer centres its [0,1] block model.
        applyCargoPose(p);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(AddonItems.EXTRACTION_CRATE.get()),ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,p,buffers,d.level(),d.getId());
        p.popPose();
    }
    @Override public boolean shouldRender(DeliveryDroneEntity d,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){
        if(!d.isAlive()||d.isRemoved())return false;
        var m=Dd01VisualPose.matrix(d.getYaw(1),d.getBodyPitch(1),d.getRoll(1));
        AABB bounds=null;
        for(int i=0;i<8;i++){
            var v=m.transformPosition(new Vector3d((i&1)==0?Dd01VisualPose.MIN_X:Dd01VisualPose.MAX_X,(i&2)==0?Dd01VisualPose.MIN_Y:Dd01VisualPose.MAX_Y,(i&4)==0?Dd01VisualPose.MIN_Z:Dd01VisualPose.MAX_Z));
            var point=new Vec3(d.getX()+v.x,d.getY()+v.y,d.getZ()+v.z);var corner=new AABB(point,point);bounds=bounds==null?corner:bounds.minmax(corner);
        }
        return frustum.isVisible(bounds.inflate(.05));
    }
}
