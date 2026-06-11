package banduty.stoneycore.util.definitionsloader;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorAttachmentDefinitionsStorage {
    protected static final Map<ResourceLocation, ArmorAttachmentDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    public static ArmorAttachmentDefinitionData getData(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return getDefaultData();
        }
        return getData(itemStack.getItem());
    }

    public static ArmorAttachmentDefinitionData getData(Item item) {
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

    public static void addDefinition(ResourceLocation id, ArmorAttachmentDefinitionData data) {
        DEFINITIONS.put(id, data);
    }

    private static ArmorAttachmentDefinitionData getDefaultData() {
        return new ArmorAttachmentDefinitionData(0, 0, "", 0, 0, 0, 0, 0, ResourceLocation.fromNamespaceAndPath("", ""));
    }

    public static Map<ResourceLocation, ArmorAttachmentDefinitionData> getDefinitions() {
        return DEFINITIONS;
    }
}