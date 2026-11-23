package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class Rd01Renderer extends CachedEntityRenderer<Rd01Entity> {
    public Rd01Renderer(EntityRendererProvider.Context c){super(c,new Rd01Model());shadowRadius=1.2f;}
    @Override protected void applyRotations(Rd01Entity d,PoseStack p,float age,float yaw,float partial){}
    @Override public RenderType getRenderType(Rd01Entity d,net.minecraft.resources.ResourceLocation texture,MultiBufferSource buffers,float partial){return RenderType.entityCutoutNoCull(texture);}
    @Override public void render(Rd01Entity d,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light) {
        p.pushPose();var position=d.getPosition(partial);var transform=Rd01Pose.aircraft(d,partial);
        transform.setTranslation(transform.m30()-position.x,transform.m31()-position.y-.01,transform.m32()-position.z);
        p.translate(transform.m30(),transform.m31(),transform.m32());p.mulPose(new org.joml.Quaternionf(transform.getNormalizedRotation(new org.joml.Quaterniond())));
        super.render(d,yaw,partial,p,buffers,light);
        p.popPose();
        if(net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes())for(var box:Rd01Geometry.parts(d))LevelRenderer.renderLineBox(p,buffers.getBuffer(RenderType.lines()),box.move(-d.getX(),-d.getY(),-d.getZ()),0,1,1,1);
    }
}
