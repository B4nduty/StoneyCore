package banduty.stoneycore.event;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UseEntityHandler implements UseEntityCallback {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 300;
    @Override
    public InteractionResult interact(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel) || hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer serverPlayer) || !player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }

        long now = System.currentTimeMillis();
        UUID playerId = player.getUUID();
        if (cooldowns.containsKey(playerId) && now - cooldowns.get(playerId) < COOLDOWN_MS) {
            return InteractionResult.FAIL;
        }

        Optional<Land> landOpt = LandState.get(serverLevel).getAllLands().stream()
                .filter(land -> land.getOwnerUUID().equals(playerId) || land.isAlly(playerId))
                .findFirst();

        if (landOpt.isPresent() && SiegeManager.isLandDefenseSiege(serverLevel, landOpt.get()) &&
                !SiegeManager.getAllParticipants(serverLevel).contains(serverPlayer)) return InteractionResult.FAIL;

        landOpt.ifPresent(land -> {
            if (land.getOwnerUUID().equals(player.getUUID()) && player.getItemBySlot(EquipmentSlot.HEAD).is(land.getLandType().coreItem())
                    && player.isShiftKeyDown() && entity instanceof Player playerEntity) {
                if (land.isAlly(playerEntity.getUUID())) {
                    land.removeAlly(playerEntity.getUUID());
                    player.displayClientMessage(Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".owner.remove_ally", player.getName()).withStyle(ChatFormatting.DARK_RED), true);
                    playerEntity.displayClientMessage(Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".ally.remove_ally", land.getLandTitle(serverLevel).getString()).withStyle(ChatFormatting.DARK_RED), true);
                    cooldowns.put(playerId, now);
                } else if (land.getAllies().size() >= land.getMaxAllies() || land.getMaxAllies() < 0) {
                    land.addAlly(playerEntity.getUUID());
                    player.displayClientMessage(Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".owner.add_ally", player.getName()).withStyle(ChatFormatting.DARK_GREEN), true);
                    playerEntity.displayClientMessage(Component.translatable("component.land." + land.getLandType().id().getNamespace() + ".ally.add_ally", land.getLandTitle(serverLevel).getString()).withStyle(ChatFormatting.DARK_GREEN), true);
                    cooldowns.put(playerId, now);
                }
            }
        });

        return InteractionResult.PASS;
    }
}
