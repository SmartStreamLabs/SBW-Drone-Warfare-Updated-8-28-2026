package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.blockentity.*;
public final class Sk01DecorationRenderer implements BlockEntityRenderer<Sk01DecorationBlockEntity>{
 public static final Sk01Entry MODEL=new Sk01Entry(new java.util.UUID(0,0),new java.util.UUID(0,0),0,0,0,0,1,"sbwdroneconfig:sk01",0,false);
 @Override public int getViewDistance(){return 64;}
 @Override public void render(Sk01DecorationBlockEntity block,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-block.getBlockState().getValue(Sk01DecorationBlock.FACING).toYRot()));var camera=net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();Sk01Model.draw(MODEL,camera.distanceToSqr(block.getBlockPos().getCenter())>24*24,pose,buffers,light);pose.popPose();}
}
