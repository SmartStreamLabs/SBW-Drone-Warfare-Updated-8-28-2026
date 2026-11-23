package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;

/** Only handles the new item's hands and compact status, without changing camera or existing input handlers. */
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class Dj01Client {
    private static void hand(PoseStack p,net.minecraft.client.renderer.MultiBufferSource buffers,int light,boolean right,Vector3f wrist,Vector3f elbow){
        var m=Minecraft.getInstance();if(m.player==null)return;
        var renderer=(net.minecraft.client.renderer.entity.player.PlayerRenderer)m.getEntityRenderDispatcher().getRenderer(m.player);
        Vector3f direction=new Vector3f(wrist).sub(elbow).normalize();
        p.pushPose();p.translate(wrist.x,wrist.y,wrist.z);p.mulPose(new org.joml.Quaternionf().rotationTo(new Vector3f(0,1,0),direction));
        // Native skin arm wrist centres: x=-6/+6, y=12 model pixels. Preserve player skin and sleeve rendering.
        p.translate(right?6d/16:-6d/16,-12d/16,0);
        Dj01Pose.firstPersonHand=true;try{if(right)renderer.renderRightHand(p,buffers,light,m.player);else renderer.renderLeftHand(p,buffers,light,m.player);}finally{Dj01Pose.firstPersonHand=false;}
        p.popPose();
    }
    @net.neoforged.bus.api.SubscribeEvent public static void hands(net.neoforged.neoforge.client.event.RenderHandEvent event){
        var m=Minecraft.getInstance();if(m.player==null||!Dj01Item.is(m.player.getMainHandItem())||m.getCameraEntity()!=m.player||VatagClient.active()||Id01Client.active())return;
        event.setCanceled(true);if(event.getHand()!=InteractionHand.MAIN_HAND)return;
        var stack=m.player.getMainHandItem();boolean right=m.player.getMainArm()==HumanoidArm.RIGHT;
        boolean active=m.player.isUsingItem()&&m.player.getUsedItemHand()==InteractionHand.MAIN_HAND&&Dj01Item.is(m.player.getUseItem())&&Dj01Item.emitting(stack)&&Dj01Item.energy(stack)>0;
        var p=event.getPoseStack();var buffers=event.getMultiBufferSource();int light=event.getPackedLight();p.pushPose();
        float x=active?0:right?.18f:-.18f,y=active?-.405f:-.54f,z=-.65f;
        p.translate(x,y,z);Dj01ItemRenderer.instance().nativeModel(stack,active,p,buffers,light,OverlayTexture.NO_OVERLAY);p.popPose();
        var rear=new Vector3f(x,y-.035f,z+.195f);var fore=new Vector3f(x,y-.015f,z-.3f);
        hand(p,buffers,light,right,rear,new Vector3f(right?.38f:-.38f,-.66f,.12f));
        hand(p,buffers,light,!right,fore,new Vector3f(right?-.38f:.38f,-.62f,.02f));
    }
    @net.neoforged.bus.api.SubscribeEvent public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event){
        var m=Minecraft.getInstance();if(m.player==null||m.screen!=null||m.options.hideGui||m.getCameraEntity()!=m.player||!Dj01Item.is(m.player.getMainHandItem()))return;
        var stack=m.player.getMainHandItem();boolean active=m.player.isUsingItem()&&Dj01Item.emitting(stack)&&Dj01Item.energy(stack)>0;
        var text=net.minecraft.network.chat.Component.translatable("dj01.hud",net.minecraft.network.chat.Component.translatable(active?"dj01.active":"dj01.standby"),Dj01Item.energy(stack),Dj01Item.CAPACITY);
        var g=event.getGuiGraphics();g.drawString(m.font,text,(g.guiWidth()-m.font.width(text))/2,g.guiHeight()-84,active?0x99e7c0:0xc8cec8,true);
    }
    private Dj01Client(){}
}
