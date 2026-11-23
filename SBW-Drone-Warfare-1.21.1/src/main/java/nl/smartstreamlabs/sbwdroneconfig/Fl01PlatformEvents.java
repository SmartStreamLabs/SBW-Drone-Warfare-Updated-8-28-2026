package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Fl01PlatformEvents {
    private Fl01PlatformEvents(){}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void rightClick(PlayerInteractEvent.RightClickBlock event){
        // Vanilla normally skips a block when crouching with an item. LP-02 retrieval must also work with full hands.
        if(event.isCanceled() || event.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND || !event.getEntity().isShiftKeyDown()
                || event.getUseBlock()==TriState.FALSE || !event.getLevel().hasChunkAt(event.getPos())
                || !event.getLevel().mayInteract(event.getEntity(),event.getPos())
                || !(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof Fl01PlatformBlock))return;
        event.setUseBlock(TriState.TRUE);event.setUseItem(TriState.FALSE);
    }
}
