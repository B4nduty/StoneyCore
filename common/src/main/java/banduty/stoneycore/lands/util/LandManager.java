package banduty.stoneycore.lands.util;

import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class LandManager {
    public static InteractionResult createLand(ServerPlayer serverPlayer, BlockPos blockPos, LandType landType) {
        ServerLevel world = (ServerLevel) serverPlayer.level();
        LandState state = LandState.get(world);

        if (state.getLandByOwner(serverPlayer.getUUID()).isPresent()) {
            serverPlayer.displayClientMessage(Component.translatable("component.land." + landType.id().getNamespace() + ".already_ruling"), true);
            return InteractionResult.PASS;
        }

        Land land = new Land(serverPlayer.getUUID(), blockPos, landType.baseRadius(), landType, "", landType.maxAllies());
        land.initializeClaim(world, landType.baseRadius(), Services.PLATFORM.getClaimTasks());
        state.addLand(land);
        giveCoreItem(serverPlayer, landType);

        serverPlayer.displayClientMessage(Component.translatable("component.land." + landType.id().getNamespace() + ".created"), true);
        return InteractionResult.SUCCESS;
    }

    public static boolean isBlockInAnyClaim(ServerLevel world, BlockPos pos) {
        return LandState.get(world).isClaimed(pos);
    }

    public static boolean isOwnerOfClaim(ServerPlayer serverPlayer, BlockPos pos) {
        return LandState.get((ServerLevel) serverPlayer.level()).isOwner(pos, serverPlayer.getUUID());
    }

    public static boolean isAllayOfClaim(ServerPlayer serverPlayer, BlockPos pos) {
        return LandState.get((ServerLevel) serverPlayer.level()).isAllay(pos, serverPlayer.getUUID());
    }

    public static Optional<Land> getLandAtCore(ServerLevel world, BlockPos blockPos) {
        return LandState.get(world).getLandAtCorePos(blockPos);
    }

    public static Optional<Land> getLandByPosition(ServerLevel world, BlockPos pos) {
        return LandState.get(world).getLandAt(pos);
    }

    private static void giveCoreItem(ServerPlayer serverPlayer, LandType landType) {
        ItemStack coreItem = new ItemStack(landType.coreItem());

        serverPlayer.getInventory().add(coreItem);
    }

    public static Component getLandName(ServerLevel serverLevel, UUID attacker) {
        Optional<Land> land = LandState.get(serverLevel).getLandByOwner(attacker);
        if (land.isEmpty()) return Component.literal("");
        return land.get().getLandTitle(serverLevel);
    }
}

