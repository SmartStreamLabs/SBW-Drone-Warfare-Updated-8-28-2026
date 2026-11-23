package nl.smartstreamlabs.sbwdroneconfig;
/** Hidden moving-part registration; no second item or renderer. */
public final class Ps01DoorBlock extends Ps01Block{@Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec(){return simpleCodec(Ps01DoorBlock::new);}
 public Ps01DoorBlock(Properties p){super(p,true);}}
