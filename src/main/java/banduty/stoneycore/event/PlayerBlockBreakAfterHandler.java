package banduty.stoneycore.event;

import banduty.stoneycore.util.data.itemdata.SCTags;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PlayerBlockBreakAfterHandler implements PlayerBlockBreakEvents.After {
    @Override
    public void afterBlockBreak(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (heldItem.is(SCTags.WEAPONS_HARVEST.getTag())) {
            Block block = state.getBlock();

            if (block instanceof CropBlock) {
                replantCrop(level, pos, (CropBlock) block, player);
            }
        }
    }

    private void replantCrop(Level level, BlockPos pos, CropBlock cropBlock, Player player) {
        ItemStack seedStack = new ItemStack(cropBlock.asItem());

        if (!seedStack.isEmpty()) {
            level.setBlockAndUpdate(pos, cropBlock.defaultBlockState());
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, cropBlock.defaultBlockState()));
            if (!player.isCreative()) {
                seedStack.shrink(1);
            }
        }
    }
}