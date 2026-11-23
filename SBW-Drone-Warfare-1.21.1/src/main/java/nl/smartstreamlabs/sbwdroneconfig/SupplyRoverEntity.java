package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.Vec3;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.server.level.ServerPlayer;import net.minecraft.core.BlockPos;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.syncher.*;import java.util.*;
public class SupplyRoverEntity extends VatagEntity implements RescueVehicle {
 private static final EntityDataAccessor<String> LINK=SynchedEntityData.defineId(SupplyRoverEntity.class,EntityDataSerializers.STRING);
 private static final EntityDataAccessor<Integer> CARGO=SynchedEntityData.defineId(SupplyRoverEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> OPEN=SynchedEntityData.defineId(SupplyRoverEntity.class,EntityDataSerializers.BOOLEAN);
 public static final int DISPLAY_SLOTS=12;
 private static final java.util.List<EntityDataAccessor<net.minecraft.world.item.ItemStack>> DISPLAY=java.util.stream.IntStream.range(0,DISPLAY_SLOTS).mapToObj(i->SynchedEntityData.defineId(SupplyRoverEntity.class,EntityDataSerializers.ITEM_STACK)).toList();
 public net.minecraft.world.item.ItemStack displayedCargo(int slot){return entityData.get(DISPLAY.get(slot));}
 private void syncCargo(){
  var stacks=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();var weights=new java.util.ArrayList<Integer>();int count=0;
  for(int i=0;i<getContainerSize();i++){var stack=getItem(i);if(!stack.isEmpty()){count++;stacks.add(stack);weights.add(Math.max(1,Math.min(stack.getCount(),(int)Math.ceil(3.0*stack.getCount()/Math.max(1,stack.getMaxStackSize())))));}}
  entityData.set(CARGO,count);var visible=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
  for(int layer=0;layer<3&&visible.size()<DISPLAY_SLOTS;layer++)for(int i=0;i<stacks.size()&&visible.size()<DISPLAY_SLOTS;i++)if(weights.get(i)>layer)visible.add(stacks.get(i));
  for(int i=0;i<DISPLAY_SLOTS;i++){var next=i<visible.size()?visible.get(i).copy():net.minecraft.world.item.ItemStack.EMPTY;if(!next.isEmpty())next.setCount(1);if(!net.minecraft.world.item.ItemStack.matches(next,displayedCargo(i)))entityData.set(DISPLAY.get(i),next);}
 }
 private float door,oldDoor,latch,oldLatch;
 private BlockPos lastPartner=BlockPos.ZERO;private final Map<ServerPlayer,net.minecraft.world.inventory.AbstractContainerMenu> viewers=new HashMap<>();
 public SupplyRoverEntity(EntityType<? extends SupplyRoverEntity> t,Level l){super(t,l);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(LINK,"");b.define(CARGO,0);b.define(OPEN,false);for(var a:DISPLAY)b.define(a,net.minecraft.world.item.ItemStack.EMPTY);}
 public com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity unit(){return this;}public UUID partner(){String id=entityData.get(LINK);return id.isEmpty()?null:UUID.fromString(id);}public void partner(UUID id,BlockPos last){entityData.set(LINK,id==null?"":id.toString());lastPartner=last;}public BlockPos lastPartnerPosition(){return lastPartner;}public boolean occupied(){return false;}
 public void claim(ServerPlayer p){claimOwner(p);}
 public int cargoSlots(){return entityData.get(CARGO);}public boolean cargoOpen(){return entityData.get(OPEN);}
 public float cargoDoor(float p){return net.minecraft.util.Mth.lerp(p,oldDoor,door);}public float hitchLatch(float p){return net.minecraft.util.Mth.lerp(p,oldLatch,latch);}
 @Override public void input(ServerPlayer p,int flags,float yaw,float pitch){super.input(p,flags&~48,yaw,0);}
 @Override public Vec3 camera(boolean aim,float partial){var v=transformPosition(getVehicleTransform(partial),.125,2.4375,.39375);return new Vec3(v.x,v.y,v.z);}
 @Override public void move(MoverType type,Vec3 movement){if(!HitchJoint.propose(this,type,movement))super.move(type,movement);}
 @Override public boolean canCollideWith(Entity other){return !HitchJoint.paired(this,other)&&super.canCollideWith(other);}
 @Override public void baseTick(){super.baseTick();if(level().isClientSide){oldDoor=door;oldLatch=latch;door=net.minecraft.util.Mth.approach(door,cargoOpen()?1:0,1f/14);latch=net.minecraft.util.Mth.approach(latch,partner()==null?1:0,1f/6);}if(!level().isClientSide){viewers.entrySet().removeIf(e->e.getKey().hasDisconnected()||e.getKey().containerMenu!=e.getValue()||e.getKey().distanceToSqr(this)>64);entityData.set(OPEN,!viewers.isEmpty());syncCargo();RescueCoupling.tick(this);}}
 @Override public void openMenu(Player p){
  if(!(p instanceof ServerPlayer sp)||!permitted(sp)||p.distanceToSqr(this)>64)return;
  sp.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,actor)->net.minecraft.world.inventory.ChestMenu.sixRows(id,inv,new SupplyCargoInventory(this)),getDisplayName()));
  viewers.put(sp,sp.containerMenu);
 }
 @Override public boolean canPlaceItem(int slot,net.minecraft.world.item.ItemStack stack){
  return slot>=0&&slot<getContainerSize()&&(getItem(slot).isEmpty()||net.minecraft.world.item.ItemStack.isSameItemSameComponents(getItem(slot),stack));
 }
 @Override public InteractionResult interact(Player p,InteractionHand h){if(isRemoved())return InteractionResult.FAIL;if(p instanceof ServerPlayer sp){if(!permitted(sp))return InteractionResult.FAIL;var result=RescueCoupling.tool(sp,this,h);if(result!=null)return result;}if(p instanceof ServerPlayer sp&&p.getItemInHand(h).isEmpty()&&!p.isShiftKeyDown())sp.displayClientMessage(net.minecraft.network.chat.Component.literal("SR-01: right-click with a Monitor to link, then use it to drive. Shift + empty hand: cargo. Crowbar: couple MT-01."),true);var before=p.containerMenu;var result=super.interact(p,h);if(p instanceof ServerPlayer sp&&sp.containerMenu!=before&&p.getItemInHand(h).isEmpty())viewers.put(sp,sp.containerMenu);return result;}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);RescueCoupling.save(this,n);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);RescueCoupling.load(this,n);}
 @Override public void remove(RemovalReason why){if(!level().isClientSide&&why.shouldDestroy())RescueCoupling.detach(this);if(!level().isClientSide)RescueCoupling.forget(getUUID());super.remove(why);}
}
