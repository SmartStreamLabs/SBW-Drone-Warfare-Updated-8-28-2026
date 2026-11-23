package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import java.util.*;

/** Retextured immutable quads are baked into ordinary chunk buffers, not drawn by one BER per pile. */
public final class RubbleModel extends BakedModelWrapper<BakedModel> {
    private record Key(List<BakedQuad> shape,BlockState source){}
    private static final Map<Key,List<BakedQuad>> CACHE=new LinkedHashMap<>(32,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<Key,List<BakedQuad>> e){return size()>512;}};
    public RubbleModel(BakedModel parent){super(parent);}
    public static void clear(){synchronized(CACHE){CACHE.clear();}}
    @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType layer){
        var quads=originalModel.getQuads(state,side,random,data,layer);var source=data.get(RubbleBlockEntity.SOURCE);if(source==null)return quads;
        var key=new Key(quads,source);synchronized(CACHE){var cached=CACHE.get(key);if(cached!=null)return cached;}
        var sprite=net.minecraft.client.Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(source);var out=new ArrayList<BakedQuad>(quads.size());
        for(var q:quads){var old=q.getSprite();int[] values=q.getVertices().clone();int stride=values.length/4;for(int v=0;v<4;v++){int at=v*stride;float u=Float.intBitsToFloat(values[at+4]),w=Float.intBitsToFloat(values[at+5]);float nu=sprite.getU0()+(u-old.getU0())/(old.getU1()-old.getU0())*(sprite.getU1()-sprite.getU0());float nv=sprite.getV0()+(w-old.getV0())/(old.getV1()-old.getV0())*(sprite.getV1()-sprite.getV0());values[at+4]=Float.floatToRawIntBits(nu);values[at+5]=Float.floatToRawIntBits(nv);}out.add(new BakedQuad(values,-1,q.getDirection(),sprite,q.isShade()));}
        var result=List.copyOf(out);synchronized(CACHE){CACHE.put(key,result);}return result;
    }
    @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon(ModelData data){var source=data.get(RubbleBlockEntity.SOURCE);return source==null?originalModel.getParticleIcon(data):net.minecraft.client.Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(source);}
    @Override public ModelData getModelData(net.minecraft.world.level.BlockAndTintGetter level,net.minecraft.core.BlockPos pos,BlockState state,ModelData modelData){if(level.getBlockEntity(pos)instanceof RubbleBlockEntity e)return e.getModelData();var recent=RubbleParticleSource.find(level,pos);return recent==null?modelData:modelData.derive().with(RubbleBlockEntity.SOURCE,recent).build();}
}
