package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.platform.Services;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

public class Radius {
    public static LiteralArgumentBuilder<CommandSourceStack> registerRadius() {
        return literal("radius")
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
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();
                            ServerLevel serverLevel = src.getLevel();
                            String ownerName = StringArgumentType.getString(ctx, "owner");

                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                            LandState state = LandState.get(serverLevel);
                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                            Land land = landOpt.get();
                            src.sendSuccess(() -> Component.literal(ownerName + "’s Land is " + land.getRadius() + " blocks"), false);
                            return 1;
                        })
                        .then(literal("set")
                                .then(argument("radius", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            CommandSourceStack src = ctx.getSource();
                                            ServerLevel serverLevel = src.getLevel();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            int radius = IntegerArgumentType.getInteger(ctx, "radius");

                                            if (radius <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                                            LandState state = LandState.get(serverLevel);
                                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                                            Land oldLand = landOpt.get();
                                            int current = oldLand.getRadius();

                                            if (current == radius) {
                                                src.sendSuccess(() -> Component.literal("Radius already set to " + radius), false);
                                                return 0;
                                            }

                                            Land land = oldLand.copy();
                                            land.initializeClaim(serverLevel, radius, Services.PLATFORM.getClaimTasks());

                                            state.removeLand(oldLand);
                                            state.addLand(land);

                                            state.setDirty();
                                            src.sendSuccess(() -> Component.literal("Radius set to " + radius + " for " + ownerName), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("add")
                                .then(argument("radius", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            CommandSourceStack src = ctx.getSource();
                                            ServerLevel serverLevel = src.getLevel();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            int toAdd = IntegerArgumentType.getInteger(ctx, "radius");
                                            if (toAdd <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                                            LandState state = LandState.get(serverLevel);
                                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                                            Land land = landOpt.get().copy();

                                            int newRadius = land.getRadius() + toAdd;
                                            if (newRadius <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            land.initializeClaim(serverLevel, newRadius, Services.PLATFORM.getClaimTasks());

                                            state.removeLand(landOpt.get());
                                            state.addLand(land);

                                            state.setDirty();

                                            src.sendSuccess(() -> Component.literal("Radius increased to " + newRadius + " for " + ownerName), true);
                                            return 1;

                                        })
                                )
                        )
                        .then(literal("remove")
                                .then(argument("radius", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            CommandSourceStack src = ctx.getSource();
                                            ServerLevel serverLevel = src.getLevel();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            int toRemove = IntegerArgumentType.getInteger(ctx, "radius");

                                            if (toRemove <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                                            LandState state = LandState.get(serverLevel);
                                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                                            Land land = landOpt.get();
                                            int newRadius = land.getRadius() - toRemove;
                                            if (newRadius <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            land.setRadius(newRadius, serverLevel);
                                            state.setDirty();

                                            src.sendSuccess(() -> Component.literal("Radius decreased to " + newRadius + " for " + ownerName), true);
                                            return 1;
                                        })
                                )
                        )
                );
    }
}