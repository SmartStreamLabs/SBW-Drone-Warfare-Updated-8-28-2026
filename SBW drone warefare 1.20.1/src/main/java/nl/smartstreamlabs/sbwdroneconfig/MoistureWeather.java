package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;import net.minecraft.core.BlockPos;import net.minecraft.world.level.biome.Biome;
/** Replaceable internal weather source. Only vanilla is implemented. Never loads a chunk. */
public interface MoistureWeather {
 record Sample(boolean rain,boolean thunder){}
 Sample at(Entity entity);
 MoistureWeather VANILLA=e->{var l=e.level();BlockPos p=BlockPos.containing(e.getX(),e.getBoundingBox().maxY,e.getZ());if(p.getY()<l.getMinBuildHeight()||p.getY()>=l.getMaxBuildHeight()||!l.hasChunkAt(p)||!l.dimensionType().hasSkyLight()||l.dimensionType().hasCeiling()||e.isInWaterOrBubble()||!l.isRainingAt(p)||l.getBiome(p).value().getPrecipitationAt(p)!=Biome.Precipitation.RAIN)return new Sample(false,false);return new Sample(true,l.isThundering());};
}
