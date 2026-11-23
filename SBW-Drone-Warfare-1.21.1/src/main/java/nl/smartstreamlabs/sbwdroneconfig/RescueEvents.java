package nl.smartstreamlabs.sbwdroneconfig;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class RescueEvents {
 @net.neoforged.bus.api.SubscribeEvent public static void joints(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){RescueCoupling.step();}

 @net.neoforged.bus.api.SubscribeEvent public static void traveling(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.clear(p);else if(e.getEntity() instanceof RescueVehicle v&&!v.unit().level().isClientSide)RescueCoupling.detach(v);}

 @net.neoforged.bus.api.SubscribeEvent public static void playerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.releasedSneak(p);}

 @net.neoforged.bus.api.SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.clear(p);}
 @net.neoforged.bus.api.SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.clear(p);}
 @net.neoforged.bus.api.SubscribeEvent public static void died(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)RescueCoupling.clear(p);}
 @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){for(var p:e.getServer().getPlayerList().getPlayers())RescueCoupling.clear(p);RescueCoupling.clearAll();}
 @net.neoforged.bus.api.SubscribeEvent public static void mount(net.neoforged.neoforge.event.entity.EntityMountEvent e){if(e.isDismounting()&&e.getEntityMounting() instanceof net.minecraft.server.level.ServerPlayer p&&e.getEntityBeingMounted() instanceof MedicalTrailerEntity t&&!t.forcedExit&&t.isAlive()&&!t.isRemoved()&&p.isAlive()&&!p.hasDisconnected()&&t.safeExit(p).isEmpty()){e.setCanceled(true);p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(t));p.displayClientMessage(net.minecraft.network.chat.Component.literal("No safe exit beside the stretcher. Move to open solid ground."),true);}}
}
