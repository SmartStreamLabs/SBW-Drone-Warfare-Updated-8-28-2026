package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
/** SBW's supported GeoVehicle renderer keeps vehicle attitude, seat/camera and native lights. */
public class Mj03Renderer extends CachedVehicleRenderer<Mj03Entity> {
    public Mj03Renderer(EntityRendererProvider.Context c){super(c,new Mj03Model());shadowRadius=2;}
    @Override public void renderRecursively(com.mojang.blaze3d.vertex.PoseStack p,Mj03Entity e,software.bernie.geckolib.cache.object.GeoBone b,net.minecraft.client.renderer.RenderType t,net.minecraft.client.renderer.MultiBufferSource buffers,com.mojang.blaze3d.vertex.VertexConsumer v,boolean rerender,float partial,int light,int overlay,float r,float g,float blue,float alpha){
        boolean glow=e.lights() && (b.getName().startsWith("headlight_") || b.getName().startsWith("tail_light_"));
        glow|=b.getName().equals("status_ready") && e.phase()==Mj03Entity.DRIVE;
        glow|=b.getName().equals("status_transition") && (e.phase()==Mj03Entity.DEPLOYING || e.phase()==Mj03Entity.PACKING);
        glow|=b.getName().equals("status_jamming") && e.operational();
        if(b.getName().equals("cab_glass") && !e.isWreck()){
            t=net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(e));
            v=buffers.getBuffer(t);
        }
        super.renderRecursively(p,e,b,t,buffers,v,rerender,partial,glow?net.minecraft.client.renderer.LightTexture.FULL_BRIGHT:light,overlay,r,g,blue,alpha);
    }
}
