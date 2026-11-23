package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.Minecraft;import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;import net.minecraft.sounds.SoundSource;import net.minecraft.util.Mth;import net.minecraft.core.particles.ParticleTypes;import java.util.*;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class Wd01Client {
 private static final Map<UUID,Drive> SOUNDS=new HashMap<>();
 public static void effects(Wd01Entity w){
  var m=Minecraft.getInstance();double speed=w.getDeltaMovement().horizontalDistance();boolean driving=w.floating()&&!w.isWreck()&&Math.abs(w.getPower())>.025;
  if(driving&&m.gameRenderer.getMainCamera().getPosition().distanceToSqr(w.position())<64*64&&SOUNDS.size()<24){var old=SOUNDS.get(w.getUUID());if(old==null||!m.getSoundManager().isActive(old)){var s=new Drive(w);SOUNDS.put(w.getUUID(),s);m.getSoundManager().play(s);}}
  if(!w.floating()||speed<.035||w.tickCount%3!=0||m.gameRenderer.getMainCamera().getPosition().distanceToSqr(w.position())>64*64)return;
  var t=w.getVehicleTransform(1);if(w instanceof Ndc01Entity)t.translate(0,.7,0);int count=speed>.2?4:2;for(int i=0;i<count;i++){
   var at=t.transformPosition(new org.joml.Vector3d((i%2==0?-(w instanceof Ndc01Entity?1.15:.55):(w instanceof Ndc01Entity?1.15:.55)),.015,i<2?(w instanceof Ndc01Entity?5.7:2.42):(w instanceof Ndc01Entity?-5.8:-2.4)));
   w.level().addParticle(i<2?ParticleTypes.SPLASH:ParticleTypes.BUBBLE_POP,at.x,at.y,at.z,0,.025,0);
  }
 }
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var m=Minecraft.getInstance();SOUNDS.entrySet().removeIf(en->{var s=en.getValue();if(s.boat.isRemoved()||s.boat.level()!=m.level||!s.boat.isAlive()){m.getSoundManager().stop(s);return true;}return !m.getSoundManager().isActive(s);});}
 private static final class Drive extends AbstractTickableSoundInstance {
  final Wd01Entity boat;Drive(Wd01Entity w){super(AddonSounds.WD01_DRIVE.get(),SoundSource.AMBIENT,w.level().random);boat=w;looping=true;relative=false;delay=0;attenuation=Attenuation.LINEAR;volume=.001f;tick();}
  @Override public boolean canStartSilent(){return true;}
  @Override public void tick(){var m=Minecraft.getInstance();if(boat.isRemoved()||!boat.isAlive()||boat.isWreck()||m.level!=boat.level()){stop();return;}x=boat.getX();y=boat.getY();z=boat.getZ();float load=boat.floating()?Math.abs(boat.getPower()):0;volume=Mth.lerp(.2f,volume,load*.45f);pitch=Mth.lerp(.15f,pitch,.75f+load*.4f);if(load<.01&&volume<.004)stop();}
 }
 private Wd01Client(){}
}
