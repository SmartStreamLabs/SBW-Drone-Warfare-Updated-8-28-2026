package nl.smartstreamlabs.sbwdroneconfig.mixin;
import nl.smartstreamlabs.sbwdroneconfig.InjuryServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Settle an already opened medkit before a packet can split, drop or move its reserved stack. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class InjuryInventoryPacketMixin {
    @Shadow public ServerPlayer player;
    @Inject(method="handleContainerClick",at=@At("HEAD"))
    private void injury$inventory(ServerboundContainerClickPacket packet,CallbackInfo c) {
        if(player.server.isSameThread())InjuryServer.inventoryAction(player);
    }
    @Inject(method="handleSetCarriedItem",at=@At("HEAD"))
    private void injury$slot(ServerboundSetCarriedItemPacket packet,CallbackInfo c) {
        if(player.server.isSameThread()&&packet.getSlot()!=player.getInventory().selected)InjuryServer.inventoryAction(player);
    }
    @Inject(method="handlePlayerAction",at=@At("HEAD"))
    private void injury$drop(ServerboundPlayerActionPacket packet,CallbackInfo c) {
        if(!player.server.isSameThread())return;
        switch(packet.getAction()) {
            case DROP_ITEM, DROP_ALL_ITEMS, SWAP_ITEM_WITH_OFFHAND -> InjuryServer.inventoryAction(player);
            default -> { }
        }
    }
}
