package banduty.stoneycore.util.definitionsloader;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccessoriesDefinitionsStorage {
    protected static final Map<ResourceLocation, AccessoriesDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    public static AccessoriesDefinitionData getData(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return getDefaultData();
        }
        return getData(itemStack.getItem());
    }

    public static AccessoriesDefinitionData getData(Item item) {
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

    public static void addDefinition(ResourceLocation id, AccessoriesDefinitionData data) {
        DEFINITIONS.put(id, data);
    }

    private static AccessoriesDefinitionData getDefaultData() {
        return new AccessoriesDefinitionData(0, 0, "", 0, 0, 0, new ResourceLocation("", ""));
    }
}