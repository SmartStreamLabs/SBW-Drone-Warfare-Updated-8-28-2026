package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class CarrierFleetScreen extends AbstractContainerScreen<CarrierFleetMenu> {
    private Button back;private final Button[] control;
    public CarrierFleetScreen(CarrierFleetMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=380;imageHeight=232;control=new Button[menu.rows.size()];}
    private void send(int action){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
    @Override protected void init(){
        imageWidth=Math.min(420,width-12);super.init();
        back=addRenderableWidget(Button.builder(Component.literal("Return to carrier"),b->send(0)).bounds(leftPos+8,topPos+23,(imageWidth-20)/2,18).build());
        addRenderableWidget(Button.builder(Component.literal("Loaded bays / inventories"),b->send(1)).bounds(leftPos+12+(imageWidth-20)/2,topPos+23,(imageWidth-20)/2,18).build());
        for(int i=0;i<control.length;i++){final int row=i;control[i]=addRenderableWidget(Button.builder(Component.literal("Control"),b->send(10+row)).bounds(leftPos+imageWidth-67,topPos+49+i*21,59,18).build());}
        var prev=addRenderableWidget(Button.builder(Component.literal("Previous"),b->send(2)).bounds(leftPos+8,topPos+imageHeight-22,70,18).build());prev.active=menu.page>0;
        var next=addRenderableWidget(Button.builder(Component.literal("Next"),b->send(3)).bounds(leftPos+imageWidth-78,topPos+imageHeight-22,70,18).build());next.active=menu.page+1<menu.total;
        update();
    }
    private void update(){
        for(int i=0;i<control.length;i++){
            int state=menu.state(i);control[i].active=state==0||state==1||state==2;
            control[i].setTooltip(Tooltip.create(Component.literal(state==2?"Load the last known chunk, validate the connection, then switch. Current control is retained if unavailable.":CarrierFleet.STATUS[Math.max(0,Math.min(state,CarrierFleet.STATUS.length-1))])));
        }
        int state=menu.state(0);back.active=state==0||state==1||state==2;back.setTooltip(Tooltip.create(Component.literal(CarrierFleet.STATUS[Math.max(0,Math.min(state,CarrierFleet.STATUS.length-1))])));
    }
    @Override public void containerTick(){super.containerTick();update();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xf5101c24);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+2,0xff9db887);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,"Carrier Fleet · permanent home link",8,8,0xe0eadd,false);
        for(int i=0;i<menu.rows.size();i++){
            var row=menu.rows.get(i);int y=48+i*21;String label=(i==0?"Carrier":String.format(java.util.Locale.ROOT,"#%03d",row.number()))+" · "+row.name();
            g.drawString(font,font.plainSubstrByWidth(label,imageWidth-86),8,y,0xe0eadd,false);
            int state=Math.max(0,Math.min(menu.state(i),CarrierFleet.STATUS.length-1));String details=CarrierFleet.STATUS[state]+(state==2||state==3?" · Energy unknown":" · "+menu.energy(i)+"/"+menu.capacity(i)+" FE");
            g.drawString(font,font.plainSubstrByWidth(details,imageWidth-86),8,y+9,state<=1?0x9cdda7:0xe1bd88,false);
        }
        g.drawWordWrap(font,Component.literal(menu.leaving),8,184,imageWidth-16,0xe1bd88);
        g.drawCenteredString(font,"Page "+(menu.page+1)+"/"+Math.max(1,menu.total),imageWidth/2,imageHeight-17,0xb8c5c5);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g);super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
