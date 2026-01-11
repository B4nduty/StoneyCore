package banduty.stoneycore.items.manuscript;

import banduty.stoneycore.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class Manuscript extends Item {
    private static final String STACK_KEY = "TargetStack";

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
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.remove(STACK_KEY);
        if (nbt.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static ItemStack createForStack(ItemStack targetStack) {
        ItemStack manuscript = Services.MANUSCRIPT.getManuscript(targetStack);
        setTargetStack(manuscript, targetStack);
        return manuscript;
    }

    public static void setTargetStack(ItemStack manuscript, ItemStack targetStack) {
        if (targetStack == null || targetStack.isEmpty()) return;
        CompoundTag nbt = manuscript.getOrCreateTag();
        CompoundTag stackTag = new CompoundTag();
        targetStack.save(stackTag);
        nbt.put(STACK_KEY, stackTag);
    }

    public static ItemStack getTargetStack(ItemStack manuscript) {
        if (manuscript.hasTag() && manuscript.getTag().contains(STACK_KEY)) {
            CompoundTag stackNbt = manuscript.getTag().getCompound(STACK_KEY);
            return ItemStack.of(stackNbt);
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
    public Component getName(ItemStack manuscript) {
        ItemStack target = getTargetStack(manuscript);
        if (!target.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(target.getItem());
            return Component.translatable("item." + id.getNamespace() + ".manuscript_" + id.getPath());
        }
        return super.getName(manuscript);
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
