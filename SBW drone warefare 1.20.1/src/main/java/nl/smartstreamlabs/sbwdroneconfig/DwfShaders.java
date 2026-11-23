package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.client.renderer.ShaderInstance;import com.mojang.blaze3d.vertex.DefaultVertexFormat;
/** The supplied glass has alpha 7/255. Vanilla's 0.1 cutout would erase it. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class DwfShaders{
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event){event.registerBlockEntityRenderer(AddonBlockEntities.DWF01.get(),DwfRenderer::new);}

 public static ShaderInstance glass;
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void register(net.minecraftforge.client.event.RegisterShadersEvent event)throws java.io.IOException{
  DwfModel.clear();DwfProductPreview.clear();DwfStaticGpu.shader=null;event.registerShader(new ShaderInstance(event.getResourceProvider(),net.minecraft.resources.ResourceLocation.tryParse("sbwdroneconfig:dwf01_static"),DefaultVertexFormat.NEW_ENTITY),shader->DwfStaticGpu.shader=shader);event.registerShader(new ShaderInstance(event.getResourceProvider(),net.minecraft.resources.ResourceLocation.tryParse("sbwdroneconfig:dwf01_glass"),DefaultVertexFormat.NEW_ENTITY),shader->glass=shader);
 }
 private DwfShaders(){}
}
