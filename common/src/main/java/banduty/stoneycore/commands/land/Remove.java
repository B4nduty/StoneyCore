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
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Remove {
    public static LiteralArgumentBuilder<CommandSourceStack> registerRemove() {
        return literal("remove")
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
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();
                            ServerLevel serverLevel = src.getLevel();
                            String ownerName = StringArgumentType.getString(ctx, "owner");

                            UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                            if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

                            LandState state = LandState.get(serverLevel);
                            Optional<Land> landOpt = state.getLandByOwner(ownerUUID);
                            if (landOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

                            serverLevel.setBlockAndUpdate(landOpt.get().getCorePos(), Blocks.AIR.defaultBlockState());
                            state.removeLand(landOpt.get());
                            src.sendSuccess(() -> Component.literal("Land removed for " + ownerName), true);
                            return 1;
                        })
                );
    }
}
