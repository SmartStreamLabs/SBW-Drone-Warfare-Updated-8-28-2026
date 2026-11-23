package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
/** SBW may consume mouse events before vanilla updates keyUse; poll the configured binding as well. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class GroundVehicleExitClient {
 private static boolean armed;private static int mounted=-1;
 private static boolean down(){var m=Minecraft.getInstance();var key=m.options.keyUse.getKey();long w=m.getWindow().getWindow();return m.options.keyUse.isDown()||(key.getType()==com.mojang.blaze3d.platform.InputConstants.Type.MOUSE?org.lwjgl.glfw.GLFW.glfwGetMouseButton(w,key.getValue())==org.lwjgl.glfw.GLFW.GLFW_PRESS:key.getType()==com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM&&key.getValue()>=0&&com.mojang.blaze3d.platform.InputConstants.isKeyDown(w,key.getValue()));}
 private static boolean exit(){var m=Minecraft.getInstance();var v=m.player==null?null:m.player.getVehicle();if(!armed||v==null||v.getId()!=mounted||!GroundVehicleExit.supported(v)||m.screen!=null||!m.isWindowActive())return false;armed=false;AddonNetwork.groundExit(new GroundVehicleExit(v.getId()));return true;}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var m=Minecraft.getInstance();var v=m.player==null?null:m.player.getVehicle();if(v==null||!GroundVehicleExit.supported(v)||m.screen!=null||!m.isWindowActive()){mounted=-1;armed=false;return;}if(mounted!=v.getId()){mounted=v.getId();armed=false;}if(!down())armed=true;else exit();}
 @net.minecraftforge.eventbus.api.SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST,receiveCanceled=true) public static void use(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered e){var m=Minecraft.getInstance();if(e.isUseItem()&&m.player!=null&&GroundVehicleExit.supported(m.player.getVehicle())){exit();e.setCanceled(true);e.setSwingHand(false);}}
 private GroundVehicleExitClient(){}
}
