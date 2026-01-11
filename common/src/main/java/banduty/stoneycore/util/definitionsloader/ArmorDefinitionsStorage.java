package banduty.stoneycore.util.definitionsloader;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorDefinitionsStorage {
    protected static final Map<ResourceLocation, ArmorDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    public static ArmorDefinitionData getData(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return getDefaultData();
        }
        return getData(itemStack.getItem());
    }

    public static ArmorDefinitionData getData(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return DEFINITIONS.getOrDefault(itemId, getDefaultData());
    }

    public static boolean containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        return containsItem(itemStack.getItem());
    }

    public static boolean containsItem(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return DEFINITIONS.containsKey(itemId);
    }

    public static void clearDefinitions() {
        DEFINITIONS.clear();
    }

    public static void addDefinition(ResourceLocation id, ArmorDefinitionData data) {
        DEFINITIONS.put(id, data);
    }

    private static ArmorDefinitionData getDefaultData() {
        return new ArmorDefinitionData(Map.of(), Map.of(), 0);
    }
}