package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.siege.SiegeManager.Siege;
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

public class SiegeCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> registerSiege() {
        return literal("siege")
                .then(literal("start")
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
                                .then(argument("target", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (ServerPlayerEntity player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                                                builder.suggest(player.getGameProfile().getName());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx ->
                                                start(ctx.getSource(), StringArgumentType.getString(ctx, "owner"), StringArgumentType.getString(ctx, "target")))
                                )
                        )
                )
                .then(literal("cancel")
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
                                .executes(ctx ->
                                        cancel(ctx.getSource(), StringArgumentType.getString(ctx, "owner")))
                        )
                );
    }

    private static int start(ServerCommandSource src, String ownerName, String targetName) {
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerWorld world = src.getWorld();
        UUID targetUUID = SCCommandsHandler.getUUID(src, targetName);
        if (targetUUID == null) return SCCommandsHandler.error(src, "Unknown target " + targetName);

        LandState state = LandState.get(world);
        Optional<Land> ownerLandOpt = state.getLandByOwner(ownerUUID);
        Optional<Land> targetLandOpt = state.getLandByOwner(targetUUID);

        if (ownerLandOpt.isEmpty())
            return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");
        if (targetLandOpt.isEmpty())
            return SCCommandsHandler.error(src, targetName + " doesn’t own a Land");

        SiegeManager.startSiege(world, ownerUUID, targetUUID);
        src.sendFeedback(() -> Text.literal("Siege started by " + ownerName + " against " + targetName), true);
        return 1;
    }

    private static int cancel(ServerCommandSource src, String ownerName) {
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerWorld world = src.getWorld();
        LandState state = LandState.get(world);
        Optional<Land> ownerLandOpt = state.getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty())
            return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();

        Optional<Siege> siegeOpt = SiegeManager.getPlayerSiege(world, ownerUUID);
        if (siegeOpt.isEmpty())
            return SCCommandsHandler.error(src, ownerLand.getName().isBlank() ? ownerName + " isn’t in a siege" : ownerLand.getName() + " isn’t in a siege");

        Siege siege = siegeOpt.get();

        for (UUID participant : siege.attackers) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(participant);
            if (player != null) SiegeManager.sendTitle(player, Text.literal("The Siege has been Cancelled"));
        }
        for (UUID participant : siege.defenders) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(participant);
            if (player != null) SiegeManager.sendTitle(player, Text.literal("The Siege has been Cancelled"));
        }

        SiegeManager.getSiegesForWorld(world).remove(siege);
        siege.clearBossBars();

        src.sendFeedback(() -> Text.literal("Siege involving " + ownerName + " has been cancelled"), true);
        return 1;
    }
}
