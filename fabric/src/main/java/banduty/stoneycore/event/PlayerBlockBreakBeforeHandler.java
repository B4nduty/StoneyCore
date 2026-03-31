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
        if (!(playerEntity instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel serverLevel)) {
            return true;
        }

        Optional<SiegeManager.Siege> siegeOpt = SiegeManager.getPlayerSiege(serverLevel, serverPlayer.getUUID());
        if (siegeOpt.isPresent() && siegeOpt.get().disabledPlayers.contains(serverPlayer.getUUID())) {
            return false;
        }

        Optional<Land> maybeLand = LandState.get(serverLevel).getLandAt(blockPos);
        if (maybeLand.isEmpty()) return true;

        Land land = maybeLand.get();
        boolean isLandCore = land.getCorePos().equals(blockPos);

        boolean isInSiege = siegeOpt
                .map(siege -> siege.defendingLand.equals(land) || siege.attackingLand.equals(land))
                .orElse(false);

        return playerEntity.isCreative() ||
                LandManager.isOwnerOfClaim(serverPlayer, blockPos) ||
                LandManager.isAllayOfClaim(serverPlayer, blockPos) ||
                isLandCore ||
                isInSiege;
    }
}