package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.Vec3;
import java.util.WeakHashMap;

/** Original shared cached mesh; effects use a fixed tiny mesh and at most 24 cosmetic casings per vehicle. */
public final class Adv01Renderer extends com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer<Adv01Entity> implements FastMeshRenderer<Adv01Entity> {
    private Adv01Gpu.Batch gpuBatch;
    private final WeakHashMap<Adv01Entity,Effects> effects=new WeakHashMap<>();
    private static final float[][] BOX={{-1,-1,-1,1,-1,-1,1,1,-1,-1,1,-1},{-1,-1,1,-1,1,1,1,1,1,1,-1,1},{-1,-1,-1,-1,1,-1,-1,1,1,-1,-1,1},{1,-1,-1,1,-1,1,1,1,1,1,1,-1},{-1,-1,-1,-1,-1,1,1,-1,1,1,-1,-1},{-1,1,-1,1,1,-1,1,1,1,-1,1,1}};
    private static final float[][] FLASH={{-.13f,0,0,.13f,0,0,.025f,0,.55f,-.025f,0,.55f},{0,-.13f,0,0,.13f,0,0,.025f,.55f,0,-.025f,.55f}};
    public Adv01Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context){super(context,new Adv01Model());shadowRadius=2;}
    @Override public void vehicleAxis(Adv01Entity e,PoseStack poses,float yaw,float partial){
        var matrix=e.getVehicleTransform(partial);var at=matrix.transformPosition(new org.joml.Vector3d()).sub(e.getPosition(partial).x,e.getPosition(partial).y,e.getPosition(partial).z);
        poses.translate(at.x,at.y,at.z);poses.mulPose(matrix.getUnnormalizedRotation(new org.joml.Quaterniond()).get(new org.joml.Quaternionf()));
    }
    @Override protected void applyRotations(Adv01Entity e,PoseStack p,float age,float yaw,float partial,float scale){}
    @Override public RenderType getRenderType(Adv01Entity e,net.minecraft.resources.ResourceLocation texture,MultiBufferSource buffers,float partial){return RenderType.entityCutoutNoCull(getTextureLocation(e));}
    @Override public void renderCube(PoseStack p,software.bernie.geckolib.cache.object.GeoCube c,VertexConsumer v,int light,int overlay,int color){if(gpuBatch!=null){gpuBatch.pose(c,p);return;}FastMeshRenderer.super.renderCube(p,c,v,light,overlay,color);}
    @Override public void actuallyRender(PoseStack p,Adv01Entity e,software.bernie.geckolib.cache.object.BakedGeoModel model,RenderType type,MultiBufferSource buffers,VertexConsumer consumer,boolean rerender,float partial,int light,int overlay,int color){
        var previous=gpuBatch;var batch=!rerender&&type!=null&&(buffers instanceof MultiBufferSource.BufferSource&&consumer instanceof BufferBuilder||WhiteHotRender.gpuSource(buffers))?Adv01Gpu.batch(model):null;gpuBatch=batch;
        if(batch!=null)batch.begin();try{super.actuallyRender(p,e,model,type,buffers,consumer,rerender,partial,light,overlay,color);if(batch!=null){WhiteHotRender.captureGpu(buffers,batch,type,((color>>>24)&255)/255f);batch.draw(type,light,overlay,((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,((color>>>24)&255)/255f);}}finally{gpuBatch=previous;}
    }
    private static void mesh(PoseStack p,VertexConsumer v,float[][] mesh,int r,int g,int b,int alpha){for(var face:mesh)for(int i=0;i<face.length;i+=3)v.addVertex(p.last().pose(),face[i],face[i+1],face[i+2]).setColor(r,g,b,alpha);}
    @Override public void render(Adv01Entity e,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){
        super.render(e,yaw,partial,p,buffers,light);var fx=effects.computeIfAbsent(e,k->new Effects());fx.update(e);
        var vertex=buffers.getBuffer(RenderType.lightning());var base=e.getPosition(partial);double age=e.level().getGameTime()+partial-e.shotTime();
        if(age>=0&&age<2){var gun=e.getBarrelTransform(partial).translate(0,.065,2.63);gun.setTranslation(gun.m30()-base.x,gun.m31()-base.y,gun.m32()-base.z);p.pushPose();p.last().pose().mul(new org.joml.Matrix4f(gun));mesh(p,vertex,FLASH,255,216,125,(int)(220*(1-age/2)));p.popPose();}
        for(var c:fx.casings)if(c.active){p.pushPose();p.translate(c.x-base.x,c.y-base.y,c.z-base.z);p.mulPose(com.mojang.math.Axis.XP.rotation((float)c.age*.55f));p.scale(.018f,.018f,.065f);mesh(p,vertex,BOX,178,141,65,255);p.popPose();}
        if(e.lights())for(double x:new double[]{-1.16,1.16}){var at=Adv01Entity.vec(e.getVehicleTransform(partial).transformPosition(new org.joml.Vector3d(x,1.284,3.507)));p.pushPose();p.translate(at.x-base.x,at.y-base.y,at.z-base.z);p.mulPose(e.getVehicleTransform(partial).getUnnormalizedRotation(new org.joml.Quaterniond()).get(new org.joml.Quaternionf()));p.scale(.085f,.048f,.004f);mesh(p,vertex,BOX,230,240,218,235);p.popPose();}
    }
    private static final class Casing {boolean active;double x,y,z,vx,vy,vz;int age;}
    private static final class Effects {
        final Casing[] casings=new Casing[24];long tick=-1,seen=-100;int cursor;
        Effects(){for(int i=0;i<casings.length;i++)casings[i]=new Casing();}
        void update(Adv01Entity e){long now=e.level().getGameTime();if(tick==now)return;tick=now;
            for(var c:casings)if(c.active){c.x+=c.vx;c.y+=c.vy;c.z+=c.vz;c.vy-=.04;c.vx*=.96;c.vz*=.96;if(++c.age>=14)c.active=false;}
            if(e.shotTime()!=seen){seen=e.shotTime();if(now-seen>=0&&now-seen<3){var at=e.getBarrelTransform(1).transformPosition(new org.joml.Vector3d(.3,.08,.15));var v=e.getBarrelTransform(1).transformDirection(new org.joml.Vector3d(.19,.1,0));var c=casings[cursor++%casings.length];c.active=true;c.age=0;c.x=at.x;c.y=at.y;c.z=at.z;c.vx=v.x;c.vy=v.y;c.vz=v.z;}}
        }
    }
}
