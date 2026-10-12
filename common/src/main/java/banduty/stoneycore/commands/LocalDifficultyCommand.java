package banduty.stoneycore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;

public final class LocalDifficultyCommand {
    private LocalDifficultyCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("difficulty")
                        .then(Commands.literal("local")
                                .requires(src -> src.getPlayer() != null
                                        && src.getPlayer().getAbilities().instabuild)
                                .executes(ctx -> get(ctx.getSource()))
                                .then(Commands.literal("get")
                                        .executes(ctx -> get(ctx.getSource())))
                                .then(Commands.literal("set")
                                        .requires(src -> src.hasPermission(2))
                                        .then(Commands.argument(
                                                "value",
                                                FloatArgumentType.floatArg(0.0F, 4.0F)
                                        ).executes(ctx -> set(
                                                ctx.getSource(),
                                                FloatArgumentType.getFloat(ctx, "value")
                                        )))
                                )
                                .then(Commands.literal("reset")
                                        .requires(src -> src.hasPermission(2))
                                        .executes(ctx -> reset(ctx.getSource()))
                                )
                        )
        );
    }

    private static int get(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            return SCCommandsHandler.error(
                    src, "This command must be executed by a player."
            );
        }

        BlockPos pos = player.blockPosition();
        DifficultyInstance difficulty =
                player.serverLevel().getCurrentDifficultyAt(pos);

        src.sendSuccess(() -> Component.literal(
                String.format(
                        "Local difficulty: %.2f | Special multiplier: %.2f",
                        difficulty.getEffectiveDifficulty(),
                        difficulty.getSpecialMultiplier()
                )
        ), false);

        return 1;
    }

    private static int set(CommandSourceStack src, float value) {
        return SCCommandsHandler.error(
                src,
                "Local difficulty overrides are not implemented yet. "
                        + "Connect this command to a stored override and difficulty calculation hook."
        );
    }

    private static int reset(CommandSourceStack src) {
        return SCCommandsHandler.error(
                src,
                "Local difficulty overrides are not implemented yet."
        );
    }
}
