package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.data.vehicle.VehicleData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;

/** Work follows ground vehicles, not mines. Native authored TerrainCompat points supply actual ground contact. */
public final class Lm01Contact {
 public static void vehicle(VehicleEntity v,Vec3 previous){
  if(!(v.level()instanceof ServerLevel l)||!Lm01Config.VEHICLES.get()||v.isRemoved()||v.isWreck()||!v.isAlive()||v.isPassenger())return;
  var data=VehicleData.from(v).getDefault();String engine=data.getEngineType().name();if(!engine.equalsIgnoreCase("Wheel")&&!engine.equalsIgnoreCase("Track"))return;
  var points=data.getTerrainCompat();if(points==null||points.isEmpty())return;
  var frame=v.getVehicleTransform(1);var delta=previous==null?Vec3.ZERO:v.position().subtract(previous);
  int steps=Math.min(8,Math.max(1,(int)Math.ceil(delta.horizontalDistance()/.35)));if(delta.lengthSqr()>16)steps=1;
  var cell=new BlockPos.MutableBlockPos();int count=0;
  for(var foot:points){if(count++>=16)break;var world=frame.transformPosition(new org.joml.Vector3d(foot.x,foot.y,foot.z));
   for(int i=0;i<steps;i++){double f=(double)i/steps,x=world.x-delta.x*f,y=world.y-delta.y*f,z=world.z-delta.z*f;cell.set(x,y+.025,z);
    var chunk=l.getChunkSource().getChunkNow(cell.getX()>>4,cell.getZ()>>4);if(chunk==null)continue;var state=chunk.getBlockState(cell);
    if(!state.is(AddonBlocks.LM01.get())||state.getValue(Lm01Block.STAGE)!=Lm01Block.Stage.ARMED)continue;
    if(Lm01Block.triggerBox(cell,state).intersects(new AABB(x-.10,y-.04,z-.10,x+.10,y+.08,z+.10)))Lm01Block.trigger(l,cell.immutable());
   }
  }
 }
 /** Bounded foot-cell checks follow living actors; idle mines still have no ticker or radius scan. */
 public static void living(net.minecraft.world.entity.LivingEntity entity){
  if(!(entity.level() instanceof ServerLevel level)||!entity.isAlive()||entity.isSpectator()||entity.isRemoved()||entity instanceof net.minecraft.world.entity.decoration.ArmorStand)return;
  if(entity instanceof net.minecraft.world.entity.player.Player?!Lm01Config.PLAYERS.get():!Lm01Config.MOBS.get())return;
  var box=entity.getBoundingBox();int count=0;var cell=new BlockPos.MutableBlockPos();
  for(int x=(int)Math.floor(box.minX);x<=Math.floor(box.maxX);x++)for(int z=(int)Math.floor(box.minZ);z<=Math.floor(box.maxZ);z++)for(int y=(int)Math.floor(box.minY-.03);y<=Math.floor(box.minY+.03);y++){
   if(count++>=64)return;var chunk=level.getChunkSource().getChunkNow(x>>4,z>>4);if(chunk==null)continue;cell.set(x,y,z);if(chunk.getBlockState(cell).is(AddonBlocks.LM01.get()))Lm01Block.contact(level,cell.immutable(),entity);
  }
 }
 private Lm01Contact(){}
}
