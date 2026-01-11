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

        // Check siege restrictions
        if (SiegeManager.isPlayerInLandUnderSiege(serverLevel, serverPlayer) &&
                !(SiegeManager.getPlayerSiege(serverLevel, serverPlayer.getUUID())
                        .map(siege -> !siege.disabledPlayers.contains(serverPlayer.getUUID()))
                        .orElse(false))) {
            event.setCanceled(true);
            return;
        }

        // Check land claim restrictions
        if (LandManager.isBlockInAnyClaim(serverLevel, blockPos)) {
            Optional<Land> maybeLand = LandState.get(serverLevel).getLandAt(blockPos);
            boolean isLandCore = false;
            if (maybeLand.isPresent()) {
                Land land = maybeLand.get();
                isLandCore = land.getCorePos().equals(blockPos);
            }

            // Allow breaking if: creative mode, owner, allay, or land core
            boolean canBreak = serverPlayer.isCreative() ||
                    LandManager.isOwnerOfClaim(serverPlayer, blockPos) ||
                    LandManager.isAllayOfClaim(serverPlayer, blockPos) ||
                    isLandCore;

            if (!canBreak) {
                event.setCanceled(true);
            }
        }
    }
}