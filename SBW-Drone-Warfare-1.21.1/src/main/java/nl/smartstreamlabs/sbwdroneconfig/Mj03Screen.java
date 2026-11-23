package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import java.util.*;
public class Mj03Screen extends Screen {
    private final int entity;private final List<String> names;private EditBox input;
    public Mj03Screen(int id,List<String> list){super(Component.translatable("entity.sbwdroneconfig.mobile_jammer_truck_mj03"));entity=id;names=List.copyOf(list);}
    public static void open(Mj03OpenMessage m){Minecraft.getInstance().setScreen(new Mj03Screen(m.entity(),m.names()));}
    private Mj03Entity truck(){return minecraft.level!=null && minecraft.level.getEntity(entity) instanceof Mj03Entity t?t:null;}
    private void send(int a){AddonNetwork.mj03Control(new Mj03ControlMessage(entity,a,input.getValue()));}
    @Override protected void init(){
        int x=(width-270)/2,y=(height-224)/2;
        addRenderableWidget(Button.builder(Component.translatable("mj03.toggle"),b->send(0)).bounds(x+10,y+61,152,20).build());
        addRenderableWidget(Button.builder(Component.translatable("mj03.lights"),b->send(3)).bounds(x+170,y+61,90,20).build());
        input=new EditBox(font,x+10,y+104,250,20,Component.translatable("screen.sbwdroneconfig.siren.input"));input.setMaxLength(32);addRenderableWidget(input);
        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.siren.add"),b->send(1)).bounds(x+10,y+130,120,20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.siren.remove"),b->send(2)).bounds(x+140,y+130,120,20).build());
    }
    @Override public void tick(){if(truck()==null || truck().isRemoved() || minecraft.player.distanceToSqr(truck())>100)onClose();}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int mx,int my,float p){
        renderBackground(g,mx,my,p);int x=(width-270)/2,y=(height-224)/2;g.fill(x,y,x+270,y+224,0xf015191e);super.render(g,mx,my,p);
        g.drawCenteredString(font,title,width/2,y+10,0xffffff);var t=truck();
        if(t!=null){g.drawString(font,Component.translatable("mj03.phase."+t.phase()),x+10,y+30,t.operational()?0x88ff88:0xffcc88,false);g.drawString(font,"12,000 blocks | "+t.getEnergy()+" FE",x+10,y+44,0xcccccc,false);}
        g.drawString(font,Component.translatable("screen.sbwdroneconfig.siren.input"),x+10,y+91,0xcccccc,false);
        for(int i=0;i<Math.min(5,names.size());i++)g.drawString(font,names.get(i),x+12,y+158+11*i,0xcccccc,false);
    }
}
