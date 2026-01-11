package banduty.stoneycore.commands;

import banduty.stoneycore.commands.land.*;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class FabricSCCommandsHandler implements CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection env) {
        dispatcher.register(
                Commands.literal("land")
                        .then(Ally.registerAlly())
                        .then(Create.registerCreate())
                        .then(Remove.registerRemove())
                        .then(Radius.registerRadius())
                        .then(Name.registerName())
                        .then(TransferOwnership.registerTransferOwnership())
                        .then(SiegeCommand.registerSiege())
        );
    }
}