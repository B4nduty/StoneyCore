package banduty.stoneycore.event;

import banduty.stoneycore.util.itemdata.SCTags;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PlayerBlockBreakAfterHandler implements PlayerBlockBreakEvents.After {
    @Override
    public void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);

        if (heldItem.isIn(SCTags.WEAPONS_HARVEST.getTag())) {
            Block block = state.getBlock();

            if (block instanceof CropBlock) {
                replantCrop(world, pos, (CropBlock) block, player);
            }
        }
    }

    private void replantCrop(World world, BlockPos pos, CropBlock cropBlock, PlayerEntity player) {
        ItemStack seedStack = new ItemStack(cropBlock.asItem());

        if (!seedStack.isEmpty()) {
            world.setBlockState(pos, cropBlock.getDefaultState());
            world.emitGameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Emitter.of(player, cropBlock.getDefaultState()));
            if (!player.isCreative()) {
                seedStack.decrement(1);
            }
        }
    }
}