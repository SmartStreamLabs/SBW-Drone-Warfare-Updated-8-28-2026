package nl.smartstreamlabs.sbwdroneconfig.mixin;
import org.spongepowered.asm.mixin.*;
import nl.smartstreamlabs.sbwdroneconfig.*;
import com.refinedmods.refinedstorage.api.network.node.*;
import com.refinedmods.refinedstorage.capability.NetworkNodeProxyCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
/** RS discovers nodes through its Forge capability, not Java instanceof alone.
 * Loaded only with the verified legacy RS dependency. */
@Mixin(DwfPortBlockEntity.class)
public abstract class DwfRs1PortMixin extends DwfBlockEntity implements INetworkNodeProxy {
 @Unique private LazyOptional<INetworkNodeProxy> dwf$rsProxy=LazyOptional.of(()->this);
 protected DwfRs1PortMixin(BlockPos pos,BlockState state){super(pos,state);}
 @Override public INetworkNode getNode(){return DwfRs1.node((DwfPortBlockEntity)(Object)this);}
 @Override public <T>LazyOptional<T>getCapability(Capability<T>cap,Direction side){if(cap==NetworkNodeProxyCapability.NETWORK_NODE_PROXY_CAPABILITY)return dwf$rsProxy.cast();return super.getCapability(cap,side);}
 @Override public void invalidateCaps(){super.invalidateCaps();dwf$rsProxy.invalidate();}
 @Override public void reviveCaps(){super.reviveCaps();dwf$rsProxy=LazyOptional.of(()->this);}
}
