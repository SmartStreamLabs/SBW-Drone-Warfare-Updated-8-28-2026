package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class PlatformInventoryScreen extends AbstractContainerScreen<PlatformInventoryMenu> {
    public PlatformInventoryScreen(PlatformInventoryMenu m,Inventory i,Component t){super(m,i,t);imageWidth=256;imageHeight=238;inventoryLabelY=-10000;}
    @Override protected void init(){super.init();addRenderableWidget(Button.builder(Component.literal("Back to platform"),b->send(0)).bounds(leftPos+16,topPos+217,224,18).build());addRenderableWidget(Button.builder(Component.literal("Refuel / charge"),b->send(1)).bounds(leftPos+126,topPos+103,114,18).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Place gasoline in the drone inventory to refuel. Battery charging follows normal drone rules. Launch and unload remain locked while editing."))).build());}
    private void send(int a){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,a);}
    @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xf5101c24);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xffa8bd70);for(var s:menu.slots){g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xff718178);g.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xff26332f);}}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,16,8,0xe1eadd,false);g.drawString(font,menu.capacity()<=0?"Energy: not required":"Energy: "+menu.energy()+" / "+menu.capacity()+" FE",16,20,0xa7d9b1,false);g.drawString(font,menu.shownFuel()<0?"Fuel: not required":"Gasoline: "+menu.shownFuel()+" ticks",16,31,0xb9cbd2,false);if(menu.hasPayload)g.drawString(font,"Payload",68,109,0xe1eadd,false);g.drawString(font,"Your inventory",47,127,0xc1d0c8,false);}
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g);super.render(g,x,y,p);renderTooltip(g,x,y);}
}
