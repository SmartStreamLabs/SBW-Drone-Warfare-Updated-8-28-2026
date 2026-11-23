package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.client.renderer.*;import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.world.item.*;
/** Cached unspawned render proxy; never joins the world or runs vehicle/mine ticks. */
public final class Gad01ItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static Gad01ItemRenderer INSTANCE;private Gad01Entity proxy;private int hash;private boolean loaded;
 private Gad01ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 public static void clear(){if(INSTANCE!=null){INSTANCE.proxy=null;INSTANCE.loaded=false;}}
 public static Gad01ItemRenderer instance(){if(INSTANCE==null)INSTANCE=new Gad01ItemRenderer();return INSTANCE;}
 @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack p,MultiBufferSource buffers,int light,int overlay){var m=Minecraft.getInstance();if(m.level==null)return;if(proxy==null||proxy.level()!=m.level){proxy=AddonEntities.GAD01.get().create(m.level);loaded=false;}if(proxy==null)return;int next=Gad01Item.stateHash(stack);if(!loaded||hash!=next){if(Gad01Item.saved(stack)!=null)proxy.load(Gad01Item.saved(stack).copy());else{for(int i=0;i<3;i++)proxy.removeItemNoUpdate(i);proxy.setChanged();}proxy.setPos(0,0,0);proxy.xo=proxy.yo=proxy.zo=0;proxy.setYRot(0);proxy.yRotO=0;proxy.setXRot(0);proxy.xRotO=0;proxy.setZRot(0);hash=next;loaded=true;}p.pushPose();p.translate(.5,0,.5);m.getEntityRenderDispatcher().getRenderer(proxy).render(proxy,0,0,p,buffers,light);p.popPose();}
}
