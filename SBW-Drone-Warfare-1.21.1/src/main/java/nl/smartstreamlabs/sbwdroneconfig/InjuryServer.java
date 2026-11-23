package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;

/** Server authority for medicine, lifetime wounds and bounded movement. No global entity scans. */
public final class InjuryServer {
    public static final String KEY = "sbwdroneconfigInjury";
    public static final ResourceKey<DamageType> BLEED = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","injury_bleeding"));
    private static final TagKey<DamageType> SOURCES = TagKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","injury_sources"));
    private static final TagKey<DamageType> IMPACTS = TagKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","injury_drone_impacts"));
    private static final TagKey<DamageType> EXPLOSIONS = TagKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","injury_explosions"));
    private static final TagKey<DamageType> EXCLUDED = TagKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","injury_excluded"));
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath("sbwdroneconfig","injury_mobility");
    private static final Map<UUID,InjuryState> STATES = new HashMap<>();
    private static final Set<UUID> ACTIVE = new HashSet<>();
    private static final Map<UUID,Treatment> TREATMENTS = new HashMap<>();
    private static final Map<UUID,UUID> PATIENTS = new HashMap<>();
    private static final Map<UUID,Movement> MOVES = new HashMap<>();
    private static final Map<UUID,Long> DAMAGED = new HashMap<>();
    private static final Map<UUID,Boolean> FLIGHT = new HashMap<>();
    private static final Map<UUID,Float> SPEEDS = new HashMap<>();
    private static boolean enabledPreviously = true;
    private static final Map<UUID,Impact> IMPACT_WINDOW = new HashMap<>();
    private static final class Impact {
        final long tick; final UUID source; final DamageSource fallback;
        float damage; boolean saved;
        Impact(long t,DamageSource s){tick=t;source=s.getDirectEntity()==null?null:s.getDirectEntity().getUUID();fallback=s;}
        boolean same(long t,DamageSource s){return tick==t&&(source!=null?s.getDirectEntity()!=null&&source.equals(s.getDirectEntity().getUUID()):fallback==s);}
    }

