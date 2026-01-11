package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.itemdata.SCTags;
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
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerBlockBreakAfterHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (heldItem.is(SCTags.WEAPONS_HARVEST.getTag())) {
            Block block = state.getBlock();

            if (block instanceof CropBlock) {
                replantCrop(level, pos, (CropBlock) block, player);
            }
        }
    }

    private static void replantCrop(Level level, BlockPos pos, CropBlock cropBlock, Player player) {
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