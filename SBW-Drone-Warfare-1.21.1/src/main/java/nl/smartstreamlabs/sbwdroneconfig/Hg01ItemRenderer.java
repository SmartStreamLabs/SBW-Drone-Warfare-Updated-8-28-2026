package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.client.renderer.*;import net.minecraft.world.item.*;import com.mojang.blaze3d.vertex.PoseStack;
public final class Hg01ItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static Hg01ItemRenderer INSTANCE;private static Hg01Entity proxy;
 private Hg01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 public static Hg01ItemRenderer instance(){if(INSTANCE==null)INSTANCE=new Hg01ItemRenderer();return INSTANCE;}public static void clear(){proxy=null;}
 public static void nativeModel(PoseStack p,MultiBufferSource buffers,int light){var m=Minecraft.getInstance();if(m.level==null)return;if(proxy==null||proxy.level()!=m.level)proxy=AddonEntities.HG01.get().create(m.level);if(proxy!=null)m.getEntityRenderDispatcher().getRenderer(proxy).render(proxy,0,0,p,buffers,light);}
 @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){p.pushPose();p.translate(.5,.405,.5);nativeModel(p,buffers,light);p.popPose();}
}
