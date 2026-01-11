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

public class TransferOwnership {

    public static LiteralArgumentBuilder<CommandSourceStack> registerTransferOwnership() {
        return literal("transferownership")
                .then(argument("owner", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            ServerLevel serverLevel = ctx.getSource().getLevel();
                            for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                var uuid = player.getUUID();
                                if (LandState.get(serverLevel).getLandByOwner(uuid).isPresent()) {
                                    builder.suggest(player.getGameProfile().getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .then(argument("newOwner", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    ServerLevel world = ctx.getSource().getLevel();
                                    for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                        var uuid = player.getUUID();
                                        if (LandState.get(world).getLandByOwner(uuid).isEmpty()) {
                                            builder.suggest(player.getGameProfile().getName());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    String ownerName = StringArgumentType.getString(ctx, "owner");
                                    String newOwnerName = StringArgumentType.getString(ctx, "newOwner");

                                    UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                    if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                                    UUID newOwnerUUID = SCCommandsHandler.getUUID(src, newOwnerName);
                                    if (newOwnerUUID == null) return SCCommandsHandler.error(src, "Unknown new owner " + newOwnerName);

                                    LandState state = LandState.get(src.getLevel());

                                    Optional<Land> ownerLandOpt = state.getLandByOwner(ownerUUID);
                                    if (ownerLandOpt.isEmpty())
                                        return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                                    if (state.getLandByOwner(newOwnerUUID).isPresent())
                                        return SCCommandsHandler.error(src, newOwnerName + " already has a Land");

                                    Land land = ownerLandOpt.get();

                                    land.setOwnerUUID(newOwnerUUID);

                                    src.sendSuccess(() -> Component.literal("Land ownership transferred from " + ownerName + " to " + newOwnerName), true);
                                    return 1;
                                })
                        )
                );
    }
}
