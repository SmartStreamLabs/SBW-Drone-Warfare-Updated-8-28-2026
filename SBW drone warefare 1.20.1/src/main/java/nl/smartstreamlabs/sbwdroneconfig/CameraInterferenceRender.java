package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.*;
import java.util.*;

/** One postprocess of the existing video image, before HUD/UI. No second world pass. */
public final class CameraInterferenceRender {
    private static ShaderInstance shader;
    private static boolean failed;
    private static final LinkedHashMap<RenderTarget,Slot> slots=new LinkedHashMap<>();
    private static final BufferBuilder quad=new BufferBuilder(256);
    public static void shaders(net.minecraftforge.client.event.RegisterShadersEvent event){
        release();CameraInterferenceClient.reset();shader=null;failed=false;
        try{event.registerShader(new ShaderInstance(event.getResourceProvider(),new net.minecraft.resources.ResourceLocation(SbwDroneRangeConfig.MOD_ID,"camera_interference"),DefaultVertexFormat.POSITION_TEX),s->shader=s);}
        catch(java.io.IOException|RuntimeException e){failed=true;SbwDroneRangeConfig.LOGGER.warn("Drone video interference shader unavailable; retaining the normal feed",e);}
    }
    static void retain(UUID full,UUID preview){var it=slots.entrySet().iterator();while(it.hasNext()){var e=it.next();if(!DvtClient.ownsTarget(e.getKey())&&!e.getValue().id.equals(e.getValue().preview?preview:full)){e.getValue().close();it.remove();}}}
    public static void release(){for(var s:slots.values())s.close();slots.clear();}
    static void releaseTarget(RenderTarget target){var s=slots.remove(target);if(s!=null)s.close();}
    public static boolean available(){return shader!=null&&!failed;}
    public static int targetCount(){return slots.size();}
    public static void process(float partial){
        var view=CameraInterferenceClient.rendering();if(view==null||!available())return;
        float intensity=Math.min(1,(view.before+(view.amount-view.before)*Math.max(0,Math.min(1,partial)))*CameraInterferenceConfig.STRENGTH.get().floatValue());
        if(intensity<.002&&!view.lost){releaseTarget(Minecraft.getInstance().getMainRenderTarget());return;}
        var original=Minecraft.getInstance().getMainRenderTarget();Slot slot=null;boolean copied=false;
        try(WhiteHotState saved=new WhiteHotState()){
            try{
                slot=slot(original,view.id);RenderSystem.disableScissor();GL11.glDisable(GL11.GL_STENCIL_TEST);
                copy(original,slot.scene);copied=true;
                boolean stutters=CameraInterferenceConfig.STUTTERS.get()&&intensity>.35&&!view.lost;
                if(stutters&&slot.history==null)slot.history=new TextureTarget(original.width,original.height,false,Minecraft.ON_OSX);
                if(!stutters&&slot.history!=null){slot.history.destroyBuffers();slot.history=null;slot.hasHistory=false;}
                boolean frozen=stutters&&slot.hasHistory&&System.nanoTime()<view.freezeUntil;
                if(stutters&&!frozen){copy(slot.scene,slot.history);slot.hasHistory=true;}
                original.bindWrite(true);RenderSystem.viewport(0,0,original.viewWidth,original.viewHeight);RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableBlend();RenderSystem.disableCull();RenderSystem.colorMask(true,true,true,true);
                RenderSystem.setShader(()->shader);RenderSystem.setShaderColor(1,1,1,1);
                RenderSystem.setShaderTexture(0,slot.scene.getColorTextureId());RenderSystem.setShaderTexture(1,frozen?slot.history.getColorTextureId():slot.scene.getColorTextureId());
                shader.safeGetUniform("Intensity").set(intensity);shader.safeGetUniform("Lost").set(view.lost?1f:0f);shader.safeGetUniform("Frozen").set(frozen?1f:0f);shader.safeGetUniform("Clock").set((System.nanoTime()%3_600_000_000_000L)/1_000_000_000f);shader.safeGetUniform("Seed").set((view.seed&65535)*.031f);shader.safeGetUniform("Resolution").set((float)original.viewWidth,(float)original.viewHeight);
                quad.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);quad.vertex(-1,-1,0).uv(0,0).endVertex();quad.vertex(1,-1,0).uv(1,0).endVertex();quad.vertex(1,1,0).uv(1,1).endVertex();quad.vertex(-1,1,0).uv(0,1).endVertex();BufferUploader.drawWithShader(quad.end());
                int error=GL11.glGetError();if(error!=GL11.GL_NO_ERROR)throw new IllegalStateException("Interference framebuffer error 0x"+Integer.toHexString(error));
            }catch(RuntimeException e){if(copied&&slot!=null)copy(slot.scene,original);failed=true;SbwDroneRangeConfig.LOGGER.warn("Drone video interference disabled until resource reload",e);release();}
        }
    }
    private static Slot slot(RenderTarget target,UUID id){
        var s=slots.get(target);if(s!=null&&(!s.id.equals(id)||s.width!=target.width||s.height!=target.height)){s.close();slots.remove(target);s=null;}
        if(s==null){if(slots.size()>=(DvtClient.feedCount()>0?4:2)){var it=slots.entrySet().iterator();var old=it.next();old.getValue().close();it.remove();}s=new Slot(id,target);slots.put(target,s);}return s;
    }
    private static void copy(RenderTarget from,RenderTarget to){GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,from.frameBufferId);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,to.frameBufferId);GL30.glBlitFramebuffer(0,0,from.width,from.height,0,0,to.width,to.height,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_NEAREST);}
    private static final class Slot {
        final UUID id;final boolean preview;final int width,height;final RenderTarget scene;RenderTarget history;boolean hasHistory;
        Slot(UUID id,RenderTarget t){this.id=id;preview=PortableFpvDetectorRenderContext.isOffscreenRendering();width=t.width;height=t.height;scene=new TextureTarget(width,height,false,Minecraft.ON_OSX);}
        void close(){scene.destroyBuffers();if(history!=null)history.destroyBuffers();}
    }
    private CameraInterferenceRender(){}
}
