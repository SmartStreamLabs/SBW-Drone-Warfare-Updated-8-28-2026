package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.*;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.world.entity.player.Inventory;import net.minecraft.network.chat.Component;
public final class Gad01Screen extends AbstractContainerScreen<Gad01Menu>{
 public Gad01Screen(Gad01Menu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=170;inventoryLabelY=76;}
 @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xff202a24);for(var slot:menu.slots){int a=leftPos+slot.x-1,b=topPos+slot.y-1;g.fill(a,b,a+18,b+18,0xff101713);g.fill(a+1,b+1,a+17,b+17,0xff5b655a);}g.drawString(font,Component.translatable("gad01.mine1"),leftPos+40,topPos+20,0xd2dfca,false);g.drawString(font,Component.translatable("gad01.mine2"),leftPos+76,topPos+20,0xd2dfca,false);g.drawString(font,Component.translatable("gad01.battery"),leftPos+118,topPos+20,0xd2dfca,false);g.drawWordWrap(font,Component.translatable("gad01.load_help"),leftPos+8,topPos+54,160,0xb6c6b7);}
 @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,8,6,0xe5eede,false);g.drawString(font,playerInventoryTitle,8,76,0xb6c6b7,false);}
 @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g,x,y,partial);super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
