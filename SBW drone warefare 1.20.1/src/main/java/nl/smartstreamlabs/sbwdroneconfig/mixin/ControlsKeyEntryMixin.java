package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
/** Vanilla aligns every label using the longest modded name, which can put the column off-screen. */
@Mixin(targets="net.minecraft.client.gui.screens.controls.KeyBindsList$KeyEntry")
public abstract class ControlsKeyEntryMixin {
 @Shadow @Final private Button changeButton;
 @Redirect(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"))
 private int drone$fitControlName(GuiGraphics g,net.minecraft.client.gui.Font font,Component name,int x,int y,int color,boolean shadow,GuiGraphics rowGraphics,int index,int rowY,int rowX,int rowWidth,int rowHeight,int mouseX,int mouseY,boolean hovered,float partial){
  int start=Math.max(8,x),space=Math.max(24,rowX+93-start),width=font.width(name);
  float scale=Math.min(1,space/(float)Math.max(1,width));
  g.pose().pushPose();g.pose().translate(start,y+(1-scale)*4,0);g.pose().scale(scale,scale,1);
  int result=g.drawString(font,name,0,0,color,shadow);g.pose().popPose();return result;
 }
}
