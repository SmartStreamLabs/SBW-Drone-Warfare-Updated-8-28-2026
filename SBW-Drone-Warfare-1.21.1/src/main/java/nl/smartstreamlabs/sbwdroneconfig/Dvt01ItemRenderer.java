package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.*;import net.minecraft.world.item.*;import com.mojang.blaze3d.vertex.PoseStack;
/** Actual shared mesh in GUI, JEI, hands, ground and frames. Never requests a video feed. */
public final class Dvt01ItemRenderer extends BlockEntityWithoutLevelRenderer {
 public Dvt01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 @Override public void renderByItem(ItemStack s,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){boolean open=context==ItemDisplayContext.GUI||context==ItemDisplayContext.FIXED;p.pushPose();p.translate(.5,open?-.05:.27,.5);Dvt01Model.get().draw(p,buffers,light,overlay,open?0:1,open?0:1,null);p.popPose();}
}
