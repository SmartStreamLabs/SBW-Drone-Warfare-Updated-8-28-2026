package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.platform.NativeImage;import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.texture.DynamicTexture;import net.minecraft.resources.ResourceLocation;import java.util.*;
/** Split original texture coverage once on reload: opaque writes depth, glass writes colour only. */
public final class Mpl01Materials {
 public record Pair(ResourceLocation opaque,ResourceLocation glass){}
 private static final Map<ResourceLocation,Pair>CACHE=new HashMap<>();
 public static Pair get(ResourceLocation source){return CACHE.computeIfAbsent(source,Mpl01Materials::load);}
 private static Pair load(ResourceLocation source){var m=Minecraft.getInstance();try(var in=m.getResourceManager().getResource(source).orElseThrow().open();var original=NativeImage.read(in)){
  var opaque=new NativeImage(original.getWidth(),original.getHeight(),false);var glass=new NativeImage(original.getWidth(),original.getHeight(),false);
  for(int y=0;y<original.getHeight();y++)for(int x=0;x<original.getWidth();x++){int c=original.getPixelRGBA(x,y),a=c>>>24;opaque.setPixelRGBA(x,y,a>=254?c:0);glass.setPixelRGBA(x,y,a>0&&a<254?c:0);}
  var a=ResourceLocation.tryParse("sbwdroneconfig:runtime/mpl01_opaque");var b=ResourceLocation.tryParse("sbwdroneconfig:runtime/mpl01_glass");m.getTextureManager().register(a,new DynamicTexture(opaque));m.getTextureManager().register(b,new DynamicTexture(glass));return new Pair(a,b);
 }catch(java.io.IOException e){throw new IllegalStateException("Cannot load MPL-01 materials",e);}}
 public static void clear(){CACHE.clear();var m=Minecraft.getInstance();Runnable close=()->{m.getTextureManager().release(ResourceLocation.tryParse("sbwdroneconfig:runtime/mpl01_opaque"));m.getTextureManager().release(ResourceLocation.tryParse("sbwdroneconfig:runtime/mpl01_glass"));};if(com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread())close.run();else com.mojang.blaze3d.systems.RenderSystem.recordRenderCall(close::run);}
 private Mpl01Materials(){}
}
