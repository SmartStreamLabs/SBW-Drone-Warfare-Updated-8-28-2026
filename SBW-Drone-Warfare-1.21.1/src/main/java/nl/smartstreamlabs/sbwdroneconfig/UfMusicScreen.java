package nl.smartstreamlabs.sbwdroneconfig;

import java.util.UUID;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.gui.components.*;import net.minecraft.client.gui.screens.Screen;import net.minecraft.core.BlockPos;import net.minecraft.network.chat.Component;

public final class UfMusicScreen extends Screen {
    public final BlockPos pos;private final UUID identity;
    private int x,y,w,ticks;private Button play,pause,stop,repeat;private SettingSlider volume,range;
    private long sentVolume,sentRange;private int pendingVolume=-1,pendingRange=-1;
    public UfMusicScreen(BlockPos pos,UUID identity){super(Component.translatable("ufmusic.title"));this.pos=pos;this.identity=identity;}
    private UfMusicReply state(){var e=UfMusicClient.ENTRIES.get(pos);return e==null?null:e.state;}
    private void send(int action,int value){UfMusicClient.request(pos,identity,action,value);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
    @Override protected void init(){
        w=Math.min(348,width-20);x=(width-w)/2;y=Math.max(3,(height-236)/2);int third=(w-8)/3;var s=state();
        play=addRenderableWidget(Button.builder(Component.translatable("ufmusic.play"),b->send(UfMusicServer.PLAY,0)).bounds(x,y+84,third,20).build());
        pause=addRenderableWidget(Button.builder(Component.translatable("ufmusic.pause"),b->send(UfMusicServer.PAUSE,0)).bounds(x+third+4,y+84,third,20).build());
        stop=addRenderableWidget(Button.builder(Component.translatable("ufmusic.stop"),b->send(UfMusicServer.STOP,0)).bounds(x+2*(third+4),y+84,w-2*(third+4),20).build());
        volume=addRenderableWidget(new SettingSlider(x,y+110,w,s==null?50:s.volume(),false));
        repeat=addRenderableWidget(Button.builder(Component.empty(),b->send(UfMusicServer.REPEAT,state()!=null&&state().repeat()?0:1)).bounds(x,y+136,w,20).build());
        range=addRenderableWidget(new SettingSlider(x,y+162,w,s==null?64:s.range(),true));
        addRenderableWidget(Button.builder(Component.translatable("ufmusic.done"),b->onClose()).bounds(x,y+212,w,20).build());
    }
    private void flush(boolean force){long now=System.nanoTime();if(pendingVolume>=0&&(force||now-sentVolume>=200_000_000L)){send(UfMusicServer.VOLUME,pendingVolume);pendingVolume=-1;sentVolume=now;}if(pendingRange>=0&&(force||now-sentRange>=200_000_000L)){send(UfMusicServer.RANGE,pendingRange);pendingRange=-1;sentRange=now;}}
    @Override public void tick(){var s=state();if(s==null||!identity.equals(s.identity())||Minecraft.getInstance().level==null){onClose();return;}if(ticks++%20==0)send(UfMusicServer.WATCH,0);flush(false);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        // Draw the backdrop first: drawing the 1.21 blur after labels made earlier menus unreadable.
        g.fill(0,0,width,height,0x9a071012);g.fill(x-8,y-3,x+w+8,y+236,0xf01a272b);g.fill(x-8,y-3,x+w+8,y-1,0xff5793ad);
        g.drawCenteredString(font,title,width/2,y+2,0xffe6edf0);var s=state();var entry=UfMusicClient.ENTRIES.get(pos);
        if(s!=null){
            boolean missing=s.duration()<=0||!UfMusicClient.available();String failure=entry!=null&&entry.voice!=null?entry.voice.failure:"";
            g.drawCenteredString(font,font.plainSubstrByWidth(s.title(),w-4),width/2,y+20,0xffbeced6);
            String status=missing||!failure.isEmpty()?"ufmusic.missing":s.state()==UfMusicClock.PLAYING?"ufmusic.playing":s.state()==UfMusicClock.PAUSED?"ufmusic.paused":"ufmusic.stopped";
            g.drawString(font,Component.translatable(status),x,y+36,missing?0xffffcb71:0xff9ed5bd);
            long elapsed=entry==null?0:entry.position();String time=clock(elapsed)+" / "+(s.duration()<=0?"--:--":clock(s.duration()));g.drawString(font,time,x+w-font.width(time),y+36,0xffe0e6e8);
            g.fill(x,y+53,x+w,y+61,0xff0b151a);int fill=s.duration()<=0?0:(int)Math.min(w,w*elapsed/s.duration());g.fill(x,y+53,x+fill,y+61,0xff4d9dc3);
            g.drawString(font,Component.translatable(s.edit()?"ufmusic.personal":"ufmusic.readonly"),x,y+68,0xffa4b5be);
            play.active=s.edit();pause.active=s.edit()&&!missing&&s.state()!=UfMusicClock.STOPPED;stop.active=s.edit()&&s.state()!=UfMusicClock.STOPPED;
            pause.setMessage(Component.translatable(s.state()==UfMusicClock.PAUSED?"ufmusic.resume":"ufmusic.pause"));volume.active=s.edit();range.active=s.operator();repeat.active=false;
            repeat.setMessage(Component.translatable("ufmusic.repeat",Component.translatable(s.repeat()?"ufmusic.on":"ufmusic.off")));
            volume.sync(s.volume());range.sync(s.range());
            String notice=!s.notice().isEmpty()?s.notice():!failure.isEmpty()?failure:missing?"ufmusic.missing_hint":"ufmusic.foot";
            var lines=font.split(Component.translatable(notice),w);for(int i=0;i<Math.min(2,lines.size());i++)g.drawString(font,lines.get(i),x,y+188+i*10,0xffe2ca91);
        }
        super.render(g,mx,my,partial);
    }
    public static String clock(long ms){long secs=Math.max(0,ms/1000);return String.format(java.util.Locale.ROOT,"%d:%02d",secs/60,secs%60);}
    @Override public boolean mouseReleased(double mx,double my,int button){boolean result=super.mouseReleased(mx,my,button);flush(true);return result;}
    @Override public void onClose(){flush(true);send(UfMusicServer.CLOSE,0);Minecraft.getInstance().setScreen(null);}
    private final class SettingSlider extends AbstractSliderButton {
        private final boolean radius;private long input;
        SettingSlider(int x,int y,int w,int v,boolean radius){super(x,y,w,20,Component.empty(),radius?(v-8)/248d:v/100d);this.radius=radius;updateMessage();}
        int setting(){return radius?8+(int)Math.round(value*248):(int)Math.round(value*100);}
        @Override protected void updateMessage(){setMessage(Component.translatable(radius?"ufmusic.range":"ufmusic.volume",setting()));}
        @Override protected void applyValue(){input=System.nanoTime();if(radius)pendingRange=setting();else pendingVolume=setting();flush(false);}
        void sync(int v){if(System.nanoTime()-input>600_000_000L){value=radius?(v-8)/248d:v/100d;updateMessage();}}
    }
}
