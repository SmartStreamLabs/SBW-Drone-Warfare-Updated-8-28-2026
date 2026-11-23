package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.UUID;

/** Server owns the sole inventory. Clients only receive snapshots at phase/content changes. */
public class GlidePlatformBlockEntity extends BlockEntity implements GeoBlockEntity, MenuProvider, MissionPlatform {
    public enum Phase {OPEN,CLOSING,READY,RETURNING,LAUNCHING,RECOVERING}
    public static final int CLAMP_TICKS=GlidePlatformClampAnimation.TICKS;
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    private final MissionDraft mission=new MissionDraft();
    @Override public MissionDraft mission(){return mission;}
    @Override public String missionKind(){return "glide";}
    @Override public boolean missionEditable(){return phase==Phase.READY || phase==Phase.OPEN;}
    @Override public void missionSync(){sync();}
    private ItemStack drone=ItemStack.EMPTY;
    private Phase phase=Phase.OPEN;
    private long phaseStart;
    private float startOpen=1;
    private UUID owner,actor,launchId,loadIdentity;
    private boolean manualLaunch;
    private boolean automatic(){return mission.enabled&&!manualLaunch;}
    public UUID loadedId(){return drone.isEmpty()?null:loadIdentity;}
    public void requestManualLaunch(Player p){if(!(p instanceof ServerPlayer sp)||!PlatformMonitor.ready(sp,loadedId()))return;requestLaunch(p,true);}
    private int lastFailure;
    private boolean networkRead;
    private boolean recoveryClockPending;
    private long nextExitCheck;
    private boolean cachedExitClear;
    private int handoffEntity=-1;
    private UUID handoffUuid;
    private long handoffStart;
    private ItemStack lastVisual=ItemStack.EMPTY;
    private final java.util.Set<UUID> viewers=new java.util.HashSet<>();
    private float lidFrom=1;private boolean lidClosed=true;private long lidStart;
    public float lidClosedAmount(float partial){if(level==null)return 1;float t=Math.max(0,Math.min(1,(level.getGameTime()-lidStart+partial)/GlidePlatformClampAnimation.LID_TICKS));return lidFrom+((lidClosed?1:0)-lidFrom)*t;}
    private void updateLid(){if(level==null || level.isClientSide)return;boolean closed=viewers.isEmpty() || phase==Phase.LAUNCHING || phase==Phase.RETURNING;if(closed!=lidClosed){lidFrom=lidClosedAmount(0);lidClosed=closed;lidStart=level.getGameTime();level.playSound(null,worldPosition,closed?net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE:net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_OPEN,net.minecraft.sounds.SoundSource.BLOCKS,.22F,1.0F);sync();}}
    public void viewerOpened(Player player){viewers.add(player.getUUID());updateLid();}
    public void viewerClosed(Player player){viewers.remove(player.getUUID());updateLid();}

