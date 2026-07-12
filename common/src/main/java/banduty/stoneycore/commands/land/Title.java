package banduty.stoneycore.commands.land;

import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Title {
    public static LiteralArgumentBuilder<CommandSourceStack> registerTitle() {
        return literal("title")
                .requires(src -> src.hasPermission(0))
                .then(literal("create")
                        .then(argument("titleName", StringArgumentType.word())
                                .then(argument("color", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (ChatFormatting format : ChatFormatting.values()) {
                                                if (format.isColor()) builder.suggest(format.getName());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> createCustomTitle(
                                                ctx.getSource(),
                                                getOwnerNameOrSelf(ctx),
                                                StringArgumentType.getString(ctx, "titleName"),
                                                StringArgumentType.getString(ctx, "color")
                                        ))
                                )
                        )
                )
                .then(literal("add")
                        .then(argument("ally", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    CommandSourceStack src = ctx.getSource();
                                    ServerLevel serverLevel = src.getLevel();
                                    String ownerName = getOwnerNameOrSelf(ctx);
                                    UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                    if (ownerUUID != null) {
                                        LandState.get(serverLevel).getLandByOwner(ownerUUID).ifPresent(land -> {
                                            builder.suggest(ownerName);
                                            for (UUID allyUUID : land.getAllies()) {
                                                ServerPlayer allyPlayer = src.getServer().getPlayerList().getPlayer(allyUUID);
                                                if (allyPlayer != null) builder.suggest(allyPlayer.getGameProfile().getName());
                                            }
                                        });
                                    }
                                    return builder.buildFuture();
                                })
                                .then(argument("titleName", StringArgumentType.word())
                                        .suggests(Title::suggestExistingTitles)
                                        .executes(ctx -> addTitleToPlayer(
                                                ctx.getSource(),
                                                getOwnerNameOrSelf(ctx),
                                                StringArgumentType.getString(ctx, "ally"),
                                                StringArgumentType.getString(ctx, "titleName")
                                        ))
                                )
                        )
                )
                .then(literal("remove")
                        .then(argument("ally", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    CommandSourceStack src = ctx.getSource();
                                    ServerLevel serverLevel = src.getLevel();
                                    String ownerName = getOwnerNameOrSelf(ctx);
                                    UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
                                    if (ownerUUID != null) {
                                        LandState.get(serverLevel).getLandByOwner(ownerUUID).ifPresent(land -> {
                                            builder.suggest(ownerName);
                                            for (UUID allyUUID : land.getAllies()) {
                                                ServerPlayer allyPlayer = src.getServer().getPlayerList().getPlayer(allyUUID);
                                                if (allyPlayer != null) builder.suggest(allyPlayer.getGameProfile().getName());
                                            }
                                        });
                                    }
                                    return builder.buildFuture();
                                })
                                .then(argument("titleName", StringArgumentType.word())
                                        .suggests(Title::suggestExistingTitles)
                                        .executes(ctx -> removeTitleFromPlayer(
                                                ctx.getSource(),
                                                getOwnerNameOrSelf(ctx),
                                                StringArgumentType.getString(ctx, "ally"),
                                                StringArgumentType.getString(ctx, "titleName")
                                        ))
                                )
                        )
                )
                .then(literal("get")
                        .then(argument("titleName", StringArgumentType.word())
                                .suggests(Title::suggestExistingTitles)
                                .executes(ctx -> getPlayersWithTitle(
                                        ctx.getSource(),
                                        getOwnerNameOrSelf(ctx),
                                        StringArgumentType.getString(ctx, "titleName")
                                ))
                        )
                );
    }

    private static CompletableFuture<Suggestions> suggestExistingTitles(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        CommandSourceStack src = ctx.getSource();
        String ownerName = getOwnerNameOrSelf(ctx);
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);

        if (ownerUUID != null) {
            LandState.get(src.getLevel()).getLandByOwner(ownerUUID).ifPresent(land -> {
                for (String title : land.getTitles().keySet()) {
                    builder.suggest(title);
                }
            });
        }
        return builder.buildFuture();
    }

    private static String getOwnerNameOrSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            return StringArgumentType.getString(ctx, "owner");
        } catch (IllegalArgumentException e) {
            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                return player.getGameProfile().getName();
            }
            return "";
        }
    }

    private static int createCustomTitle(CommandSourceStack src, String ownerName, String titleName, String colorName) {
        if (ownerName.isEmpty()) return SCCommandsHandler.error(src, "This command must be run by a player.");
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        LandState state = LandState.get(src.getLevel());
        var ownerLandOpt = state.getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();

        if (ownerLand.getTitles().containsKey(titleName)) {
            return SCCommandsHandler.error(src, "Title '" + titleName + "' already exists in this Land!");
        }

        ChatFormatting format = ChatFormatting.getByName(colorName);
        if (format == null || !format.isColor()) return SCCommandsHandler.error(src, "Invalid color: " + colorName);

        ownerLand.createTitle(titleName, format.getColor());
        state.setDirty();

        src.sendSuccess(() -> Component.literal("Title '" + titleName + "' created with color " + colorName), true);
        return 1;
    }

    private static int addTitleToPlayer(CommandSourceStack src, String ownerName, String allyName, String titleName) {
        if (ownerName.isEmpty()) return SCCommandsHandler.error(src, "This command must be run by a player.");
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        UUID allyUUID = SCCommandsHandler.getUUID(src, allyName);

        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);
        if (allyUUID == null) return SCCommandsHandler.error(src, "Unknown player " + allyName);

        LandState state = LandState.get(src.getLevel());
        var ownerLandOpt = state.getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();
        if (!ownerLand.isAlly(allyUUID) && !ownerUUID.equals(allyUUID)) {
            return SCCommandsHandler.error(src, allyName + " is not a member/ally of this Land");
        }

        if (!ownerLand.getTitles().containsKey(titleName)) {
            return SCCommandsHandler.error(src, "Title '" + titleName + "' does not exist. Create it first!");
        }

        ownerLand.assignTitleToPlayer(allyUUID, titleName);
        state.setDirty();

        src.sendSuccess(() -> Component.literal("Assigned title '" + titleName + "' to " + allyName), true);
        return 1;
    }

    private static int removeTitleFromPlayer(CommandSourceStack src, String ownerName, String allyName, String titleName) {
        if (ownerName.isEmpty()) return SCCommandsHandler.error(src, "This command must be run by a player.");
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        UUID allyUUID = SCCommandsHandler.getUUID(src, allyName);

        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);
        if (allyUUID == null) return SCCommandsHandler.error(src, "Unknown player " + allyName);

        LandState state = LandState.get(src.getLevel());
        var ownerLandOpt = state.getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();
        if (!titleName.equals(ownerLand.getPlayerTitle(allyUUID))) {
            return SCCommandsHandler.error(src, allyName + " does not have the title '" + titleName + "'");
        }

        ownerLand.removeTitleFromPlayer(allyUUID);
        state.setDirty();

        src.sendSuccess(() -> Component.literal("Removed title '" + titleName + "' from " + allyName), true);
        return 1;
    }

    private static int getPlayersWithTitle(CommandSourceStack src, String ownerName, String titleName) {
        if (ownerName.isEmpty()) return SCCommandsHandler.error(src, "This command must be run by a player.");
        UUID ownerUUID = SCCommandsHandler.getUUID(src, ownerName);
        if (ownerUUID == null) return SCCommandsHandler.error(src, "Unknown owner " + ownerName);

        ServerLevel serverLevel = src.getLevel();
        var ownerLandOpt = LandState.get(serverLevel).getLandByOwner(ownerUUID);
        if (ownerLandOpt.isEmpty()) return SCCommandsHandler.error(src, ownerName + " doesn’t own a Land");

        Land ownerLand = ownerLandOpt.get();
        if (!ownerLand.getTitles().containsKey(titleName)) {
            return SCCommandsHandler.error(src, "Title '" + titleName + "' does not exist in this land.");
        }

        List<UUID> playersWithTitle = ownerLand.getPlayersWithTitle(titleName);
        if (playersWithTitle.isEmpty()) {
            src.sendSuccess(() -> Component.literal("No one in this land currently holds the title '" + titleName + "'"), false);
        } else {
            StringBuilder list = new StringBuilder();
            Integer colorInt = ownerLand.getTitles().get(titleName);
            TextColor color = colorInt != null ? TextColor.fromRgb(colorInt) : TextColor.fromLegacyFormat(ChatFormatting.WHITE);

            for (UUID uuid : playersWithTitle) {
                String name = Land.getOwnerName(serverLevel, uuid);
                if (!list.isEmpty()) list.append(", ");
                list.append(name);
            }

            src.sendSuccess(() -> Component.literal(ownerName + "'s Land citizens with title '")
                    .append(Component.literal(titleName).withStyle(s -> s.withColor(color)))
                    .append("': " + list), false);
        }
        return 1;
    }
}