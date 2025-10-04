package banduty.stoneycore.commands;

import banduty.stoneycore.commands.land.*;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class SCCommandsHandler implements CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment env) {
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

    public static UUID getUUID(ServerCommandSource src, String name) {
        ServerPlayerEntity player = src.getServer().getPlayerManager().getPlayer(name);
        return player != null ? player.getUuid() : null;
    }

    public static int error(ServerCommandSource src, String msg) {
        src.sendError(Text.literal(msg));
        return 0;
    }
}