package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

/** One state snapshot per processed camera image, never per entity or material. */
final class WhiteHotState implements AutoCloseable {
    private final int draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING),read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
    private final int[] viewport=new int[4],scissor=new int[4],textures=new int[12],nativeTextures=new int[4];
    private final boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),scissored=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST),offset=GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL),stencil=GL11.glIsEnabled(GL11.GL_STENCIL_TEST),depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final int depthFunction=GL11.glGetInteger(GL11.GL_DEPTH_FUNC),srcRgb=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dstRgb=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),srcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),eqRgb=GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB),eqAlpha=GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA),program=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),activeTexture=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final float offsetFactor=GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_FACTOR),offsetUnits=GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_UNITS);
    private final int oldLastProgram=nl.smartstreamlabs.sbwdroneconfig.mixin.WhiteHotShaderAccessor.drone$lastProgram();
    private final ShaderInstance oldLastShader=nl.smartstreamlabs.sbwdroneconfig.mixin.WhiteHotShaderAccessor.drone$lastShader();
    private final ShaderInstance shader=RenderSystem.getShader();
    private final float[] color=RenderSystem.getShaderColor().clone();
    private final Matrix4f projection=new Matrix4f(RenderSystem.getProjectionMatrix()),texture=new Matrix4f(RenderSystem.getTextureMatrix());
    private final com.mojang.blaze3d.vertex.VertexSorting sorting=RenderSystem.getVertexSorting();
    private final boolean[] colorMask=new boolean[4];
    WhiteHotState(){
        GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX,scissor);
        for(int i=0;i<textures.length;i++)textures[i]=RenderSystem.getShaderTexture(i);
        for(int i=0;i<nativeTextures.length;i++){RenderSystem.activeTexture(GL13.GL_TEXTURE0+i);nativeTextures[i]=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);}RenderSystem.activeTexture(activeTexture);
        try(var s=org.lwjgl.system.MemoryStack.stackPush()){var b=s.malloc(4);GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK,b);for(int i=0;i<4;i++)colorMask[i]=b.get(i)!=0;}
    }
    @Override public void close(){
        // Forge ShaderInstance.apply restores its BlendMode; restore explicit GL state afterwards.
        RenderSystem.setShader(()->shader);if(shader!=null)shader.apply();GL20.glUseProgram(program);
        nl.smartstreamlabs.sbwdroneconfig.mixin.WhiteHotShaderAccessor.drone$lastProgram(oldLastProgram);nl.smartstreamlabs.sbwdroneconfig.mixin.WhiteHotShaderAccessor.drone$lastShader(oldLastShader);
        BufferUploader.reset();GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);
        RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
        if(scissored)RenderSystem.enableScissor(scissor[0],scissor[1],scissor[2],scissor[3]);else RenderSystem.disableScissor();
        if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();RenderSystem.depthFunc(depthFunction);RenderSystem.depthMask(depthMask);
        if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(srcRgb,dstRgb,srcAlpha,dstAlpha);RenderSystem.blendEquation(eqRgb);if(eqAlpha!=eqRgb)GL20.glBlendEquationSeparate(eqRgb,eqAlpha);
        if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
        RenderSystem.polygonOffset(offsetFactor,offsetUnits);if(offset)RenderSystem.enablePolygonOffset();else RenderSystem.disablePolygonOffset();
        if(stencil)GL11.glEnable(GL11.GL_STENCIL_TEST);else GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.colorMask(colorMask[0],colorMask[1],colorMask[2],colorMask[3]);
        RenderSystem.setProjectionMatrix(projection,sorting);RenderSystem.setTextureMatrix(texture);RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);
        for(int i=0;i<textures.length;i++)RenderSystem.setShaderTexture(i,textures[i]);
        for(int i=0;i<nativeTextures.length;i++){RenderSystem.activeTexture(GL13.GL_TEXTURE0+i);com.mojang.blaze3d.platform.GlStateManager._bindTexture(nativeTextures[i]);}RenderSystem.activeTexture(activeTexture);
    }
}
