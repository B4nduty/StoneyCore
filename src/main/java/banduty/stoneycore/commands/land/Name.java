package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Name {

    public static LiteralArgumentBuilder<ServerCommandSource> registerName() {
        return literal("name")
                .then(argument("owner", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            ServerWorld world = ctx.getSource().getWorld();
                            for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                                var uuid = player.getUuid();
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

    private static int setName(ServerCommandSource src, String ownerName, String newName) {
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerWorld world = src.getWorld();
        LandState state = LandState.get(world);
        Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
        if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land land = landOpt.get();
        land.setName(newName);
        src.sendFeedback(() -> Text.literal("Land name for " + ownerName + " set to '" + newName + "'"), true);
        return 1;
    }

    private static int removeName(ServerCommandSource src, String ownerName) {
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerWorld world = src.getWorld();
        LandState state = LandState.get(world);
        Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
        if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land land = landOpt.get();
        land.setName("");
        src.sendFeedback(() -> Text.literal("Land name for " + ownerName + " removed"), true);
        return 1;
    }
}
