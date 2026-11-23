package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.VertexConsumer;
/** Same animated vertex stream, with 1.21's implicit vertex completion. */
final class WhiteHotVertex implements VertexConsumer {
    private final WhiteHotRender.Batch batch;private VertexConsumer normal;
    private float x,y,z,u,v;private int alpha=255;private float shaderAlpha=1;private boolean pending;
    WhiteHotVertex(WhiteHotRender.Batch batch){this.batch=batch;}
    VertexConsumer bind(VertexConsumer normal){finish();this.normal=normal;shaderAlpha=com.mojang.blaze3d.systems.RenderSystem.getShaderColor()[3];return this;}
    @Override public VertexConsumer addVertex(float x,float y,float z){finish();normal.addVertex(x,y,z);this.x=x;this.y=y;this.z=z;u=v=0;alpha=255;pending=true;return this;}
    @Override public VertexConsumer setColor(int r,int g,int b,int a){normal.setColor(r,g,b,a);alpha=a;return this;}
    @Override public VertexConsumer setUv(float u,float v){normal.setUv(u,v);this.u=u;this.v=v;return this;}
    @Override public VertexConsumer setUv1(int u,int v){normal.setUv1(u,v);return this;}
    @Override public VertexConsumer setUv2(int u,int v){normal.setUv2(u,v);return this;}
    @Override public VertexConsumer setNormal(float x,float y,float z){normal.setNormal(x,y,z);return this;}
    void finish(){if(pending){pending=false;batch.vertex(x,y,z,u,v,(int)(alpha*shaderAlpha));}}
    void clear(){pending=false;normal=null;}
}
