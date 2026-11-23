package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.ParticleStatus;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Random;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

/** Fixed budgets, bounded work per tick, shared atlas, no server simulation or chunk tickets. */
public final class ExplosionVfxClient {
    public static final int MAX_EFFECTS = 12, MAX_PARTICLES = 256;
    private static final SpriteSet[] SPRITES = new SpriteSet[3];
    private static final ArrayList<Effect> EFFECTS = new ArrayList<>();
    private static final ArrayList<ExplosionLobeParticle> PARTICLES = new ArrayList<>();
    private static final ArrayDeque<Marker> MARKERS = new ArrayDeque<>();
    private static ClientLevel world;
    private static long clock;
    private static double coverage;
    private static long received, spawned, suppressed, rejected;
    private ExplosionVfxClient() {}

    public static void init(net.neoforged.bus.api.IEventBus bus) {
        bus.addListener(ExplosionVfxClient::registerSprites);
        bus.addListener(ExplosionVfxRender::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(ExplosionVfxClient.class);
    }

    static SpriteSet hgSprite(int index){return SPRITES[index];}
    private static void registerSprites(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ExplosionVfxParticles.FIRE.get(), sprites -> { SPRITES[0] = sprites; return (o,l,x,y,z,a,b,c) -> null; });
        event.registerSpriteSet(ExplosionVfxParticles.SMOKE.get(), sprites -> { SPRITES[1] = sprites; return (o,l,x,y,z,a,b,c) -> null; });
        event.registerSpriteSet(ExplosionVfxParticles.REST.get(), sprites -> { SPRITES[2] = sprites; return (o,l,x,y,z,a,b,c) -> null; });
    }

    private static void synchronizeWorld() {
        ClientLevel next = Minecraft.getInstance().level;
        if (world != next) {
            for (var particle : PARTICLES) particle.remove();
            PARTICLES.clear(); EFFECTS.clear(); MARKERS.clear();
            world = next; clock = 0;
        }
    }

    public static void receive(ExplosionVfxMessage m) {
        synchronizeWorld();
        if (world == null || !world.dimension().location().equals(m.dimension()) || !Double.isFinite(m.x()) || !Double.isFinite(m.y()) || !Double.isFinite(m.z())) return;
        ExplosionAudioClient.receive(m);if(m.profile()==3){Hg01Fx.receive(m);return;}
        if(m.intensity()<=0)return;
        ++received;
        if (m.suppress()) {
            if (MARKERS.size() == 64) MARKERS.removeFirst();
            MARKERS.addLast(new Marker(m.x(), m.y(), m.z(), clock + 100));
        }
        if (m.profile() == 0 || !Float.isFinite(m.radius()) || m.radius() <= 0) return;
        Vec3 pos = new Vec3(m.x(), m.y(), m.z());
        double distance = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(pos);
        if (distance > 1024) return;
        if (EFFECTS.size() == MAX_EFFECTS) {
            Effect oldest = EFFECTS.remove(0);
            for (var particle : oldest.lobes) particle.fadeOut();
        }
        if (PARTICLES.size() > 208) {
            for (int i = 0; i < Math.min(48, PARTICLES.size()); ++i) PARTICLES.get(i).fadeOut();
        }
        float detail = distance < 48 ? 1 : distance < 112 ? 0.6F : 0.3F;
        ParticleStatus setting = Minecraft.getInstance().options.particles().get();
        if (setting == ParticleStatus.DECREASED) detail *= 0.7F;
        if (setting == ParticleStatus.MINIMAL) detail *= 0.4F;
        detail *= Math.max(0.25F, 1F - EFFECTS.size() * 0.08F)*RubbleClientConfig.DETAIL.get().floatValue();
        Effect effect = new Effect(pos, ExplosionVfxProfile.of(m.radius()*(float)Math.sqrt(m.intensity()),m.profile()),
                detail, new Random(m.seed()), Math.max(0, (int)Math.min(600, world.getGameTime() - m.start())));
        EFFECTS.add(effect);
        if(Boolean.getBoolean("sbw.vfx.trace")) System.out.println("VFX_CLIENT radius="+m.radius()+" profile="+m.profile()
                +" fireDiameter="+effect.profile.fireDiameter()+" plumeHeight="+effect.profile.plumeHeight()+" detail="+detail);
    }

