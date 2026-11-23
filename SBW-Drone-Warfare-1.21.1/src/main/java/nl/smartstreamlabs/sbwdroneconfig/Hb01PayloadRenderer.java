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
public final class Hb01PayloadRenderer {
    private static final java.util.Set<Hb01PayloadRenderer> INSTANCES=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    public Hb01PayloadRenderer(){INSTANCES.add(this);}
    public static void clear(){for(var renderer:java.util.List.copyOf(INSTANCES)){for(var visual:renderer.visuals.values())visual.discard();renderer.visuals.clear();renderer.fits.clear();renderer.visualLevel=null;}}

    private record Cached(BakedModel model,Hb01PayloadFit fit){}
    private final Map<Item,Cached> fits=new HashMap<>();
    private final Map<Item,net.minecraft.world.entity.Entity> visuals=new HashMap<>();
    private Level visualLevel;
    private net.minecraft.world.entity.Entity display(ItemStack stack,Level level){
        if(visualLevel!=level){visuals.clear();fits.clear();visualLevel=level;}
        String path=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        net.minecraft.world.entity.EntityType<?> type=switch(path){
            case "anti_vehicle_mortar_payload","mortar_shell","mortar_shell_wp" -> com.atsuishio.superbwarfare.init.ModEntities.MORTAR_SHELL.get();
            case "rpg_rocket_standard" -> com.atsuishio.superbwarfare.init.ModEntities.RPG_ROCKET_STANDARD.get();
            case "rpg_rocket_tbg" -> com.atsuishio.superbwarfare.init.ModEntities.RPG_ROCKET_TBG.get();
            case "javelin_missile" -> com.atsuishio.superbwarfare.init.ModEntities.JAVELIN_MISSILE.get();
            default -> null;
        };
        return type==null?null:visuals.computeIfAbsent(stack.getItem(),i->type.create(level));
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    private void model(ItemStack stack,Level level,int id,PoseStack pose,MultiBufferSource buffers,int light){
        var visual=display(stack,level);
        if(visual!=null){
            // One unticked client-only visual per payload kind. Never spawned, added to
            // the world, networked or recreated per frame. Reuse SBW's actual 3D mesh.
            visual.getPersistentData().putBoolean("sbwdroneconfigPayloadPreview",true);visual.tickCount=20;
            net.minecraft.client.renderer.entity.EntityRenderer renderer=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(visual);
            renderer.render(visual,0,0,pose,buffers,light);
        }else Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,pose,buffers,level,id);
    }
    public void render(HeavyBomberDroneEntity d,PoseStack p,MultiBufferSource buffers,int light){
        for(int socket=0;socket<4;socket++){
            ItemStack stack=d.hb01VisualPayload(socket);if(stack.isEmpty())continue;
            if(Hg01Payload.is(stack)){Hg01PayloadRenderer.bomber(Hb01VisualPose.socket(socket),p,buffers,light);continue;}
            var fit=fitFor(stack,d.level(),d.getId());if(fit==null)continue;var anchor=Hb01VisualPose.socket(socket);var m=fit.matrix();
            p.pushPose();p.translate(anchor.x,anchor.y,anchor.z);
            p.translate(m.m30(),m.m31(),m.m32());p.scale((float)fit.scale(),(float)fit.scale(),(float)fit.scale());
            p.mulPose(new Quaternionf(m.getUnnormalizedRotation(new Quaterniond())));
            model(stack,d.level(),d.getId(),p,buffers,light);
            p.popPose();
        }
    }
    public Hb01PayloadFit fitFor(ItemStack stack,Level level,int id){
        BakedModel model=Minecraft.getInstance().getItemRenderer().getModel(stack,level,null,id);
        Cached cached=fits.get(stack.getItem());if(cached!=null&&cached.model()==model)return cached.fit();
        // Measure the real NONE render, including custom SBW/Bedrock renderers and their internal offsets.
        // This captures vertices into a bounded accumulator, never a GPU buffer or an extra world entity.
        BoundsConsumer measured=new BoundsConsumer();
        model(stack,level,id,new PoseStack(),type->measured,0xF000F0);
        double[] b=measured.bounds;
        if(!Double.isFinite(b[0])){
            // Native model instances may still be loading. Retry without caching an
            // empty bound, which would otherwise turn into an oversized invisible load.
            return null;
        }
        var fit=Hb01PayloadFit.fromCenteredBounds(b[0],b[1],b[2],b[3],b[4],b[5]);
        fits.put(stack.getItem(),new Cached(model,fit));return fit; // replaced automatically when resource reload gives a new baked model
    }
    private static final class BoundsConsumer implements VertexConsumer {
        final double[] bounds={Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
        private void include(double x,double y,double z){double[] v={x,y,z};for(int i=0;i<3;i++){bounds[i]=Math.min(bounds[i],v[i]);bounds[i+3]=Math.max(bounds[i+3],v[i]);}}
        @Override public VertexConsumer addVertex(float x,float y,float z){include(x,y,z);return this;}
        @Override public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        @Override public VertexConsumer setUv(float u,float v){return this;}
        @Override public VertexConsumer setUv1(int u,int v){return this;}
        @Override public VertexConsumer setUv2(int u,int v){return this;}
        @Override public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
}
