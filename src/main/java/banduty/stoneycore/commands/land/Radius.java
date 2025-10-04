package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.event.StartTickHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Radius {
    public static LiteralArgumentBuilder<ServerCommandSource> registerRadius() {
        return literal("radius")
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
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            ServerWorld world = src.getWorld();
                            String ownerName = StringArgumentType.getString(ctx, "owner");

                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                            LandState state = LandState.get(world);
                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                            Land land = landOpt.get();
                            src.sendFeedback(() -> Text.literal(ownerName + "’s Land is " + land.getRadius() + " blocks"), false);
                            return 1;
                        })
                        .then(literal("set")
                                .then(argument("radius", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerWorld world = src.getWorld();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            int radius = IntegerArgumentType.getInteger(ctx, "radius");

                                            if (radius <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                                            LandState state = LandState.get(world);
                                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                                            Land oldLand = landOpt.get();
                                            int current = oldLand.getRadius();

                                            if (current == radius) {
                                                src.sendFeedback(() -> Text.literal("Radius already set to " + radius), false);
                                                return 0;
                                            }

                                            Land land = oldLand.copy();
                                            land.initializeClaim(world, radius, StartTickHandler.CLAIM_TASKS);

                                            state.removeLand(oldLand);
                                            state.addLand(land);

                                            state.markDirty();
                                            src.sendFeedback(() -> Text.literal("Radius set to " + radius + " for " + ownerName), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("add")
                                .then(argument("radius", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerWorld world = src.getWorld();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            int toAdd = IntegerArgumentType.getInteger(ctx, "radius");
                                            if (toAdd <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                                            LandState state = LandState.get(world);
                                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                                            Land land = landOpt.get().copy();

                                            int newRadius = land.getRadius() + toAdd;
                                            if (newRadius <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            land.initializeClaim(world, newRadius, StartTickHandler.CLAIM_TASKS);

                                            state.removeLand(landOpt.get());
                                            state.addLand(land);

                                            state.markDirty();

                                            src.sendFeedback(() -> Text.literal("Radius increased to " + newRadius + " for " + ownerName), true);
                                            return 1;

                                        })
                                )
                        )
                        .then(literal("remove")
                                .then(argument("radius", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerWorld world = src.getWorld();
                                            String ownerName = StringArgumentType.getString(ctx, "owner");
                                            int toRemove = IntegerArgumentType.getInteger(ctx, "radius");

                                            if (toRemove <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                                            LandState state = LandState.get(world);
                                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                                            Land land = landOpt.get();
                                            int newRadius = land.getRadius() - toRemove;
                                            if (newRadius <= 0) return SCCommandsHandler.error(src, "Radius should be a number higher than 0");

                                            land.setRadius(newRadius, world);
                                            state.markDirty();

                                            src.sendFeedback(() -> Text.literal("Radius decreased to " + newRadius + " for " + ownerName), true);
                                            return 1;
                                        })
                                )
                        )
                );
    }
}