package banduty.stoneycore.items.custom.manuscript;

import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Manuscript extends Item {
    public Manuscript(Properties properties) {
        super(properties);
    }

    public static boolean hasTargetStack(ItemStack manuscript) {
        return !getTargetStack(manuscript).isEmpty();
    }

    public static ItemStack getManuscriptFor(ItemStack targetStack) {
        return Manuscript.createForStack(targetStack);
    }

    public static void removeTargetStack(ItemStack stack) {
        stack.remove(SCDataComponents.TARGET_STACK);
    }

    public static ItemStack createForStack(ItemStack targetStack) {
        ItemStack manuscript = new ItemStack(SCItems.MANUSCRIPT);
        setTargetStack(manuscript, targetStack);
        return manuscript;
    }

    public static void setTargetStack(ItemStack manuscript, ItemStack targetStack) {
        manuscript.set(SCDataComponents.TARGET_STACK, targetStack);
    }

    public static ItemStack getTargetStack(ItemStack manuscript) {
        if (manuscript.has(SCDataComponents.TARGET_STACK)) {
            return manuscript.get(SCDataComponents.TARGET_STACK);
        }
        return ItemStack.EMPTY;
    }

    public static ResourceLocation getTargetItemId(ItemStack manuscript) {
        ItemStack target = getTargetStack(manuscript);
        if (!target.isEmpty()) {
            return BuiltInRegistries.ITEM.getKey(target.getItem());
        }
        return null;
    }

    public static String getTargetItemPath(ItemStack manuscript) {
        ResourceLocation id = getTargetItemId(manuscript);
        return id != null ? id.getPath() : "default";
    }

    public static String getTargetItemNamespace(ItemStack manuscript) {
        ResourceLocation id = getTargetItemId(manuscript);
        return id != null ? id.getNamespace() : "default";
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack target = getTargetStack(stack);
        if (!target.isEmpty()) {
            // Get the original item's display name
            Component originalName = target.getHoverName();
            // Return a translatable component with the original name as an argument
            return Component.translatable("item.stoneycore.manuscript.with_item", originalName);
        }
        return super.getName(stack);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide()) {
            ItemStack target = getTargetStack(itemStack);
            if (!target.isEmpty()) {
                target.getItem().inventoryTick(target, level, entity, slot, selected);

                if (target.isEmpty()) {
                    removeTargetStack(itemStack);
                }
            }
        }
        super.inventoryTick(itemStack, level, entity, slot, selected);
    }

    /**
     * Predefined Types, it will not change how it works, is just to help with Model DataGen
     */
    public enum Types {
        LONG_HAFTED, LONGSWORDS, SPAULDERS, SHORT_HAFTED, SWALLOWTAIL, SWORDS, ARQUEBUS, BODKIN, BOOTS,
        BREASTPLATE, BREECHES, BROAD, CHAUSSES, CLOTH, COAT, COIF, CROSSBOW, GAUNTLETS, GREATSWORDS, GREAVES, BESAGEWS,
        RIM_GUARDS, HANDGONNE, HELMET, LONGBOW, HORSE
    }
}
