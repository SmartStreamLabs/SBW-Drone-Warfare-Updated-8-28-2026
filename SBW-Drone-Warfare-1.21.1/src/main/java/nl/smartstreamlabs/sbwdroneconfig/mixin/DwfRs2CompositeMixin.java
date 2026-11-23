package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import nl.smartstreamlabs.sbwdroneconfig.DwfRs2;import java.util.*;import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
@Pseudo @Mixin(targets="com.refinedmods.refinedstorage.common.storage.externalstorage.CompositeExternalStorageProvider",remap=false)
public abstract class DwfRs2CompositeMixin implements DwfRs2.Composite{
 @Shadow(remap=false)@Final private List<ExternalStorageProvider>providers;
 public List<ExternalStorageProvider>dwfProviders(){return providers;}
}
