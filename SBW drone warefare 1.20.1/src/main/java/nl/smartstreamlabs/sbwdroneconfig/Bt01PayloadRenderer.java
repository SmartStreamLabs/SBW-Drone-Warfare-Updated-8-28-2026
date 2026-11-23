package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import java.util.HashMap;
import java.util.Map;

/** Four real item models; no decorative loads, ticking entities or independent supply storage. */
public final class Bt01PayloadRenderer {
    private record Cached(BakedModel model,Bt01PayloadFit fit){}
    private final Map<Item,Cached> fits=new HashMap<>();
    public void render(BayraktarTb2Entity d,PoseStack p,MultiBufferSource buffers,int light){
        for(int socket=0;socket<4;socket++){
            ItemStack stack=d.bt01VisualPayload(socket);if(stack.isEmpty())continue;
            var fit=fitFor(stack,d.level(),d.getId());var anchor=Bt01VisualPose.socket(socket);var m=fit.matrix();
            p.pushPose();p.translate(anchor.x,anchor.y,anchor.z);
            p.translate(m.m30(),m.m31(),m.m32());p.scale((float)fit.scale(),(float)fit.scale(),(float)fit.scale());
            p.mulPose(new Quaternionf(m.getUnnormalizedRotation(new Quaterniond())));
            Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,p,buffers,d.level(),d.getId());
            p.popPose();
        }
    }
    public Bt01PayloadFit fitFor(ItemStack stack,Level level,int id){
        BakedModel model=Minecraft.getInstance().getItemRenderer().getModel(stack,level,null,id);
        Cached cached=fits.get(stack.getItem());if(cached!=null&&cached.model()==model)return cached.fit();
        // Measure the real NONE render, including custom SBW/Bedrock renderers and their internal offsets.
        // This captures vertices into a bounded accumulator, never a GPU buffer or an extra world entity.
        BoundsConsumer measured=new BoundsConsumer();
        Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemDisplayContext.NONE,0xF000F0,OverlayTexture.NO_OVERLAY,new PoseStack(),type->measured,level,id);
        double[] b=measured.bounds;
        if(!Double.isFinite(b[0])){
            SbwDroneRangeConfig.LOGGER.warn("BT-01 payload item emitted no renderable geometry: {}",stack);
            b=new double[]{0,0,0,0,0,0};
        }
        var fit=Bt01PayloadFit.fromCenteredBounds(b[0],b[1],b[2],b[3],b[4],b[5]);
        fits.put(stack.getItem(),new Cached(model,fit));return fit; // replaced automatically when resource reload gives a new baked model
    }
    private static final class BoundsConsumer implements VertexConsumer {
        final double[] bounds={Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
        private void include(double x,double y,double z){double[] v={x,y,z};for(int i=0;i<3;i++){bounds[i]=Math.min(bounds[i],v[i]);bounds[i+3]=Math.max(bounds[i+3],v[i]);}}
        @Override public VertexConsumer vertex(double x,double y,double z){include(x,y,z);return this;}
        @Override public VertexConsumer color(int r,int g,int b,int a){return this;}
        @Override public VertexConsumer uv(float u,float v){return this;}
        @Override public VertexConsumer overlayCoords(int u,int v){return this;}
        @Override public VertexConsumer uv2(int u,int v){return this;}
        @Override public VertexConsumer normal(float x,float y,float z){return this;}
        @Override public void endVertex(){}
        @Override public void defaultColor(int r,int g,int b,int a){}
        @Override public void unsetDefaultColor(){}
    }
}
