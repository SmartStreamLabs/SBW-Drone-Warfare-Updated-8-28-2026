package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

/** Supplied OBJ in block units, compiled once per resource reload; no model rebuilding per siren/frame. */
public final class Ds02Renderer implements BlockEntityRenderer<Ds02BlockEntity> {
    private record Part(String group,String name,float[] vertices){}
    private static List<Part> parts;
    private static ResourceLocation id(String s){return new ResourceLocation(SbwDroneRangeConfig.MOD_ID,s);}
    private static final ResourceLocation MODEL=id("meshes/ds02.mesh.json");
    private static final RenderType BODY=RenderType.entityCutout(id("textures/block/drone_warning_siren_ds02.png"));
    private static final RenderType LENS=RenderType.entityTranslucent(id("textures/block/drone_warning_siren_ds02.png"));
    private static final RenderType GLOW=RenderType.entityTranslucentEmissive(id("textures/block/drone_warning_siren_ds02_emissive.png"));
    public static void clear(){parts=null;}
    private static List<Part> mesh(){
        if(parts==null){
            try(var r=Minecraft.getInstance().getResourceManager().getResourceOrThrow(MODEL).openAsReader()){
                var result=new ArrayList<Part>();
                for(var node:JsonParser.parseReader(r).getAsJsonArray()){
                    var o=node.getAsJsonObject();var data=o.getAsJsonArray("v");float[] v=new float[data.size()];
                    for(int i=0;i<v.length;i++)v[i]=data.get(i).getAsFloat();result.add(new Part(o.get("group").getAsString(),o.get("name").getAsString(),v));
                }
                parts=List.copyOf(result);
            }catch(java.io.IOException ex){throw new IllegalStateException("DS02 supplied mesh could not be loaded",ex);}
        }return parts;
    }
    @Override public boolean shouldRenderOffScreen(Ds02BlockEntity be){return true;}
    @Override public int getViewDistance(){return 128;}
    @Override public void render(Ds02BlockEntity be,float partial,PoseStack stack,MultiBufferSource buffers,int light,int overlay){
        stack.pushPose();stack.translate(.5,0,.5);stack.mulPose(Axis.YP.rotationDegrees(-be.getBlockState().getValue(Ds02Block.FACING).toYRot()));
        float door=-105*be.door(partial);
        float rotor=be.alarming()?(float)(Math.floorMod(be.getLevel().getGameTime()-be.alarmStart(),25)+partial)*360/25:0;
        // Opaque body first; one small translucent lens pass, preserving its real inner reflector.
        for(int pass=0;pass<2;pass++)for(Part p:mesh()){
            boolean lens=p.group.equals("beacon_lens");if(lens!=(pass==1))continue;
            stack.pushPose();
            if(p.group.equals("cabinet_door"))pivot(stack,-.28125,.625,.44375,door);
            if(p.group.equals("beacon_rotor"))pivot(stack,0,4.75625,0,rotor);
            emit(p,stack,buffers.getBuffer(lens?LENS:BODY),light,overlay,1);
            boolean glow=be.alarming()&&(lens || p.group.equals("beacon_rotor") || p.name.equals("status_lens_amber")) || be.enabled() && p.name.equals("status_lens_green");
            if(glow)emit(p,stack,buffers.getBuffer(GLOW),LightTexture.FULL_BRIGHT,overlay,.8f);
            stack.popPose();
        }
        stack.popPose();
    }
    private static void pivot(PoseStack s,double x,double y,double z,float degrees){s.translate(x,y,z);s.mulPose(Axis.YP.rotationDegrees(degrees));s.translate(-x,-y,-z);}
    private static void emit(Part p,PoseStack s,VertexConsumer consumer,int light,int overlay,float alpha){
        float[] a=p.vertices;var pose=s.last();
        for(int i=0;i<a.length;i+=8)consumer.vertex(pose.pose(),a[i],a[i+1],a[i+2]).color(1,1,1,alpha).uv(a[i+3],a[i+4]).overlayCoords(overlay).uv2(light).normal(pose.normal(),a[i+5],a[i+6],a[i+7]).endVertex();
    }
}
