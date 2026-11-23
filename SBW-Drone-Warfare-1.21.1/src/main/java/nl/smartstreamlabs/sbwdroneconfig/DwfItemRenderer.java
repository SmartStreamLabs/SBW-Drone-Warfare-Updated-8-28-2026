package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.*;import net.minecraft.world.item.*;
/** Reuses the machine geometry directly; no preview block or entity is created. */
public final class DwfItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static DwfItemRenderer instance;private DwfModel model;private DwfModel.PoseBuffer poses;
 private DwfItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 public static DwfItemRenderer instance(){if(instance==null)instance=new DwfItemRenderer();return instance;}
 @Override public void renderByItem(ItemStack item,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){var current=DwfModel.get();if(model!=current){model=current;poses=model.poseBuffer();}var matrices=model.restPose();p.pushPose();p.translate(.5,.12,.5);p.scale(.065f,.065f,.065f);model.draw(p,buffers,light,overlay,matrices,false);model.draw(p,buffers,light,overlay,matrices,true);p.popPose();}
}
