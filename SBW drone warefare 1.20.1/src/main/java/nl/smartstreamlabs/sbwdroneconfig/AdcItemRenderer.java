package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.client.renderer.*;import net.minecraft.world.item.*;import com.mojang.blaze3d.vertex.PoseStack;
public final class AdcItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static AdcItemRenderer cached;private AdcItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}public static AdcItemRenderer instance(){if(cached==null)cached=new AdcItemRenderer();return cached;}
 @Override public void renderByItem(ItemStack s,ItemDisplayContext c,PoseStack p,MultiBufferSource b,int light,int overlay){p.pushPose();p.translate(.5,.13,.5);p.scale(.19f,.19f,.19f);AdcModel.get().draw(p,b,light,overlay,0,25,0,0,5,false,true);p.popPose();}
}
