package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.siege.SiegeManager;
import banduty.stoneycore.siege.SiegeManager.Siege;
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

public class SiegeCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> registerSiege() {
        return literal("siege")
                .then(literal("start")
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
                                .then(argument("target", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
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
                                    ServerLevel world = ctx.getSource().getLevel();
                                    for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                        var uuid = player.getUUID();
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

    private static int start(CommandSourceStack src, String ownerName, String targetName) {
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerLevel world = src.getLevel();
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
        src.sendSuccess(() -> Component.literal("Siege started by " + ownerName + " against " + targetName), true);
        return 1;
    }

    private static int cancel(CommandSourceStack src, String ownerName) {
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerLevel world = src.getLevel();
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
            ServerPlayer player = world.getServer().getPlayerList().getPlayer(participant);
            if (player != null) Services.PLATFORM.sendTitle(player, Component.literal("The Siege has been Cancelled"));
        }
        for (UUID participant : siege.defenders) {
            ServerPlayer player = world.getServer().getPlayerList().getPlayer(participant);
            if (player != null) Services.PLATFORM.sendTitle(player, Component.literal("The Siege has been Cancelled"));
        }

        SiegeManager.getSiegesForLevel(world).remove(siege);
        siege.clearBossEvents();

        src.sendSuccess(() -> Component.literal("Siege involving " + ownerName + " has been cancelled"), true);
        return 1;
    }
}
