package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Name {

    public static LiteralArgumentBuilder<CommandSourceStack> registerName() {
        return literal("name")
                .then(argument("owner", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            ServerLevel world = ctx.getSource().getLevel();
                            for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                var uuid = player.getUUID();
                                if (LandState.get(world).getLandByOwner(uuid).isPresent()) {
                                    builder.suggest(player.getGameProfile().getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .then(literal("set")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(ctx -> setName(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "owner"),
                                                StringArgumentType.getString(ctx, "name")))
                                )
                        )
                        .then(literal("remove")
                                .executes(ctx -> removeName(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "owner")))
                        )
                );
    }

    private static int setName(CommandSourceStack src, String ownerName, String newName) {
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerLevel serverLevel = src.getLevel();
        LandState state = LandState.get(serverLevel);
        Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
        if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land land = landOpt.get();
        land.setName(newName);
        src.sendSuccess(() -> Component.literal("Land name for " + ownerName + " set to '" + newName + "'"), true);
        return 1;
    }

    private static int removeName(CommandSourceStack src, String ownerName) {
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerLevel serverLevel = src.getLevel();
        LandState state = LandState.get(serverLevel);
        Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
        if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land land = landOpt.get();
        land.setName("");
        src.sendSuccess(() -> Component.literal("Land name for " + ownerName + " removed"), true);
        return 1;
    }
}
