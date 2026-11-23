package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.*;import net.minecraft.client.renderer.*;import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.world.item.*;
/** One cached unspawned missile proxy for the real 3D PM-01 item. */
public final class PatriotItemRenderer extends BlockEntityWithoutLevelRenderer{
 private static PatriotItemRenderer INSTANCE;private Pm01Entity missile;private PatriotItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}public static PatriotItemRenderer instance(){if(INSTANCE==null)INSTANCE=new PatriotItemRenderer();return INSTANCE;}
 @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack p,MultiBufferSource b,int light,int overlay){var m=Minecraft.getInstance();if(m.level==null)return;if(missile==null||missile.level()!=m.level)missile=AddonEntities.PM01.get().create(m.level);if(missile==null)return;missile.setPos(0,0,0);missile.xo=missile.yo=missile.zo=0;missile.setYRot(0);missile.yRotO=0;missile.setXRot(0);missile.xRotO=0;p.pushPose();p.translate(.5,.5,.5);m.getEntityRenderDispatcher().getRenderer(missile).render(missile,0,0,p,b,light);p.popPose();}
}
