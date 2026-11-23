package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Copies already animated/model-transformed vertices; the normal renderer still runs once. */
final class WhiteHotVertex implements VertexConsumer {
    private final WhiteHotRender.Batch batch;
    private VertexConsumer normal;private float shaderAlpha=1;
    private double x,y,z;private float u,v;private int alpha=255,defaultAlpha=255;
    WhiteHotVertex(WhiteHotRender.Batch batch){this.batch=batch;}
    VertexConsumer bind(VertexConsumer normal){this.normal=normal;shaderAlpha=com.mojang.blaze3d.systems.RenderSystem.getShaderColor()[3];return this;}
    @Override public VertexConsumer vertex(double x,double y,double z){normal.vertex(x,y,z);this.x=x;this.y=y;this.z=z;u=v=0;alpha=defaultAlpha;return this;}
    @Override public VertexConsumer color(int r,int g,int b,int a){normal.color(r,g,b,a);alpha=a;return this;}
    @Override public VertexConsumer uv(float u,float v){normal.uv(u,v);this.u=u;this.v=v;return this;}
    @Override public VertexConsumer overlayCoords(int u,int v){normal.overlayCoords(u,v);return this;}
    @Override public VertexConsumer uv2(int u,int v){normal.uv2(u,v);return this;}
    @Override public VertexConsumer normal(float x,float y,float z){normal.normal(x,y,z);return this;}
    @Override public void endVertex(){normal.endVertex();batch.vertex((float)x,(float)y,(float)z,u,v,(int)(alpha*shaderAlpha));}
    @Override public void defaultColor(int r,int g,int b,int a){normal.defaultColor(r,g,b,a);defaultAlpha=a;}
    @Override public void unsetDefaultColor(){normal.unsetDefaultColor();defaultAlpha=255;}
    void finish(){}
    void clear(){normal=null;}
}
