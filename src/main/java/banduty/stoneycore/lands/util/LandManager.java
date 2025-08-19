package banduty.stoneycore.lands.util;

import banduty.stoneycore.event.StartTickHandler;
import banduty.stoneycore.lands.LandType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.UUID;

public class LandManager {
    public static ActionResult createLand(ServerPlayerEntity player, BlockPos blockPos, LandType landType) {
        ServerWorld world = (ServerWorld) player.getWorld();
        LandState state = LandState.get(world);

        if (state.getLandByOwner(player.getUuid()).isPresent()) {
            player.sendMessage(Text.translatable("text.land." + landType.id().getNamespace() + ".already_ruling"), true);
            return ActionResult.PASS;
        }

        Land land = new Land(player.getUuid(), blockPos, landType.baseRadius(), landType, "");
        land.initializeClaim(world, 0, StartTickHandler.CLAIM_TASKS);
        state.addLand(land);
        giveCoreItem(player, landType);

        player.sendMessage(Text.translatable("text.land." + landType.id().getNamespace() + ".created"), true);
        return ActionResult.SUCCESS;
    }

    public static boolean isBlockInAnyClaim(ServerWorld world, BlockPos pos) {
        return LandState.get(world).isClaimed(pos);
    }

    public static boolean isOwnerOfClaim(ServerPlayerEntity player, BlockPos pos) {
        return LandState.get((ServerWorld) player.getWorld()).isOwner(pos, player.getUuid());
    }

    public static boolean isAllayOfClaim(ServerPlayerEntity player, BlockPos pos) {
        return LandState.get((ServerWorld) player.getWorld()).isAllay(pos, player.getUuid());
    }

    public static Optional<Land> getLandAtCore(ServerWorld world, BlockPos blockPos) {
        return LandState.get(world).getLandAtCorePos(blockPos);
    }

    public static Optional<Land> getLandByPosition(ServerWorld world, BlockPos pos) {
        return LandState.get(world).getLandAt(pos);
    }

    private static void giveCoreItem(ServerPlayerEntity player, LandType landType) {
        ItemStack coreItem = new ItemStack(landType.coreItem());

        player.getInventory().insertStack(coreItem);
    }

    public static Text getLandName(ServerWorld serverWorld, UUID attacker) {
        Optional<Land> land = LandState.get(serverWorld).getLandByOwner(attacker);
        if (land.isEmpty()) return Text.literal("");
        return land.get().getLandTitle(serverWorld);
    }
}

