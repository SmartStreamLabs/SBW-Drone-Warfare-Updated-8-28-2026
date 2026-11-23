package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.vertex.*;import net.minecraft.client.renderer.*;import net.minecraft.resources.ResourceLocation;import java.util.*;
/** Alpha glass after opaque internals, depth testing on, depth writes off. */
public final class VehicleGlassRenderTypes extends RenderType{
 private static final Map<ResourceLocation,RenderType>GLASS=new HashMap<>();
 private VehicleGlassRenderTypes(){super("vehicle_glass",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,true,()->{},()->{});}
 public static RenderType glass(ResourceLocation texture){return GLASS.computeIfAbsent(texture,t->create("vehicle_glass",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,1536,true,true,CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState(new TextureStateShard(t,false,false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true)));}
}
