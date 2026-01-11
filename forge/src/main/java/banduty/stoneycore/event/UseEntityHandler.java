package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UseEntityHandler {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 300;

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        InteractionHand hand = event.getHand();

        // Only handle server side, main hand, and empty hand
        if (player.level().isClientSide() ||
                hand != InteractionHand.MAIN_HAND ||
                !(player instanceof ServerPlayer serverPlayer) ||
                !player.getItemInHand(hand).isEmpty()) {
            return;
        }

        // Check cooldown
        long now = System.currentTimeMillis();
        UUID playerId = player.getUUID();
        if (cooldowns.containsKey(playerId) && now - cooldowns.get(playerId) < COOLDOWN_MS) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) player.level();

        // Find player's land
        Optional<Land> landOpt = LandState.get(serverLevel).getAllLands().stream()
                .filter(land -> land.getOwnerUUID().equals(playerId) || land.isAlly(playerId))
                .findFirst();

        // Check if player can interact during siege
        if (landOpt.isPresent() &&
                SiegeManager.isLandDefenseSiege(serverLevel, landOpt.get()) &&
                !SiegeManager.getAllParticipants(serverLevel).contains(serverPlayer)) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            return;
        }

        // Handle ally management
        if (landOpt.isPresent() && target instanceof Player targetPlayer) {
            handleAllyManagement(serverPlayer, targetPlayer, landOpt.get(), serverLevel, playerId, now);
        }
    }

    private static void handleAllyManagement(ServerPlayer player, Player targetPlayer, Land land, ServerLevel serverLevel, UUID playerId, long now) {
        // Check conditions for ally management
        boolean isOwner = land.getOwnerUUID().equals(player.getUUID());
        boolean wearingCoreItem = player.getItemBySlot(EquipmentSlot.HEAD).is(land.getLandType().coreItem());
        boolean isSneaking = player.isShiftKeyDown();

        if (isOwner && wearingCoreItem && isSneaking) {
            boolean isAlly = land.isAlly(targetPlayer.getUUID());

            if (isAlly) {
                // Remove ally
                land.removeAlly(targetPlayer.getUUID());
                player.displayClientMessage(
                        Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".owner.remove_ally",
                                targetPlayer.getName()).withStyle(ChatFormatting.DARK_RED),
                        true
                );
                targetPlayer.displayClientMessage(
                        Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".ally.remove_ally",
                                land.getLandTitle(serverLevel).getString()).withStyle(ChatFormatting.DARK_RED),
                        true
                );
                cooldowns.put(playerId, now);
            } else {
                // Check if can add ally
                boolean canAddAlly = land.getAllies().size() < land.getMaxAllies() || land.getMaxAllies() < 0;

                if (canAddAlly) {
                    // Add ally
                    land.addAlly(targetPlayer.getUUID());
                    player.displayClientMessage(
                            Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".owner.add_ally",
                                    targetPlayer.getName()).withStyle(ChatFormatting.DARK_GREEN),
                            true
                    );
                    targetPlayer.displayClientMessage(
                            Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".ally.add_ally",
                                    land.getLandTitle(serverLevel).getString()).withStyle(ChatFormatting.DARK_GREEN),
                            true
                    );
                    cooldowns.put(playerId, now);
                } else {
                    // Max allies reached
                    player.displayClientMessage(
                            Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".max_allies_reached")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
            }
        }
    }

    // Clean up old cooldowns periodically
    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.START) {
            long now = System.currentTimeMillis();
            cooldowns.entrySet().removeIf(entry -> now - entry.getValue() > COOLDOWN_MS);
        }
    }
}