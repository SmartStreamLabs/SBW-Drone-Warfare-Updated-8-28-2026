package nl.smartstreamlabs.sbwdroneconfig;

import java.util.UUID;import net.minecraft.world.phys.*;import net.minecraft.core.BlockPos;import net.minecraft.world.level.ChunkPos;
/** No entity, inventory, owner or source entity data is retained. */
public record Sk01Entry(UUID id,UUID source,double x,double y,double z,float yaw,float scale,String model,long created,boolean pending) {
 public Sk01Entry {if(id==null||source==null||model==null||model.length()>160||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||!Float.isFinite(yaw)||!Float.isFinite(scale)||scale<.1f||scale>4)throw new IllegalArgumentException("Invalid SK-01 record");if(x==0)x=0;if(y==0)y=0;if(z==0)z=0;if(yaw==0)yaw=0;}
 public long chunk(){return ChunkPos.asLong(((int)Math.floor(x))>>4,((int)Math.floor(z))>>4);}
 public Vec3 position(){return new Vec3(x,y,z);}
 public AABB bounds(){double a=Math.toRadians(yaw),c=Math.abs(Math.cos(a)),s=Math.abs(Math.sin(a)),rx=(.506*c+.951*s)*scale,rz=(.506*s+.951*c)*scale;return new AABB(x-rx,y,z-rz,x+rx,y+.436*scale,z+rz);}
 public Sk01Entry settled(Vec3 at){return new Sk01Entry(id,source,at.x,at.y,at.z,yaw,scale,model,created,false);}
}
