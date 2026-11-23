package nl.smartstreamlabs.sbwdroneconfig;
import net.neoforged.bus.api.*;import net.neoforged.neoforge.event.level.*;import net.minecraft.server.level.*;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class Ps01Events{
 @SubscribeEvent(priority=EventPriority.HIGHEST)public static void propose(ExplosionEvent.Detonate e){if(e.getLevel()instanceof ServerLevel l)Ps01Protection.prepare(l,e.getExplosion(),e.getAffectedBlocks());}
 @SubscribeEvent(priority=EventPriority.LOWEST)public static void finalise(ExplosionEvent.Detonate e){if(e.getLevel()instanceof ServerLevel)Ps01Protection.finish(e.getExplosion(),e.getAffectedBlocks());}
 @SubscribeEvent(priority=EventPriority.LOW)public static void hurt(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e){e.setAmount(Ps01Protection.reduce(e.getEntity(),e.getSource(),e.getAmount()));}
 @SubscribeEvent(priority=EventPriority.LOWEST)public static void crowbar(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){var s=e.getLevel().getBlockState(e.getPos());if(s.getBlock()instanceof Ps01Block b&&e.getEntity().isShiftKeyDown()&&e.getItemStack().is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR)){e.setCanceled(true);e.setCancellationResult(e.getLevel().isClientSide?net.minecraft.world.InteractionResult.SUCCESS:b.interact(s,e.getLevel(),e.getPos(),e.getEntity(),e.getHand(),e.getHitVec()));}}
 @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)public static void denied(BlockEvent.EntityPlaceEvent e){if(e.isCanceled()&&e.getEntity()instanceof ServerPlayer p&&e.getPlacedBlock().getBlock()instanceof Ps01Block)p.displayClientMessage(net.minecraft.network.chat.Component.literal("Placement denied by server protection"),true);}
 @SubscribeEvent public static void projectile(net.neoforged.neoforge.event.entity.ProjectileImpactEvent e){if(e.getRayTraceResult()instanceof net.minecraft.world.phys.BlockHitResult hit)Ps01Compat.impact(e.getProjectile(),hit);}
 @SubscribeEvent(priority=EventPriority.HIGHEST)public static void breakBlock(BlockEvent.BreakEvent e){if(!Ps01Compat.CHECKING.get()&&e.getState().getBlock()instanceof Ps01Block&&e.getLevel()instanceof ServerLevel l){var b=Ps01Block.main(l,e.getPos(),e.getState());if(b!=null&&!b.permitted(e.getPlayer()))e.setCanceled(true);}}
 @SubscribeEvent public static void loaded(ChunkEvent.Load e){if(e.getLevel()instanceof ServerLevel l){long key=e.getChunk().getPos().toLong();l.getServer().tell(new net.minecraft.server.TickTask(l.getServer().getTickCount()+1,()->Ps01WorldData.get(l).chunk(l,key)));}}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){Ps01Protection.tick();}
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){Ps01Protection.reset();}
}
