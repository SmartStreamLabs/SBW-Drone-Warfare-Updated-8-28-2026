package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.renderer.*;
public final class Hg01PayloadRenderer{
 public static void fpv(CubedFpvDroneEntity d,float partial,PoseStack p,MultiBufferSource b,int light){if(!Hg01Payload.is(d.hgVisual()))return;var m=Hg01Payload.fpvFrame(d,partial);p.pushPose();p.translate(m.m30(),m.m31(),m.m32());p.mulPose(new org.joml.Quaternionf(m.getUnnormalizedRotation(new org.joml.Quaterniond())));p.pushPose();p.translate(-.04,-.17,.14);p.scale(.08f,.185f,.08f);net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(net.minecraft.world.level.block.Blocks.CHAIN.defaultBlockState(),p,b,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);p.popPose();p.translate(0,-.17-3.04/16,.18);Hg01ItemRenderer.nativeModel(p,b,light);p.popPose();}
 public static void bomber(org.joml.Vector3d anchor,PoseStack p,MultiBufferSource b,int light){p.pushPose();p.translate(anchor.x,anchor.y-3.04/16,anchor.z);Hg01ItemRenderer.nativeModel(p,b,light);p.popPose();}
 private Hg01PayloadRenderer(){}
}
