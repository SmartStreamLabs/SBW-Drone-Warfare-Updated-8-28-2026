package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class GlidePlatformEvents {
    private GlidePlatformEvents(){}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void rightClick(PlayerInteractEvent.RightClickBlock event){
        // Vanilla normally skips a block when crouching with an item. LP-03 retrieval must also work with full hands.
        if(event.isCanceled() || event.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND || !event.getEntity().isShiftKeyDown()
                || event.getUseBlock()==Event.Result.DENY || !event.getLevel().hasChunkAt(event.getPos())
                || !event.getLevel().mayInteract(event.getEntity(),event.getPos())
                || !(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof GlidePlatformBlock))return;
        event.setUseBlock(Event.Result.ALLOW);event.setUseItem(Event.Result.DENY);
    }
}
