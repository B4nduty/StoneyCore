package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.platform.Services;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Create {
    public static LiteralArgumentBuilder<CommandSourceStack> registerCreate() {
        return literal("create")
                .then(argument("landType", ResourceLocationArgument.id())
                        .suggests((ctx, builder) -> {
                            for (LandType type : LandTypeRegistry.getAll()) {
                                builder.suggest(type.id().toString());
                            }
                            return builder.buildFuture();
                        })
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
                                .then(argument("x", IntegerArgumentType.integer())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest(ctx.getSource().getPlayer().getOnPos().getX());
                                            return builder.buildFuture();
                                        })
                                        .then(argument("y", IntegerArgumentType.integer())
                                                .suggests((ctx, builder) -> {
                                                    builder.suggest(ctx.getSource().getPlayer().getOnPos().getY());
                                                    return builder.buildFuture();
                                                })
                                                .then(argument("z", IntegerArgumentType.integer())
                                                        .suggests((ctx, builder) -> {
                                                            builder.suggest(ctx.getSource().getPlayer().getOnPos().getZ());
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx ->
                                                                createLand(ctx.getSource(), ResourceLocationArgument.getId(ctx, "landType"), StringArgumentType.getString(ctx, "owner"),
                                                                IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z")))
                                                )
                                        )
                                )
                        )
                );
    }

    private static int createLand(CommandSourceStack src, ResourceLocation typeId, String ownerName, int x, int y, int z) {
        Optional<LandType> typeOpt = LandTypeRegistry.getById(typeId);
        if (typeOpt.isEmpty()) return SCCommandsHandler.error(src, typeId + " isn’t a correct LandType");

        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerLevel serverLevel = src.getLevel();
        LandState state = LandState.get(serverLevel);
        if (state.getLandByOwner(ownerUUID).isPresent())
            return SCCommandsHandler.error(src, ownerName + " already own a Land");

        BlockPos pos = new BlockPos(x, y, z);
        if (state.getLandAt(pos).isPresent())
            return SCCommandsHandler.error(src, "This block pos is occupied by another Land");

        Land land = new Land(ownerUUID, pos, typeOpt.get().baseRadius(), typeOpt.get(), "", typeOpt.get().maxAllies());
        land.initializeClaim(serverLevel, typeOpt.get().baseRadius(), Services.PLATFORM.getClaimTasks());
        serverLevel.setBlockAndUpdate(pos, typeOpt.get().coreBlock().defaultBlockState());
        state.addLand(land);
        src.sendSuccess(() -> Component.literal("Land created for " + ownerName), true);
        return 1;
    }
}
