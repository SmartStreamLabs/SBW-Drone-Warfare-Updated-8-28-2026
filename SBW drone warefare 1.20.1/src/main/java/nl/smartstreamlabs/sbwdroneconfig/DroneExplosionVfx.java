package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Server-only attribution and compact visual notifications. Never changes explosion mechanics. */
public final class DroneExplosionVfx {
    public static final String PAYLOAD_TAG = "sbwdroneconfigVfxPayload";
    private static long sequence;
    private DroneExplosionVfx() {}

    public static boolean isAddonEntity(Entity entity) {
        return entity != null && SbwDroneRangeConfig.MOD_ID.equals(
                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());
    }

    public static boolean isSbwSource(Entity entity){return entity!=null&&SbwDroneRangeConfig.SBW_MOD_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());}

    public static boolean isAttributed(Entity entity) {
        return isAddonEntity(entity) || entity != null && entity.getPersistentData().getBoolean(PAYLOAD_TAG);
    }

    public static void markPayload(Entity payload) {
        payload.getPersistentData().putBoolean(PAYLOAD_TAG, true);
    }

    /** A marker consumes exactly one following vanilla explosion packet at this position. */
    public static void prepare(ServerLevel level, Vec3 pos) {
        send(level, pos, 0, 0, true);
    }

    public static void crash(ServerLevel level, Entity drone, float radius) {
        // An empty airframe retains existing crash damage but gets only a modest visual burst.
        boolean warhead = (drone instanceof LucasDroneEntity && !(drone instanceof BayraktarTb2Entity))
                || drone instanceof GlideBombEntity
                || drone instanceof Geranium2Entity;
        float visualRadius = warhead ? radius : Math.min(radius, 1.8F);
        send(level, drone.position(), visualRadius, warhead ? 2 : 1, false);
    }

    public static void detonate(ServerLevel level, Vec3 pos, float radius) {
        if (Float.isFinite(radius) && radius > 0) send(level, pos, radius, radius >= 6 ? 2 : 1, false);
    }

    public static void mortar(ServerLevel level, Entity payload, float radius) {
        detonate(level, payload.position(), radius);
    }


    private static final ThreadLocal<Pending> PENDING = new ThreadLocal<>();
    private static final Scope NO_SCOPE = new Scope(null, false);

    /** Attribute only the next constructed explosion inside this call, including nested calls. */
    public static Scope explosionScope(net.minecraft.world.level.Level level, Entity source,
                                        double x, double y, double z, float radius, boolean enabled) {
        if (!enabled || !(level instanceof ServerLevel)) return NO_SCOPE;
        Pending previous = PENDING.get();
        PENDING.set(new Pending(level,source,x,y,z,Math.max(0,radius)));
        return new Scope(previous,true);
    }

    public static boolean claimExplosion(net.minecraft.world.level.Level level, Entity source,
                                          double x, double y, double z, float radius) {
        Pending p=PENDING.get();
        if(p==null || p.claimed || p.level!=level || p.source!=source
                || p.x!=x || p.y!=y || p.z!=z || p.radius!=radius) return false;
        p.claimed=true;
        return true;
    }

    private static final class Pending {
        final net.minecraft.world.level.Level level; final Entity source;
        final double x,y,z; final float radius; boolean claimed;
        Pending(net.minecraft.world.level.Level l,Entity e,double x,double y,double z,float r) {
            level=l;source=e;this.x=x;this.y=y;this.z=z;radius=r;
        }
    }

    public static final class Scope implements AutoCloseable {
        private final Pending previous; private final boolean active; private boolean closed;
        private Scope(Pending previous,boolean active) {this.previous=previous;this.active=active;}
        @Override public void close() {
            if(!active || closed) return;
            closed=true;
            if(previous==null) PENDING.remove(); else PENDING.set(previous);
        }
    }

    private static long visualSeed(ServerLevel level, Vec3 pos) {
        // Visual randomness must never consume world/gameplay RNG.
        long seed = ++sequence ^ level.getGameTime() * 0x9E3779B97F4A7C15L
                ^ Double.doubleToLongBits(pos.x) ^ Long.rotateLeft(Double.doubleToLongBits(pos.z), 23);
        seed = (seed ^ (seed >>> 30)) * 0xBF58476D1CE4E5B9L;
        return seed ^ (seed >>> 27);
    }

    public static boolean handGrenade(Entity e){if(e==null)return false;String id=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();return id.contains("grenade")&&!id.contains("launcher");}
    public static void grenade(ServerLevel level,Vec3 pos,float radius){if(Float.isFinite(radius)&&radius>0)send(level,pos,radius,4,false);}
    public static void hg01(ServerLevel level,Vec3 position){send(level,position,Hg01Config.RADIUS.get().floatValue(),3,false);}
    private static void send(ServerLevel level, Vec3 pos, float radius, int profile, boolean suppress) {
        if (!Double.isFinite(pos.x) || !Double.isFinite(pos.y) || !Double.isFinite(pos.z)) return;
        if(Boolean.getBoolean("sbw.vfx.trace") && profile!=0)
            System.out.println("VFX_SERVER radius="+radius+" profile="+profile+" dimensions="+ExplosionVfxProfile.of(radius,profile));
        ExplosionVfxMessage message = new ExplosionVfxMessage(pos.x, pos.y, pos.z,
                Math.min(64, Math.max(0, radius)), profile, level.getGameTime(), visualSeed(level,pos), suppress, level.dimension().location(),profile>0?RubbleEvents.visualScale(level,pos):1);
        for (var player : level.players()) {
            double distance = player.distanceToSqr(pos);
            double viewDistance = DroneClientChunkSyncManager.trackingPosition(player).distanceToSqr(pos);
            if (Math.min(distance,viewDistance) > 2048.0 * 2048.0) continue;
            boolean marker = suppress && distance < 4096.0;
            if (profile == 0 && !marker) continue;
            AddonNetwork.sendExplosionVfx(player, marker == suppress ? message
                    : new ExplosionVfxMessage(pos.x,pos.y,pos.z,message.radius(),profile,message.start(),message.seed(),false,message.dimension()));
        }
    }
}
