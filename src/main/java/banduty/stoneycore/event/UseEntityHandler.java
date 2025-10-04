package banduty.stoneycore.event;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UseEntityHandler implements UseEntityCallback {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 300;
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (!(world instanceof ServerWorld serverWorld) || hand != Hand.MAIN_HAND || !(player instanceof ServerPlayerEntity serverPlayerEntity) || !player.getStackInHand(hand).isEmpty()) {
            return ActionResult.PASS;
        }

        long now = System.currentTimeMillis();
        UUID playerId = player.getUuid();
        if (cooldowns.containsKey(playerId) && now - cooldowns.get(playerId) < COOLDOWN_MS) {
            return ActionResult.FAIL;
        }

        Optional<Land> landOpt = LandState.get(serverWorld).getAllLands().stream()
                .filter(land -> land.getOwnerUUID().equals(playerId) || land.isAlly(playerId))
                .findFirst();

        if (landOpt.isPresent() && SiegeManager.isLandDefenseSiege(serverWorld, landOpt.get()) &&
                !SiegeManager.getAllParticipants(serverWorld).contains(serverPlayerEntity)) return ActionResult.FAIL;

        landOpt.ifPresent(land -> {
            if (land.getOwnerUUID().equals(player.getUuid()) && player.getEquippedStack(EquipmentSlot.HEAD).isOf(land.getLandType().coreItem())
                    && player.isSneaking() && entity instanceof PlayerEntity playerEntity) {
                if (land.isAlly(playerEntity.getUuid())) {
                    land.removeAlly(playerEntity.getUuid());
                    player.sendMessage(Text.translatable("text.land." + land.getLandType().id().getNamespace() + ".owner.remove_ally", player.getName()).formatted(Formatting.DARK_RED), true);
                    playerEntity.sendMessage(Text.translatable("text.land." + land.getLandType().id().getNamespace() + ".ally.remove_ally", land.getLandTitle(serverWorld).getString()).formatted(Formatting.DARK_RED), true);
                    cooldowns.put(playerId, now);
                } else if (land.getAllies().size() >= land.getMaxAllies() || land.getMaxAllies() < 0) {
                    land.addAlly(playerEntity.getUuid());
                    player.sendMessage(Text.translatable("text.land." + land.getLandType().id().getNamespace() + ".owner.add_ally", player.getName()).formatted(Formatting.DARK_GREEN), true);
                    playerEntity.sendMessage(Text.translatable("text.land." + land.getLandType().id().getNamespace() + ".ally.add_ally", land.getLandTitle(serverWorld).getString()).formatted(Formatting.DARK_GREEN), true);
                    cooldowns.put(playerId, now);
                }
            }
        });

        return ActionResult.PASS;
    }
}
