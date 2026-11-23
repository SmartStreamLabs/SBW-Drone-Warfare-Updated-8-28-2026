package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.renderer.ShaderInstance;import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/** The supplied glass has alpha 7/255. Vanilla's 0.1 cutout would erase it. */
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class DwfShaders{
 @net.neoforged.bus.api.SubscribeEvent public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(AddonBlockEntities.DWF01.get(),DwfRenderer::new);}

 public static ShaderInstance glass;
 @net.neoforged.bus.api.SubscribeEvent public static void register(net.neoforged.neoforge.client.event.RegisterShadersEvent event)throws java.io.IOException{
  DwfModel.clear();DwfProductPreview.clear();DwfStaticGpu.shader=null;event.registerShader(new ShaderInstance(event.getResourceProvider(),net.minecraft.resources.ResourceLocation.tryParse("sbwdroneconfig:dwf01_static"),DefaultVertexFormat.NEW_ENTITY),shader->DwfStaticGpu.shader=shader);event.registerShader(new ShaderInstance(event.getResourceProvider(),net.minecraft.resources.ResourceLocation.tryParse("sbwdroneconfig:dwf01_glass"),DefaultVertexFormat.NEW_ENTITY),shader->glass=shader);
 }
 private DwfShaders(){}
}
