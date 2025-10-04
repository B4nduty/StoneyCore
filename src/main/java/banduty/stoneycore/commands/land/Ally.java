package banduty.stoneycore.commands.land;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Ally {
    public static LiteralArgumentBuilder<ServerCommandSource> registerAlly() {
        return literal("ally")
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
                        .then(literal("add")
                                .then(argument("newAlly", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerWorld world = src.getWorld();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                            if (ownerUUID != null) {
                                                var ownerLandOpt = LandState.get(world).getLandByOwner(ownerUUID);
                                                if (ownerLandOpt.isPresent()) {
                                                    Land ownerLand = ownerLandOpt.get();
                                                    for (ServerPlayerEntity player : src.getServer().getPlayerManager().getPlayerList()) {
                                                        var uuid = player.getUuid();
                                                        if (!uuid.equals(ownerUUID) && !ownerLand.isAlly(uuid))
                                                            builder.suggest(player.getGameProfile().getName());
                                                    }
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx ->
                                                addAlly(ctx.getSource(), StringArgumentType.getString(ctx, "owner"), StringArgumentType.getString(ctx, "newAlly")))
                                )
                        )
                        .then(literal("remove")
                                .then(argument("ally", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerWorld world = src.getWorld();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);

                                            if (ownerUUID != null) {
                                                var ownerLandOpt = LandState.get(world).getLandByOwner(ownerUUID);
                                                if (ownerLandOpt.isPresent()) {
                                                    Land ownerLand = ownerLandOpt.get();
                                                    for (var allyUUID : ownerLand.getAllies()) {
                                                        var allyPlayer = src.getServer().getPlayerManager().getPlayer(allyUUID);
                                                        if (allyPlayer != null) builder.suggest(allyPlayer.getGameProfile().getName());
                                                    }
                                                }
                                            }

                                            return builder.buildFuture();
                                        })
                                        .executes(ctx ->
                                                removeAlly(ctx.getSource(), StringArgumentType.getString(ctx, "owner"), StringArgumentType.getString(ctx, "ally")))
                                )
                        )
                        .then(literal("get")
                                .executes(ctx ->
                                    getAlly(ctx.getSource(), StringArgumentType.getString(ctx, "owner")))
                        )
                );
    }

    private static int addAlly(ServerCommandSource src, String ownerName, String newAllyName) {
        var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        var newAllyUUID = SCCommandsHandler.getUUID(src, newAllyName);

        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);
        if (newAllyUUID == null) return SCCommandsHandler.error(src, "Unknown player " + newAllyName);

        ServerWorld world = src.getWorld();
        LandState state = LandState.get(world);
        var ownerLandOpt = state.getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        if (state.getLandByPlayer(newAllyUUID).isPresent())
            return SCCommandsHandler.error(src, newAllyName + " already is part of a Land");

        Land ownerLand = ownerLandOpt.get();
        if (ownerLand.getMaxAllies() > 0 && ownerLand.getAllies().size() >= ownerLand.getMaxAllies())
            return SCCommandsHandler.error(src, ownerName + " Land has reached the max amount of allies");

        ownerLand.addAlly(newAllyUUID);
        src.sendFeedback(() -> Text.literal(newAllyName + " is now an ally of " + ownerName), true);
        return 1;
    }

    private static int removeAlly(ServerCommandSource src, String ownerName, String allyName) {
        var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        var oldAllyUUID = SCCommandsHandler.getUUID(src, allyName);

        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);
        if (oldAllyUUID == null) return SCCommandsHandler.error(src, "Unknown player " + allyName);

        ServerWorld world = src.getWorld();
        LandState state = LandState.get(world);
        var ownerLandOpt = state.getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();
        if (!ownerLand.getAllies().contains(oldAllyUUID))
            return SCCommandsHandler.error(src, allyName + " isn’t part of this Land");

        ownerLand.removeAlly(oldAllyUUID);
        src.sendFeedback(() -> Text.literal(allyName + " was removed from " + ownerName + "'s allies"), true);
        return 1;
    }

    private static int getAlly(ServerCommandSource src, String ownerName) {
        var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerWorld world = src.getWorld();
        var ownerLandOpt = LandState.get(world).getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();
        if (ownerLand.getAllies().isEmpty()) {
            src.sendFeedback(() -> Text.literal(ownerName + " has no allies"), false);
        } else {
            StringBuilder alliesList = new StringBuilder();
            for (var allyUUID : ownerLand.getAllies()) {
                var allyPlayer = src.getServer().getPlayerManager().getPlayer(allyUUID);
                String allyName = allyPlayer != null ? allyPlayer.getGameProfile().getName() : allyUUID.toString();
                if (!alliesList.isEmpty()) alliesList.append(", ");
                alliesList.append(allyName);
            }
            src.sendFeedback(() -> Text.literal(ownerName + "'s allies: " + alliesList), false);
        }
        return 1;
    }
}
