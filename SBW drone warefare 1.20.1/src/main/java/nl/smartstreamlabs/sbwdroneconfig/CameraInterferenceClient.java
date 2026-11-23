package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import java.util.UUID;

/** Separate full-view and receiver sessions. No jammer lookup or world scan in rendering. */
public final class CameraInterferenceClient {
    private static Object world;
    private static com.mojang.blaze3d.pipeline.RenderTarget primaryTarget;
    private static View full,preview;
    private static int ticks;
    public static void init(net.minecraftforge.eventbus.api.IEventBus bus){bus.addListener(CameraInterferenceClient::setup);bus.addListener(CameraInterferenceRender::shaders);}
    private static void setup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event){net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(CameraInterferenceClient.class);}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event){
        if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
        var m=Minecraft.getInstance();
        if(world!=m.level||m.player==null||!m.player.isAlive()){world=m.level;reset();if(m.player==null||!m.player.isAlive())return;}
        primaryTarget=m.getMainRenderTarget();ticks++;
        Entity direct=DroneThermalVisionClient.fullView();
        var target=DroneJammerClient.thermalPreviewTarget();Entity remote=m.level==null||target==null?null:m.level.getEntity(target.entityId());
        full=update(full,direct);preview=update(preview,remote);
        CameraInterferenceRender.retain(full==null?null:full.id,preview==null?null:preview.id);
    }
    private static View update(View previous,Entity entity){
        var m=Minecraft.getInstance();
        if(entity==null||!entity.isAlive()||entity.isRemoved()||entity.level()!=m.level)return null;
        View v=previous!=null&&previous.id.equals(entity.getUUID())?previous:new View(entity);
        v.entity=entity;v.before=v.amount;
        v.raw=DroneJamOverlayClient.getJamProgressFor(v.id);v.lost=DroneJamOverlayClient.isHardJammed(v.id);
        boolean fiber=DroneFiberOpticClient.hasActiveFiberSessionFor(v.id)&&v.raw==0&&!v.lost;
        float wanted=fiber?0:InterferenceMath.strength(v.raw,AddonConfig.overlayStartThreshold());
        v.amount=fiber?0:InterferenceMath.smooth(v.amount,wanted,.05);
        if(fiber){v.before=0;v.raw=0;v.lost=false;v.freezeUntil=0;}
        if(!CameraInterferenceConfig.STUTTERS.get())v.freezeUntil=0;
        else if(v.amount>.35&&ticks-v.lastFreeze>10&&InterferenceMath.event(v.seed,ticks)<.025f*v.amount*v.amount){v.lastFreeze=ticks;v.freezeUntil=System.nanoTime()+70_000_000L;}
        return v;
    }
    static View rendering(){
        if(DvtClient.capturing())return DvtClient.interference;
        var m=Minecraft.getInstance();if(m.player==null||!m.player.isAlive()||m.level!=world)return null;
        boolean offscreen=PortableFpvDetectorRenderContext.isOffscreenRendering();if(!offscreen&&m.getMainRenderTarget()!=primaryTarget)return null;View v=offscreen?preview:full;
        if(v==null||v.entity.isRemoved()||!v.entity.isAlive()||v.entity.level()!=m.level)return null;
        if(offscreen)return DroneClientViewContext.isReadOnlyCameraIntercept(v.entity)?v:null;
        // Check the live session as well: exiting between ticks must never process the body camera.
        return DroneThermalVisionClient.fullView()==v.entity?v:null;
    }
    public static void reset(){primaryTarget=null;full=preview=null;ticks=0;CameraInterferenceRender.release();}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void hud(net.minecraftforge.client.event.RenderGuiEvent.Post event){
        var m=Minecraft.getInstance();if(m.screen==null&&!m.options.hideGui&&full!=null&&rendering()==full)label(event.getGuiGraphics(),full,0,0,event.getGuiGraphics().guiWidth(),event.getGuiGraphics().guiHeight());
    }
    public static void previewLabel(GuiGraphics g,UUID id,int x,int y,int w,int h){if(preview!=null&&preview.id.equals(id)&&!Minecraft.getInstance().options.hideGui)label(g,preview,x,y,w,h);}
    private static void label(GuiGraphics g,View v,int x,int y,int w,int h){
        if(w<90||h<50||v.raw<AddonConfig.overlayStartThreshold()&&!v.lost)return;
        if(v.lost&&DroneOsdClient.showsSignalLost(v.id))return;
        String text=v.lost?"SIGNAL LOST":"INTERFERENCE  RF "+Math.round((1-v.raw)*100)+"%";
        var font=Minecraft.getInstance().font;float scale=Math.min(v==preview?.65f:1f,(w-12f)/font.width(text));
        g.enableScissor(x,y,x+w,y+h);g.pose().pushPose();
        try{g.pose().translate(x+w/2f,y+h/2f+18,0);g.pose().scale(scale,scale,1);int half=font.width(text)/2;g.fill(-half-3,-2,half+3,10,0xb0090b0c);g.drawCenteredString(font,text,0,0,v.lost?0xffd4cdcd:0xffeadbb4);}finally{g.pose().popPose();g.disableScissor();}
    }
    static final class View {
        final UUID id;final int seed;Entity entity;float before,amount,raw;boolean lost;long freezeUntil;int lastFreeze=-100;
        View(Entity entity){this.entity=entity;id=entity.getUUID();seed=id.hashCode();}
    }
    private CameraInterferenceClient(){}
}
