package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.level.block.*;import net.minecraft.world.phys.*;import net.minecraft.world.level.ClipContext;import java.util.*;
/** Existing shader-free light-block technique, bounded to a short underwater beam. Never replaces plants/structures. */
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Sd01Lights {
 private record Key(ServerLevel level,BlockPos pos){}private record Lease(UUID owner,long until){}
 private static final Map<Key,Lease> LEASES=new HashMap<>();public static final int MAX_LIGHTS=128;
 public static void tick(Sd01Entity d){if(!(d.level() instanceof ServerLevel l))return;if(!d.lights()||!d.submerged()||d.isRemoved()||!d.isAlive()||d.isWreck()||!AddonConfig.enableSpotlightModule()||!AddonConfig.spotlightRealLightEnabled()){clear(d);return;}if(d.tickCount%10!=0)return;
  var f=d.cameraFrame(1);var p=f.transformPosition(new org.joml.Vector3d());var v=f.transformDirection(new org.joml.Vector3d(0,0,1)).normalize();Vec3 origin=new Vec3(p.x,p.y,p.z),direction=new Vec3(v.x,v.y,v.z);double range=Math.min(Sd01Config.LIGHT_RANGE.get(),Math.min(AddonConfig.spotlightRange(),AddonConfig.spotlightLightRange()));for(double i=0;i<=range;i++)if(!l.hasChunkAt(BlockPos.containing(origin.add(direction.scale(i))))){range=Math.max(0,i-1);break;}var hit=l.clip(new ClipContext(origin,origin.add(direction.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,d));if(hit.getType()==HitResult.Type.BLOCK)range=Math.max(0,origin.distanceTo(hit.getLocation())-.15);
  var wanted=new LinkedHashMap<BlockPos,Integer>();for(double r=.65;r<=range;r+=1.5){var at=BlockPos.containing(origin.add(direction.scale(r)));if(wanted.size()==12)break;var state=l.getBlockState(at);var key=new Key(l,at);var old=LEASES.get(key);if(old!=null&&!old.owner.equals(d.getUUID()))continue;if(state.is(Blocks.WATER)&&state.getFluidState().isSource()||state.is(AddonBlocks.SD01_LIGHT.get())&&state.getValue(LightBlock.WATERLOGGED)&&old!=null&&old.owner.equals(d.getUUID()))wanted.put(at,Math.max(3,15-(int)(r*.7)));}
  for(var key:new ArrayList<>(LEASES.keySet())){var lease=LEASES.get(key);if(key.level==l&&lease.owner.equals(d.getUUID())&&!wanted.containsKey(key.pos))release(key);}
  for(var en:wanted.entrySet()){var key=new Key(l,en.getKey());if(!LEASES.containsKey(key)&&LEASES.size()>=MAX_LIGHTS)break;LEASES.put(key,new Lease(d.getUUID(),l.getGameTime()+25));var state=AddonBlocks.SD01_LIGHT.get().defaultBlockState().setValue(LightBlock.WATERLOGGED,true).setValue(LightBlock.LEVEL,en.getValue());if(!l.getBlockState(en.getKey()).equals(state))l.setBlock(en.getKey(),state,3);l.scheduleTick(en.getKey(),AddonBlocks.SD01_LIGHT.get(),30);}
 }
 public static void expire(ServerLevel level,BlockPos pos){var key=new Key(level,pos);var lease=LEASES.get(key);if(lease!=null&&lease.until>level.getGameTime()){level.scheduleTick(pos,AddonBlocks.SD01_LIGHT.get(),30);return;}release(key);}
 private static void release(Key key){LEASES.remove(key);if(key.level.hasChunkAt(key.pos)&&key.level.getBlockState(key.pos).is(AddonBlocks.SD01_LIGHT.get()))key.level.setBlock(key.pos,key.level.getBlockState(key.pos).getValue(LightBlock.WATERLOGGED)?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState(),3);}
 public static void clear(Sd01Entity d){for(var entry:new ArrayList<>(LEASES.entrySet()))if(entry.getKey().level==d.level()&&entry.getValue().owner.equals(d.getUUID()))release(entry.getKey());}
 public static int count(){return LEASES.size();}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var k:new ArrayList<>(LEASES.keySet()))release(k);LEASES.clear();}
 private Sd01Lights(){}
}
