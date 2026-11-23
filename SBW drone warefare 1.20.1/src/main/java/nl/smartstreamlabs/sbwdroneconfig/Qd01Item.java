package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.item.*;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;import net.minecraft.core.BlockPos;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.chat.Component;import java.util.List;
/** Shared spawn-item/native-container transaction, validated before consumption or world mutation. */
public class Qd01Item extends Item {
 public Qd01Item(Properties p){super(p);}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){return deploy(l,p,h,null);}
 @Override public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext c){return c.getPlayer()==null?InteractionResult.PASS:deploy(c.getLevel(),c.getPlayer(),c.getHand(),null).getResult();}
 @Override public void appendHoverText(ItemStack s,Level l,List<Component> lines,TooltipFlag f){lines.add(Component.translatable("qd01.tooltip"));lines.add(Component.translatable("qd01.controls"));}
 public static CompoundTag container(ItemStack s){var n=BlockItem.getBlockEntityData(s);return n!=null&&"sbwdroneconfig:quadruped_drone_qd01".equals(n.getString("EntityType"))?n:null;}
 public static InteractionResultHolder<ItemStack> deploy(Level l,Player p,InteractionHand h,CompoundTag saved){
  var s=p.getItemInHand(h);if(l.isClientSide)return InteractionResultHolder.success(s);
  if(!(p instanceof ServerPlayer sp)||p.isSpectator()||!p.isAlive()||!p.mayBuild())return fail(p,s,"qd01.denied");
  var hit=getPlayerPOVHitResult(l,p,ClipContext.Fluid.NONE);if(hit.getType()!=HitResult.Type.BLOCK||hit.getDirection()!=net.minecraft.core.Direction.UP)return fail(p,s,"qd01.ground");
  Vec3 at=hit.getLocation().add(0,.005,0);var q=AddonEntities.QD01.get().create(l);if(q==null)return InteractionResultHolder.fail(s);
  if(saved!=null&&saved.contains("Entity",10))q.load(saved.getCompound("Entity").copy());else q.setEnergy(q.getMaxEnergy());if(!q.permitted(sp))return fail(p,s,"qd01.denied");
  q.releaseControl();q.setPos(at);q.setYRot(p.getYRot());q.setXRot(0);q.setZRot(0);q.setPower(0);q.setDeltaMovement(Vec3.ZERO);
  var nativeFrame=q.nativeFrame(1);for(int i=0;i<4;i++){var n=nativeFrame.transformPosition(new org.joml.Vector3d(Qd01Ground.x(i),0,Qd01Ground.z(i)));var foot=Qd01Ground.contact(l,q,new Vec3(n.x,n.y,n.z),.12,.18);if(foot==null||Math.abs(foot.y-at.y)>.12)return fail(p,s,"qd01.ground");var pos=BlockPos.containing(foot.add(0,-.01,0));if(!l.mayInteract(p,pos)||!p.mayUseItemAt(pos,hit.getDirection(),s))return fail(p,s,"qd01.denied");}
  var area=q.getBoundingBox();if(!l.hasChunksAt(BlockPos.containing(area.minX,area.minY,area.minZ),BlockPos.containing(area.maxX,area.maxY,area.maxZ))||!l.getWorldBorder().isWithinBounds(area))return fail(p,s,"qd01.clearance");q.updateOBB();var parts=q.getOBBs();
  for(var shape:l.getBlockCollisions(q,area))for(var box:shape.toAabbs())if(parts.stream().anyMatch(o->com.atsuishio.superbwarfare.tools.OBB.isColliding(o,box)))return fail(p,s,"qd01.clearance");
  for(var e:l.getEntities(q,area,en->en.isPickable()&&!en.isSpectator())){boolean overlap=e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v?parts.stream().anyMatch(a->v.getOBBs().stream().anyMatch(b->com.atsuishio.superbwarfare.tools.OBB.isColliding(a,b))):parts.stream().anyMatch(a->com.atsuishio.superbwarfare.tools.OBB.isColliding(a,e.getBoundingBox()));if(overlap)return fail(p,s,"qd01.clearance");}
  q.claimOwner(sp);if(!l.addFreshEntity(q))return fail(p,s,"qd01.clearance");if(!p.getAbilities().instabuild)s.shrink(1);p.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(s.getItem()));return InteractionResultHolder.consume(s);
 }
 private static InteractionResultHolder<ItemStack> fail(Player p,ItemStack s,String key){p.displayClientMessage(Component.translatable(key),true);return InteractionResultHolder.fail(s);}
}
