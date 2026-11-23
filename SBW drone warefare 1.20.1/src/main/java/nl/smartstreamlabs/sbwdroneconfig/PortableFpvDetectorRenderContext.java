package nl.smartstreamlabs.sbwdroneconfig;

public final class PortableFpvDetectorRenderContext {
    private static int offscreenRenderDepth;

    private PortableFpvDetectorRenderContext() {
    }

    static void beginOffscreenRender() {
        offscreenRenderDepth++;
    }

    static void endOffscreenRender() {
        if (offscreenRenderDepth > 0) {
            offscreenRenderDepth--;
        }
    }

    public static boolean isOffscreenRendering() {
        return offscreenRenderDepth > 0;
    }
}
