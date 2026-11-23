package nl.smartstreamlabs.sbwdroneconfig;
public final class Sk01DecorationBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
 public Sk01DecorationBlockEntity(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){super(AddonBlockEntities.SK01_DECORATION.get(),pos,state);}
 public net.minecraft.world.phys.AABB getRenderBoundingBox(){return new net.minecraft.world.phys.AABB(worldPosition).inflate(1,.05,1);}
}
