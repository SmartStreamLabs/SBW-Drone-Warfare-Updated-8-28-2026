package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.*;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.world.entity.player.Inventory;import net.minecraft.network.chat.Component;
public final class MplServiceScreen extends AbstractContainerScreen<MplServiceMenu>{
 public MplServiceScreen(MplServiceMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=170;inventoryLabelY=76;}
 @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff202a24);for(var slot:menu.slots){int a=leftPos+slot.x-1,b=topPos+slot.y-1;g.fill(a,b,a+18,b+18,0xff101713);g.fill(a+1,b+1,a+17,b+17,0xff5b655a);}g.drawString(font,Component.translatable("mpl01.service_slots"),leftPos+8,topPos+54,0xb6c6b7,false);}

 @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,8,6,0xe5eede,false);g.drawString(font,playerInventoryTitle,8,76,0xb6c6b7,false);}
 @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g);super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
