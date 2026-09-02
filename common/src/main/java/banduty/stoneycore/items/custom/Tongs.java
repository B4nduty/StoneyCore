package banduty.stoneycore.items.custom;

import banduty.stoneycore.data.CapturedItemData;
import banduty.stoneycore.data.SCDataComponents;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Tongs extends Item implements CraftmanAnvilHelper {

    public Tongs(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        ItemStack capturedItem = getCapturedItem(stack);
        capturedItem.inventoryTick(level, entity, slotId, isSelected);
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack capturedItem = getCapturedItem(stack);
        if (capturedItem.isEmpty()) return super.getName(stack);
        return Component.translatable("item.stoneycore.tongs_with_item", capturedItem.getItem().getName(capturedItem));
    }

    public boolean hasCapturedItem(ItemStack tongs) {
        return tongs.has(SCDataComponents.CAPTURED_ITEM.get());
    }

    public ItemStack getCapturedItem(ItemStack tongs) {
        CapturedItemData captured = tongs.get(SCDataComponents.CAPTURED_ITEM.get());
        return captured != null ? captured.toItemStack() : ItemStack.EMPTY;
    }

    public void setCapturedItem(ItemStack tongs, ItemStack captured) {
        tongs.set(SCDataComponents.CAPTURED_ITEM.get(), CapturedItemData.fromItemStack(captured.copy()));
    }

    public void removeCapturedItem(ItemStack tongs) {
        tongs.remove(SCDataComponents.CAPTURED_ITEM.get());
    }

    public CapturedItemData getCapturedItemData(ItemStack tongs) {
        return tongs.get(SCDataComponents.CAPTURED_ITEM.get());
    }

    @Override
    public ItemStack acceptCraftmanAnvilItem(ItemStack itemStack) {
        if (hasCapturedItem(itemStack)) {
            ItemStack captured = getCapturedItem(itemStack);
            removeCapturedItem(itemStack);
            return captured;
        }
        return itemStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.pass(stack);

        if (hasCapturedItem(stack)) {
            ItemStack captured = getCapturedItem(stack);
            if (!captured.isEmpty()) {
                player.addItem(captured.copy());
            }
            removeCapturedItem(stack);
            return InteractionResultHolder.success(stack);
        }

        InteractionHand secondHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack secondStack = player.getItemInHand(secondHand);

        if (secondStack.getItem() instanceof QuenchItem quenchItem && !quenchItem.isFinished(secondStack)) {
            setCapturedItem(stack, secondStack.copyWithCount(1));
            secondStack.shrink(1);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);

        ItemStack itemStack = getCapturedItem(stack);

        if (itemStack.getItem() instanceof QuenchItem quenchItem) {
            if (player == null) {
                return InteractionResult.PASS;
            }

            BlockPos waterPos = quenchItem.getLookedWater(player, level);

            if (waterPos != null) {
                return quenchItem.handleWaterInteraction(level, waterPos, player, itemStack, context.getHand());
            }

            if (state.is(Blocks.WATER_CAULDRON) && state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                return quenchItem.handleWaterCauldron(level, pos, player, itemStack);
            }
        }

        return InteractionResult.PASS;
    }
}