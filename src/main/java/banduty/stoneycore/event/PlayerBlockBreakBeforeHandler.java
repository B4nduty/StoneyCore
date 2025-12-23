package banduty.stoneycore.event;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandManager;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PlayerBlockBreakBeforeHandler implements PlayerBlockBreakEvents.Before {

    @Override
    public boolean beforeBlockBreak(Level world, Player playerEntity, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        if (!(playerEntity instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel serverLevel)) return true;

        if (SiegeManager.isPlayerInLandUnderSiege(serverLevel, serverPlayer) && !(SiegeManager.getPlayerSiege(serverLevel, serverPlayer.getUUID())
                .map(siege -> !siege.disabledPlayers.contains(serverPlayer.getUUID())).orElse(false))) {
            return false;
        }

        if (LandManager.isBlockInAnyClaim(serverLevel, blockPos)) {
            Optional<Land> maybeLand = LandState.get(serverLevel).getLandAt(blockPos);
            boolean isLandCore = false;
            if (maybeLand.isPresent()) {
                Land land = maybeLand.get();
                isLandCore = land.getCorePos().equals(blockPos);
            }

            return playerEntity.isCreative() || LandManager.isOwnerOfClaim(serverPlayer, blockPos) ||
                    LandManager.isAllayOfClaim(serverPlayer, blockPos) || isLandCore;
        }

        return true;
    }
}