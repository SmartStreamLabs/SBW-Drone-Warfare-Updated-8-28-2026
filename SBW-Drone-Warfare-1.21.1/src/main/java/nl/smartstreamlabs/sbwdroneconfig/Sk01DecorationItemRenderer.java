package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.*;import net.minecraft.world.item.*;import com.mojang.blaze3d.vertex.PoseStack;
public final class Sk01DecorationItemRenderer extends BlockEntityWithoutLevelRenderer {
 public Sk01DecorationItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 @Override public void renderByItem(ItemStack item,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(.5,.28,.5);Sk01Model.draw(Sk01DecorationRenderer.MODEL,false,pose,buffers,light);pose.popPose();}
}
