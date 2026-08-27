package banduty.stoneycore.commands;

import banduty.stoneycore.commands.land.*;
import banduty.stoneycore.stamina.StaminaCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SCCommandsHandler {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("land")
                        .then(Ally.registerAlly())
                        .then(Create.registerCreate())
                        .then(Remove.registerRemove())
                        .then(Radius.registerRadius())
                        .then(Name.registerName())
                        .then(TransferOwnership.registerTransferOwnership())
                        .then(SiegeCommand.registerSiege())
                        .then(Title.registerTitle())
        );

        StaminaCommand.register(dispatcher);
    }

    public static UUID getUUID(CommandSourceStack src, String name) {
        ServerPlayer player = src.getServer().getPlayerList().getPlayerByName(name);
        return player != null ? player.getUUID() : null;
    }

    public static int error(CommandSourceStack src, String msg) {
        src.sendFailure(Component.literal(msg));
        return 0;
    }
}