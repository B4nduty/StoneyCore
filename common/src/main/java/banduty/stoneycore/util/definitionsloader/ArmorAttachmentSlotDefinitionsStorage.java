package banduty.stoneycore.util.definitionsloader;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorAttachmentSlotDefinitionsStorage {
    protected static final Map<String, ArmorAttachmentSlotDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    public static void mergeAndAddDefinition(ArmorAttachmentSlotDefinitionData incomingData) {
        if (incomingData.replace()) {
            DEFINITIONS.put(incomingData.slot(), incomingData);
            return;
        }

        ArmorAttachmentSlotDefinitionData existingData = DEFINITIONS.get(incomingData.slot());

        if (existingData != null) {
            List<ResourceLocation> combinedItems = new ArrayList<>(existingData.items());
            for (ResourceLocation item : incomingData.items()) {
                if (!combinedItems.contains(item)) combinedItems.add(item);
            }

            ArmorAttachmentSlotDefinitionData merged = new ArmorAttachmentSlotDefinitionData(
                    incomingData.slot().isEmpty() ? existingData.slot() : incomingData.slot(),
                    incomingData.armor().isEmpty() ? existingData.armor() : incomingData.armor(),
                    combinedItems,
                    incomingData.icon().isEmpty() ? existingData.icon() : incomingData.icon(),
                    false
            );

            DEFINITIONS.put(incomingData.slot(), merged);
        } else {
            DEFINITIONS.put(incomingData.slot(), incomingData);
        }
    }

    public static ArmorAttachmentSlotDefinitionData getData(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return getDefaultData();
        }
        return getData(itemStack.getItem());
    }

    public static ArmorAttachmentSlotDefinitionData getData(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        return DEFINITIONS.values().stream()
                .filter(def -> def.items().contains(itemId))
                .findFirst()
                .orElse(getDefaultData());
    }

    public static boolean containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        return containsItem(itemStack.getItem());
    }

    public static boolean containsItem(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        return DEFINITIONS.values().stream()
                .anyMatch(def -> def.items().contains(itemId));
    }

    public static void clearDefinitions() {
        DEFINITIONS.clear();
    }

    private static ArmorAttachmentSlotDefinitionData getDefaultData() {
        return new ArmorAttachmentSlotDefinitionData("", "", new ArrayList<>(), "", false);
    }

    public static Map<String, ArmorAttachmentSlotDefinitionData> getDefinitions() {
        return DEFINITIONS;
    }

    public static boolean shareSameSlot(ItemStack stack1, ItemStack stack2) {
        String armor1 = getData(stack1).slot();
        String armor2 = getData(stack2).slot();

        return armor1.equals(armor2);
    }

    public static List<ArmorAttachmentSlotDefinitionData> getAllAvailableSlots() {
        return new ArrayList<>(DEFINITIONS.values());
    }

    public static List<ArmorAttachmentSlotDefinitionData> getSlotsForArmorType(ArmorItem.Type type) {
        return DEFINITIONS.values().stream()
                .filter(def -> getArmorType(def) == type)
                .toList();
    }
    public static ArmorItem.Type getArmorType(ArmorAttachmentSlotDefinitionData armorAttachmentSlotDefinitionData) {
        try {
            return ArmorItem.Type.valueOf(armorAttachmentSlotDefinitionData.armor().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ArmorItem.Type.BODY;
        }
    }
}