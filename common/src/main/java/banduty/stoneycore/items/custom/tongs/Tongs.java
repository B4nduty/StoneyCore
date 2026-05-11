package banduty.stoneycore.items.custom.tongs;

import banduty.stoneycore.block.CraftmanAnvilBlock;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.custom.CraftmanAnvilHelper;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.util.data.itemdata.ItemStackHolder;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class Tongs extends Item implements CraftmanAnvilHelper {
    private static final String STACK_KEY = "TargetStack";

    public Tongs(Properties properties) {
        super(properties);
    }

    public static boolean hasTargetStack(ItemStack stack) {
        return !getTargetStack(stack).isEmpty();
    }

    public static ItemStack getTongsFor(ItemStack targetStack) {
        return Tongs.createForStack(targetStack);
    }

    public static ItemStack createForStack(ItemStack targetStack) {
        ItemStack manuscript = new ItemStack(SCItems.TONGS.get());
        setTargetStack(manuscript, targetStack);
        return manuscript;
    }

    public static void removeTargetStack(ItemStack stack) {
        stack.remove(SCDataComponents.TARGET_STACK.get());
    }

    public static void setTargetStack(ItemStack stack, ItemStack targetStack) {
        stack.set(SCDataComponents.TARGET_STACK.get(), new ItemStackHolder(targetStack));
    }

    public static ItemStack getTargetStack(ItemStack stack) {
        if (stack.has(SCDataComponents.TARGET_STACK.get())) {
            return stack.get(SCDataComponents.TARGET_STACK.get()).stack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack target = getTargetStack(stack);
        if (!target.isEmpty())
            return Component.translatable("item.stoneycore.tongs_with_item", target.getHoverName());
        return super.getName(stack);
    }

    @Override
    public ItemStack acceptCraftmanAnvilItem(ItemStack itemStack) {
        if (hasTargetStack(itemStack) && getTargetStack(itemStack).getItem() instanceof HotIron) {
            ItemStack targetStack = getTargetStack(itemStack);
            ItemStack finalItemStack = targetStack.copy();
            removeTargetStack(itemStack);
            return finalItemStack;
        }
        return itemStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {

        ItemStack itemStack = user.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResultHolder.pass(itemStack);

        var hitResult = user.pick(5.0D, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var blockPos = ((BlockHitResult) hitResult).getBlockPos();
            var blockState = level.getBlockState(blockPos);
            if (blockState.getBlock() instanceof CraftmanAnvilBlock) {
                return InteractionResultHolder.pass(itemStack);
            }
        }

        InteractionHand secondHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack secondStack = user.getItemInHand(secondHand);

        ItemStack targetStack = getTargetStack(itemStack);

        if (!targetStack.isEmpty()) {
            user.addItem(targetStack.copy());
            removeTargetStack(itemStack);
            return InteractionResultHolder.success(itemStack);
        }

        if (secondStack.getItem() instanceof HotIron) {
            setTargetStack(itemStack, secondStack.copyWithCount(1));
            secondStack.shrink(1);
            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide()) {
            ItemStack target = getTargetStack(itemStack);
            if (!target.isEmpty()) {
                if (target.getItem() instanceof HotIron) {
                    HotIron.setHeldByTongs(target, true);
                }

                target.getItem().inventoryTick(target, level, entity, slot, selected);

                if (target.getItem() instanceof HotIron) {
                    HotIron.setHeldByTongs(target, false);
                }

                if (target.isEmpty()) {
                    removeTargetStack(itemStack);
                } else {
                    setTargetStack(itemStack, target);
                }
            }
        }
        super.inventoryTick(itemStack, level, entity, slot, selected);
    }
}
