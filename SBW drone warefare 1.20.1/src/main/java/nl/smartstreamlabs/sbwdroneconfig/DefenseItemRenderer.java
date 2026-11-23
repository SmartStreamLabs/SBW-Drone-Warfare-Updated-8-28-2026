package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.client.renderer.*;import net.minecraft.world.item.*;import com.mojang.blaze3d.vertex.PoseStack;
public final class DefenseItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static DefenseItemRenderer cached;private DefenseItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}public static DefenseItemRenderer instance(){if(cached==null)cached=new DefenseItemRenderer();return cached;}
 @Override public void renderByItem(ItemStack s,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){if(!(s.getItem()instanceof DefenseItem item))return;p.pushPose();p.translate(.5,.23,.5);p.scale(.083f,.083f,.083f);DefenseModel.get(item.kind).draw(item.kind,p,buffers,light,overlay,1,-1,new float[4],false,true);p.popPose();}
}
