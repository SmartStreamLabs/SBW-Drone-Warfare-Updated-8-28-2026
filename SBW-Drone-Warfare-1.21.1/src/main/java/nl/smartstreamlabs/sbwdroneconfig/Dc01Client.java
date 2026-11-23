package nl.smartstreamlabs.sbwdroneconfig;
/** Read the configured binding even when SBW consumes a conflicting key event. */
public final class Dc01Client {
 public static final net.minecraft.client.KeyMapping DOCKS=new net.minecraft.client.KeyMapping("key.sbwdroneconfig.dc01_docks",org.lwjgl.glfw.GLFW.GLFW_KEY_F7,"key.categories.sbwdroneconfig");
 private static boolean held,exitArmed;
 public static void pollDockKey(){var m=net.minecraft.client.Minecraft.getInstance();var use=m.options.keyUse;var useKey=use.getKey();long handle=m.getWindow().getWindow();boolean useDown=use.isDown()||(useKey.getType()==com.mojang.blaze3d.platform.InputConstants.Type.MOUSE?org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle,useKey.getValue())==org.lwjgl.glfw.GLFW.GLFW_PRESS:useKey.getType()==com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM&&useKey.getValue()>=0&&com.mojang.blaze3d.platform.InputConstants.isKeyDown(handle,useKey.getValue()));if(!useDown)exitArmed=true;else if(exitArmed&&m.screen==null&&m.isWindowActive()&&(VatagClient.vehicle() instanceof Gad01Entity||VatagClient.vehicle() instanceof Hgd01Entity||VatagClient.vehicle() instanceof Qd01Entity||VatagClient.vehicle() instanceof Dc01Entity||VatagClient.vehicle() instanceof Wd01Entity)){exitArmed=false;VatagClient.exitCarrierControl();return;}}
 public static void resetInput(){held=false;exitArmed=false;} private Dc01Client(){}
}