    private static final class Movement {
        long tick, damageTick=Long.MIN_VALUE; double budget, impulseBudget; Vec3 position;
        Movement(ServerPlayer p) { tick=p.serverLevel().getGameTime(); budget=.12; position=p.position(); }
    }
    private static final class Treatment {
        final ServerPlayer actor, patient; final ItemStack stack; final InteractionHand hand;
        final boolean medkit; final long start; final int duration; boolean committed, consumed;
        Treatment(ServerPlayer a,ServerPlayer p,InteractionHand h,boolean m,int duration) {
            actor=a;patient=p;hand=h;stack=a.getItemInHand(h);medkit=m;start=a.serverLevel().getGameTime();this.duration=duration;
        }
    }
    private static InjuryState state(ServerPlayer p) {
        return STATES.computeIfAbsent(p.getUUID(), k -> load(p.getPersistentData().getCompound(KEY)));
    }
    public static InjuryState inspect(ServerPlayer p) { return state(p); }
    public static boolean downed(ServerPlayer p) {
        var s=STATES.get(p.getUUID());
        return InjuryConfig.ENABLED.get() && s!=null && s.downed();
    }
    public static boolean cannotOperate(ServerPlayer p) { return InjuryHooks.downed(p); }
    public static boolean combat(DamageSource source) {
        if(source.is(BLEED)||source.is(DamageTypes.FELL_OUT_OF_WORLD)||source.is(DamageTypes.GENERIC_KILL)||source.is(EXCLUDED)) return false;
        String id=source.typeHolder().unwrapKey().map(k->k.location().toString()).orElse("");
        if(InjuryConfig.EXCLUDED.get().contains(id))return false;
        return InjuryConfig.EXTRA.get().contains(id)
            || InjuryConfig.PROJECTILES.get() && (source.is(DamageTypeTags.IS_PROJECTILE)||source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile||source.is(SOURCES))
            || InjuryConfig.EXPLOSIONS.get() && (source.is(DamageTypeTags.IS_EXPLOSION)||source.is(EXPLOSIONS))
            || InjuryConfig.IMPACTS.get() && (source.is(IMPACTS)||source.getDirectEntity()!=null&&(SbwCompat.isDrone(source.getDirectEntity())||source.getDirectEntity() instanceof VatagEntity));
    }
    /** The only injury damage hook: precisely at Player.actuallyHurt's post-protection health commit. */
    public static void commit(ServerPlayer p,float requestedHealth,DamageSource source) {
        float before=p.getHealth(), damage=before-requestedHealth;
        if(!InjuryConfig.ENABLED.get()||p.isCreative()||p.isSpectator()||!(damage>0)||!Float.isFinite(damage)) { p.setHealth(requestedHealth); return; }
        if(!source.is(BLEED)){interruptPatient(p);DAMAGED.put(p.getUUID(),p.serverLevel().getGameTime());}
        boolean eligible=combat(source), saved=false;
        InjuryState s=eligible?state(p):STATES.get(p.getUUID());
        Impact impact=null;
        if(eligible){long t=p.serverLevel().getGameTime();impact=IMPACT_WINDOW.get(p.getUUID());if(impact==null||!impact.same(t,source)){impact=new Impact(t,source);IMPACT_WINDOW.put(p.getUUID(),impact);}impact.damage+=damage;}
        boolean totem=p.getMainHandItem().is(Items.TOTEM_OF_UNDYING)||p.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
        if(eligible&&requestedHealth<=0&&before>0&&InjuryConfig.BEFORE_DEATH.get()&&!s.downed()&&!s.rescueUsed&&!totem&&!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            requestedHealth=2; saved=true;impact.saved=true;
        }
        // TaCZ can pay armor-penetrating and ordinary damage separately for one bullet.
        // Converting that one fatal impact covers its remainder in the same tick, never another projectile/tick.
        if(eligible&&impact.saved&&!saved&&requestedHealth<=0)requestedHealth=Math.min(before,2);
        p.setHealth(requestedHealth);
        if(eligible) {
            s.hit(Math.min(before,damage),impact.damage,InjuryConfig.LIGHT.get().floatValue(),InjuryConfig.SEVERE.get().floatValue(),InjuryConfig.REPEATED.get().floatValue(),InjuryConfig.WINDOW.get(),saved);
            if(s.active()||s.traumaWindow>0)ACTIVE.add(p.getUUID());
            persist(p,s);enforce(p,s);sync(p,true);
            AddonNetwork.injuryFeedback(p,new InjuryFeedback(0,0,"injury.hit",true));
        }
    }
    private static void interruptPatient(ServerPlayer p) {
        cancel(p,true);
        UUID medic=PATIENTS.get(p.getUUID());
        if(medic!=null){Treatment t=TREATMENTS.get(medic);if(t!=null)cancel(t.actor,true);}
    }
    private static boolean usable(ServerPlayer actor,ServerPlayer patient) {
        return actor.isAlive()&&patient.isAlive()&&!actor.hasDisconnected()&&!patient.hasDisconnected()
            &&actor.level()==patient.level()&&!actor.isSpectator()&&!patient.isSpectator()
            &&(actor==patient||actor.distanceToSqr(patient)<=9&&actor.hasLineOfSight(patient));
    }
    public static boolean begin(ServerPlayer actor,ServerPlayer patient,InteractionHand hand,MedicalItem item) {
        if(!InjuryConfig.ENABLED.get()||!usable(actor,patient))return false;
        InjuryState s=state(patient);
        if(s.stage==InjuryState.Stage.RECOVERING&&s.bleeding==0)return false;
        if(!s.active()||!item.trauma()&&s.bleeding==0) { notice(actor,"injury.no_treatment");return false; }
        if(actor==patient&&s.downed()&&!InjuryConfig.SELF_SEVERE.get()) { notice(actor,"injury.self_disabled");return false; }
        UUID current=PATIENTS.get(patient.getUUID());
        if(current!=null&&!current.equals(actor.getUUID())) { notice(actor,"injury.busy");return false; }
        if(TREATMENTS.containsKey(actor.getUUID()))return false;
        int duration=item.trauma()?(actor==patient?InjuryConfig.SELF_MEDKIT.get():InjuryConfig.MEDKIT.get()):InjuryConfig.BANDAGE.get();
        Treatment t=new Treatment(actor,patient,hand,item.trauma(),duration);
        if(t.stack.isEmpty()||t.stack.getItem()!=item)return false;
        TREATMENTS.put(actor.getUUID(),t);PATIENTS.put(patient.getUUID(),actor.getUUID());
        feedback(t,0);return true;
    }
    public static void cancel(ServerPlayer actor,boolean notify) {
        Treatment t=TREATMENTS.remove(actor.getUUID());if(t==null)return;
        PATIENTS.remove(t.patient.getUUID(),actor.getUUID());
        if(t.committed)consume(t);
        actor.stopUsingItem();
        if(notify){var m=new InjuryFeedback(0,0,"injury.interrupted",false);AddonNetwork.injuryFeedback(actor,m);if(t.patient!=actor&&!t.patient.hasDisconnected())AddonNetwork.injuryFeedback(t.patient,m);}
    }
    private static void feedback(Treatment t,int elapsed) {
        String label=t.medkit?(state(t.patient).bleeding>0&&elapsed<t.duration/2?"injury.stopping":"injury.treating"):"injury.applying";
        var m=new InjuryFeedback(elapsed,t.duration,label,false);
        AddonNetwork.injuryFeedback(t.actor,m);if(t.patient!=t.actor)AddonNetwork.injuryFeedback(t.patient,m);
    }
    private static void consume(Treatment t) {
        if(t.consumed)return;
        if(!t.actor.getAbilities().instabuild&&!t.stack.isEmpty())t.stack.shrink(1);
        t.consumed=true;
    }
    public static void inventoryAction(ServerPlayer p){cancel(p,true);}
    private static void finish(Treatment t) {
        TREATMENTS.remove(t.actor.getUUID());PATIENTS.remove(t.patient.getUUID(),t.actor.getUUID());
        InjuryState s=state(t.patient);
        boolean success=t.medkit?s.medkit(InjuryConfig.RECOVERY.get()):s.bandage(InjuryConfig.RECOVERY.get());
        if(t.committed||success)consume(t);
        if(success) {
            consume(t);
            if(t.medkit)t.patient.heal(InjuryConfig.HEAL.get().floatValue());
            ACTIVE.add(t.patient.getUUID());persist(t.patient,s);enforce(t.patient,s);sync(t.patient,false);
        }
        var message=new InjuryFeedback(0,0,success?(s.downed()?"injury.stabilized":"injury.completed"):"injury.no_treatment",false);
        AddonNetwork.injuryFeedback(t.actor,message);if(t.patient!=t.actor)AddonNetwork.injuryFeedback(t.patient,message);
        t.actor.stopUsingItem();
    }
    private static void notice(ServerPlayer p,String key) { p.displayClientMessage(Component.translatable(key),true);AddonNetwork.injuryFeedback(p,new InjuryFeedback(0,0,key,false)); }
    private static void stopControls(ServerPlayer p) {
        VatagSessions.stop(p);Rd01Sessions.stop(p);Id01Sessions.stop(p);
        PlayerDroneAnchorManager.release(p,true);
        for(var h:InteractionHand.values()) {
            ItemStack stack=p.getItemInHand(h);
            if(SbwCompat.isUsingLinkedMonitor(stack)) {
                Entity d=SbwCompat.findLinkedDrone(p,stack);
                if(d!=null)SbwCompat.resetDroneInput(d);
                SbwCompat.stopUsingMonitor(stack);
                DroneClientChunkSyncManager.release(p,true);
            }
        }
    }
    private static void enforce(ServerPlayer p,InjuryState s) {
        boolean restricted=s.downed()&&!p.isCreative()&&!p.isSpectator();
        float modifier=restricted?(float)Math.max(-.98,Math.min(-.6,InjuryConfig.CRAWL.get()/.3-1)):s.stage==InjuryState.Stage.RECOVERING?-.25f:0;
        if(!Objects.equals(SPEEDS.get(p.getUUID()),modifier)) {
            var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);
            if(speed!=null){speed.removeModifier(SPEED);if(modifier!=0)speed.addTransientModifier(new AttributeModifier(SPEED,modifier,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
            SPEEDS.put(p.getUUID(),modifier);
        }
        if(restricted) {
            stopControls(p);p.setSprinting(false);p.stopFallFlying();
            if(!FLIGHT.containsKey(p.getUUID()))FLIGHT.put(p.getUUID(),p.getAbilities().mayfly);
            if(p.getAbilities().flying||p.getAbilities().mayfly){p.getAbilities().flying=false;p.getAbilities().mayfly=false;p.onUpdateAbilities();}
            if(p.isSleeping())p.stopSleepInBed(true,true);
            if(p.isPassenger()&&!(p.getVehicle() instanceof Rd01Entity)&&!(p.getVehicle() instanceof MedicalTrailerEntity))p.stopRiding();
            if(!p.isPassenger())p.setPose(Pose.SWIMMING);
        } else {
            MOVES.remove(p.getUUID());
            Boolean mayfly=FLIGHT.remove(p.getUUID());
            if(mayfly!=null&&!p.isCreative()&&!p.isSpectator()){p.getAbilities().mayfly=mayfly;p.onUpdateAbilities();}
        }
    }
    /** Rejects forged movement packets; ordinary crawl still uses vanilla collision/prediction. */
    public static boolean rejectMove(ServerPlayer p,double x,double y,double z) {
        if(!InjuryHooks.prone(p))return false;
        var m=MOVES.computeIfAbsent(p.getUUID(),k->new Movement(p));long now=p.serverLevel().getGameTime();
        double speed=InjuryConfig.CRAWL.get();
        if(m.position.distanceToSqr(p.position())>16){m.position=p.position();m.budget=.12;m.tick=now;}
        m.budget=Math.min(speed*6+.12,m.budget+Math.max(0,now-m.tick)*speed);m.tick=now;
        double dx=x-p.getX(),dz=z-p.getZ(),dy=y-p.getY(),distance=Math.hypot(dx,dz);
        // The ordinary server knockback remains meaningful; it does not grant a new injury grace period.
        long hit=DAMAGED.getOrDefault(p.getUUID(),Long.MIN_VALUE);
        if(hit!=m.damageTick){m.damageTick=hit;m.impulseBudget=hit!=Long.MIN_VALUE&&now>=hit&&now-hit<=10?Math.min(2.5,p.getDeltaMovement().horizontalDistance()*8):0;}
        boolean risingWithoutSupport=dy>.15&&p.level().noCollision(p,p.getBoundingBox().move(dx,dy-.16,dz))&&!p.horizontalCollision;
        if(distance>m.budget+m.impulseBudget+.015||risingWithoutSupport) {
            p.connection.teleport(p.getX(),p.getY(),p.getZ(),p.getYRot(),p.getXRot());return true;
        }
        double normal=Math.min(distance,Math.max(0,m.budget));m.budget-=normal;double extra=distance-normal;double impulseUsed=Math.min(extra,m.impulseBudget);m.impulseBudget-=impulseUsed;m.budget-=extra-impulseUsed;m.position=new Vec3(x,y,z);return false;
    }
    public static void tick(net.minecraft.server.MinecraftServer server) {
        if(!InjuryConfig.ENABLED.get()){if(enabledPreviously)for(var id:List.copyOf(STATES.keySet())){var p=server.getPlayerList().getPlayer(id);if(p!=null)clear(p);}enabledPreviously=false;return;}
        enabledPreviously=true;
        for(var id:List.copyOf(ACTIVE)) {
            var p=server.getPlayerList().getPlayer(id);if(p==null){ACTIVE.remove(id);continue;}
            if(!InjuryConfig.ENABLED.get()){clear(p);continue;}
            InjuryState s=state(p);var old=s.stage;s.tick();enforce(p,s);
            if(s.bleeding>0&&p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&++s.bleedClock>=InjuryConfig.BLEED_INTERVAL.get()) {
                s.bleedClock=0;
                float amount=(s.bleeding==2?InjuryConfig.BLEED_HEAVY:InjuryConfig.BLEED_LIGHT).get().floatValue();
                if(amount>0)p.hurt(new DamageSource(p.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BLEED)),amount);
            }
            if(!p.isAlive())continue;
            if(old!=s.stage){persist(p,s);sync(p,false);}
            if(p.tickCount%20==0)persist(p,s);
            if(!s.active()&&s.traumaWindow<=0){ACTIVE.remove(id);MOVES.remove(id);}
        }
        for(Treatment t:List.copyOf(TREATMENTS.values())) {
            int elapsed=(int)(t.actor.serverLevel().getGameTime()-t.start);
            if(!InjuryConfig.ENABLED.get()||!usable(t.actor,t.patient)||!t.actor.isUsingItem()||t.actor.getUsedItemHand()!=t.hand||t.actor.getItemInHand(t.hand)!=t.stack||t.stack.isEmpty()||t.actor.getUseItem()!=t.stack) { cancel(t.actor,true);continue; }
            if(t.medkit&&!t.committed&&elapsed>=Math.max(1,t.duration/3)&&state(t.patient).bleeding>0){
                var medical=state(t.patient);if(medical.downed())medical.bandage(InjuryConfig.RECOVERY.get());else {medical.bleeding=0;medical.bleedClock=0;}t.committed=true;
                persist(t.patient,medical);enforce(t.patient,medical);sync(t.patient,false);
            }
            if(elapsed>=t.duration)finish(t);else if(elapsed%10==0)feedback(t,elapsed);
        }
    }
    public static InjurySync snapshot(ServerPlayer p) {
        var s=state(p);return new InjurySync(p.getId(),p.getUUID(),s.stage.ordinal(),s.bleeding,s.recovery);
    }
    public static void sync(ServerPlayer p,boolean hit) { AddonNetwork.injuryState(p,snapshot(p)); }
    public static void persist(ServerPlayer p,InjuryState s) { p.getPersistentData().put(KEY,save(s)); }
    public static CompoundTag save(InjuryState s) {
        var n=new CompoundTag();n.putInt("Version",1);n.putInt("Stage",s.stage.ordinal());n.putInt("Bleeding",s.bleeding);n.putInt("Recovery",s.recovery);n.putInt("BleedClock",s.bleedClock);n.putFloat("Trauma",s.trauma);n.putInt("TraumaWindow",s.traumaWindow);n.putBoolean("RescueUsed",s.rescueUsed);return n;
    }
    public static InjuryState load(CompoundTag n) {
        var s=new InjuryState();int stage=n.getInt("Stage");s.stage=InjuryState.Stage.values()[Math.max(0,Math.min(4,stage))];s.bleeding=n.getInt("Bleeding");s.recovery=n.getInt("Recovery");s.bleedClock=n.getInt("BleedClock");s.trauma=n.getFloat("Trauma");s.traumaWindow=n.getInt("TraumaWindow");s.rescueUsed=n.getBoolean("RescueUsed");s.normalize();return s;
    }
    public static void login(ServerPlayer p) {
        STATES.remove(p.getUUID());SPEEDS.remove(p.getUUID());
        if(!InjuryConfig.ENABLED.get()){clear(p);return;}
        var s=state(p);if(s.active()||s.traumaWindow>0)ACTIVE.add(p.getUUID());enforce(p,s);sync(p,false);
    }
    public static void logout(ServerPlayer p) {
        interruptPatient(p);var s=STATES.remove(p.getUUID());if(s!=null)persist(p,s);
        ACTIVE.remove(p.getUUID());MOVES.remove(p.getUUID());SPEEDS.remove(p.getUUID());IMPACT_WINDOW.remove(p.getUUID());DAMAGED.remove(p.getUUID());
        // Save the original flight permission, not a medically suppressed value.
        Boolean mayfly=FLIGHT.remove(p.getUUID());if(mayfly!=null)p.getAbilities().mayfly=mayfly;
    }
    public static void clear(ServerPlayer p) {
        interruptPatient(p);var s=new InjuryState();STATES.put(p.getUUID(),s);ACTIVE.remove(p.getUUID());
        enforce(p,s);persist(p,s);sync(p,false);IMPACT_WINDOW.remove(p.getUUID());
    }
    public static void shutdown() { STATES.clear();ACTIVE.clear();TREATMENTS.clear();PATIENTS.clear();MOVES.clear();SPEEDS.clear();FLIGHT.clear();IMPACT_WINDOW.clear();DAMAGED.clear(); }
    private InjuryServer() {}
}
