package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Our screens draw their own backdrop before labels and widgets. */
public abstract class ConfigBaseScreen extends Screen {
 protected ConfigBaseScreen(Component title){super(title);}
 // The integrated server must keep draining configuration requests/replies while its editor is open.
 @Override public boolean isPauseScreen(){return false;}
}
