package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class NetBulkPlacementCommand {
    private NetBulkPlacementCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sbw")
                        .then(Commands.literal("net")
                                .then(Commands.literal("undo")
                                        .executes(NetBulkPlacementCommand::runUndo)))
        );
    }

    private static int runUndo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            source.sendFailure(Component.literal("Net undo can only be used by a player."));
            return 0;
        }

        NetBulkPlacementServer.undoLast(player);
        return Command.SINGLE_SUCCESS;
    }
}
