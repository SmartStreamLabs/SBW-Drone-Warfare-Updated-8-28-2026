package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.*;import net.minecraft.client.renderer.*;import net.minecraft.world.item.*;
public final class Pb01ItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static Pb01ItemRenderer cached;public static Pb01ItemRenderer instance(){if(cached==null)cached=new Pb01ItemRenderer();return cached;}private Pb01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){var n=DefenseItem.data(stack).getCompound("BunkerState");p.pushPose();p.translate(.5,.3,.5);p.scale(.075f,.075f,.075f);Pb01Model.get().draw(p,buffers,light,light,overlay,n.getInt("Step")/21f,65535&~Pb01BlockEntity.broken(n),!n.contains("Lights")||n.getBoolean("Lights"),false,true);p.popPose();}
}
