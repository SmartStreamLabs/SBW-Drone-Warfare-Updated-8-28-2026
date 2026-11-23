package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
public final class Dvt01Renderer implements BlockEntityRenderer<Dvt01BlockEntity>{
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(Dvt01BlockEntity b){return b.getRenderBoundingBox();}
 @Override public int getViewDistance(){return 48;}
 @Override public boolean shouldRenderOffScreen(Dvt01BlockEntity b){return true;}
 @Override public void render(Dvt01BlockEntity b,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){if(b.getLevel()==null)return;var m=net.minecraft.client.Minecraft.getInstance();var bounds=b.getRenderBoundingBox();boolean visible=m.gameRenderer.getMainCamera().getPosition().distanceToSqr(bounds.getCenter())<48*48&&DvtClient.visible(bounds);var surface=b.open&&visible&&!DvtClient.capturing()?DvtClient.surface(b):null;p.pushPose();var mountedOffset=Pb01Dvt.offset(b);p.translate(mountedOffset.x,mountedOffset.y,mountedOffset.z);p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(180-b.getBlockState().getValue(Dvt01Block.FACING).toYRot()));Dvt01Model.get().draw(p,buffers,light,overlay,b.closed(partial),b.folded(partial),surface);p.popPose();}
}
