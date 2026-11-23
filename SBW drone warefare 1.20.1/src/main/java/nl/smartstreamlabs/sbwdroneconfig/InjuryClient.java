package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class InjuryClient {
    private static int clock, hitUntil, noticeUntil, receivedAt, duration, elapsed;
    private static String notice="";
    public static void feedback(InjuryFeedback m) {
        if(m.hit()){hitUntil=clock+8;return;}
        boolean starting=duration==0&&m.duration()>0;
        elapsed=m.elapsed();duration=m.duration();receivedAt=clock;notice=m.status();noticeUntil=clock+(duration>0?30:60);
        var game=Minecraft.getInstance();
        if(m.duration()==0&&(m.status().equals("injury.no_treatment")||m.status().equals("injury.busy")||m.status().equals("injury.self_disabled"))&&game.player!=null&&game.player.getUseItem().getItem()instanceof MedicalItem)game.player.stopUsingItem();
        float volume=InjuryClientConfig.VOLUME.get().floatValue();
        if(game.player!=null&&volume>0) {
            if(starting)game.player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER,volume*.65f,1.1f);
            else if(m.status().equals("injury.completed")||m.status().equals("injury.stabilized"))game.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP,volume*.5f,1.25f);
        }
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
        clock++;var m=Minecraft.getInstance();
        if(m.level==null||m.player==null){InjuryClientState.clear();duration=hitUntil=noticeUntil=0;return;}
        if(duration>0&&clock-receivedAt>30){duration=0;notice="injury.interrupted";noticeUntil=clock+40;}
        var s=InjuryClientState.get(m.player.getUUID());
        if(s!=null&&s.downed()&&!m.isPaused()&&clock%60==0&&InjuryClientConfig.VOLUME.get()>0)
            m.player.playSound(AddonSounds.INJURY_HEARTBEAT.get(),InjuryClientConfig.VOLUME.get().floatValue()*.45f,1);
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void hud(net.minecraftforge.client.event.RenderGuiEvent.Post e) {
        var m=Minecraft.getInstance();
        if(m.player==null||m.level==null||m.options.hideGui||PortableFpvDetectorRenderContext.isOffscreenRendering())return;
        var g=e.getGuiGraphics();int w=g.guiWidth(),h=g.guiHeight();
        if(hitUntil>clock&&InjuryClientConfig.EFFECTS.get()>0) {
            int alpha=(int)(100*InjuryClientConfig.EFFECTS.get()*(hitUntil-clock)/8d),color=alpha<<24|0xa51f24;
            g.fill(0,0,w,5,color);g.fill(0,h-5,w,h,color);g.fill(0,0,5,h,color);g.fill(w-5,0,w,h,color);
        }
        if(!InjuryClientConfig.HUD.get())return;
        var s=InjuryClientState.get(m.player.getUUID());
        if(s!=null&&s.stage()!=InjuryState.Stage.HEALTHY) {
            int x=Math.max(4,w/2-132), y=Math.max(4,h-107),width=Math.min(264,w-8);
            g.fill(x,y,x+width,y+31,0xc41a2224);
            var title=Component.translatable("injury.stage."+s.stage().name().toLowerCase(java.util.Locale.ROOT));
            g.drawString(m.font,title,x+6,y+4,s.downed()?0xffe3b971:0xfff1e5dc,false);
            var bleeding=Component.translatable("injury.bleed."+s.bleeding());
            g.drawString(m.font,bleeding,x+6,y+17,s.bleeding()>0?0xffff8585:0xffa6d9b0,false);
        }
        if(clock<noticeUntil) {
            int width=Math.min(240,w-12),x=(w-width)/2,y=Math.max(10,h/2+48);
            g.fill(x-4,y-4,x+width+4,y+24,0xd0162022);
            g.drawCenteredString(m.font,Component.translatable(notice),w/2,y,0xffe9eee8);
            if(duration>0) {
                double progress=Math.min(1,(elapsed+Math.max(0,clock-receivedAt))/(double)duration);
                g.fill(x,y+13,x+width,y+19,0xff384346);g.fill(x,y+13,x+(int)(width*progress),y+19,0xff90bca6);
            }
        }
    }
    private InjuryClient() {}
}
