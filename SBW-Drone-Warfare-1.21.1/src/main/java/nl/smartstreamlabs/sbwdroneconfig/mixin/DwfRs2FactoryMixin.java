package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;import nl.smartstreamlabs.sbwdroneconfig.*;import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
@Pseudo @Mixin(targets="com.refinedmods.refinedstorage.neoforge.storage.externalstorage.ItemHandlerPlatformExternalStorageProviderFactory",remap=false)
public abstract class DwfRs2FactoryMixin{
 @Inject(method="create",at=@At("HEAD"),cancellable=true,require=1)private void dwf$provider(net.minecraft.server.level.ServerLevel level,net.minecraft.core.BlockPos pos,net.minecraft.core.Direction side,CallbackInfoReturnable<ExternalStorageProvider>ci){var provider=DwfRs2.external(level,pos);if(provider!=null)ci.setReturnValue(provider);}
}
