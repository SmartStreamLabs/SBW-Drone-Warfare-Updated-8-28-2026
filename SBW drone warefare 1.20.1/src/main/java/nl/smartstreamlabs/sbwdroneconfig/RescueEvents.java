package nl.smartstreamlabs.sbwdroneconfig;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class RescueEvents {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void joints(net.minecraftforge.event.TickEvent.ServerTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.END)RescueCoupling.step();}

 @net.minecraftforge.eventbus.api.SubscribeEvent public static void traveling(net.minecraftforge.event.entity.EntityTravelToDimensionEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.clear(p);else if(e.getEntity() instanceof RescueVehicle v&&!v.unit().level().isClientSide)RescueCoupling.detach(v);}

 @net.minecraftforge.eventbus.api.SubscribeEvent public static void playerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.END&&e.player instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.releasedSneak(p);}

 @net.minecraftforge.eventbus.api.SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.clear(p);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void dimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.clear(p);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void died(net.minecraftforge.event.entity.living.LivingDeathEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.clear(p);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent e){for(var p:e.getServer().getPlayerList().getPlayers())RescueCoupling.clear(p);RescueCoupling.clearAll();}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void mount(net.minecraftforge.event.entity.EntityMountEvent e){if(e.isDismounting()&&e.getEntityMounting() instanceof net.minecraft.server.level.ServerPlayer p&&e.getEntityBeingMounted() instanceof MedicalTrailerEntity t&&!t.forcedExit&&t.isAlive()&&!t.isRemoved()&&p.isAlive()&&!p.hasDisconnected()&&t.safeExit(p).isEmpty()){e.setCanceled(true);p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(t));p.displayClientMessage(net.minecraft.network.chat.Component.literal("No safe exit beside the stretcher. Move to open solid ground."),true);}}
}
