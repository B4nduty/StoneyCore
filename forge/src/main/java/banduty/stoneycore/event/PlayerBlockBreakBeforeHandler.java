package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandManager;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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