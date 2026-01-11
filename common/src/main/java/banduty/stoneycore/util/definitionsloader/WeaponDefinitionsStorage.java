package banduty.stoneycore.util.definitionsloader;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponDefinitionsStorage {
    protected static final Map<ResourceLocation, WeaponDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    public static WeaponDefinitionData getData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return getDefaultData();
        }
        return getData(stack.getItem());
    }

    public static WeaponDefinitionData getData(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return DEFINITIONS.getOrDefault(id, getDefaultData());
    }

    public static boolean containsItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return containsItem(stack.getItem());
    }

    public static boolean containsItem(Item item) {
        return DEFINITIONS.containsKey(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isMelee(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return isMelee(stack.getItem());
    }

    public static boolean isRanged(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return isRanged(stack.getItem());
    }

    public static boolean isAmmo(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return isAmmo(stack.getItem());
    }

    public static boolean isMelee(Item item) {
        WeaponDefinitionData data = getData(item);
        return data.melee() != null && data.usage().contains(WeaponDefinitionData.Usage.MELEE);
    }

    public static boolean isRanged(Item item) {
        WeaponDefinitionData data = getData(item);
        return data.ranged() != null && data.usage().contains(WeaponDefinitionData.Usage.RANGED);
    }

    public static boolean isAmmo(Item item) {
        WeaponDefinitionData data = getData(item);
        return data.ammo() != null && data.usage().contains(WeaponDefinitionData.Usage.AMMO);
    }

    public static void clearDefinitions() {
        DEFINITIONS.clear();
    }

    public static void addDefinition(ResourceLocation id, WeaponDefinitionData data) {
        DEFINITIONS.put(id, data);
    }

    private static WeaponDefinitionData getDefaultData() {
        return new WeaponDefinitionData(
                EnumSet.noneOf(WeaponDefinitionData.Usage.class),
                null, null, null
        );
    }
}