package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.client.renderer.*;import net.minecraft.world.item.*;import com.mojang.blaze3d.vertex.PoseStack;
public final class Ps01ItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static Ps01ItemRenderer cached;private Ps01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}public static Ps01ItemRenderer instance(){if(cached==null)cached=new Ps01ItemRenderer();return cached;}
 @Override public void renderByItem(ItemStack s,ItemDisplayContext context,PoseStack p,MultiBufferSource b,int light,int overlay){p.pushPose();p.translate(.5,.25,.5);p.scale(.105f,.105f,.105f);Ps01Model.get().draw(p,b,light,light,overlay,0,false,false,1,1,false,true);p.popPose();}
}
