package nl.smartstreamlabs.sbwdroneconfig;
public final class Qd01Client {
 public static void shot(Qd01Entity q){var m=net.minecraft.client.Minecraft.getInstance();if(m.level==null)return;var p=q.getShootPos("Cannon",1);var d=q.getBarrelTransform(1).transformDirection(new org.joml.Vector3d(0,0,1));m.level.addParticle(new com.atsuishio.superbwarfare.client.particle.CannonMuzzleFlareOption(1f,.85f,.5f,1,.08f,1,.04f),p.x,p.y,p.z,d.x*.1,d.y*.1,d.z*.1);if(VatagClient.active()&&VatagClient.vehicle()==q){var g=q.getGunData("Cannon");if(g!=null){var s=g.get(com.atsuishio.superbwarfare.data.gun.GunProp.SOUND_INFO).getFire1P();if(s!=null)m.getSoundManager().play(new net.minecraft.client.resources.sounds.SimpleSoundInstance(s,net.minecraft.sounds.SoundSource.BLOCKS,.6f,1,net.minecraft.util.RandomSource.create(),p.x,p.y,p.z));}}}
 private Qd01Client(){}
}
