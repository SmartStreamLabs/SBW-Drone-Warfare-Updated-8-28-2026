package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class CarrierFleetClient {
    private static boolean held;
    private static java.util.UUID linked(){
        var m=Minecraft.getInstance();if(m.player==null)return null;var monitor=m.player.getMainHandItem();if(!SbwCompat.isMonitor(monitor))return null;var home=CarrierFleet.home(monitor);if(home!=null)return home;
        var tag=PlatformMonitor.tag(monitor);if(tag.hasUUID("NdcCarrier"))return tag.getUUID("NdcCarrier");var v=VatagClient.vehicle();return CarrierFleet.carrier(v)?v.getUUID():null;
    }
    public static void open(){var id=linked();if(id!=null)AddonNetwork.vatagInput(new VatagInput(10,id,0,0,0,0));}
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
        var m=Minecraft.getInstance();if(m.player==null||m.level==null){held=false;return;}
        var binding=Dc01Client.DOCKS;boolean clicked=false;while(binding.consumeClick())clicked=true;var key=binding.getKey();long window=m.getWindow().getWindow();
        boolean physical=key.getType()==InputConstants.Type.MOUSE?GLFW.glfwGetMouseButton(window,key.getValue())==GLFW.GLFW_PRESS:key.getType()==InputConstants.Type.KEYSYM&&key.getValue()>=0&&InputConstants.isKeyDown(window,key.getValue());
        boolean down=binding.isDown()||physical,pressed=clicked||down&&!held;held=down;
        if(pressed&&m.screen==null&&m.getOverlay()==null&&m.isWindowActive()&&SbwCompat.isMonitor(m.player.getMainHandItem()))open();
    }
    @net.neoforged.bus.api.SubscribeEvent public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event){
        var m=Minecraft.getInstance();if(m.screen!=null||linked()==null||m.player==null||!(VatagClient.active()||Id01Client.active()||SbwCompat.isUsingLinkedMonitor(m.player.getMainHandItem())))return;
        var g=event.getGuiGraphics();int center=m.getWindow().getGuiScaledWidth()/2;String text=Dc01Client.DOCKS.getTranslatedKeyMessage().getString()+" · Fleet / Return to carrier";int half=m.font.width(text)/2+5;
        g.fill(center-half,43,center+half,57,0xb0101c24);g.drawCenteredString(m.font,text,center,46,0xc9e5bc);
    }
}
