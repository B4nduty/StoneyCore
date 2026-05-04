package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandManager;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Optional;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerBlockBreakBeforeHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer) ||
                !(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos blockPos = event.getPos();

        Optional<SiegeManager.Siege> siegeOpt =
                SiegeManager.getPlayerSiege(serverLevel, serverPlayer.getUUID());

        if (siegeOpt.isPresent() && siegeOpt.get().disabledPlayers.contains(serverPlayer.getUUID())) {
            event.setCanceled(true);
            return;
        }

        Optional<Land> maybeLand = LandState.get(serverLevel).getLandAt(blockPos);
        if (maybeLand.isEmpty()) return;

        Land land = maybeLand.get();
        boolean isLandCore = land.getCorePos().equals(blockPos);

        boolean isInSiege = siegeOpt
                .map(siege -> siege.defendingLand.equals(land) || siege.attackingLand.equals(land))
                .orElse(false);

        boolean canBreak = serverPlayer.isCreative() ||
                LandManager.isOwnerOfClaim(serverPlayer, blockPos) ||
                LandManager.isAllayOfClaim(serverPlayer, blockPos) ||
                isLandCore ||
                isInSiege;

        if (!canBreak) {
            event.setCanceled(true);
        }
    }
}