package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;

public final class FiberOpticDebugCommand {
    private FiberOpticDebugCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sbwdrone")
                        .then(Commands.literal("playsoundtest")
                                .executes(FiberOpticDebugCommand::runPlaySoundTest))
                        .then(Commands.literal("fiberdebug")
                                .executes(FiberOpticDebugCommand::runFiberDebug))
        );
    }

    private static int runPlaySoundTest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            source.sendFailure(Component.literal("playsoundtest can only be used by a player."));
            return 0;
        }

        source.getLevel().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                AddonSounds.LUCAS_DRONE_ENGINE.get(),
                SoundSource.AMBIENT,
                2.5F,
                1.0F
        );
        source.sendSuccess(() -> Component.literal("Played LUCAS engine sound test at your location."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int runFiberDebug(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            source.sendFailure(Component.literal("Fiber debug can only be used by a player."));
            return 0;
        }

        Entity drone = findRelevantDrone(player);
        if (drone == null) {
            source.sendFailure(Component.literal("No linked drone found for fiberdebug."));
            return 0;
        }

        boolean spoolModuleFound = DroneModuleSystem.hasFiberOpticSpoolUpgrade(drone);
        DroneLinkMode linkMode = FiberOpticLinkSystem.getLinkMode(drone);
        String anchorPos = formatVec3(
                FiberOpticLinkSystem.getAnchorX(drone),
                FiberOpticLinkSystem.getAnchorY(drone),
                FiberOpticLinkSystem.getAnchorZ(drone)
        );
        String dronePos = formatVec3(drone.getX(), drone.getY(), drone.getZ());
        double currentLength = FiberOpticLinkSystem.getCurrentCableLength(drone);
        int activeSegments = FiberOpticLinkSystem.getTrackedSegmentCount(drone);
        long lastClientSyncTick = FiberOpticLinkSystem.getLastClientSyncTick(drone);

        source.sendSuccess(() -> Component.literal("server linkMode: " + linkMode), false);
        source.sendSuccess(() -> Component.literal("spool module found: " + spoolModuleFound), false);
        source.sendSuccess(() -> Component.literal("anchorPos: " + anchorPos), false);
        source.sendSuccess(() -> Component.literal("dronePos: " + dronePos), false);
        source.sendSuccess(() -> Component.literal("currentLength: " + formatDouble(currentLength)), false);
        source.sendSuccess(() -> Component.literal("active cable segments: " + activeSegments), false);
        source.sendSuccess(() -> Component.literal("client sync last sent: tick " + lastClientSyncTick), false);

        SbwDroneRangeConfig.LOGGER.info(
                "fiberdebug player={} drone={} linkMode={} spoolModuleFound={} anchorPos={} dronePos={} currentLength={} activeSegments={} clientSyncLastSent={}",
                player.getGameProfile().getName(),
                drone.getUUID(),
                linkMode,
                spoolModuleFound,
                anchorPos,
                dronePos,
                formatDouble(currentLength),
                activeSegments,
                lastClientSyncTick
        );
        return Command.SINGLE_SUCCESS;
    }

    private static Entity findRelevantDrone(ServerPlayer player) {
        Entity activeDrone = SbwCompat.findActiveLinkedDrone(player);
        if (activeDrone != null) {
            return activeDrone;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (SbwCompat.isLinkedMonitor(mainHand)) {
            Entity linkedMainHandDrone = SbwCompat.findLinkedDrone(player, mainHand);
            if (linkedMainHandDrone != null) {
                return linkedMainHandDrone;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (SbwCompat.isLinkedMonitor(offhand)) {
            return SbwCompat.findLinkedDrone(player, offhand);
        }

        return null;
    }

    private static String formatVec3(double x, double y, double z) {
        return "[" + formatDouble(x) + ", " + formatDouble(y) + ", " + formatDouble(z) + "]";
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
