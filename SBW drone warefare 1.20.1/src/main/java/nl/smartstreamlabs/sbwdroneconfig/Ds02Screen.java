package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.util.List;

/** Same trusted-pilot controls as the compact siren; leased viewers keep one physical door open. */
public final class Ds02Screen extends Screen {
    private final BlockPos pos;private final List<String> names;private EditBox input;private int ticks;
    public Ds02Screen(BlockPos p,List<String> names){super(Component.translatable("block.sbwdroneconfig.drone_warning_siren_ds02"));pos=p;this.names=List.copyOf(names);}
    private void send(int a){AddonNetwork.ds02Control(new Ds02ControlMessage(pos,a));}
    @Override protected void init(){
        int x=(width-260)/2,y=42;
        input=new EditBox(font,x+10,y+36,240,20,Component.translatable("screen.sbwdroneconfig.siren.input"));input.setMaxLength(32);addRenderableWidget(input);
        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.siren.add"),b->submit(false)).bounds(x+10,y+62,114,20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.sbwdroneconfig.siren.remove"),b->submit(true)).bounds(x+136,y+62,114,20).build());
        addRenderableWidget(Button.builder(Component.translatable("ds02.toggle"),b->send(2)).bounds(x+10,y+166,240,20).build());send(0);
    }
    private void submit(boolean remove){String value=input.getValue().trim();if(!value.isEmpty())AddonNetwork.updateSirenBlacklist(new UpdateSirenBlacklistMessage(pos,value,remove));}
    @Override public void tick(){super.tick();if(++ticks%20==0)send(0);if(minecraft.level==null || !(minecraft.level.getBlockEntity(pos) instanceof Ds02BlockEntity))onClose();}
    @Override public void removed(){super.removed();if(!(Minecraft.getInstance().screen instanceof Ds02Screen next && next.pos.equals(pos)))send(1);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);int x=(width-260)/2,y=42;g.fill(x,y,x+260,y+198,0xf012161c);
        g.drawCenteredString(font,title,width/2,y+8,0xffffff);
        g.drawString(font,Component.translatable("screen.sbwdroneconfig.siren.input"),x+10,y+24,0xcccccc,false);
        g.drawString(font,Component.translatable("screen.sbwdroneconfig.siren.blacklist"),x+10,y+92,0xcccccc,false);
        for(int i=0;i<Math.min(4,names.size());i++)g.drawString(font,names.get(i),x+12,y+105+i*11,0xffffff,false);
        if(minecraft.level!=null && minecraft.level.getBlockEntity(pos) instanceof Ds02BlockEntity be)
            g.drawString(font,Component.translatable(be.enabled()?be.alarming()?"ds02.alarm":"ds02.ready":"ds02.off"),x+10,y+151,be.alarming()?0xff6666:0x99dd99,false);
        super.render(g,mx,my,partial);
    }
}
