package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** One confirmed event, bounded queue, at most two adjacent mono layers. */
public final class ExplosionAudioClient {
    private record Pending(Vec3 pos,float radius,long seed,long due,long emitted,double ticksPerBlock) {}
    private static final PriorityQueue<Pending> QUEUE=new PriorityQueue<>(Comparator.comparingLong(Pending::due));
    private static final ArrayList<Pending> RETIME=new ArrayList<>(64);
    private static final ArrayDeque<List<Voice>> ACTIVE=new ArrayDeque<>();
    private static ClientLevel world;
    private static long scheduled,played,dropped;
    private static void sync() {
        var next=Minecraft.getInstance().level;
        if(world!=next) {clear();world=next;}
    }
    public static void clear() {
        var sm=Minecraft.getInstance().getSoundManager();
        for(var group:ACTIVE) for(var voice:group) voice.cancel();
        ACTIVE.clear();QUEUE.clear();RETIME.clear();
    }
    private static Vec3 listener() {var mc=Minecraft.getInstance();var camera=mc.gameRenderer.getMainCamera();return camera.isInitialized()?camera.getPosition():mc.player==null?Vec3.ZERO:mc.player.getEyePosition();}
    public static void receive(ExplosionVfxMessage m) {
        sync();if(world==null || !world.dimension().location().equals(m.dimension()) || m.profile()==0 || !Float.isFinite(m.radius()) || m.radius()<=0) return;
        Vec3 pos=new Vec3(m.x(),m.y(),m.z());if(!Double.isFinite(pos.lengthSqr()))return;
        double distance=listener().distanceTo(pos);if(distance>ExplosionAudioConfig.RANGE.get())return;
        double seconds=ExplosionAudioMath.delay(distance,ExplosionAudioConfig.SPEED.get(),ExplosionAudioConfig.DELAY.get());
        long elapsed=Math.max(0,world.getGameTime()-m.start());
        if(QUEUE.size()>=64) {++dropped;return;}
        QUEUE.add(new Pending(pos,m.radius(),m.seed(),world.getGameTime()+Math.max(0,(long)Math.ceil(seconds*20)-elapsed),world.getGameTime()-elapsed,ExplosionAudioConfig.DELAY.get()?20.0/ExplosionAudioConfig.SPEED.get():0));++scheduled;
        if(Boolean.getBoolean("sbw.audio.trace"))System.out.println("AUDIO_SCHEDULE seed="+m.seed()+" distance="+distance+" seconds="+seconds+" tick="+world.getGameTime()+" due="+QUEUE.stream().filter(p->p.seed==m.seed()).findFirst().orElseThrow().due);
    }
    public static void tick() {
        sync();if(world==null || Minecraft.getInstance().isPaused())return;
        var sm=Minecraft.getInstance().getSoundManager();
        ACTIVE.removeIf(g->g.stream().allMatch(v->world.getGameTime()-v.born>10 && !sm.isActive(v)));
        // A camera change or movement before arrival must move the wavefront deadline too.
        Vec3 ear=listener();
        while(!QUEUE.isEmpty()) {
            var p=QUEUE.remove();double distance=ear.distanceTo(p.pos);if(distance>ExplosionAudioConfig.RANGE.get())continue;
            long due=ExplosionAudioMath.arrival(p.emitted,distance,p.ticksPerBlock);
            if(due!=p.due) {
                if(Boolean.getBoolean("sbw.audio.trace") && Math.abs(due-p.due)>1)System.out.println("AUDIO_RETIME seed="+p.seed+" old="+p.due+" new="+due);
                p=new Pending(p.pos,p.radius,p.seed,due,p.emitted,p.ticksPerBlock);
            }
            RETIME.add(p);
        }
        QUEUE.addAll(RETIME);RETIME.clear();
        while(!QUEUE.isEmpty() && QUEUE.peek().due<=world.getGameTime()) {
            var p=QUEUE.remove();double d=listener().distanceTo(p.pos);
            if(d>=ExplosionAudioConfig.RANGE.get())continue;
            while(ACTIVE.size()>=ExplosionAudioConfig.EFFECTS.get()) for(var v:ACTIVE.removeFirst())v.cancel();
            double position=ExplosionAudioMath.layerPosition(d);int lo=(int)position;float blend=(float)(position-lo);
            String bank=p.radius>=6?"heavy":"small";int variant=Math.floorMod(p.seed,3);
            var group=new ArrayList<Voice>();
            for(int i=lo;i<=Math.min(3,lo+1);i++) {
                float weight=i==lo?1-blend:blend;if(weight<0.001)continue;
                String layer=new String[]{"near","mid","far","veryfar"}[i];
                var event=AddonSounds.EXPLOSION_AUDIO.get(bank+"_"+layer+"_"+variant).get();
                float strength=(float)Math.min(1,0.35+Math.sqrt(p.radius)/8);
                Voice v=new Voice(event,p.pos,weight*strength);v.updateGain();sm.play(v);group.add(v);
            }
            ACTIVE.add(group);++played;
            if(Boolean.getBoolean("sbw.audio.trace"))System.out.println("AUDIO_PLAY seed="+p.seed+" distance="+d+" tick="+world.getGameTime()+" voices="+group.size()+" origin="+p.pos);
        }
        for(var group:ACTIVE) for(var v:group) v.updateGain();
    }
    public static String diagnostics() {return "pending="+QUEUE.size()+",active="+ACTIVE.size()+",scheduled="+scheduled+",played="+played+",dropped="+dropped;}
    private static final class Voice extends AbstractTickableSoundInstance {
        private final Vec3 origin;private final float weight;private int age;private final long born;private final ClientLevel ownerWorld;
        Voice(SoundEvent event,Vec3 pos,float weight) {
            super(event,SoundSource.BLOCKS,RandomSource.create());this.origin=pos;this.weight=weight;this.born=world.getGameTime();this.ownerWorld=world;
            x=pos.x;y=pos.y;z=pos.z;pitch=1;relative=false;attenuation=Attenuation.NONE;
        }
        void updateGain(){volume=(float)(weight*2.5*ExplosionAudioConfig.VOLUME.get()*ExplosionAudioMath.distanceBoost(listener().distanceTo(origin))*ExplosionAudioMath.gain(listener().distanceTo(origin),ExplosionAudioConfig.RANGE.get()));}
        void cancel(){stop();Minecraft.getInstance().getSoundManager().stop(this);}
        @Override public void tick(){if(++age>400 || ownerWorld!=Minecraft.getInstance().level){cancel();return;}updateGain();}
        @Override public boolean canPlaySound(){return !isStopped() && ownerWorld==Minecraft.getInstance().level;}
        @Override public boolean canStartSilent(){return true;}
    }
}
