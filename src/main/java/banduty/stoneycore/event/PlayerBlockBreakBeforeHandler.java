package banduty.stoneycore.event;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandManager;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PlayerBlockBreakBeforeHandler implements PlayerBlockBreakEvents.Before {

    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        if (!(playerEntity instanceof ServerPlayerEntity serverPlayerEntity) || !(world instanceof ServerWorld serverWorld)) return true;

        if (SiegeManager.isPlayerInLandUnderSiege(serverWorld, serverPlayerEntity) && !(SiegeManager.getPlayerSiege(serverWorld, serverPlayerEntity.getUuid())
                .map(siege -> !siege.disabledPlayers.contains(serverPlayerEntity.getUuid())).orElse(false))) {
            return false;
        }

        if (LandManager.isBlockInAnyClaim(serverWorld, blockPos)) {
            Optional<Land> maybeLand = LandState.get(serverWorld).getLandAt(blockPos);
            boolean isLandCore = false;
            if (maybeLand.isPresent()) {
                Land land = maybeLand.get();
                isLandCore = land.getCorePos().equals(blockPos);
            }

            return playerEntity.isCreative() || LandManager.isOwnerOfClaim(serverPlayerEntity, blockPos) ||
                    LandManager.isAllayOfClaim(serverPlayerEntity, blockPos) || isLandCore;
        }

        return true;
    }
}