package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

/** A single sorted, softly blended batch after clouds/weather, with terrain depth testing.
 * Delaying the batch prevents later vanilla clouds cutting rectangles through the plume. */
public final class ExplosionVfxRender {
    private static ShaderInstance shader;
    private static final ArrayList<ExplosionLobeParticle> SORTED = new ArrayList<>(256);
    private static float fogStart, fogEnd;
    private static final float[] fogColor = new float[4];
    private static com.mojang.blaze3d.shaders.FogShape fogShape;
    private ExplosionVfxRender() {}
    public static void register(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID,"explosion_soft"), DefaultVertexFormat.PARTICLE), s -> shader = s);
        } catch (IOException e) { throw new java.io.UncheckedIOException("Could not load Drone Warfare soft explosion renderer",e); }
    }
    public static void captureFog() {
        fogStart=RenderSystem.getShaderFogStart();fogEnd=RenderSystem.getShaderFogEnd();
        System.arraycopy(RenderSystem.getShaderFogColor(),0,fogColor,0,4);
        fogShape=RenderSystem.getShaderFogShape();
    }
    public static void draw(List<ExplosionLobeParticle> particles, Camera camera, float partial, Matrix4f view) {
        SORTED.clear();
        for (var p:particles) if(p.isAlive()) SORTED.add(p);
        if(SORTED.isEmpty() || shader==null) return;
        Vec3 eye=camera.getPosition();
        SORTED.sort((a,b)->Double.compare(b.distanceSquared(eye),a.distanceSquared(eye)));
        Minecraft mc=Minecraft.getInstance();
        var oldShader=RenderSystem.getShader();
        float oldStart=RenderSystem.getShaderFogStart(),oldEnd=RenderSystem.getShaderFogEnd();
        float[] oldColor=RenderSystem.getShaderFogColor().clone();
        var oldShape=RenderSystem.getShaderFogShape();
        var modelView=RenderSystem.getModelViewStack();
        modelView.pushMatrix();modelView.set(view);
        RenderSystem.applyModelViewMatrix();
        try {
            mc.getMainRenderTarget().bindWrite(false);
            mc.gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.enableDepthTest();RenderSystem.depthMask(false);
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(()->shader);RenderSystem.setShaderTexture(0,TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.setShaderFogStart(fogStart);RenderSystem.setShaderFogEnd(fogEnd);
            RenderSystem.setShaderFogColor(fogColor[0],fogColor[1],fogColor[2],fogColor[3]);
            if(fogShape!=null)RenderSystem.setShaderFogShape(fogShape);
            var buffer=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);
            for(var p:SORTED)p.render(buffer,camera,partial);
            var mesh=buffer.build();if(mesh!=null)BufferUploader.drawWithShader(mesh);
        } finally {
            RenderSystem.depthMask(true);RenderSystem.disableBlend();
            mc.gameRenderer.lightTexture().turnOffLightLayer();
            RenderSystem.setShaderFogStart(oldStart);RenderSystem.setShaderFogEnd(oldEnd);
            RenderSystem.setShaderFogColor(oldColor[0],oldColor[1],oldColor[2],oldColor[3]);
            RenderSystem.setShaderFogShape(oldShape);RenderSystem.setShader(()->oldShader);
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();SORTED.clear();
        }
    }
}
