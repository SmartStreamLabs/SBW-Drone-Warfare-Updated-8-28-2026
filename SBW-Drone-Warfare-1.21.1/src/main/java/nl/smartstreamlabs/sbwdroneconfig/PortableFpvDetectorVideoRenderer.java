package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

final class PortableFpvDetectorVideoRenderer {
    private static RenderTarget videoTarget;
    private static int targetWidth;
    private static int targetHeight;
    private static boolean rendering;
    private static Object cachedLevel;
    private static java.util.UUID cachedDrone;
    private static boolean validFrame;
    private static long nextCaptureNanos;
    // Bounded diagnostics also used by the native performance check; no per-frame logging.
    private static long captureCount, captureNanos;

    private static long lastFailureMillis;

    private PortableFpvDetectorVideoRenderer() {
    }

    static boolean render(GuiGraphics guiGraphics, PortableFpvDetectorTarget target, int x, int y, int width, int height) {
        return render(guiGraphics, target, x, y, width, height, DroneJammerClient.latestScan().videoEnabled());
    }

    static boolean render(GuiGraphics guiGraphics, PortableFpvDetectorTarget target, int x, int y, int width, int height, boolean videoEnabled) {
        if (rendering || target == null || !target.cameraAvailable() || !videoEnabled) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.gameRenderer == null || minecraft.getMainRenderTarget() == null) {
            return false;
        }

        Entity drone = minecraft.level.getEntity(target.entityId());
        if (drone == null || !drone.isAlive() || !target.droneId().equals(drone.getUUID()) || !SbwCompat.isDrone(drone)) {
            return false;
        }

        width = Math.max(16, width);
        height = Math.max(16, height);
        double scale=Math.min(1d,Math.min(320d/width,180d/height));
        ensureTarget(Math.max(16,(int)Math.round(width*scale)),Math.max(16,(int)Math.round(height*scale)));
        if(cachedLevel!=minecraft.level||!target.droneId().equals(cachedDrone)){
            cachedLevel=minecraft.level;cachedDrone=target.droneId();validFrame=false;nextCaptureNanos=0;
        }
        long captureStart=System.nanoTime();
        if(captureStart<nextCaptureNanos){
            if(validFrame){guiGraphics.flush();drawTargetTexture(guiGraphics,x,y,width,height);}
            return validFrame;
        }
        // Failure paths are throttled too. Never show an earlier drone's image.
        nextCaptureNanos=captureStart+1_000_000_000L;
        validFrame=false;

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        Entity previousCameraEntity = minecraft.getCameraEntity();
        CameraType previousCameraType = minecraft.options.getCameraType();

        guiGraphics.flush();
        rendering = true;
        PortableFpvDetectorRenderContext.beginOffscreenRender();
        // A nested world render must not inherit the GUI's -11000 Z model-view translation.
        var videoModelView=RenderSystem.getModelViewStack();
        videoModelView.pushMatrix().identity();
        RenderSystem.applyModelViewMatrix();

        try {
            RenderSystem.backupProjectionMatrix();
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.setCameraEntity(drone);
            DroneClientViewContext.beginReadOnlyCameraIntercept(drone.getUUID());
            minecraft.gameRenderer.setRenderHand(false);
            minecraft.gameRenderer.setRenderBlockOutline(false);

            ((nl.smartstreamlabs.sbwdroneconfig.mixin.WhiteHotTargetAccessor)(Object)minecraft).drone$setVideoTarget(videoTarget);
            RenderSystem.disableScissor();
            RenderSystem.depthMask(true);RenderSystem.enableDepthTest();RenderSystem.disableBlend();RenderSystem.setShaderColor(1,1,1,1);
            videoTarget.clear(Minecraft.ON_OSX);
            videoTarget.bindWrite(true);
            minecraft.gameRenderer.renderLevel(DeltaTracker.ZERO);
        } catch (RuntimeException exception) {
            maybeLogFailure(exception);
            return false;
        } finally {
            videoTarget.unbindWrite();
            ((nl.smartstreamlabs.sbwdroneconfig.mixin.WhiteHotTargetAccessor)(Object)minecraft).drone$setVideoTarget(mainTarget);
            DroneClientViewContext.endReadOnlyCameraIntercept();
            Entity restoredCameraEntity = previousCameraEntity == null ? minecraft.player : previousCameraEntity;
            minecraft.setCameraEntity(restoredCameraEntity);
            minecraft.options.setCameraType(previousCameraType);
            if (minecraft.level != null && restoredCameraEntity != null) {
                minecraft.gameRenderer.getMainCamera().setup(
                        minecraft.level,
                        restoredCameraEntity,
                        !previousCameraType.isFirstPerson(),
                        previousCameraType.isMirrored(),
                        0.0F
                );
            }
            minecraft.gameRenderer.setRenderHand(true);
            minecraft.gameRenderer.setRenderBlockOutline(true);
            mainTarget.bindWrite(true);
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.viewport(0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            RenderSystem.disableScissor();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            videoModelView.popMatrix();RenderSystem.applyModelViewMatrix();
            PortableFpvDetectorRenderContext.endOffscreenRender();
            rendering = false;
        }

        long cost=Math.max(1,System.nanoTime()-captureStart);
        captureCount++;captureNanos+=cost;
        // At most 10 camera renders/sec; expensive captures reduce the refresh rate further.
        // No catch-up loop after a stall or a hidden window.
        nextCaptureNanos=System.nanoTime()+Math.max(100_000_000L,Math.min(1_000_000_000L,cost*4));
        validFrame=true;

        drawTargetTexture(guiGraphics, x, y, width, height);
        return true;
    }

    static void release() {
        validFrame=false;cachedLevel=null;cachedDrone=null;nextCaptureNanos=0;
        if (videoTarget != null) {
            WhiteHotRender.releaseTarget(videoTarget);
            CameraInterferenceRender.releaseTarget(videoTarget);
            videoTarget.destroyBuffers();
            videoTarget = null;
            targetWidth = 0;
            targetHeight = 0;
        }
    }

    private static void ensureTarget(int width, int height) {
        if (videoTarget != null && targetWidth == width && targetHeight == height) {
            return;
        }
        release();
        videoTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        videoTarget.setClearColor(0.0F, 0.015F, 0.0F, 1.0F);
        targetWidth = width;
        targetHeight = height;
    }

    private static void drawTargetTexture(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, videoTarget.getColorTextureId());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, x, y + height, 0.0F).setUv(0.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y + height, 0.0F).setUv(1.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y, 0.0F).setUv(1.0F, 1.0F);
        buffer.addVertex(matrix, x, y, 0.0F).setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void maybeLogFailure(RuntimeException exception) {
        long now = System.currentTimeMillis();
        if (now - lastFailureMillis < 5000L) {
            return;
        }
        lastFailureMillis = now;
        SbwDroneRangeConfig.LOGGER.warn("Portable FPV detector video render failed safely.", exception);
    }
}
