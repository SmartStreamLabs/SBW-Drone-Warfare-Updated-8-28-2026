package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import java.util.*;

/** Depth-tested White Hot from the current world pass. No entity rerenders or world scans. */
public final class WhiteHotRender {
    static ShaderInstance maskShader,compositeShader;
    private static boolean failed;
    private static int nesting;
    private static Frame frame;
    private static final int MAX_BATCHES=256,MAX_VERTICES=1_000_000;
    private static final List<Batch> pool=new ArrayList<>();
    private static final List<GpuShot> gpuPool=new ArrayList<>();
    private static final IdentityHashMap<RenderType,Boolean> MATERIALS=new IdentityHashMap<>();
    private static final IdentityHashMap<RenderType,Batch> consolidated=new IdentityHashMap<>();
    private static final LinkedHashMap<RenderTarget,Targets> targets=new LinkedHashMap<>();
    private static final ByteBufferBuilder quadBytes=new ByteBufferBuilder(256);
    private WhiteHotRender(){}
    public static boolean available(){return !failed&&maskShader!=null&&compositeShader!=null;}
    public static void reload(){MATERIALS.clear();release();failed=false;maskShader=compositeShader=null;}
    public static void release(){
        for(Targets t:targets.values())t.close();targets.clear();discard();frame=null;nesting=0;
    }
    static void releaseTarget(RenderTarget target){Targets t=targets.remove(target);if(t!=null)t.close();}
    private static void fail(RuntimeException e){
        if(!failed)SbwDroneRangeConfig.LOGGER.warn("White Hot unavailable; restoring normal camera image until reload or retry",e);
        failed=true;
    }
    static void retry(){failed=false;}
    public static void begin(){
        if(nesting++>0){if(frame!=null)frame.aborted=true;return;}
        frame=null;
        Minecraft m=Minecraft.getInstance();
        if(!available()||m.getOverlay()!=null||!DroneThermalVisionClient.renderThisCamera())return;
        consolidated.clear();frame=new Frame(m.getMainRenderTarget());
    }
    public static MultiBufferSource wrap(MultiBufferSource source){
        if(nesting!=1||frame==null||frame.aborted)return source;
        return new Wrapped(source);
    }
    private record Wrapped(MultiBufferSource source) implements MultiBufferSource{
        @Override public VertexConsumer getBuffer(RenderType type){
            VertexConsumer normal=source.getBuffer(type);
            Frame f=frame;if(f==null||f.aborted||ignored(type))return normal;
            try{
                if(f.projection==null){f.projection=new Matrix4f(RenderSystem.getProjectionMatrix());f.modelView=new Matrix4f(RenderSystem.getModelViewMatrix());}
                Batch b=type.canConsolidateConsecutiveGeometry()?consolidated.get(type):null;
                if(b==null){
                    if(f.used>=MAX_BATCHES)throw new IllegalStateException("White Hot material budget exceeded");
                    if(f.used==pool.size())pool.add(new Batch());b=pool.get(f.used++);b.start(type);
                    if(type.canConsolidateConsecutiveGeometry())consolidated.put(type,b);
                }
                return b.adapter.bind(normal);
            }catch(RuntimeException e){f.aborted=true;fail(e);return normal;}
        }
    }
    public static boolean gpuSource(MultiBufferSource source){return source instanceof Wrapped wrapped&&wrapped.source() instanceof MultiBufferSource.BufferSource&&Adv01Gpu.heatAvailable()&&frame!=null&&!frame.aborted;}
    static void captureGpu(MultiBufferSource source,Adv01Gpu.Batch batch,RenderType type,float alpha){
        Frame f=frame;if(!gpuSource(source)||f==null||ignored(type))return;
        if(f.used+f.gpuUsed>=MAX_BATCHES){f.aborted=true;fail(new IllegalStateException("White Hot mesh budget exceeded"));return;}
        if(f.projection==null){f.projection=new Matrix4f(RenderSystem.getProjectionMatrix());f.modelView=new Matrix4f(RenderSystem.getModelViewMatrix());}
        if(f.gpuUsed==gpuPool.size())gpuPool.add(new GpuShot());gpuPool.get(f.gpuUsed++).capture(batch,type,alpha);
    }
    private static final class GpuShot{
        Adv01Gpu.Batch batch;RenderType type;float alpha;float[]poses;
        void capture(Adv01Gpu.Batch b,RenderType t,float a){batch=b;type=t;alpha=a;if(poses==null||poses.length!=b.palette.capacity())poses=new float[b.palette.capacity()];for(int i=0;i<poses.length;i++)poses[i]=b.palette.get(i);}
        void draw(RenderTarget target){for(int i=0;i<poses.length;i++)batch.palette.put(i,poses[i]);batch.drawHeat(type,target,alpha);}
        void clear(){batch=null;type=null;}
    }
    private static boolean ignored(RenderType type){if(MATERIALS.size()>2048)MATERIALS.clear();return !MATERIALS.computeIfAbsent(type,t->WhiteHotMaterials.capture(t.toString()));}
    public static void end(){
        if(nesting==0)return;if(--nesting!=0)return;
        Frame f=frame;if(f==null)return;
        Targets t=null;boolean copied=false;
        try(WhiteHotState state=new WhiteHotState()){
            try{
                if(f.aborted||!available())return;
                t=targetFor(f);
                GL11.glDisable(GL11.GL_STENCIL_TEST);RenderSystem.disableScissor();copy(f.target,t.scene,GL11.GL_COLOR_BUFFER_BIT|(f.solidDepth?0:GL11.GL_DEPTH_BUFFER_BIT));copied=true;
                RenderSystem.depthMask(true);RenderSystem.colorMask(true,true,true,true);t.mask.setClearColor(0,0,0,0);t.mask.clear(Minecraft.ON_OSX);
                if(f.projection!=null){
                    RenderSystem.setProjectionMatrix(f.projection,RenderSystem.getVertexSorting());
                    var mv=RenderSystem.getModelViewStack();mv.pushMatrix();mv.set(f.modelView);RenderSystem.applyModelViewMatrix();
                    try{for(int i=0;i<f.used;i++)pool.get(i).draw(t.mask);for(int i=0;i<f.gpuUsed;i++)gpuPool.get(i).draw(t.mask);}finally{mv.popMatrix();RenderSystem.applyModelViewMatrix();}
                }
                if(f.aborted)return;
                f.target.bindWrite(true);RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableBlend();RenderSystem.disableCull();RenderSystem.colorMask(true,true,true,true);
                RenderSystem.setShader(()->compositeShader);RenderSystem.setShaderColor(1,1,1,1);
                RenderSystem.setShaderTexture(0,t.scene.getColorTextureId());RenderSystem.setShaderTexture(1,t.mask.getColorTextureId());RenderSystem.setShaderTexture(2,t.scene.getDepthTextureId());RenderSystem.setShaderTexture(3,t.mask.getDepthTextureId());
                var quad=new BufferBuilder(quadBytes,VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
                quad.addVertex(-1,-1,0).setUv(0,0);quad.addVertex(1,-1,0).setUv(1,0);quad.addVertex(1,1,0).setUv(1,1);quad.addVertex(-1,1,0).setUv(0,1);BufferUploader.drawWithShader(quad.buildOrThrow());checkGl();
            }catch(RuntimeException e){if(copied&&t!=null)copy(t.scene,f.target,GL11.GL_COLOR_BUFFER_BIT);fail(e);}
        }finally{discard();frame=null;consolidated.clear();}
    }
    private static Targets targetFor(Frame f){
        Targets t=targets.get(f.target);
        if(t!=null&&(t.width!=f.target.width||t.height!=f.target.height||t.stencil!=f.target.isStencilEnabled())){t.close();targets.remove(f.target);t=null;}
        if(t==null){
            if(targets.size()>=3){var it=targets.entrySet().iterator();var old=it.next();old.getValue().close();it.remove();}
            t=new Targets(f.target);targets.put(f.target,t);
        }
        return t;
    }
    /** Snapshot opaque occluders before translucent terrain, using the same normal buffers. */
    public static void solidDepth(){
        Frame f=frame;if(nesting!=1||f==null||f.aborted)return;
        try(WhiteHotState state=new WhiteHotState()){
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            RenderSystem.disableScissor();copy(f.target,targetFor(f).scene,GL11.GL_DEPTH_BUFFER_BIT);f.solidDepth=true;
        }catch(RuntimeException e){f.aborted=true;fail(e);}
    }
    private static void checkGl(){int error=GL11.glGetError();if(error!=GL11.GL_NO_ERROR)throw new IllegalStateException("Unsupported White Hot framebuffer/render state: 0x"+Integer.toHexString(error));}
    private static void copy(RenderTarget a,RenderTarget b,int flags){
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,a.frameBufferId);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,b.frameBufferId);
        GL30.glBlitFramebuffer(0,0,a.width,a.height,0,0,b.width,b.height,flags,GL11.GL_NEAREST);checkGl();
    }
    private static void discard(){for(Batch b:pool)b.discard();for(GpuShot shot:gpuPool)shot.clear();}
    private static final class Frame {
        final RenderTarget target;int used,gpuUsed,vertices;boolean aborted,solidDepth;Matrix4f projection,modelView;
        Frame(RenderTarget target){this.target=target;}
    }
    private static final class Targets implements AutoCloseable {
        final int width,height;final boolean stencil;final RenderTarget scene,mask;
        Targets(RenderTarget original){
            width=original.width;height=original.height;stencil=original.isStencilEnabled();
            RenderTarget a=new TextureTarget(width,height,true,Minecraft.ON_OSX),b=null;
            try{if(stencil)a.enableStencil();b=new TextureTarget(width,height,true,Minecraft.ON_OSX);}catch(RuntimeException e){a.destroyBuffers();if(b!=null)b.destroyBuffers();throw e;}
            scene=a;mask=b;
        }
        public void close(){scene.destroyBuffers();mask.destroyBuffers();}
    }
    static final class Batch {
        final ByteBufferBuilder bytes=new ByteBufferBuilder(8192);BufferBuilder buffer;
        final WhiteHotVertex adapter=new WhiteHotVertex(this);
        RenderType original;int emitted;boolean started;boolean textured;float cutoff;
        void start(RenderType type){emitted=0;original=type;textured=type.format().getElementAttributeNames().contains("UV0");cutoff=type.toString().contains("translucent")?.01f:.1f;buffer=new BufferBuilder(bytes,type.mode(),DefaultVertexFormat.POSITION_TEX_COLOR);started=true;}
        void vertex(float x,float y,float z,float u,float v,int alpha){
            Frame f=frame;if(f==null||f.aborted)return;
            if(++f.vertices>MAX_VERTICES){f.aborted=true;fail(new IllegalStateException("White Hot vertex budget exceeded"));return;}
            emitted++;
            buffer.addVertex(x,y,z).setColor(255,255,255,alpha).setUv(u,v);
        }
        void draw(RenderTarget mask){
            adapter.finish();if(!started)return;if(emitted==0){discard();return;}
            original.setupRenderState();
            try{
                mask.bindWrite(true);GL11.glDisable(GL11.GL_STENCIL_TEST);RenderSystem.disableScissor();RenderSystem.enableDepthTest();RenderSystem.depthFunc(GL11.GL_LEQUAL);RenderSystem.depthMask(true);RenderSystem.colorMask(true,true,true,true);RenderSystem.disableBlend();
                RenderSystem.setShader(()->maskShader);RenderSystem.setShaderColor(1,1,1,1);maskShader.safeGetUniform("UseTexture").set(textured?1:0);maskShader.safeGetUniform("AlphaCutoff").set(cutoff);
                BufferUploader.drawWithShader(buffer.buildOrThrow());started=false;
            }finally{original.clearRenderState();}
        }
        void discard(){bytes.clear();buffer=null;started=false;original=null;adapter.clear();}
    }
}
