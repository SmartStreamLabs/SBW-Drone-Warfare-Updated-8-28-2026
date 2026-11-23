package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
public final class AdcRenderer implements BlockEntityRenderer<AdcBlockEntity>{
 @Override public boolean shouldRenderOffScreen(AdcBlockEntity b){return true;}@Override public int getViewDistance(){return 160;}@Override public boolean shouldRender(AdcBlockEntity b,net.minecraft.world.phys.Vec3 camera){return b.getRenderBoundingBox().getCenter().distanceToSqr(camera)<160*160&&(AdcClient.frustum==null||AdcClient.frustum.isVisible(b.getRenderBoundingBox()));}
 @Override public void render(AdcBlockEntity b,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(180-b.facing().toYRot()));AdcModel.get().draw(p,buffers,light,overlay,b.viewYaw(partial),b.viewPitch(partial),b.recoil(partial),b.sensorYaw(partial),b.sensorPitch(partial),false,true);p.popPose();}
}
