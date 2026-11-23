package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.*;

/** Runs before the loader can replace a malformed file or discard old aliases. Never scans world contents. */
public final class ConfigPreflight {
 private ConfigPreflight(){}
 private static Path defaults(){return FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath());}
 public static void globals(){process(FMLPaths.CONFIGDIR.get(),false,defaults(),null);}
 public static void server(MinecraftServer server){process(server.getWorldPath(LevelResource.ROOT).resolve("serverconfig"),true,defaults(),FMLPaths.CONFIGDIR.get());}
 private static void process(Path folder,boolean server,Path defaults,Path fallback){
  var families=new LinkedHashMap<String,ConfigCatalog.Definition>();
  for(var d:ConfigCatalog.definitions()){
   if((d.scope()==ModConfig.Type.SERVER)!=server)continue;
   if(!server&&d.scope()==ModConfig.Type.CLIENT&&FMLEnvironment.dist!=Dist.CLIENT)continue;
   families.putIfAbsent(d.scope()+":"+d.file(),d);
  }
  Path root=folder.toAbsolutePath().normalize();
  for(var d:families.values()){
   Path file=root.resolve(d.file()).normalize();if(!file.getParent().equals(root))throw new IllegalStateException("Invalid registered configuration filename");
   if(!Files.exists(file)&&fallback!=null){var other=fallback.toAbsolutePath().normalize();file=other.resolve(d.file()).normalize();if(!file.getParent().equals(other))throw new IllegalStateException("Invalid fallback config filename");}
   Path source=file;
   if(!Files.exists(source)&&defaults!=null)source=defaults.resolve(d.file());
   Path legacy=FMLPaths.CONFIGDIR.get().resolve("sbwdroneconfig-common.toml");
   boolean legacyAddon=d.family().equals("AddonConfig")&&Files.isRegularFile(legacy);
   if(!Files.isRegularFile(source)&&!legacyAddon)continue;
   try{
    var parsed=RecoveringConfigToml.parse(Files.isRegularFile(source)?Files.readString(source,StandardCharsets.UTF_8):"");
    Object rawVersion=parsed.config().get(List.of(ConfigSchema.VERSION));
    if(legacyAddon&&(!(rawVersion instanceof Number n)||n.intValue()<1)){
     var original=RecoveringConfigToml.parse(Files.readString(legacy,StandardCharsets.UTF_8));
     int copied=ConfigMigrations.seedLegacy(d.owner(),parsed.config(),original.config());
     if(copied>0)SbwDroneRangeConfig.LOGGER.info("Preserved {} legacy settings in {}",copied,d.file());
     for(String notice:original.notices())SbwDroneRangeConfig.LOGGER.warn("Legacy common config: {}",notice);
    }
    var migrated=ConfigMigrations.prepare(d.family(),d.owner(),parsed.config(),List.of());
    if(migrated.changed()||!parsed.notices().isEmpty()){
     byte[] bytes=new com.electronwill.nightconfig.toml.TomlWriter().writeToString(migrated.config()).getBytes(StandardCharsets.UTF_8);
     AtomicConfigFiles.replace(file,bytes,Files.exists(file));
     SbwDroneRangeConfig.LOGGER.info("Migrated Drone Warfare configuration {} (schema {})",d.file(),ConfigSchema.CURRENT);
     for(String notice:parsed.notices())SbwDroneRangeConfig.LOGGER.warn("{}: {}",d.file(),notice);
     for(String notice:migrated.notices())SbwDroneRangeConfig.LOGGER.info("{}: {}",d.file(),notice);
    }
   }catch(IOException|RuntimeException e){throw new IllegalStateException("Cannot safely prepare Drone Warfare config "+file+"; refusing loader fallback/reset",e);}
  }
 }
}
