package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.UUID;

public final class BayraktarTb2Command {
    private static final double NEAREST_OWNED_DRONE_RANGE = 128.0D;

    private BayraktarTb2Command() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sbw")
                        .then(Commands.literal("bayraktar")
                                .then(Commands.literal("waypoint")
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                                        .then(Commands.argument("y", IntegerArgumentType.integer())
                                                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                        .executes(BayraktarTb2Command::setWaypoint)))))
                                        .then(Commands.literal("clear")
                                                .executes(BayraktarTb2Command::clearWaypoint))
                                        .then(Commands.literal("show")
                                                .executes(BayraktarTb2Command::showWaypoint))))
        );
    }

    private static int setWaypoint(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = playerOrFail(context);
        if (player == null) {
            return 0;
        }

        BayraktarTb2Entity drone = findBayraktar(player);
        if (drone == null) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.bayraktar_no_drone"), true);
            return 0;
        }

        BlockPos waypoint = new BlockPos(
                IntegerArgumentType.getInteger(context, "x"),
                IntegerArgumentType.getInteger(context, "y"),
                IntegerArgumentType.getInteger(context, "z")
        );
        drone.setWaypoint(waypoint);
        player.displayClientMessage(Component.translatable(
                "message.sbwdroneconfig.bayraktar_waypoint_set",
                waypoint.getX(),
                waypoint.getY(),
                waypoint.getZ()
        ), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearWaypoint(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = playerOrFail(context);
        if (player == null) {
            return 0;
        }

        BayraktarTb2Entity drone = findBayraktar(player);
        if (drone == null) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.bayraktar_no_drone"), true);
            return 0;
        }

        drone.clearWaypoint();
        player.displayClientMessage(Component.translatable("message.sbwdroneconfig.bayraktar_waypoint_cleared"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int showWaypoint(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = playerOrFail(context);
        if (player == null) {
            return 0;
        }

        BayraktarTb2Entity drone = findBayraktar(player);
        if (drone == null) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.bayraktar_no_drone"), true);
            return 0;
        }

        BlockPos waypoint = drone.getWaypoint();
        Component message = waypoint == null
                ? Component.translatable("message.sbwdroneconfig.bayraktar_waypoint_none")
                : Component.translatable("message.sbwdroneconfig.bayraktar_waypoint_status", waypoint.getX(), waypoint.getY(), waypoint.getZ());
        player.displayClientMessage(message, true);
        return Command.SINGLE_SUCCESS;
    }

    private static ServerPlayer playerOrFail(CommandContext<CommandSourceStack> context) {
        try {
            return context.getSource().getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            context.getSource().sendFailure(Component.literal("Bayraktar commands can only be used by a player."));
            return null;
        }
    }

    private static BayraktarTb2Entity findBayraktar(ServerPlayer player) {
        Entity active = SbwCompat.findActiveLinkedDrone(player);
        if (active instanceof BayraktarTb2Entity bayraktar) {
            return bayraktar;
        }

        Entity linked = SbwCompat.findLinkedDrone(player, SbwCompat.getLinkedMonitor(player));
        if (linked instanceof BayraktarTb2Entity bayraktar) {
            return bayraktar;
        }

        UUID owner = player.getUUID();
        AABB searchBox = player.getBoundingBox().inflate(NEAREST_OWNED_DRONE_RANGE);
        return player.serverLevel()
                .getEntitiesOfClass(BayraktarTb2Entity.class, searchBox, drone -> owner.equals(SbwCompat.getStoredDroneOwnerUuid(drone)))
                .stream()
                .min(Comparator.comparingDouble(drone -> drone.distanceToSqr(player)))
                .orElse(null);
    }
}
