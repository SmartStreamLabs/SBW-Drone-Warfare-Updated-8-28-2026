package nl.smartstreamlabs.sbwdroneconfig;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.GuiGraphics;
import java.nio.file.Files;
import java.util.*;

/** Small thumbnails of registered mod logos, loaded on visible-row changes and released with the screen. */
public final class ConfigModIcons implements AutoCloseable {
 private record Icon(ResourceLocation texture,int width,int height){}
 private final Map<String,Optional<Icon>> icons=new HashMap<>();
 public void prepare(OtherModsConfigAccess.Entry entry){icons.computeIfAbsent(entry.id(),id->load(entry));}
 private Optional<Icon> load(OtherModsConfigAccess.Entry entry){try{
  var file=entry.info().getLogoFile();if(file.isEmpty())return Optional.empty();
  var path=entry.info().getOwningFile().getFile().findResource(file.get());
  try(var stream=Files.newInputStream(path);var image=NativeImage.read(stream)){
   float ratio=Math.min(1f,64f/Math.max(image.getWidth(),image.getHeight()));int w=Math.max(1,Math.round(image.getWidth()*ratio)),h=Math.max(1,Math.round(image.getHeight()*ratio));var thumb=new NativeImage(w,h,true);image.resizeSubRectTo(0,0,image.getWidth(),image.getHeight(),thumb);
   var texture=Minecraft.getInstance().getTextureManager().register("drone-config-"+entry.id(),new DynamicTexture(thumb));return Optional.of(new Icon(texture,w,h));
  }
 }catch(Exception e){SbwDroneRangeConfig.LOGGER.debug("No usable config-browser logo for {}",entry.id(),e);return Optional.empty();}}
 public boolean draw(GuiGraphics g,String id,int x,int y,int size){var icon=icons.getOrDefault(id,Optional.empty()).orElse(null);if(icon==null)return false;float ratio=(float)size/Math.max(icon.width,icon.height);int w=Math.max(1,Math.round(icon.width*ratio)),h=Math.max(1,Math.round(icon.height*ratio));g.blit(icon.texture,x+(size-w)/2,y+(size-h)/2,0,0,w,h,w,h);return true;}
 @Override public void close(){for(var value:icons.values())value.ifPresent(i->Minecraft.getInstance().getTextureManager().release(i.texture));icons.clear();}
}