    public GlidePlatformBlockEntity(BlockPos pos,BlockState state){super(AddonBlockEntities.GLIDE_PLATFORM.get(),pos,state);}
    public ItemStack drone(){return drone;}
    public ItemStack previewDrone(){
        if(handoffEntity>=0 && level!=null) {
            var entity=level.getEntity(handoffEntity);
            if(entity!=null && entity.getUUID().equals(handoffUuid))return ItemStack.EMPTY;
        }
        if(!drone.isEmpty())return drone;
        return level!=null && handoffEntity>=0 && level.getGameTime()-handoffStart<=GlidePlatformLayout.DEPARTURE_TICKS?lastVisual:ItemStack.EMPTY;
    }
    public float previewTravel(float partial){return handoffEntity<0 || level==null?0:Math.max(0,Math.min(GlidePlatformLayout.DEPARTURE_TICKS,level.getGameTime()-handoffStart+partial));}
    private void clearHandoff(){handoffEntity=-1;handoffUuid=null;lastVisual=ItemStack.EMPTY;}
    public boolean departing(net.minecraft.world.entity.Entity entity){return entity instanceof GlideBombEntity bomb && bomb.isLp03Departing() && bomb.isLp03Mount(worldPosition) && entity.distanceToSqr(GlidePlatformLayout.mount(worldPosition,facing()))<64;}
    private boolean previousDroneStillOnRails(){
        if(level==null || handoffEntity<0)return false;
        var entity=level.getEntity(handoffEntity);var center=GlidePlatformLayout.center(worldPosition,facing());
        return entity!=null && entity.isAlive() && entity.getUUID().equals(handoffUuid) && entity.getBoundingBox().intersects(new AABB(center.x-1,center.y,center.z-1.5,center.x+1,center.y+2,center.z+1.5));
    }
    public Phase phase(){return phase;}
    public Direction facing(){return getBlockState().getValue(GlidePlatformBlock.FACING);}
    public float openness(float partial){
        float target=phase==Phase.OPEN || phase==Phase.RETURNING || phase==Phase.LAUNCHING?1:0;
        if(level==null)return 1;
        float t=Math.max(0,Math.min(1,(level.getGameTime()-phaseStart+partial)/CLAMP_TICKS));
        return startOpen+(target-startOpen)*t;
    }
    public boolean canUse(Player player){return !isRemoved() && level!=null && player.level()==level && player.distanceToSqr(Vec3.atCenterOf(worldPosition))<=64 && (owner==null || owner.equals(player.getUUID()) || player.hasPermissions(2));}
    private void fail(Player player,int reason){lastFailure=reason;player.displayClientMessage(Component.translatable(reasonKey(reason)),true);sync();}
    public static String reasonKey(int reason){return switch(reason){case 1->"lp03.no_drone";case 2->"lp03.busy";case 3->"lp03.blocked";case 4->"lp03.blocked";case 5->"lp03.no_monitor";case 6->"lp03.owner";case 7->"lp03.spawn_failed";case 8->"lp03.inventory_full";case 9->"mission.invalid";case 10->"platform.inventory.no_energy";default->"lp03.ready";};}
    public int reason(Player player){
        if(!canUse(player))return 6;
        if(drone.isEmpty())return 1;if(PlatformInventory.editing(this))return 2;
        if(phase!=Phase.READY)return 2;if(!PlatformInventory.batteryReady(this))return 10;
        if(!automatic() && (!automatic() && monitor(player).isEmpty()))return 5;
        if(automatic() && !mission.valid)return 9;
        if(!exitClear())return 4;
        return 0;
    }
    public int displayReason(Player player){
        if(!canUse(player))return 6;
        if(drone.isEmpty())return 1;if(PlatformInventory.editing(this))return 2;
        if(phase!=Phase.READY)return 2;if(!PlatformInventory.batteryReady(this))return 10;
        if(!automatic() && (!automatic() && monitor(player).isEmpty()))return 5;
        if(automatic() && !mission.valid)return 9;
        if(level.getGameTime()>=nextExitCheck){cachedExitClear=exitClear();nextExitCheck=level.getGameTime()+20;}
        return cachedExitClear?0:4;
    }
    private ItemStack monitor(Player player){if(SbwCompat.isMonitor(player.getMainHandItem()))return player.getMainHandItem();if(SbwCompat.isMonitor(player.getOffhandItem()))return player.getOffhandItem();return player.getInventory().items.stream().filter(SbwCompat::isMonitor).findFirst().orElse(ItemStack.EMPTY);}
    private CompoundTag entityData(){return drone.getOrDefault(net.minecraft.core.component.DataComponents.ENTITY_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();}
    public void loadDrone(Player player,ItemStack held){if(PlatformInventory.guard(this,player))return;
        if(!canUse(player)){fail(player,6);return;}
        if(!drone.isEmpty() || phase!=Phase.OPEN){fail(player,2);return;}
        if(!held.is(AddonItems.GLIDE_BOMB.get()))return;
        if(previousDroneStillOnRails()){fail(player,2);return;}
        Vec3 mount=GlidePlatformLayout.mount(worldPosition,facing());
        if(!volumeClear(GlidePlatformLayout.droneBounds(worldPosition,facing()),false) || !openingClear()){fail(player,4);return;}
        clearHandoff();loadIdentity=UUID.randomUUID();drone=held.copy();drone.setCount(1);owner=player.getUUID();if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;lastFailure=0;
        if(!player.getAbilities().instabuild)held.shrink(1);
        change(Phase.CLOSING);
    }
    public void requestReturn(Player player){if(PlatformInventory.guard(this,player))return;
        if(!canUse(player)){fail(player,6);return;}
        if(drone.isEmpty()){fail(player,1);return;}
        if(phase!=Phase.READY){fail(player,2);return;}
        if(!openingClear()){fail(player,4);return;}
        actor=player.getUUID();change(Phase.RETURNING);
    }
    public void requestLaunch(Player player){requestLaunch(player,false);}
    private void requestLaunch(Player player,boolean manual){if(PlatformInventory.guard(this,player))return;
        if(phase!=Phase.READY){fail(player,2);return;}manualLaunch=manual;
        if(!manualLaunch&&!missionValid(player))return;
        int error=reason(player);if(error!=0){fail(player,error);return;}
        UUID candidate=loadIdentity;
        if(automatic() && !MissionManager.get(((ServerLevel)level).getServer()).reserve(this,player,candidate))return;
        actor=player.getUUID();launchId=candidate;lastFailure=0;change(Phase.LAUNCHING);
    }
    public void cancel(Player player){if(PlatformInventory.guard(this,player))return;
        if(canUse(player) && (phase==Phase.LAUNCHING || phase==Phase.RETURNING)) {if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;change(Phase.CLOSING);}
    }
    GlideBombEntity createDrone(){
        if(!(level instanceof ServerLevel server))return null;
        var result=AddonEntities.GLIDE_BOMB.get().create(server);if(result==null)return null;
        CompoundTag data=entityData();
        for(String key:new String[]{"UUID","Pos","Motion","Rotation","Passengers","Leash","Dimension"})data.remove(key);
        if(!data.isEmpty()){
            var defaults=new CompoundTag();result.saveWithoutId(defaults);defaults.merge(data);
            // SBW 0.8.9 Final writes an empty Item compound, but its 1.21 reader parses it as a required stack.
            // Omit only that empty default; populated payload data passes through unchanged.
            if(defaults.contains("Item",10) && defaults.getCompound("Item").isEmpty())defaults.remove("Item");
            result.load(defaults);
        }
        if(drone.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME))result.setCustomName(drone.getHoverName());
        return result;
    }
    private boolean openingClear(){return level!=null && volumeClear(GlidePlatformLayout.openingBounds(worldPosition,facing()),false);}
    public boolean exitClear(){
        if(level==null)return false;
        Vec3 p=GlidePlatformLayout.mount(worldPosition,facing());Vec3 end=p.add(GlidePlatformLayout.forward(facing()).scale(5)).add(0,Math.tan(Math.toRadians(-GlidePlatformLayout.RAIL_PITCH))*5,0);
        // Bounded preflight check, only at an interaction and immediately before spawn; never a per-tick spatial scan.
        AABB corridor=GlidePlatformLayout.droneBounds(worldPosition,facing()).expandTowards(end.subtract(p));
        return openingClear() && volumeClear(corridor,true);
    }
    private boolean volumeClear(AABB corridor,boolean checkEntities){
        if(!level.hasChunksAt(BlockPos.containing(corridor.minX,corridor.minY,corridor.minZ),BlockPos.containing(corridor.maxX,corridor.maxY,corridor.maxZ)))return false;
        for(BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(corridor.minX,corridor.minY,corridor.minZ),BlockPos.containing(corridor.maxX,corridor.maxY,corridor.maxZ))) {
            var state=level.getBlockState(pos);
            if(state.is(AddonBlocks.GLIDE_PLATFORM.get()) && GlidePlatformLayout.anchor(pos,state.getValue(GlidePlatformBlock.FACING),state.getValue(GlidePlatformBlock.PART)).equals(worldPosition))continue;
            if(LaunchClearance.intersects(state.getCollisionShape(level,pos),pos,corridor))return false;
        }
        return !checkEntities || level.getEntities((net.minecraft.world.entity.Entity)null,corridor,e->e.isAlive() && !e.isSpectator() && e.isPickable()).isEmpty();
    }
    public static void serverTick(Level level,BlockPos pos,BlockState state,GlidePlatformBlockEntity platform){platform.tickServer();}
    private void tickServer(){
        if(level.getGameTime()%20==0)missionRefresh();
        if(phase==Phase.RECOVERING && recoveryClockPending){recoveryClockPending=false;phaseStart=level.getGameTime();return;}
        if(phase==Phase.OPEN || phase==Phase.READY || level.getGameTime()-phaseStart<CLAMP_TICKS)return;
        if(phase==Phase.RECOVERING) {
            // A saved release never restarts itself. If its deterministic launch UUID already exists, consume the stale copy.
            if(launchId!=null && (((ServerLevel)level).getEntity(launchId)!=null || MissionManager.wasSpawned((ServerLevel)level,launchId))){drone=ItemStack.EMPTY;owner=null;}
            if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;change(drone.isEmpty()?Phase.OPEN:Phase.READY);return;
        }
        if(phase==Phase.CLOSING){change(drone.isEmpty()?Phase.OPEN:Phase.READY);return;}
        var player=actor==null?null:level.getPlayerByUUID(actor);
        if(player==null || !canUse(player)){if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;change(Phase.CLOSING);return;}
        if(phase==Phase.RETURNING) {
            var transfer=drone.copy();
            if(PlatformInventory.canReceive(player,transfer)&&player.getInventory().add(transfer)) {drone=ItemStack.EMPTY;owner=null;actor=null;change(Phase.OPEN);}
            else {fail(player,8);actor=null;change(Phase.CLOSING);}
            return;
        }
        if(!exitClear() || (!automatic() && monitor(player).isEmpty())){fail(player,!exitClear()?4:5);if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;change(Phase.CLOSING);return;}
        if(automatic() && !MissionManager.get(((ServerLevel)level).getServer()).prepared(launchId)){
            var current=MissionManager.get(((ServerLevel)level).getServer()).find(launchId);
            if(current==null || !current.active()){actor=null;launchId=null;change(Phase.CLOSING);}
            return;
        }
        if(manualLaunch&&(!(player instanceof ServerPlayer sp)||!PlatformMonitor.ready(sp,loadIdentity))){actor=null;launchId=null;change(Phase.CLOSING);return;}
        var flying=createDrone();if(flying==null){fail(player,7);if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;change(Phase.CLOSING);return;}
        Vec3 at=GlidePlatformLayout.mount(worldPosition,facing());
        flying.setLp03Mount(worldPosition,facing().toYRot());flying.setUUID(launchId);flying.moveTo(at.x,at.y,at.z,facing().toYRot(),(float)GlidePlatformLayout.RAIL_PITCH);
        flying.yRotO=flying.getYRot();flying.xRotO=flying.getXRot();
        flying.setDeltaMovement(GlidePlatformLayout.forward(facing()).scale(GlidePlatformLayout.LAUNCH_SPEED).add(0,GlidePlatformLayout.LAUNCH_RISE,0));
        SbwCompat.assignDroneOwner(flying,player);
        if(automatic() && !MissionManager.get(((ServerLevel)level).getServer()).bind(launchId,flying)){fail(player,7);actor=null;launchId=null;change(Phase.CLOSING);return;}
        // Publish handoff identity before the entity's tracking packet. Observers suppress the static copy as soon as it exists.
        handoffEntity=flying.getId();handoffUuid=flying.getUUID();handoffStart=level.getGameTime();lastVisual=drone.copy();sync();
        if(!((ServerLevel)level).addFreshEntity(flying)){MissionManager.spawnRejected(flying);clearHandoff();fail(player,7);if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;change(Phase.CLOSING);return;}
        // Same server-thread commit: success consumes the stored copy exactly once. No spawn is retried on reload.
        if(manualLaunch)PlatformMonitor.activate((ServerPlayer)player,flying);
        else if(!automatic())SbwCompat.linkMonitorToDrone(monitor(player),flying,false);
        drone=ItemStack.EMPTY;owner=null;if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;lastFailure=0;change(Phase.OPEN);
    }
    private void change(Phase next){float current=openness(0);phase=next;phaseStart=level.getGameTime();startOpen=current;updateLid();sync();}
    private void sync(){setChanged();if(level!=null && !level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    public void dropContents(){PlatformInventory.closeFor(this);
        if(!drone.isEmpty()){
            boolean launched=launchId!=null && level instanceof ServerLevel server && (server.getEntity(launchId)!=null || MissionManager.wasSpawned(server,launchId));
            if(!launched)net.minecraft.world.Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,drone);
            drone=ItemStack.EMPTY;owner=null;if(level instanceof ServerLevel sl)MissionManager.get(sl.getServer()).abortPreparation(launchId);actor=null;launchId=null;setChanged();
        }
    }
    @Override protected void saveAdditional(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries){super.saveAdditional(tag,registries);write(tag,registries);}
    private void write(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries){if(loadIdentity!=null)tag.putUUID("PlatformLoadIdentity",loadIdentity);tag.put("CoordinateMission",mission.write());if(!drone.isEmpty())tag.put("Drone",drone.save(registries));tag.putInt("Phase",phase.ordinal());tag.putLong("PhaseStart",phaseStart);tag.putFloat("StartOpen",startOpen);if(owner!=null)tag.putUUID("Owner",owner);if(actor!=null)tag.putUUID("Actor",actor);if(launchId!=null)tag.putUUID("LaunchId",launchId);tag.putInt("Failure",lastFailure);tag.putFloat("LidFrom",lidFrom);tag.putBoolean("LidClosed",lidClosed);tag.putLong("LidStart",lidStart);tag.putInt("HandoffEntity",handoffEntity);if(handoffUuid!=null)tag.putUUID("HandoffUuid",handoffUuid);tag.putLong("HandoffStart",handoffStart);}
    @Override protected void loadAdditional(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries){
        if(!drone.isEmpty())lastVisual=drone.copy();
        super.loadAdditional(tag,registries);loadIdentity=tag.hasUUID("PlatformLoadIdentity")?tag.getUUID("PlatformLoadIdentity"):UUID.randomUUID();manualLaunch=false;mission.read(tag.getCompound("CoordinateMission"));drone=ItemStack.parseOptional(registries,tag.getCompound("Drone"));phase=Phase.values()[Math.max(0,Math.min(Phase.values().length-1,tag.getInt("Phase")))];phaseStart=tag.getLong("PhaseStart");startOpen=tag.getFloat("StartOpen");owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;actor=tag.hasUUID("Actor")?tag.getUUID("Actor"):null;launchId=tag.hasUUID("LaunchId")?tag.getUUID("LaunchId"):null;lastFailure=tag.getInt("Failure");
        if(!drone.isEmpty())lastVisual=drone.copy();handoffEntity=tag.contains("HandoffEntity")?tag.getInt("HandoffEntity"):-1;handoffUuid=tag.hasUUID("HandoffUuid")?tag.getUUID("HandoffUuid"):null;handoffStart=tag.getLong("HandoffStart");
        lidFrom=tag.contains("LidFrom")?tag.getFloat("LidFrom"):1;lidClosed=!tag.contains("LidClosed") || tag.getBoolean("LidClosed");lidStart=tag.getLong("LidStart");
        // Disk reads recover; client update packets must retain their live animation phase.
        if(!networkRead && (level==null || !level.isClientSide)){phase=Phase.RECOVERING;phaseStart=0;recoveryClockPending=true;clearHandoff();viewers.clear();lidFrom=1;lidClosed=true;lidStart=0;}
    }
    @Override public void handleUpdateTag(CompoundTag tag,net.minecraft.core.HolderLookup.Provider registries){networkRead=true;try{loadAdditional(tag,registries);}finally{networkRead=false;}}
    @Override public void onDataPacket(net.minecraft.network.Connection connection,ClientboundBlockEntityDataPacket packet,net.minecraft.core.HolderLookup.Provider registries){handleUpdateTag(packet.getTag(),registries);}
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries){CompoundTag tag=new CompoundTag();write(tag,registries);return tag;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public Component getDisplayName(){return Component.translatable("block.sbwdroneconfig.glide_bomb_platform_lp03");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){viewerOpened(player);return new GlidePlatformMenu(id,inventory,this);}
    public void open(ServerPlayer player){if(player.distanceToSqr(Vec3.atCenterOf(worldPosition))<=64)player.openMenu(this,buffer->buffer.writeBlockPos(worldPosition));else fail(player,6);}
    public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(7,4,7);}
    @Override public void onLoad(){super.onLoad();if(level!=null&&!level.isClientSide)level.scheduleTick(worldPosition,getBlockState().getBlock(),20);}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
}
