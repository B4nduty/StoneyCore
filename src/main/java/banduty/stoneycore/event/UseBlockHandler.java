package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandManager;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.NameTagItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class UseBlockHandler implements UseBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.PASS;
        }

        if (serverWorld.getRegistryKey() == World.NETHER) {
            return ActionResult.PASS;
        }

        BlockPos blockPos = hit.getBlockPos();
        BlockState state = serverWorld.getBlockState(blockPos);
        LandState stateManager = LandState.get(serverWorld);
        boolean claimed = stateManager.isClaimed(blockPos);
        Optional<Land> maybeLand = stateManager.getLandAt(blockPos);

        boolean isCoreBlock = LandTypeRegistry.getAll().stream()
                .anyMatch(type -> type.coreBlock() == state.getBlock());

        if (SiegeManager.isPlayerInLandUnderSiege(serverWorld, player) && !(SiegeManager.getPlayerSiege(serverWorld, player.getUuid())
                .map(siege -> !siege.disabledPlayers.contains(player.getUuid())).orElse(false)) && !isCoreBlock) {
            return ActionResult.FAIL;
        }

        if (maybeLand.isPresent() && !(maybeLand.get().getOwnerUUID().equals(player.getUuid()) || maybeLand.get().isAlly(player.getUuid()) || player.isCreative())) {
            if (state.getBlock().getDefaultState().hasBlockEntity()) {
                return ActionResult.PASS;
            }
            return ActionResult.FAIL;
        }

        if (!isCoreBlock) {
            return ActionResult.PASS;
        }

        if (maybeLand.isPresent()) {
            Land land = maybeLand.get();

            if (land.getOwnerUUID().equals(player.getUuid()) && (SiegeManager.isLandDefenseSiege(serverWorld, land) || SiegeManager.isLandAttackingSiege(serverWorld, land)) && player.getMainHandStack().isOf(Items.WHITE_BANNER)) {
                return SiegeManager.surrender(serverWorld, player, land);
            }
        }

        // Unclaimed core: create new land
        Optional<LandType> landTypeOpt = LandTypeRegistry.getByBlock(state.getBlock());
        if (!claimed && player.getMainHandStack().isIn(ItemTags.BANNERS)) {
            if (landTypeOpt.isEmpty()) {
                throw new IllegalArgumentException("LandType is empty");
            }
            return LandManager.createLand((ServerPlayerEntity) player, blockPos, landTypeOpt.get());
        }

        // Expand existing land
        if (maybeLand.isEmpty() || !maybeLand.get().getOwnerUUID().equals(player.getUuid()) ||
                !player.getEquippedStack(EquipmentSlot.HEAD).isOf(maybeLand.get().getLandType().coreItem())) {
            return ActionResult.PASS;
        }

        Land land = maybeLand.get();

        if (landTypeOpt.isPresent()) {
            if (player.getStackInHand(hand).getItem() instanceof NameTagItem) {
                land.setCustomName(player.getStackInHand(hand).getName().getString());
                player.sendMessage(Text.literal(land.getCustomName()), true);
                player.getStackInHand(hand).decrement(1);
                return ActionResult.PASS;
            }

            int available = countItem(player, landTypeOpt.get());
            long needed = land.getNeededExpandItemAmount();
            int maxRadius = StoneyCore.getConfig().technicalOptions.maxLandExpandRadius();
            double maxAllowedRadius = maxRadius < 0 ? Double.MAX_VALUE : maxRadius + land.getLandType().baseRadius();
            if (land.getRadius() >= maxAllowedRadius) {
                player.sendMessage(Text.translatable("text.land." + landTypeOpt.get().id().getNamespace() + ".at_max_radius"), true);
                return ActionResult.PASS;
            }

            if (available <= 0) {
                long saved = land.getExpandItemStored();
                if (saved < needed) {
                    player.sendMessage(Text.translatable("text.land." + landTypeOpt.get().id().getNamespace() + ".stored_needed", saved, needed), true);
                    return ActionResult.PASS;
                }
            }

            removeItems(player, available, landTypeOpt.get());
            land.depositExpandItem(player, serverWorld, available);
        }

        return ActionResult.PASS;
    }

    private int countItem(PlayerEntity player, LandType landType) {
        int total = 0;
        for (ItemStack stack : player.getInventory().main) {
            Item item = stack.getItem();
            Integer value = landType.itemsToExpand().get(item);
            if (value != null) {
                total += stack.getCount() * value;
            }
        }
        return total;
    }

    private void removeItems(PlayerEntity player, long amountNeeded, LandType landType) {
        for (ItemStack stack : player.getInventory().main) {
            if (amountNeeded <= 0) break;
            Item item = stack.getItem();
            Integer value = landType.itemsToExpand().get(item);
            if (value != null && value > 0) {
                int count = stack.getCount();
                int maxUsable = (int) Math.min(count, Math.ceil((double) amountNeeded / value));
                stack.decrement(maxUsable);
                amountNeeded -= (long) maxUsable * value;
            }
        }
    }

}
