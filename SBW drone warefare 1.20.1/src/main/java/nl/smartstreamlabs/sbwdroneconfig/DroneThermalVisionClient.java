package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public final class DroneThermalVisionClient {
    private static KeyMapping toggleThermalKey,helmetKey;private static boolean helmetEnabled;
    private static boolean helmetAvailable(){var m=Minecraft.getInstance();if(m.player==null||!m.player.isAlive())return false;var stack=m.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);return stack.getItem() instanceof Mu01ArmorItem armor&&armor.muSlot==net.minecraft.world.entity.EquipmentSlot.HEAD;}
    private static boolean thermalKeyMigrationChecked;
    private static boolean thermalVisionEnabled;
    /** Runs after key registration and vanilla options loading, once per client installation. */
    private static void migrateLegacyThermalKey(Minecraft m){
        if(thermalKeyMigrationChecked||toggleThermalKey==null||m.options==null)return;
        thermalKeyMigrationChecked=true;
        var marker=m.gameDirectory.toPath().resolve("config/sbwdroneconfig-thermal-key-v1.txt");
        try{
            if(java.nio.file.Files.isRegularFile(marker))return;
            if(toggleThermalKey.isUnbound()){
                var options=m.gameDirectory.toPath().resolve("options.txt");
                if(java.nio.file.Files.isRegularFile(options)){
                    java.nio.file.Files.createDirectories(marker.getParent());
                    java.nio.file.Files.copy(options,marker.resolveSibling("sbwdroneconfig-options-before-thermal-v1.bak"),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                toggleThermalKey.setKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_V));
                KeyMapping.resetMapping();
                m.options.save();
                SbwDroneRangeConfig.LOGGER.info("Restored legacy unbound drone thermal key to V; existing custom bindings are preserved");
            }
            AtomicConfigFiles.replace(marker,"thermal-key-v1: checked; later custom bindings and Unbound are preserved\n".getBytes(java.nio.charset.StandardCharsets.UTF_8),false);
        }catch(java.io.IOException e){SbwDroneRangeConfig.LOGGER.warn("Could not persist drone thermal key migration",e);}
    }

    private static Object world;
    private static UUID view,lookupId;
    private static Entity linked;
    private static int retryTicks,lastWidth,lastHeight;
    private static final Set<UUID> HIDDEN_DRONE_OPERATORS=new HashSet<>();
    private DroneThermalVisionClient(){}
    public static void init(net.minecraftforge.eventbus.api.IEventBus bus){
        bus.addListener(DroneThermalVisionClient::setup);bus.addListener(DroneThermalVisionClient::keys);bus.addListener(DroneThermalVisionClient::shaders);
    }
    private static void setup(FMLClientSetupEvent e){net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneThermalVisionClient.class);}
    private static void keys(RegisterKeyMappingsEvent e){
        helmetKey=new KeyMapping("key.sbwdroneconfig.mu01_terminal",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,"key.categories.sbwdroneconfig");e.register(helmetKey);
        toggleThermalKey=new KeyMapping("key.sbwdroneconfig.toggle_thermal",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,"key.categories.sbwdroneconfig");e.register(toggleThermalKey);
    }
    private static void shaders(RegisterShadersEvent e){
        WhiteHotRender.reload();net.minecraft.client.renderer.ShaderInstance mask=null,composite=null;
        try{
            mask=new net.minecraft.client.renderer.ShaderInstance(e.getResourceProvider(),new net.minecraft.resources.ResourceLocation(SbwDroneRangeConfig.MOD_ID,"white_hot_mask"),com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_TEX);
            composite=new net.minecraft.client.renderer.ShaderInstance(e.getResourceProvider(),new net.minecraft.resources.ResourceLocation(SbwDroneRangeConfig.MOD_ID,"white_hot_composite"),com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
            e.registerShader(mask,s->WhiteHotRender.maskShader=s);e.registerShader(composite,s->WhiteHotRender.compositeShader=s);
        }catch(java.io.IOException|RuntimeException failure){
            if(mask!=null)mask.close();if(composite!=null)composite.close();SbwDroneRangeConfig.LOGGER.warn("White Hot shaders unavailable; keeping normal drone video",failure);
        }
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        migrateLegacyThermalKey(Minecraft.getInstance());
        if(toggleThermalKey!=null)while(toggleThermalKey.consumeClick()){} // raw PRESS handles GUI video too; never repeat on hold
        Minecraft m=Minecraft.getInstance();if(helmetKey!=null)while(helmetKey.consumeClick()){}if(helmetEnabled&&!helmetAvailable()){helmetEnabled=false;thermalVisionEnabled=false;WhiteHotRender.release();}
        if(world!=m.level){world=m.level;helmetEnabled=false;view=lookupId=null;linked=null;thermalVisionEnabled=false;HIDDEN_DRONE_OPERATORS.clear();WhiteHotRender.release();}
        if(m.player==null||m.level==null){thermalVisionEnabled=false;return;}
        int width=m.getWindow().getWidth(),height=m.getWindow().getHeight();if(width!=lastWidth||height!=lastHeight){WhiteHotRender.release();lastWidth=width;lastHeight=height;}
        if(retryTicks>0)retryTicks--;
        Entity full=fullView();PortableFpvDetectorTarget preview=DroneJammerClient.thermalPreviewTarget();
        UUID current=full!=null?full.getUUID():preview!=null?preview.droneId():null;
        if(!Objects.equals(current,view)){view=current;thermalVisionEnabled=false;WhiteHotRender.release();}
        if(!AddonConfig.enableThermalVision())thermalVisionEnabled=false;
    }
    static Entity fullView(){
        Minecraft m=Minecraft.getInstance();Entity entity=null;
        if(VatagClient.active())entity=VatagClient.vehicle();else if(Rd01Client.active())entity=Rd01Client.vehicle();else if(Id01Client.active())entity=Id01Client.vehicle();
        if(entity!=null)return entity.isAlive()&&!entity.isRemoved()?entity:null;
        if(helmetEnabled&&helmetAvailable()&&!SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(m.player)))return m.player;
        if(m.player==null||!SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(m.player)))return null;
        UUID id=SbwCompat.getLinkedDroneUuid(SbwCompat.getActiveLinkedMonitor(m.player));
        if(!Objects.equals(id,lookupId)){lookupId=id;linked=null;retryTicks=0;}
        Entity camera=m.getCameraEntity();if(camera!=null&&Objects.equals(camera.getUUID(),id))linked=camera;
        if((linked==null||linked.isRemoved())&&retryTicks==0){linked=DroneClientViewContext.activeDroneEntity(m);retryTicks=20;}
        return linked!=null&&linked.isAlive()&&!linked.isRemoved()&&linked.level()==m.level?linked:null;
    }
    @SubscribeEvent public static void key(InputEvent.Key e){var m=Minecraft.getInstance();if(e.getAction()==GLFW.GLFW_PRESS&&helmetKey!=null&&helmetKey.matches(e.getKey(),e.getScanCode())&&m.screen==null&&helmetAvailable()&&(fullView()==null||fullView()==m.player)){helmetEnabled=!helmetEnabled;view=helmetEnabled?m.player.getUUID():null;thermalVisionEnabled=helmetEnabled;WhiteHotRender.retry();WhiteHotRender.release();return;}if(e.getAction()==GLFW.GLFW_PRESS&&toggleThermalKey!=null&&toggleThermalKey.matches(e.getKey(),e.getScanCode()))toggle();}
    @SubscribeEvent public static void mouse(InputEvent.MouseButton.Post e){if(e.getAction()==GLFW.GLFW_PRESS&&toggleThermalKey!=null&&toggleThermalKey.matchesMouse(e.getButton()))toggle();}
    private static void toggle(){
        Minecraft m=Minecraft.getInstance();if(m.player==null||m.level==null||!AddonConfig.thermalVisionKeybindEnabled())return;
        if(m.screen!=null&&(DroneJammerClient.thermalPreviewTarget()==null||m.screen.getFocused() instanceof EditBox))return;
        Entity full=fullView();PortableFpvDetectorTarget preview=DroneJammerClient.thermalPreviewTarget();
        if(full==null&&preview==null)return;
        if(!AddonConfig.enableThermalVision())return;
        // Input can arrive before the first lifecycle tick of a newly opened camera.
        UUID current=full!=null?full.getUUID():preview.droneId();
        if(!Objects.equals(current,view)){view=current;thermalVisionEnabled=false;WhiteHotRender.release();}
        boolean wasActive=thermalVisionEnabled&&WhiteHotRender.available();
        WhiteHotRender.retry();
        if(!WhiteHotRender.available()){m.player.displayClientMessage(Component.translatable("message.sbwdroneconfig.thermal_renderer_unavailable"),true);return;}
        thermalVisionEnabled=!wasActive;
        // The shared OSD displays VIS / THM inside the actual camera viewport.
    }
    /** Called at the world-pass boundary, including the existing detector's offscreen camera. */
    public static boolean renderThisCamera(){
        if(!thermalVisionEnabled||!AddonConfig.enableThermalVision())return false;
        if(PortableFpvDetectorRenderContext.isOffscreenRendering())return DroneClientViewContext.isReadOnlyCameraInterceptActive();
        return fullView()!=null;
    }
    public static boolean shouldHideDefaultTargetBoxes(){return renderThisCamera()&&WhiteHotRender.available();}
    static String modeLabel(){return thermalVisionEnabled&&WhiteHotRender.available()?"THM / WHITE HOT":"VIS";}
    static void videoLabel(GuiGraphics g,int x,int y,int width,int height){
        if(width<60||height<20)return;
        var font=Minecraft.getInstance().font;String label=modeLabel();float scale=Math.min(.65f,(width-40f)/Math.max(1,font.width(label)));
        g.pose().pushPose();try{g.pose().translate(x+4,y+3,0);g.pose().scale(scale,scale,1);g.drawString(font,label,0,0,0xffeeeeee,true);}finally{g.pose().popPose();}
    }
    @SubscribeEvent public static void opaque(RenderLevelStageEvent e){if(e.getStage()==RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)WhiteHotRender.solidDepth();}
    @SubscribeEvent public static void hud(RenderGuiOverlayEvent.Post e){ if(DroneOsdClient.ownsFullView())return;
        if(e.getOverlay()==null||!"hotbar".equals(e.getOverlay().id().getPath())||fullView()==null)return;
        Minecraft m=Minecraft.getInstance();String text=modeLabel();e.getGuiGraphics().drawString(m.font,text,e.getWindow().getGuiScaledWidth()-m.font.width(text)-10,10,0xffeeeeee,true);
    }
    @SubscribeEvent public static void player(RenderPlayerEvent.Pre e){
        Player p=e.getEntity();if(p!=null&&(HIDDEN_DRONE_OPERATORS.contains(p.getUUID())||SbwCompat.isUsingLinkedMonitor(SbwCompat.getActiveLinkedMonitor(p))))e.setCanceled(true);
    }
    public static void handleDroneOperatorVisibility(DroneOperatorVisibilityMessage message){if(message.hidden())HIDDEN_DRONE_OPERATORS.add(message.playerId());else HIDDEN_DRONE_OPERATORS.remove(message.playerId());}
}
