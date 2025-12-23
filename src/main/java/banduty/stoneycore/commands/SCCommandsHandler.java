package banduty.stoneycore.commands;

import banduty.stoneycore.commands.land.*;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

public class SCCommandsHandler implements CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection env) {
        dispatcher.register(
                literal("land")
                        .then(Create.registerCreate())
                        .then(Remove.registerRemove())
                        .then(Radius.registerRadius())
                        .then(Ally.registerAlly())
                        .then(Name.registerName())
                        .then(TransferOwnership.registerTransferOwnership())
                        .then(SiegeCommand.registerSiege())
        );
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