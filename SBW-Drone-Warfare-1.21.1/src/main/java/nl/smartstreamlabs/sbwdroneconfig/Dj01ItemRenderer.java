package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.*;

/** All 259 native mesh parts are batched into fixed and trigger geometry, cached until a resource reload. */
public final class Dj01ItemRenderer extends BlockEntityWithoutLevelRenderer {
    private record Triangle(float[] xyz,float[] uv,float nx,float ny,float nz){}
    private final List<Triangle> fixed=new ArrayList<>(),trigger=new ArrayList<>();
    private BakedModel sourceModel;
    private static Dj01ItemRenderer instance;
    public static Dj01ItemRenderer instance(){if(instance==null)instance=new Dj01ItemRenderer();return instance;}
    private Dj01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    private static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,p);}
    private void load(ItemStack stack){
        var mc=Minecraft.getInstance();var model=mc.getItemRenderer().getModel(stack,mc.level,null,0);if(model==sourceModel)return;
        fixed.clear();trigger.clear();
        try(var in=mc.getResourceManager().openAsReader(id("meshes/directional_jammer_dj01.json"))){
            for(var v:JsonParser.parseReader(in).getAsJsonObject().getAsJsonArray("bones")){
                var bone=v.getAsJsonObject();if(!bone.has("poly_mesh"))continue;
                var m=bone.getAsJsonObject("poly_mesh");var xyz=m.getAsJsonArray("positions");var uv=m.getAsJsonArray("uvs");var normals=m.getAsJsonArray("normals");
                var list=bone.get("name").getAsString().equals("trigger")?trigger:fixed;
                for(var f:m.getAsJsonArray("polys")){
                    var poly=f.getAsJsonArray();float[] p=new float[9],t=new float[6];int ni=0;
                    for(int i=0;i<3;i++){var r=poly.get(i).getAsJsonArray();int pi=r.get(0).getAsInt()*3,ti=r.get(2).getAsInt()*2;ni=r.get(1).getAsInt()*3;
                        p[3*i]=-xyz.get(pi).getAsFloat()/16;p[3*i+1]=xyz.get(pi+1).getAsFloat()/16;p[3*i+2]=xyz.get(pi+2).getAsFloat()/16;
                        t[2*i]=uv.get(ti).getAsFloat();t[2*i+1]=1-uv.get(ti+1).getAsFloat();
                    }
                    list.add(new Triangle(p,t,-normals.get(ni).getAsFloat(),normals.get(ni+1).getAsFloat(),normals.get(ni+2).getAsFloat()));
                }
            }
            if(fixed.size()+trigger.size()!=6532)throw new IllegalStateException("Incomplete DJ-01 mesh");sourceModel=model;
        }catch(java.io.IOException ex){throw new IllegalStateException("Cannot load DJ-01 mesh",ex);}
    }
    private static void draw(List<Triangle> list,PoseStack p,VertexConsumer out,int light,int overlay){
        var pose=p.last();for(var t:list)for(int j=0;j<4;j++){int i=Math.min(j,2);
            out.addVertex(pose.pose(),t.xyz[3*i],t.xyz[3*i+1],t.xyz[3*i+2]).setColor(255,255,255,255).setUv(t.uv[2*i],t.uv[2*i+1]).setOverlay(overlay).setLight(light).setNormal(pose,t.nx,t.ny,t.nz);
        }
    }
    public void nativeModel(ItemStack stack,boolean active,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        load(stack);var out=buffers.getBuffer(RenderType.entityCutoutNoCull(id("textures/item/directional_jammer_dj01"+(active?"_active":"")+".png")));
        draw(fixed,p,out,light,overlay);p.pushPose();p.translate(0,1.36/16,1.36/16);p.mulPose(Axis.XP.rotationDegrees(active?-12:0));p.translate(0,-1.36/16,-1.36/16);draw(trigger,p,out,light,overlay);p.popPose();
    }
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        p.pushPose();p.translate(.5,.5,.5); // cancel ItemRenderer's centering once; geometry remains in its native authored frame
        boolean third=context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        boolean held=third||context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        if(third&&Dj01Pose.apply(p)){}else if(held){p.translate(0,.56/16,-3.12/16);}
        else if(context==ItemDisplayContext.GROUND)p.translate(0,.177,0);
        else {p.translate(.00386109125,-.134742640625,.2100656853125);p.mulPose(Axis.YP.rotationDegrees(90));}
        nativeModel(stack,held&&Dj01Item.emitting(stack)&&Dj01Item.energy(stack)>0&&(!third||Dj01Pose.emitting()),p,buffers,light,overlay);p.popPose();
    }
}
