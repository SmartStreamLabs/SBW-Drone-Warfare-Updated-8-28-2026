package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.gui.components.*;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.network.chat.Component;import net.minecraft.world.entity.player.Inventory;
public class HxPlatformScreen extends AbstractContainerScreen<HxPlatformMenu> {
    private Button inventory;
    public HxPlatformScreen(HxPlatformMenu m,Inventory inv,Component t){super(m,inv,t);imageWidth=256;imageHeight=164;inventoryLabelY=-10000;}
    protected void init(){super.init();inventory=addRenderableWidget(Button.builder(Component.literal("Drone Inventory"),b->send(10)).bounds(leftPos+12,topPos+60,232,20).build());addRenderableWidget(Button.builder(Component.literal("Launch"),b->send(0)).bounds(leftPos+12,topPos+112,112,20).build());addRenderableWidget(Button.builder(Component.literal("Unload"),b->send(1)).bounds(leftPos+132,topPos+112,112,20).build());addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(leftPos+12,topPos+138,232,20).build());update();}
    private void send(int a){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,a);}
    private void update(){inventory.active=menu.inventoryAvailable();inventory.setTooltip(Tooltip.create(Component.literal(menu.inventoryHint())));}
    public void containerTick(){super.containerTick();update();}
    protected void renderBg(GuiGraphics g,float p,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xf5101c24);}
    protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,12,12,0xe1eadd,false);g.drawString(font,"Energy: "+menu.droneEnergy()+" / "+menu.droneCapacity()+" FE",12,35,0xa8d9b0,false);g.drawWordWrap(font,Component.literal(menu.inventoryHint()),12,85,232,0xe1bb7c);}
    public void render(GuiGraphics g,int x,int y,float p){renderBackground(g);super.render(g,x,y,p);renderTooltip(g,x,y);}
}
