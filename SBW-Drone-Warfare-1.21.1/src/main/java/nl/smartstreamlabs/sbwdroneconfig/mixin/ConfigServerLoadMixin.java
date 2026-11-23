package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.server.MinecraftServer;
import nl.smartstreamlabs.sbwdroneconfig.ConfigPreflight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="net.neoforged.neoforge.server.ServerLifecycleHooks",remap=false)
public abstract class ConfigServerLoadMixin {
 @Inject(method="handleServerAboutToStart",at=@At("HEAD"),require=1,remap=false)
 private static void config$beforeLoader(MinecraftServer server,CallbackInfo ci){ConfigPreflight.server(server);}
}
