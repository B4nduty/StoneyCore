package banduty.stoneycore.items.custom.tongs;

import banduty.stoneycore.items.custom.CraftmanAnvilHelper;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.manuscript.ManuscriptType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Tongs extends Item implements CraftmanAnvilHelper {

    public Tongs(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack acceptCraftmanAnvilItem(ItemStack itemStack) {
        if (ManuscriptType.hasManuscriptType(itemStack)) {
            ManuscriptType manuscriptType = ManuscriptType.getManuscriptType(itemStack);
            ItemStack targetStack = manuscriptType != null ? new ItemStack(manuscriptType.getHotIronItem()) : ItemStack.EMPTY;
            ManuscriptType.removeManuscriptType(itemStack);
            return targetStack;
        }
        return itemStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.pass(stack);

        ManuscriptType manuscriptType = ManuscriptType.getManuscriptType(stack);

        InteractionHand secondHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack secondStack = player.getItemInHand(secondHand);

        if (manuscriptType != null) {
            Item itemToGive = manuscriptType.getHotIronItem();
            if (itemToGive != null && itemToGive != Items.AIR) {
                player.addItem(new ItemStack(itemToGive));
            }
            ManuscriptType.removeManuscriptType(stack);
            return InteractionResultHolder.success(stack);
        }

        if (secondStack.getItem() instanceof HotIron) {
            ManuscriptType hotIronType = ManuscriptType.getManuscriptType(secondStack);
            if (hotIronType != null) {
                ManuscriptType.setManuscriptType(stack, hotIronType);
                secondStack.shrink(1);
                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.fail(stack);
    }
}