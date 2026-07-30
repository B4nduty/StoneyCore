package banduty.stoneycore.items.custom.manuscript;

import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface ManuscriptType extends StringRepresentable {
    Map<String, ManuscriptType> REGISTRY = new LinkedHashMap<>();
    Map<Item, ManuscriptType> ITEM_TO_TYPE = new LinkedHashMap<>();

    Item getManuscriptItem();

    Item getHotIronItem();

    Item getTongsItem();

    static ManuscriptType register(ManuscriptType type) {
        String name = type.getSerializedName();

        if (REGISTRY.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate ManuscriptType key registration attempt: " + name);
        }

        REGISTRY.put(name, type);

        // Map base item, hot iron item, and tongs item to this central type
        if (type.getManuscriptItem() != null) mapItem(type.getManuscriptItem(), type);
        if (type.getHotIronItem() != null) mapItem(type.getHotIronItem(), type);
        if (type.getTongsItem() != null) mapItem(type.getTongsItem(), type);

        return type;
    }

    private static void mapItem(Item item, ManuscriptType type) {
        if (ITEM_TO_TYPE.containsKey(item)) {
            throw new IllegalArgumentException("Duplicate ManuscriptType Item registration for item: " + item);
        }
        ITEM_TO_TYPE.put(item, type);
    }

    static ManuscriptType fromString(String name) {
        return REGISTRY.get(name);
    }

    static ManuscriptType fromItem(Item item) {
        return ITEM_TO_TYPE.get(item);
    }

    static Collection<ManuscriptType> getAllTypes() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    static Map<String, ManuscriptType> getRegistry() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    static ItemStack createForManuscriptType(ManuscriptType manuscriptType, Item item) {
        if (manuscriptType == null || item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        setManuscriptType(stack, manuscriptType);
        return stack;
    }

    static boolean hasManuscriptType(ItemStack stack) {
        return getManuscriptType(stack) != null;
    }

    static void setManuscriptType(ItemStack stack, ManuscriptType manuscriptType) {
        if (manuscriptType == null) return;
        stack.set(SCDataComponents.MANUSCRIPT_TYPE.get(), manuscriptType.getSerializedName());
    }

    static ManuscriptType getManuscriptType(ItemStack stack) {
        if (stack.has(SCDataComponents.MANUSCRIPT_TYPE.get())) {
            String typeName = stack.get(SCDataComponents.MANUSCRIPT_TYPE.get());
            if (typeName != null) {
                return ManuscriptType.fromString(typeName);
            }
        }
        return null;
    }

    static Item getManuscriptItem(ItemStack stack) {
        ManuscriptType type = getManuscriptType(stack);
        if (type != null) {
            return type.getManuscriptItem();
        }
        return Items.AIR;
    }

    static void removeManuscriptType(ItemStack stack) {
        stack.remove(SCDataComponents.MANUSCRIPT_TYPE.get());
    }
}