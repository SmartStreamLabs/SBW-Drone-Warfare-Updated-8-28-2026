package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
import org.joml.Matrix4f;
import java.nio.*;
import java.util.*;

/** Cached rigid skinning. Original vertices/UVs stay resident on the GPU; Gecko still owns
 * every animated bone and tracked anchor. Each pass uploads only the small pose palette. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class Adv01Gpu {
    private static ShaderInstance shader,heatShader;
    static boolean heatAvailable(){return heatShader!=null;}
    private static final Map<BakedGeoModel,Batch> CACHE=new IdentityHashMap<>(),GROUPED=new IdentityHashMap<>();
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void shaders(net.minecraftforge.client.event.RegisterShadersEvent event){
        for(var b:CACHE.values())if(b!=null)b.close();CACHE.clear();for(var b:GROUPED.values())if(b!=null)b.close();GROUPED.clear();shader=heatShader=null;
        try{event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation(SbwDroneRangeConfig.MOD_ID,"adv01_mesh"),DefaultVertexFormat.NEW_ENTITY),s->shader=s);event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation(SbwDroneRangeConfig.MOD_ID,"adv01_heat"),DefaultVertexFormat.NEW_ENTITY),s->heatShader=s);}
        catch(java.io.IOException|RuntimeException failure){SbwDroneRangeConfig.LOGGER.warn("ADV mesh shader unavailable; retaining complete CPU mesh rendering",failure);}
    }
    static Batch batch(BakedGeoModel model){
        if(shader==null||Boolean.getBoolean("adv01.cpuMesh"))return null;
        if(!CACHE.containsKey(model)){var b=Batch.create(model);CACHE.put(model,b);}
        return CACHE.get(model);
    }
    static Batch batchGrouped(BakedGeoModel model){
        if(shader==null||Boolean.getBoolean("adv01.cpuMesh"))return null;
        if(!GROUPED.containsKey(model))GROUPED.put(model,Batch.createGrouped(model));
        return GROUPED.get(model);
    }
    static void release(BakedGeoModel model){Batch old=CACHE.remove(model);if(old!=null)old.close();old=GROUPED.remove(model);if(old!=null)old.close();}
    private static void group(GeoBone bone,List<GeoCube> cubes,IdentityHashMap<GeoCube,Integer> indices,int[] count){
        if(!bone.getCubes().isEmpty()){int index=count[0]++;for(var cube:bone.getCubes()){cubes.add(cube);indices.put(cube,index);if(MeshRenderData.get(cube)==null)MeshRenderData.register(cube);}}
        for(var child:bone.getChildBones())group(child,cubes,indices,count);
    }
    private static void collect(GeoBone bone,List<GeoCube> cubes){cubes.addAll(bone.getCubes());for(var child:bone.getChildBones())collect(child,cubes);}
    static final class Batch implements AutoCloseable {
        final IdentityHashMap<GeoCube,Integer> indices=new IdentityHashMap<>();
        final VertexBuffer vertices;final FloatBuffer palette;final int[] textures=new int[8];final int count;int cursor;boolean closed;
        private Batch(List<GeoCube> cubes){this(cubes,null,cubes.size());}
        private Batch(List<GeoCube> cubes,IdentityHashMap<GeoCube,Integer> grouped,int parts){
            count=parts;palette=org.lwjgl.BufferUtils.createFloatBuffer(count*28);int total=0;
            for(int i=0;i<cubes.size();i++){var c=cubes.get(i);indices.put(c,grouped==null?i:grouped.get(c));total+=MeshRenderData.get(c).values.length/23*4;}
            vertices=upload(cubes,total,indices);int previous=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);RenderSystem.activeTexture(GL13.GL_TEXTURE3);int bound=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            try(Unpack unpack=new Unpack()){for(int i=0;i<textures.length;i++){int id=textures[i]=GlStateManager._genTexture();GlStateManager._bindTexture(id);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL30.GL_RGBA32F,7,count,0,GL11.GL_RGBA,GL11.GL_FLOAT,(FloatBuffer)null);}}
            GlStateManager._bindTexture(bound);RenderSystem.activeTexture(previous);BufferUploader.reset();
            SbwDroneRangeConfig.LOGGER.info("ADV GPU mesh ready: {} original vertices, {} rigid parts, one draw per vehicle",total,count);
        }
        static Batch create(BakedGeoModel model){
            List<GeoCube> cubes=new ArrayList<>();for(var b:model.topLevelBones())collect(b,cubes);
            if(cubes.isEmpty()||cubes.size()>Math.min(MeshPaletteIndex.MAX_PARTS,GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE)))return null;
            for(var c:cubes)if(MeshRenderData.get(c)==null||c.pivot().lengthSqr()!=0||c.rotation().lengthSqr()!=0)return null;
            return new Batch(cubes);
        }
        static Batch createGrouped(BakedGeoModel model){
            List<GeoCube> cubes=new ArrayList<>();var indices=new IdentityHashMap<GeoCube,Integer>();int[] count={0};
            for(var bone:model.topLevelBones())group(bone,cubes,indices,count);
            return cubes.isEmpty()||count[0]>Math.min(MeshPaletteIndex.MAX_PARTS,GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE))?null:new Batch(cubes,indices,count[0]);
        }
        void begin(){for(int i=0;i<palette.capacity();i++)palette.put(i,0);palette.position(0);palette.limit(palette.capacity());}
        void pose(GeoCube cube,PoseStack pose){
            Integer index=indices.get(cube);if(index==null)throw new IllegalStateException("ADV GPU cube missing from baked mesh");int at=index*28;pose.last().pose().get(at,palette);var n=pose.last().normal();
            palette.put(at+16,n.m00()).put(at+17,n.m01()).put(at+18,n.m02()).put(at+19,0);
            palette.put(at+20,n.m10()).put(at+21,n.m11()).put(at+22,n.m12()).put(at+23,0);
            palette.put(at+24,n.m20()).put(at+25,n.m21()).put(at+26,n.m22()).put(at+27,0);
        }
        void draw(RenderType type,int light,int overlay,float r,float g,float b,float a){drawPass(type,light,overlay,r,g,b,a,null);}
        void drawHeat(RenderType type,com.mojang.blaze3d.pipeline.RenderTarget target,float alpha){drawPass(type,0xf000f0,0,1,1,1,alpha,target);}
        private void drawPass(RenderType type,int light,int overlay,float r,float g,float b,float a,com.mojang.blaze3d.pipeline.RenderTarget target){
            ShaderInstance drawShader=target==null?shader:heatShader;
            if(closed||drawShader==null)return;
            var previousShader=RenderSystem.getShader();float[] previousColor=RenderSystem.getShaderColor().clone();int[] previousTextures=new int[4];for(int i=0;i<4;i++)previousTextures[i]=RenderSystem.getShaderTexture(i);int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            type.setupRenderState();if(target!=null){target.bindWrite(true);GL11.glDisable(GL11.GL_STENCIL_TEST);RenderSystem.disableScissor();RenderSystem.enableDepthTest();RenderSystem.depthFunc(GL11.GL_LEQUAL);RenderSystem.depthMask(true);RenderSystem.colorMask(true,true,true,true);RenderSystem.disableBlend();}
            try{
                int texture=textures[cursor++%textures.length];RenderSystem.activeTexture(GL13.GL_TEXTURE3);GlStateManager._bindTexture(texture);try(Unpack unpack=new Unpack()){GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,7,count,GL11.GL_RGBA,GL11.GL_FLOAT,palette);}RenderSystem.activeTexture(GL13.GL_TEXTURE0);
                RenderSystem.setShaderTexture(3,texture);RenderSystem.setShader(()->drawShader);RenderSystem.setShaderColor(r,g,b,a);
                drawShader.safeGetUniform("AdvLight").set((float)(light&65535),(float)(light>>>16));drawShader.safeGetUniform("AdvOverlay").set((float)(overlay&65535),(float)(overlay>>>16));
                if(target!=null)drawShader.safeGetUniform("HeatCutoff").set(type.toString().contains("translucent")?.01f:.1f);
                vertices.bind();vertices.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()),RenderSystem.getProjectionMatrix(),drawShader);
            }finally{
                VertexBuffer.unbind();BufferUploader.reset();type.clearRenderState();RenderSystem.setShader(()->previousShader);RenderSystem.setShaderColor(previousColor[0],previousColor[1],previousColor[2],previousColor[3]);for(int i=0;i<4;i++)RenderSystem.setShaderTexture(i,previousTextures[i]);RenderSystem.activeTexture(active);
            }
        }
        @Override public void close(){if(closed)return;closed=true;vertices.close();for(int texture:textures)GlStateManager._deleteTexture(texture);}
    }
    /** NativeImage atlas uploads leave row/skip/PBO state behind. CPU palette pointers must
     * never be interpreted as atlas rows or offsets into a pixel-unpack buffer. */
    private static final class Unpack implements AutoCloseable {
        final int alignment=GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT),row=GL11.glGetInteger(GL11.GL_UNPACK_ROW_LENGTH),pixels=GL11.glGetInteger(GL11.GL_UNPACK_SKIP_PIXELS),rows=GL11.glGetInteger(GL11.GL_UNPACK_SKIP_ROWS),buffer=GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        Unpack(){GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,0);GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,1);GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH,0);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS,0);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS,0);}
        public void close(){GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,alignment);GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH,row);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS,pixels);GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS,rows);GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,buffer);}
    }
    private static VertexBuffer upload(List<GeoCube> cubes,int total,IdentityHashMap<GeoCube,Integer> indices){
        BufferBuilder out=new BufferBuilder(Math.max(256,total*36));out.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.NEW_ENTITY);
        for(var cube:cubes){
            int index=indices.get(cube);var d=MeshRenderData.get(cube);var v=d.values;
            // Cube-local transforms are immutable; bake once rather than into every pose palette.
            PoseStack local=new PoseStack();software.bernie.geckolib.util.RenderUtils.translateToPivotPoint(local,cube);software.bernie.geckolib.util.RenderUtils.rotateMatrixAroundCube(local,cube);software.bernie.geckolib.util.RenderUtils.translateAwayFromPivotPoint(local,cube);
            var matrix=local.last().pose();var normal=local.last().normal();float[] transformed=v.clone();
            for(int at=0;at<v.length;at+=23){var n=new org.joml.Vector3f(v[at],v[at+1],v[at+2]);normal.transform(n);transformed[at]=n.x;transformed[at+1]=n.y;transformed[at+2]=n.z;
                for(int i=at+3;i<at+23;i+=5){var vertex=new org.joml.Vector3f(v[i],v[i+1],v[i+2]);matrix.transformPosition(vertex);transformed[i]=vertex.x;transformed[i+1]=vertex.y;transformed[i+2]=vertex.z;}}
            v=transformed;
            for(int at=0;at<v.length;at+=23)for(int i=at+3;i<at+23;i+=5)out.vertex(v[i],v[i+1],v[i+2]).color(MeshPaletteIndex.low(index),MeshPaletteIndex.high(index),MeshPaletteIndex.flags(d.flipX,d.flipY,d.flipZ),255).uv(v[i+3],v[i+4]).overlayCoords(0,0).uv2(0,0).normal(v[at],v[at+1],v[at+2]).endVertex();
        }
        VertexBuffer result=new VertexBuffer(VertexBuffer.Usage.STATIC);result.bind();result.upload(out.end());VertexBuffer.unbind();return result;
    }
}
