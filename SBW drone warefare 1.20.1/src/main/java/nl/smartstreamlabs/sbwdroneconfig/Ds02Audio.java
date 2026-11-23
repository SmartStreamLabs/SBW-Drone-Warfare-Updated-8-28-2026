package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import java.util.IdentityHashMap;

/** One continuous mono source per loaded siren; no packet-triggered sound duplication. */
public final class Ds02Audio {
    private static final IdentityHashMap<Ds02BlockEntity,Voice> VOICES=new IdentityHashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel world;
    private static Vec3 listener(){var m=Minecraft.getInstance();var c=m.gameRenderer.getMainCamera();return c.isInitialized()?c.getPosition():m.player==null?Vec3.ZERO:m.player.getEyePosition();}
    public static void tick(Ds02BlockEntity be){
        var mc=Minecraft.getInstance();sync();
        if(mc.level==null || mc.isPaused())return;
        var voice=VOICES.get(be);
        if(!valid(be)){if(voice!=null){voice.cancel();VOICES.remove(be);}return;}
        // If a resource reload stopped OpenAL, restart the same active alarm once, after startup grace.
        if(voice!=null && world.getGameTime()-voice.created>20 && !mc.getSoundManager().isActive(voice)){voice.cancel();VOICES.remove(be);voice=null;}
        if(voice==null && VOICES.size()<24){voice=new Voice(be);VOICES.put(be,voice);mc.getSoundManager().play(voice);}
    }
    private static boolean valid(Ds02BlockEntity be){
        return world!=null && be.getLevel()==world && !be.isRemoved() && world.hasChunkAt(be.getBlockPos())
            && world.getBlockEntity(be.getBlockPos())==be && be.alarming()
            && listener().distanceToSqr(Vec3.atCenterOf(be.getBlockPos()).add(0,3.56,0))<be.audibleRange()*be.audibleRange();
    }
    public static void sync(){if(world!=Minecraft.getInstance().level){for(var v:VOICES.values())v.cancel();VOICES.clear();world=Minecraft.getInstance().level;}}
    public static void clear(){for(var v:VOICES.values())v.cancel();VOICES.clear();}
    public static void maintain(){sync();VOICES.entrySet().removeIf(e->{if(!valid(e.getKey())){e.getValue().cancel();return true;}return false;});}
    public static String diagnostics(){return "voices="+VOICES.size();}
    private static final class Voice extends AbstractTickableSoundInstance {
        private final Ds02BlockEntity be;private final long created;int age;
        Voice(Ds02BlockEntity be){
            super(AddonSounds.DS02_ALARM.get(),SoundSource.BLOCKS,RandomSource.create());this.be=be;this.created=be.getLevel().getGameTime();
            x=be.getBlockPos().getX()+.5;y=be.getBlockPos().getY()+4.0625;z=be.getBlockPos().getZ()+.5;
            looping=true;delay=0;pitch=1;relative=false;attenuation=Attenuation.NONE;gain();
        }
        private void gain(){double t=Math.min(1,listener().distanceTo(new Vec3(x,y,z))/Math.max(1,be.audibleRange()));volume=(float)(.75*(1-t)*(1-t));}
        void cancel(){stop();Minecraft.getInstance().getSoundManager().stop(this);}
        @Override public void tick(){age++;if(!valid(be)){cancel();return;}gain();}
        @Override public boolean canPlaySound(){return valid(be);}
        @Override public boolean canStartSilent(){return true;}
    }
}
