package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.client.renderer.*;import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.world.item.*;
/** Shared cached, unspawned native model proxy. Never ticks physics or creates a world entity. */
public final class Sd01ItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static Sd01ItemRenderer INSTANCE;private Sd01Entity proxy;private Sd01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 public static Sd01ItemRenderer instance(){if(INSTANCE==null)INSTANCE=new Sd01ItemRenderer();return INSTANCE;}
 public static void clear(){if(INSTANCE!=null)INSTANCE.proxy=null;}
 @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){var m=Minecraft.getInstance();if(m.level==null)return;if(proxy==null||proxy.level()!=m.level)proxy=AddonEntities.SD01.get().create(m.level);if(proxy==null)return;proxy.setPos(0,0,0);proxy.xo=proxy.yo=proxy.zo=0;proxy.setYRot(0);proxy.yRotO=0;proxy.setXRot(0);proxy.xRotO=0;proxy.setZRot(0);p.pushPose();p.translate(.5,.5,.5);m.getEntityRenderDispatcher().getRenderer(proxy).render(proxy,0,0,p,buffers,light);p.popPose();}
}