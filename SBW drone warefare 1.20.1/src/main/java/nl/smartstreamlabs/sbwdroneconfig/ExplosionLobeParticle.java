package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/** Independent flipbook lobe; all lobes share Minecraft's batched particle atlas. */
public final class ExplosionLobeParticle extends TextureSheetParticle {
    private final SpriteSet frames;
    private final int kind;
    private final float size, spin, drift;
    private int forcedFade = -1;
    long deadline;
    boolean structural;
    private final double originX, originY, originZ;
    double distanceSquared(net.minecraft.world.phys.Vec3 camera) {
        double dx=x-camera.x, dy=y-camera.y, dz=z-camera.z;
        return dx*dx+dy*dy+dz*dz;
    }
    double coverage(net.minecraft.world.phys.Vec3 camera) {
        return Math.min(1, 4 * quadSize * quadSize * alpha / Math.max(1,distanceSquared(camera)));
    }

    ExplosionLobeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                          float size, int life, int kind, float rotation, float spin, SpriteSet frames) {
        super(level, x, y, z);
        this.originX=x; this.originY=y; this.originZ=z;
        this.frames = frames;
        this.kind = kind;
        this.size = size;
        this.lifetime = life;
        this.xd = vx; this.yd = vy; this.zd = vz;
        this.roll = this.oRoll = rotation;
        this.spin = spin;
        this.drift = rotation;
        this.hasPhysics = false;
        this.alpha = kind == 3 ? 0.92F : 0;
        this.quadSize = size * 0.35F;
        setSprite(frames.get(0, 15));
    }

    public void fadeOut() { if (forcedFade < 0) forcedFade = 12; }
    @Override public void tick() {
        xo = x; yo = y; zo = z; oRoll = roll;
        if (++age >= lifetime || forcedFade == 0) { remove(); return; }
        if (forcedFade > 0) --forcedFade;
        float t = age / (float) lifetime;
        // No collision queries or chunk accesses: smoke is an entirely local visual.
        float expansion = 1-(float)Math.exp(-age/(kind==0 ? 6F : 100F));
        float rise = kind==0 ? t*t : kind==3 ? 0 : 1-(float)Math.exp(-age/120F);
        double radial = size*(kind==0 ? 0.65 : 0.60)*expansion;
        double curl = Math.sin(age*0.025+drift)*size*0.12*expansion;
        x=originX+xd*radial+Math.cos(drift+age*0.012)*curl;
        z=originZ+zd*radial+Math.sin(drift+age*0.012)*curl;
        y=originY+yd*rise;
        roll += spin*(1-t);
        quadSize=size*(kind==0 ? 0.30F+0.70F*expansion : kind==3 ? 1 : 0.35F+0.85F*expansion);
        float fadeIn = Mth.clamp(age / (kind == 0 || kind == 3 ? 1F : 25F), 0, 1);
        float fadeOut = Mth.clamp((1 - t) / (kind == 0 || kind == 3 ? 0.32F : 0.42F), 0, 1);
        alpha = (kind == 0 || kind == 3 ? 0.92F : kind == 1 ? 0.60F : 0.30F) * fadeIn * fadeOut;
        if (forcedFade >= 0) alpha *= forcedFade / 12F;
        setSprite(frames.get(kind == 3 ? 0 : Math.min(15, kind==0 ? (int)(t*16) : 5+(int)(t*11)), 15));
    }
    @Override public int getLightColor(float partialTick) {
        return kind == 0 || kind == 3 ? 0xF000F0 : super.getLightColor(partialTick);
    }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.NO_RENDER; }
}
