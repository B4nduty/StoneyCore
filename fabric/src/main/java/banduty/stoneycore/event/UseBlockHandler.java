package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.CraftmanAnvilBlock;
import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.items.SmithingHammer;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandManager;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

import static banduty.stoneycore.block.CraftmanAnvilBlock.FACING;

public class UseBlockHandler implements UseBlockCallback {
    @Override
    public InteractionResult interact(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BlockPos blockPos = hit.getBlockPos();
        BlockState state = serverLevel.getBlockState(blockPos);

        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        if (player.isShiftKeyDown() && player.getMainHandItem().getItem() instanceof SmithingHammer && state.getBlock() instanceof CraftmanAnvilBlock) {
            Direction facing = state.getValue(FACING);
            Direction newFacing = facing.getClockWise(); // rotate right

            level.setBlockAndUpdate(blockPos, state.setValue(FACING, newFacing));

            level.playSound(null, blockPos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.7f, 1.0f / (level.getRandom().nextFloat() * 0.5F + 1.0F));

            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown() && player.getMainHandItem().getItem() instanceof SmithingHammer && state.getBlock() instanceof AnvilBlock) {
            serverLevel.setBlockAndUpdate(blockPos, ModBlocks.CRAFTMAN_ANVIL.defaultBlockState());
            level.playSound(null, blockPos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.7f, 1.0f / (level.getRandom().nextFloat() * 0.5F + 1.0F));
            return InteractionResult.SUCCESS;
        }

        if (!StoneyCore.getConfig().landOptions().claimLand()) return InteractionResult.PASS;

        if (serverLevel.dimension() == Level.NETHER) {
            return InteractionResult.PASS;
        }

        LandState stateManager = LandState.get(serverLevel);
        boolean claimed = stateManager.isClaimed(blockPos);
        Optional<Land> maybeLand = stateManager.getLandAt(blockPos);

        boolean isCoreBlock = LandTypeRegistry.getAll().stream()
                .anyMatch(type -> type.coreBlock() == state.getBlock());

        if (SiegeManager.isPlayerInLandUnderSiege(serverLevel, player) && !(SiegeManager.getPlayerSiege(serverLevel, player.getUUID())
                .map(siege -> !siege.disabledPlayers.contains(player.getUUID())).orElse(false)) && !isCoreBlock) {
            return InteractionResult.FAIL;
        }

        if (maybeLand.isPresent() && !(maybeLand.get().getOwnerUUID().equals(player.getUUID()) || maybeLand.get().isAlly(player.getUUID()) || player.isCreative())) {
            if (state.getBlock().defaultBlockState().hasBlockEntity()) {
                return InteractionResult.PASS;
            }
            return InteractionResult.FAIL;
        }

        if (!isCoreBlock) {
            return InteractionResult.PASS;
        }

        if (maybeLand.isPresent()) {
            Land land = maybeLand.get();

            if (land.getOwnerUUID().equals(player.getUUID()) && (SiegeManager.isLandDefenseSiege(serverLevel, land) || SiegeManager.isLandAttackingSiege(serverLevel, land)) && player.getMainHandItem().is(Items.WHITE_BANNER)) {
                return SiegeManager.surrender(serverLevel, player, land);
            }
        }

        // Unclaimed core: create new land
        Optional<LandType> landTypeOpt = LandTypeRegistry.getByBlock(state.getBlock());
        if (!claimed && player.getMainHandItem().is(ItemTags.BANNERS)) {
            if (landTypeOpt.isEmpty()) {
                throw new IllegalArgumentException("LandType is empty");
            }
            return LandManager.createLand((ServerPlayer) player, blockPos, landTypeOpt.get());
        }

        // Expand existing land
        if (maybeLand.isEmpty() || !maybeLand.get().getOwnerUUID().equals(player.getUUID()) ||
                !player.getItemBySlot(EquipmentSlot.HEAD).is(maybeLand.get().getLandType().coreItem())) {
            return InteractionResult.PASS;
        }

        Land land = maybeLand.get();

        if (landTypeOpt.isPresent()) {
            if (player.getItemInHand(hand).getItem() instanceof NameTagItem) {
                land.setName(player.getItemInHand(hand).getHoverName().getString());
                player.displayClientMessage(Component.literal(land.getName()), true);
                player.getItemInHand(hand).shrink(1);
                return InteractionResult.PASS;
            }

            int available = countItem(player, landTypeOpt.get());
            long needed = land.getNeededExpandItemAmount();
            int maxRadius = StoneyCore.getConfig().technicalOptions().maxLandExpandRadius();
            double maxAllowedRadius = maxRadius < 0 ? Double.MAX_VALUE : maxRadius + land.getLandType().baseRadius();
            if (land.getRadius() >= maxAllowedRadius) {
                player.displayClientMessage(Component.translatable("component.land." + landTypeOpt.get().id().getNamespace() + ".at_max_radius"), true);
                return InteractionResult.PASS;
            }

            if (available <= 0) {
                long saved = land.getExpandItemStored();
                if (saved < needed) {
                    player.displayClientMessage(Component.translatable("component.land." + landTypeOpt.get().id().getNamespace() + ".stored_needed", saved, needed), true);
                    return InteractionResult.PASS;
                }
            }

            removeItems(player, available, landTypeOpt.get());
            land.depositExpandItem(player, serverLevel, available);
        }

        return InteractionResult.PASS;
    }

    private int countItem(Player player, LandType landType) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            Item item = stack.getItem();
            Integer value = landType.itemsToExpand().get(item);
            if (value != null) {
                total += stack.getCount() * value;
            }
        }
        return total;
    }

    private void removeItems(Player player, long amountNeeded, LandType landType) {
        for (ItemStack stack : player.getInventory().items) {
            if (amountNeeded <= 0) break;
            Item item = stack.getItem();
            Integer value = landType.itemsToExpand().get(item);
            if (value != null && value > 0) {
                int count = stack.getCount();
                int maxUsable = (int) Math.min(count, Math.ceil((double) amountNeeded / value));
                stack.shrink(maxUsable);
                amountNeeded -= (long) maxUsable * value;
            }
        }
    }

}
