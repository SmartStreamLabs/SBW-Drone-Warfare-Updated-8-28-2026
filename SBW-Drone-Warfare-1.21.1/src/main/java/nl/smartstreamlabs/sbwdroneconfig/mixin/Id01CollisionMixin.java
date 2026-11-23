package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.entity.Entity;import net.minecraft.world.level.Level;import net.minecraft.world.phys.AABB;import net.minecraft.world.phys.shapes.VoxelShape;import java.util.List;import nl.smartstreamlabs.sbwdroneconfig.Id01Geometry;
/** Native movers receive nearby ID-01 components instead of an enclosing solid hull. */
@Mixin(value=Entity.class,remap=false)
public abstract class Id01CollisionMixin {
 @ModifyVariable(method="collide",at=@At("STORE"),ordinal=0)
 private List<VoxelShape> id01$parts(List<VoxelShape> original,net.minecraft.world.phys.Vec3 requested){
  Entity mover=(Entity)(Object)this;Level level=mover.level();AABB area=mover.getBoundingBox().expandTowards(requested);
  return nl.smartstreamlabs.sbwdroneconfig.Hx2Geometry.addCollisions(level,mover,area,nl.smartstreamlabs.sbwdroneconfig.Qd01Geometry.addCollisions(level,mover,area,nl.smartstreamlabs.sbwdroneconfig.Rd01Geometry.addCollisions(level,mover,area,Id01Geometry.addCollisions(level,mover,area,original))));
 }
}
