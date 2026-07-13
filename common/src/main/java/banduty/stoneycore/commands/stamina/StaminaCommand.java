package banduty.stoneycore.commands.stamina;

import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class StaminaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("stamina")
                        .requires(source ->
                                source.hasPermission(2)
                                        && source.getEntity() instanceof Player)

                        .executes(ctx -> get(
                                ctx.getSource(),
                                (Player) ctx.getSource().getEntity()))

                        .then(Commands.literal("get")
                                .executes(ctx -> get(
                                        ctx.getSource(),
                                        (Player) ctx.getSource().getEntity()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> get(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")))))

                        .then(Commands.literal("max")
                                .executes(ctx -> max(
                                        ctx.getSource(),
                                        (Player) ctx.getSource().getEntity()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> max(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")))))

                        .then(Commands.literal("set")
                                // /stamina set <value>
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                        .executes(ctx -> set(
                                                ctx.getSource(),
                                                (Player) ctx.getSource().getEntity(),
                                                DoubleArgumentType.getDouble(ctx, "value"))))

                                // /stamina set <player> <value>
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                .executes(ctx -> set(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        DoubleArgumentType.getDouble(ctx, "value"))))))

                        .then(Commands.literal("add")
                                // /stamina add <value>
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                        .executes(ctx -> add(
                                                ctx.getSource(),
                                                (Player) ctx.getSource().getEntity(),
                                                DoubleArgumentType.getDouble(ctx, "value"))))

                                // /stamina add <player> <value>
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                .executes(ctx -> add(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        DoubleArgumentType.getDouble(ctx, "value"))))))

                        .then(Commands.literal("remove")
                                // /stamina remove <value>
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                        .executes(ctx -> remove(
                                                ctx.getSource(),
                                                (Player) ctx.getSource().getEntity(),
                                                DoubleArgumentType.getDouble(ctx, "value"))))

                                // /stamina remove <player> <value>
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                .executes(ctx -> remove(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        DoubleArgumentType.getDouble(ctx, "value"))))))
        );
    }

    private static int get(CommandSourceStack source, Player player) {
        double stamina = StaminaData.getStamina(player);
        double max = player.getAttributeValue(SCAttributes.MAX_STAMINA);

        source.sendSuccess(() ->
                Component.literal(String.format(
                        "%s: %.2f / %.2f",
                        player.getGameProfile().getName(),
                        stamina,
                        max
                )), false);

        return 1;
    }

    private static int set(CommandSourceStack source, Player player, double value) {
        StaminaData.setStamina(player, value);
        return get(source, player);
    }

    private static int add(CommandSourceStack source, Player player, double value) {
        StaminaData.addStamina(player, value);
        return get(source, player);
    }

    private static int remove(CommandSourceStack source, Player player, double value) {
        StaminaData.removeStamina(player, value);
        return get(source, player);
    }

    private static int max(CommandSourceStack source, Player player) {
        StaminaData.setStamina(player,
                player.getAttributeValue(SCAttributes.MAX_STAMINA));
        return get(source, player);
    }
}