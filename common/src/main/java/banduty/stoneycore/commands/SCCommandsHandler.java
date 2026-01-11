package banduty.stoneycore.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SCCommandsHandler {
    public static UUID getUUID(CommandSourceStack src, String name) {
        ServerPlayer player = src.getServer().getPlayerList().getPlayerByName(name);
        return player != null ? player.getUUID() : null;
    }

    public static int error(CommandSourceStack src, String msg) {
        src.sendFailure(Component.literal(msg));
        return 0;
    }
}