package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.event.StartTickHandler;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Create {
    public static LiteralArgumentBuilder<ServerCommandSource> registerCreate() {
        return literal("create")
                .then(argument("landType", IdentifierArgumentType.identifier())
                        .suggests((ctx, builder) -> {
                            for (LandType type : LandTypeRegistry.getAll()) {
                                builder.suggest(type.id().toString());
                            }
                            return builder.buildFuture();
                        })
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
                                .then(argument("x", IntegerArgumentType.integer())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest(ctx.getSource().getPlayer().getBlockPos().getX());
                                            return builder.buildFuture();
                                        })
                                        .then(argument("y", IntegerArgumentType.integer())
                                                .suggests((ctx, builder) -> {
                                                    builder.suggest(ctx.getSource().getPlayer().getBlockPos().getY());
                                                    return builder.buildFuture();
                                                })
                                                .then(argument("z", IntegerArgumentType.integer())
                                                        .suggests((ctx, builder) -> {
                                                            builder.suggest(ctx.getSource().getPlayer().getBlockPos().getZ());
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx ->
                                                                createLand(ctx.getSource(), IdentifierArgumentType.getIdentifier(ctx, "landType"), StringArgumentType.getString(ctx, "owner"),
                                                                IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))
                                                )
                                        )
                                )
                        )
                );
    }

    private static int createLand(ServerCommandSource src, Identifier typeId, String ownerName, int x, int y, int z) {
        Optional<LandType> typeOpt = LandTypeRegistry.getById(typeId);
        if (typeOpt.isEmpty()) return SCCommandsHandler.error(src, typeId + " isn’t a correct LandType");

        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerWorld world = src.getWorld();
        LandState state = LandState.get(world);
        if (state.getLandByOwner(ownerUUID).isPresent())
            return SCCommandsHandler.error(src, ownerName + " already own a Land");

        BlockPos pos = new BlockPos(x, y, z);
        if (state.getLandAt(pos).isPresent())
            return SCCommandsHandler.error(src, "This block pos is occupied by another Land");

        Land land = new Land(ownerUUID, pos, typeOpt.get().baseRadius(), typeOpt.get(), "", typeOpt.get().maxAllies());
        land.initializeClaim(world, typeOpt.get().baseRadius(), StartTickHandler.CLAIM_TASKS);
        world.setBlockState(pos, typeOpt.get().coreBlock().getDefaultState());
        state.addLand(land);
        src.sendFeedback(() -> Text.literal("Land created for " + ownerName), true);
        return 1;
    }
}
