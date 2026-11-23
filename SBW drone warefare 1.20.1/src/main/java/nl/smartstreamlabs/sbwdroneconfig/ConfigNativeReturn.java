package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ScreenEvent;

/** Some native factories ignore their parent. Restore only terminal exits from our active browser session. */
@Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=Dist.CLIENT)
public final class ConfigNativeReturn {
 private static Screen browser;private static Object connection;
 private ConfigNativeReturn(){}
 public static void begin(Screen parent){browser=parent;connection=Minecraft.getInstance().getConnection();}
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void opening(ScreenEvent.Opening event){
  if(browser==null||event.isCanceled())return;var m=Minecraft.getInstance();var next=event.getNewScreen();
  if(m.getConnection()!=connection||next==browser){browser=null;connection=null;return;}
  if(next==null||next instanceof TitleScreen||next instanceof PauseScreen||next.getClass().getName().equals("net.minecraftforge.client.gui.ModListScreen")){
   var target=browser;browser=null;connection=null;event.setNewScreen(target);
  }
 }
 @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END||browser==null)return;var m=Minecraft.getInstance();if(m.getConnection()!=connection){browser=null;connection=null;return;}if(m.screen==null){var target=browser;browser=null;connection=null;m.setScreen(target);}}
}
