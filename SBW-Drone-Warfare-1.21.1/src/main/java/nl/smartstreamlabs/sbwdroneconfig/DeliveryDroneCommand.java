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

public final class DeliveryDroneCommand {
    private static final double NEAREST_OWNED_DRONE_RANGE = 64.0D;

    private DeliveryDroneCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sbw")
                        .then(Commands.literal("delivery")
                                .then(Commands.literal("target")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                .executes(DeliveryDroneCommand::setTarget)))))
                                .then(Commands.literal("clear")
                                        .executes(DeliveryDroneCommand::clearTarget))
                                .then(Commands.literal("status")
                                        .executes(DeliveryDroneCommand::status)))
        );
    }

    private static int setTarget(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = playerOrFail(context);
        if (player == null) {
            return 0;
        }

        DeliveryDroneEntity drone = findDeliveryDrone(player);
        if (drone == null) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_no_drone"), true);
            return 0;
        }

        BlockPos target = new BlockPos(
                IntegerArgumentType.getInteger(context, "x"),
                IntegerArgumentType.getInteger(context, "y"),
                IntegerArgumentType.getInteger(context, "z")
        );
        drone.setDeliveryTarget(target);
        player.displayClientMessage(Component.translatable(
                "message.sbwdroneconfig.delivery_target_set",
                target.getX(),
                target.getY(),
                target.getZ()
        ), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearTarget(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = playerOrFail(context);
        if (player == null) {
            return 0;
        }

        DeliveryDroneEntity drone = findDeliveryDrone(player);
        if (drone == null) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_no_drone"), true);
            return 0;
        }

        drone.clearDeliveryTarget();
        player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_target_cleared"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = playerOrFail(context);
        if (player == null) {
            return 0;
        }

        DeliveryDroneEntity drone = findDeliveryDrone(player);
        if (drone == null) {
            player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_no_drone"), true);
            return 0;
        }

        BlockPos target = drone.getDeliveryTarget();
        Component message = target == null
                ? Component.translatable("message.sbwdroneconfig.delivery_status_idle")
                : Component.translatable("message.sbwdroneconfig.delivery_status_target", target.getX(), target.getY(), target.getZ());
        player.displayClientMessage(message, true);
        return Command.SINGLE_SUCCESS;
    }

    private static ServerPlayer playerOrFail(CommandContext<CommandSourceStack> context) {
        try {
            return context.getSource().getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            context.getSource().sendFailure(Component.literal("Delivery drone commands can only be used by a player."));
            return null;
        }
    }

    private static DeliveryDroneEntity findDeliveryDrone(ServerPlayer player) {
        Entity active = SbwCompat.findActiveLinkedDrone(player);
        if (active instanceof DeliveryDroneEntity deliveryDrone) {
            return deliveryDrone;
        }

        Entity linked = SbwCompat.findLinkedDrone(player, SbwCompat.getLinkedMonitor(player));
        if (linked instanceof DeliveryDroneEntity deliveryDrone) {
            return deliveryDrone;
        }

        UUID owner = player.getUUID();
        AABB searchBox = player.getBoundingBox().inflate(NEAREST_OWNED_DRONE_RANGE);
        return player.serverLevel()
                .getEntitiesOfClass(DeliveryDroneEntity.class, searchBox, drone -> owner.equals(SbwCompat.getStoredDroneOwnerUuid(drone)))
                .stream()
                .min(Comparator.comparingDouble(drone -> drone.distanceToSqr(player)))
                .orElse(null);
    }
}
