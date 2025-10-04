package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Remove {
    public static LiteralArgumentBuilder<ServerCommandSource> registerRemove() {
        return literal("remove")
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

                            world.setBlockState(landOpt.get().getCorePos(), Blocks.AIR.getDefaultState());
                            state.removeLand(landOpt.get());
                            src.sendFeedback(() -> Text.literal("Land removed for " + ownerName), true);
                            return 1;
                        })
                );
    }
}