    public static boolean consumeMarker(double x, double y, double z) {
        synchronizeWorld();
        var iterator = MARKERS.iterator();
        while (iterator.hasNext()) {
            Marker m = iterator.next();
            if (m.expiry < clock) { iterator.remove(); continue; }
            if (Math.abs(m.x-x)<0.0001 && Math.abs(m.y-y)<0.0001 && Math.abs(m.z-z)<0.0001) {
                iterator.remove(); ++suppressed; return true;
            }
        }
        return false;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        
        synchronizeWorld();
        ExplosionAudioClient.tick();Hg01Fx.tick();
        if (world == null || Minecraft.getInstance().isPaused()) return;
        ++clock;
        MARKERS.removeIf(m -> m.expiry < clock);
        PARTICLES.removeIf(p -> { if (clock >= p.deadline) p.remove(); return !p.isAlive(); });
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        coverage = 0;
        for (var p : PARTICLES) coverage += p.coverage(camera);
        if (coverage > 6) {
            double excess = coverage - 6;
            for (var p : PARTICLES) { if(p.structural) continue; p.fadeOut(); excess -= p.coverage(camera); if (excess <= 0) break; }
        }
        var iterator = EFFECTS.iterator();
        while (iterator.hasNext()) {
            Effect e = iterator.next();
            if (e.age == 0) emit(e, 3);
            if (e.age < 32 && e.age % 4 == 0) {
                int count = e.age==0 ? 8 : Math.max(1, Math.round(2 * e.detail));
                for (int i=0; i<count; ++i) emit(e, 0);
            }
            if (e.age >= 32 && e.age <= e.profile.emissionTicks() && e.age % 8 == 4) {
                emitSmoke(e, 1);
                if (e.age >= 44 && e.age % 16 == 12) emitSmoke(e, 2);
            }
            if (++e.age > (e.heavy ? 602 : 302)) iterator.remove();
        }
    }

    private static void emitSmoke(Effect e, int kind) {
        if(!RubbleClientConfig.SMOKE.get())return;
        float amount = e.detail * e.profile.smokeDensity();
        int count = Math.max(1,(int)amount);
        if (e.random.nextFloat() < amount - count) ++count;
        for (int i=0; i<count; ++i) emit(e,kind);
    }

    private static void emit(Effect e, int kind) {
        // Far/overlapping effects emit fewer quads. Old smoke fades before hard capacity is reached.
        int sprite = kind == 3 ? 0 : kind;
        if (SPRITES[sprite] == null || PARTICLES.size() >= MAX_PARTICLES) { ++rejected; return; }
        if (kind != 0 && kind != 3 && PARTICLES.size() >= 224) { ++rejected; return; }
        double a = e.random.nextDouble() * Math.PI * 2;
        boolean fireAnchor=kind==0 && e.fireOrdinal<8;
        if(kind==0) { if(fireAnchor) a=e.fireOrdinal*Math.PI/4; ++e.fireOrdinal; }
        float diameter=e.profile.fireDiameter();
        boolean structural = kind==3 || fireAnchor || kind==1 && (e.age==36 || e.age==68 || e.age==100);
        // Positions and extent never depend on detail/particle settings.
        double spread = diameter * (kind==0 ? 0.16 : kind==1 ? 0.10 : 0.23);
        double r = fireAnchor ? spread : Math.sqrt(e.random.nextDouble()) * spread;
        double y = diameter * (kind==0 ? 0.08+(e.fireOrdinal%3)*0.05 : kind==3 ? 0.12 : 0.05);
        float size = diameter * (kind==3 ? 0.22F : kind==0 ? 0.27F : kind==1 ? 0.50F : 0.40F)
                * (0.90F + e.random.nextFloat()*0.20F);
        int life = kind==3 ? 3 : kind==0 ? 44+e.random.nextInt(13) : e.profile.smokeLife()+e.random.nextInt(61);
        float rise = kind==0 ? diameter*0.40F : kind==3 ? 0 : (e.profile.plumeHeight()-size*0.65F)*(kind==2 ? 0.45F : 1);
        ExplosionLobeParticle p = new ExplosionLobeParticle(world, e.pos.x+Math.cos(a)*r,
                e.pos.y+y, e.pos.z+Math.sin(a)*r, Math.cos(a), rise, Math.sin(a),
                size, life, kind, (float)a, (e.random.nextFloat()-0.5F)*0.035F, SPRITES[sprite]);
        p.structural=structural;
        p.deadline = clock + life + 2;
        double area = p.coverage(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        if (!structural && coverage + area > (kind == 0 || kind == 3 ? 8 : 6)) { p.remove(); ++rejected; return; }
        coverage += area;
        if(!RubbleVisualBudget.claim(p)){++rejected;return;} PARTICLES.add(p); e.lobes.add(p); Minecraft.getInstance().particleEngine.add(p); ++spawned;
    }

    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_PARTICLES) ExplosionVfxRender.captureFog();
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL || PARTICLES.isEmpty()) return;
        ExplosionVfxRender.draw(PARTICLES,event.getCamera(),event.getPartialTick().getGameTimeDeltaPartialTick(false),event.getModelViewMatrix());
    }

    /** Read-only counters for diagnostics and external QA; never a gameplay control. */
    public static String diagnostics() {
        return "effects="+EFFECTS.size()+",particles="+PARTICLES.size()+",received="+received
                +",spawned="+spawned+",suppressed="+suppressed+",rejected="+rejected;
    }

    private record Marker(double x, double y, double z, long expiry) {}
    private static final class Effect {
        final Vec3 pos; final float scale, detail; final boolean heavy; final ExplosionVfxProfile profile; final Random random; final ArrayList<ExplosionLobeParticle> lobes = new ArrayList<>(); int age, fireOrdinal;
        Effect(Vec3 p, ExplosionVfxProfile profile, float d, Random r, int a) { pos=p;this.profile=profile;scale=profile.scale();heavy=profile.heavy();detail=d;random=r;age=a; }
    }
}
