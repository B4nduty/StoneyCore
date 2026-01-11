package banduty.stoneycore.commands;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.commands.land.*;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeSCCommandsHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

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