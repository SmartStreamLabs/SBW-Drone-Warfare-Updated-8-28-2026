package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.util.UUID;
/** Selection alone never arms interception. Confirmation is a separate server-validated action. */
public final class Id01RadarScreen extends Screen {
    private UUID selected;private Button confirm;private long signature;
    public Id01RadarScreen(){super(Component.literal("ID-01 radar — select, then confirm"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){rebuild();}
    private void rebuild(){clearWidgets();var s=Id01Client.snapshot();if(s==null)return;
        int x=width/2-150,y=40;int count=Math.min(s.contacts().size(),Math.max(0,(height-125)/22));
        for(var t:s.contacts().subList(0,count)){
            String text=(t.id().equals(selected)?"> ":"")+t.name()+String.format(java.util.Locale.ROOT,"  %.0f m",t.distance())+(t.hostile()?"":" [PROTECTED]");
            var b=Button.builder(Component.literal(text),button->{selected=t.id();rebuild();}).bounds(x,y,300,20).build();b.active=t.hostile();addRenderableWidget(b);y+=22;
        }
        confirm=Button.builder(Component.literal("Confirm interception"),b->{Id01Client.action(5,selected);onClose();}).bounds(x,height-55,195,20).build();confirm.active=selected!=null&&s.contacts().stream().anyMatch(t->t.id().equals(selected)&&t.hostile());addRenderableWidget(confirm);
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(x+200,height-55,100,20).build());signature=System.nanoTime();
    }
    @Override public void tick(){if(!Id01Client.active()){onClose();return;}if(System.nanoTime()-signature>500000000L)rebuild();}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){g.fill(0,0,width,height,0xb0101b22);g.drawCenteredString(font,title,width/2,18,0xffffff);var s=Id01Client.snapshot();if(s!=null&&s.contacts().isEmpty())g.drawCenteredString(font,"No loaded drones in radar range",width/2,48,0xaaaaaa);g.drawCenteredString(font,"Friendly and unknown owners are protected. No automatic target switching.",width/2,height-25,0xaaaaaa);super.render(g,mouseX,mouseY,partial);}
}
