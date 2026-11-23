package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.gen.Invoker;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;import java.util.*;import nl.smartstreamlabs.sbwdroneconfig.DwfRs2;import com.refinedmods.refinedstorage.api.storage.external.*;import com.refinedmods.refinedstorage.api.storage.composite.ParentComposite;import com.refinedmods.refinedstorage.api.resource.*;import com.refinedmods.refinedstorage.api.resource.list.*;
@Pseudo @Mixin(targets="com.refinedmods.refinedstorage.api.storage.external.ExternalStorage",remap=false)
public abstract class DwfRs2CacheMixin implements DwfRs2.Cache{
 @Shadow(remap=false)@Final private ExternalStorageProvider provider;@Shadow(remap=false)@Final@Mutable private MutableResourceList cache;@Shadow(remap=false)@Final private Set<ParentComposite>parents;
 @Invoker(value="addToCache",remap=false)public abstract void dwfAdd(ResourceKey key,long amount);@Invoker(value="removeFromCache",remap=false)public abstract void dwfRemove(ResourceKey key,long amount);
 public long dwfCount(ResourceKey key){return cache.get(key);}public boolean dwfAttached(){return !parents.isEmpty();}public void dwfReset(){if(parents.isEmpty())cache=MutableResourceListImpl.create();}
 @Inject(method="detectChanges",at=@At("HEAD"),cancellable=true,require=1)private void dwf$incremental(CallbackInfoReturnable<Boolean>ci){var p=DwfRs2.find(provider);if(p!=null){p.attach(this);ci.setReturnValue(false);}}
 @Inject(method="onRemovedFromComposite",at=@At("RETURN"),require=1)private void dwf$detached(ParentComposite parent,CallbackInfo ci){var p=DwfRs2.find(provider);if(p!=null&&parents.isEmpty()){p.release();dwfReset();}}
}
