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

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Ally {
    public static LiteralArgumentBuilder<CommandSourceStack> registerAlly() {
        return literal("ally")
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
                        .then(literal("add")
                                .then(argument("newAlly", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            CommandSourceStack src = ctx.getSource();
                                            ServerLevel serverLevel = src.getLevel();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                            if (ownerUUID != null) {
                                                var ownerLandOpt = LandState.get(serverLevel).getLandByOwner(ownerUUID);
                                                if (ownerLandOpt.isPresent()) {
                                                    Land ownerLand = ownerLandOpt.get();
                                                    for (ServerPlayer player : src.getServer().getPlayerList().getPlayers()) {
                                                        var uuid = player.getUUID();
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
                                            CommandSourceStack src = ctx.getSource();
                                            ServerLevel serverLevel = src.getLevel();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);

                                            if (ownerUUID != null) {
                                                var ownerLandOpt = LandState.get(serverLevel).getLandByOwner(ownerUUID);
                                                if (ownerLandOpt.isPresent()) {
                                                    Land ownerLand = ownerLandOpt.get();
                                                    for (var allyUUID : ownerLand.getAllies()) {
                                                        var allyPlayer = src.getServer().getPlayerList().getPlayer(allyUUID);
                                                        if (allyPlayer != null) builder.suggest(allyPlayer.getGameProfile().getName());
                                                    }
                                                }
                                            }

                                            return builder.buildFuture();
                                        })
                                        .executes(ctx ->
                                                removeAlly(ctx.getSource(), StringArgumentType.getString(ctx, "owner"), StringArgumentType.getString(ctx, "ally"))
                                        )
                                )
                        )
                        .then(literal("get")
                                .executes(ctx ->
                                    getAlly(ctx.getSource(), StringArgumentType.getString(ctx, "owner")))
                        )
                );
    }

    private static int addAlly(CommandSourceStack src, String ownerName, String newAllyName) {
        var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        var newAllyUUID = SCCommandsHandler.getUUID(src, newAllyName);

        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);
        if (newAllyUUID == null) return SCCommandsHandler.error(src, "Unknown player " + newAllyName);

        ServerLevel serverLevel = src.getLevel();
        LandState state = LandState.get(serverLevel);
        var ownerLandOpt = state.getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        if (state.getLandByPlayer(newAllyUUID).isPresent())
            return SCCommandsHandler.error(src, newAllyName + " already is part of a Land");

        Land ownerLand = ownerLandOpt.get();
        if (ownerLand.getMaxAllies() > 0 && ownerLand.getAllies().size() >= ownerLand.getMaxAllies())
            return SCCommandsHandler.error(src, ownerName + " Land has reached the max amount of allies");

        ownerLand.addAlly(newAllyUUID);
        src.sendSuccess(() -> Component.literal(newAllyName + " is now an ally of " + ownerName), true);
        return 1;
    }

    private static int removeAlly(CommandSourceStack src, String ownerName, String allyName) {
        var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        var oldAllyUUID = SCCommandsHandler.getUUID(src, allyName);

        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);
        if (oldAllyUUID == null) return SCCommandsHandler.error(src, "Unknown player " + allyName);

        ServerLevel serverLevel = src.getLevel();
        LandState state = LandState.get(serverLevel);
        var ownerLandOpt = state.getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();
        if (!ownerLand.getAllies().contains(oldAllyUUID))
            return SCCommandsHandler.error(src, allyName + " isn’t part of this Land");

        ownerLand.removeAlly(oldAllyUUID);
        src.sendSuccess(() -> Component.literal(allyName + " was removed from " + ownerName + "'s allies"), true);
        return 1;
    }

    private static int getAlly(CommandSourceStack src, String ownerName) {
        var ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerLevel serverLevel = src.getLevel();
        var ownerLandOpt = LandState.get(serverLevel).getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();
        if (ownerLand.getAllies().isEmpty()) {
            src.sendSuccess(() -> Component.literal(ownerName + " has no allies"), false);
        } else {
            StringBuilder alliesList = new StringBuilder();
            for (var allyUUID : ownerLand.getAllies()) {
                var allyPlayer = src.getServer().getPlayerList().getPlayer(allyUUID);
                String allyName = allyPlayer != null ? allyPlayer.getGameProfile().getName() : allyUUID.toString();
                if (!alliesList.isEmpty()) alliesList.append(", ");
                alliesList.append(allyName);
            }
            src.sendSuccess(() -> Component.literal(ownerName + "'s allies: " + alliesList), false);
        }
        return 1;
    }
}
