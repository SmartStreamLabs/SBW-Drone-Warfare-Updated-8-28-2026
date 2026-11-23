package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Draft editing is local. Only Apply submits bounded strings; the server validates the active window. */
public abstract class MissionPlatformScreen<M extends AbstractContainerMenu & MissionMenu & PlatformInventoryState> extends AbstractContainerScreen<M> {
    private EditBox x,y,z,cruise;
    private Button mode,apply,launch,unload,cancel,droneInventory;
    private boolean coordinates,enabled,dirty;
    protected final String prefix;
    protected MissionPlatformScreen(M menu,Inventory inventory,Component title,String prefix){super(menu,inventory,title);this.prefix=prefix;imageWidth=264;imageHeight=238;inventoryLabelY=-10000;}
    protected abstract int reason();
    protected abstract int phase();
    protected abstract Component details();
    private MissionPlatform platform(){return minecraft==null||minecraft.player==null?null:menu.missionPlatform(minecraft.player);}
    @Override protected void init(){
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Controls"),b->{coordinates=false;visibility();}).bounds(leftPos+12,topPos+27,94,18).build());
        addRenderableWidget(Button.builder(Component.literal("Coordinates Mission"),b->{coordinates=true;visibility();}).bounds(leftPos+110,topPos+27,142,18).build());
        x=field(12,107,76);y=field(94,107,76);z=field(176,107,76);cruise=field(12,142,116);
        var p=platform();if(p!=null){var d=p.mission();enabled=d.enabled;x.setValue(d.x);y.setValue(d.y);z.setValue(d.z);cruise.setValue(d.cruise);}
        for(var box:new EditBox[]{x,y,z,cruise})box.setResponder(v->dirty=true);
        mode=addRenderableWidget(Button.builder(Component.empty(),b->{enabled=!enabled;dirty=true;}).bounds(leftPos+136,topPos+142,116,20).build());
        apply=addRenderableWidget(Button.builder(Component.literal("Apply coordinates"),b->request(false)).bounds(leftPos+12,topPos+166,240,20).build());
        launch=addRenderableWidget(Button.builder(Component.translatable(prefix+".launch"),b->send(0)).bounds(leftPos+12,topPos+190,116,20).build());
        unload=addRenderableWidget(Button.builder(Component.translatable(prefix+".return"),b->send(1)).bounds(leftPos+136,topPos+190,116,20).build());
        cancel=addRenderableWidget(Button.builder(Component.literal("Cancel mission"),b->{if(coordinates)request(true);else send(2);}).bounds(leftPos+12,topPos+214,116,20).build());
        droneInventory=addRenderableWidget(Button.builder(Component.literal("Drone Inventory"),b->{if(dirty)request(false);send(10);}).bounds(leftPos+136,topPos+214,116,20).build());
        visibility();
    }
    private EditBox field(int dx,int dy,int width){var box=new EditBox(font,leftPos+dx,topPos+dy,width,20,Component.literal("Coordinate"));box.setMaxLength(32);return addRenderableWidget(box);}
    private void visibility(){for(var box:new EditBox[]{x,y,z,cruise})box.visible=coordinates;mode.visible=apply.visible=coordinates;var p=platform();cruise.setEditable(p!=null&&!p.missionKind().equals("glide"));}
    private void send(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private void request(boolean cancel){
        var message=new MissionRequest(menu.containerId,menu.missionPos(),enabled,cancel,x.getValue(),y.getValue(),z.getValue(),cruise.getValue());
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(message);
        if(!cancel)dirty=false;
    }
    @Override public void containerTick(){super.containerTick();droneInventory.active=menu.inventoryAvailable();droneInventory.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(menu.inventoryHint())));var p=platform();launch.active=reason()==0&&!dirty;unload.active=phase()==2;cancel.active=true;mode.setMessage(Component.literal(enabled?"Mission: ON":"Mission: OFF"));}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xee101b22);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xffa8bd70);g.fill(leftPos+20,topPos+47,leftPos+40,topPos+67,0xff39434a);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,12,12,0xcbd6bb,false);g.drawString(font,menu.getSlot(0).hasItem()?menu.getSlot(0).getItem().getHoverName():Component.translatable(prefix+".no_drone"),48,53,0xffffff,false);
        g.drawString(font,font.plainSubstrByWidth("Energy: "+menu.droneEnergy()+" / "+menu.droneCapacity()+" FE",204),48,64,0xa8d9b0,false);
        var p=platform();if(p==null)return;var d=p.mission();
        if(coordinates){
            String status=d.last==null?(d.valid?"Ready":"Enter coordinates"):d.status;
            g.drawString(font,status,12,73,0xd0d9dc,false);
            if(d.valid)g.drawString(font,String.format(java.util.Locale.ROOT,"Target %s / %s / %s (%.1f m)",d.x,d.y,d.z,p.missionMount().distanceTo(d.target)),12,85,0xa8d780,false);
            else g.drawString(font,font.plainSubstrByWidth(d.message,240),12,85,0xe1b774,false);
            g.drawString(font,"X",12,97,0xffffff,false);g.drawString(font,"Y",94,97,0xffffff,false);g.drawString(font,"Z",176,97,0xffffff,false);
            g.drawString(font,p.missionKind().equals("glide")?"Unpowered trajectory":"Cruise Y (optional)",12,132,0xd0d9dc,false);
            String feedback=d.message.isEmpty()?d.result:d.message;
            if(!feedback.isEmpty()&&my>=topPos+70&&my<=topPos+94)g.renderTooltip(font,Component.literal(feedback),Math.max(5,mx-leftPos),Math.max(5,my-topPos));
            // Hover feedback is supplemented by the player's server error message when Apply fails.
        }else{
            g.drawString(font,Component.translatable(prefix+".status",Component.translatable(prefix+".phase."+phase())),12,82,0xd0d9dc,false);
            g.drawString(font,details(),12,104,0xd0d9dc,false);
            g.drawWordWrap(font,Component.translatable(reasonKey()),12,126,240,reason()==0?0xa8d780:0xe1b774);
            g.drawWordWrap(font,Component.literal("Mission: "+(d.enabled?"ON":"OFF")+" / "+d.status),12,155,240,0xd0d9dc);
        }
    }
    protected abstract String reasonKey();
    @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
