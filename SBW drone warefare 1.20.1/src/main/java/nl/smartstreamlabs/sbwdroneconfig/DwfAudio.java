package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.lang.ref.WeakReference;import net.minecraft.client.Minecraft;import net.minecraft.client.resources.sounds.*;import net.minecraft.sounds.*;
/** One faded motor per loaded, audible machine. World changes/unload never keep
 * a sound or a strong reference to an old level alive. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class DwfAudio{
 private static final Map<DwfBlockEntity,Loop>WATCHED=new WeakHashMap<>();
 public static void watch(DwfBlockEntity b){if(b.controller())WATCHED.putIfAbsent(b,null);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event){if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var m=Minecraft.getInstance();var it=WATCHED.entrySet().iterator();while(it.hasNext()){var entry=it.next();var b=entry.getKey();boolean live=m.level!=null&&b.getLevel()==m.level&&!b.isRemoved();boolean active=live&&b.visualStatus.equals("RUNNING")&&m.player!=null&&m.player.distanceToSqr(b.getBlockPos().getCenter())<48*48;if(active&&(entry.getValue()==null||entry.getValue().isStopped())){var sound=new Loop(b);entry.setValue(sound);m.getSoundManager().play(sound);}if(!live){if(entry.getValue()!=null)entry.getValue().fade();it.remove();}}}
 private static final class Loop extends AbstractTickableSoundInstance{
  private final WeakReference<DwfBlockEntity>block;private float gain;private boolean fading;private int phase=-1;
  Loop(DwfBlockEntity b){super(AddonSounds.DWF_MOTOR.get(),SoundSource.BLOCKS,net.minecraft.util.RandomSource.create());block=new WeakReference<>(b);x=b.getBlockPos().getX()+.5;y=b.getBlockPos().getY()+2;z=b.getBlockPos().getZ()+.5;looping=true;delay=0;attenuation=SoundInstance.Attenuation.LINEAR;volume=.001f;pitch=1;}
  void fade(){fading=true;}
  @Override public boolean canStartSilent(){return true;}
  @Override public void tick(){var b=block.get();var m=Minecraft.getInstance();boolean active=!fading&&b!=null&&!b.isRemoved()&&b.getLevel()==m.level&&b.visualStatus.equals("RUNNING")&&m.player!=null&&m.player.distanceToSqr(x,y,z)<48*48;gain=Math.max(0,Math.min(1,gain+(active?.1f:-.1f)));volume=(float)(gain*DwfConfig.SOUND_VOLUME.get());if(!active&&gain<=0){stop();return;}if(active){double p=Math.min(1,(b.progress+Math.max(0,m.level.getGameTime()-b.visualTick))/(double)Math.max(1,b.required));int stage=p<.2?0:p<.65?1:p<.85?2:3;if(phase>=0&&stage!=phase)m.level.playLocalSound(x,y,z,stage==3?SoundEvents.PISTON_CONTRACT:SoundEvents.PISTON_EXTEND,SoundSource.BLOCKS,(float)(DwfConfig.SOUND_VOLUME.get()*.25),1,false);phase=stage;}}
 }
 private DwfAudio(){}
}
