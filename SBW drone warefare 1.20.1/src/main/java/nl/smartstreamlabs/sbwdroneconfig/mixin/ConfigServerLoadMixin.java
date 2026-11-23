package nl.smartstreamlabs.sbwdroneconfig.mixin;
import net.minecraft.server.MinecraftServer;
import nl.smartstreamlabs.sbwdroneconfig.ConfigPreflight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="net.minecraftforge.server.ServerLifecycleHooks",remap=false)
public abstract class ConfigServerLoadMixin {
 @Inject(method="handleServerAboutToStart",at=@At("HEAD"),require=1,remap=false)
 private static void config$beforeLoader(MinecraftServer server,CallbackInfoReturnable<Boolean> ci){ConfigPreflight.server(server);}
}
