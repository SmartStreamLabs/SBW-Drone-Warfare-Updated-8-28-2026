package nl.smartstreamlabs.sbwdroneconfig;
public final class Pb01DoorBlock extends Pb01Block{public Pb01DoorBlock(Properties p){super(p,true);}@Override protected com.mojang.serialization.MapCodec<?extends net.minecraft.world.level.block.BaseEntityBlock>codec(){return simpleCodec(Pb01DoorBlock::new);}}
