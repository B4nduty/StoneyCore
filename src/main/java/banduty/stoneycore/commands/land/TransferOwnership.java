package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TransferOwnership {

    public static LiteralArgumentBuilder<ServerCommandSource> registerTransferOwnership() {
        return literal("transferownership")
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
                        .then(argument("newOwner", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    ServerWorld world = ctx.getSource().getWorld();
                                    for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                                        var uuid = player.getUuid();
                                        if (LandState.get(world).getLandByOwner(uuid).isEmpty()) {
                                            builder.suggest(player.getGameProfile().getName());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    String ownerName = StringArgumentType.getString(ctx, "owner");
                                    String newOwnerName = StringArgumentType.getString(ctx, "newOwner");

                                    UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                    if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                                    UUID newOwnerUUID = SCCommandsHandler.getUUID(src, newOwnerName);
                                    if (newOwnerUUID == null) return SCCommandsHandler.error(src, "Unknown new owner " + newOwnerName);

                                    LandState state = LandState.get(src.getWorld());

                                    Optional<Land> ownerLandOpt = state.getLandByOwner(ownerUUID);
                                    if (ownerLandOpt.isEmpty())
                                        return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                                    if (state.getLandByOwner(newOwnerUUID).isPresent())
                                        return SCCommandsHandler.error(src, newOwnerName + " already has a Land");

                                    Land land = ownerLandOpt.get();

                                    land.setOwnerUUID(newOwnerUUID);

                                    src.sendFeedback(() -> Text.literal("Land ownership transferred from " + ownerName + " to " + newOwnerName), true);
                                    return 1;
                                })
                        )
                );
    }
}
