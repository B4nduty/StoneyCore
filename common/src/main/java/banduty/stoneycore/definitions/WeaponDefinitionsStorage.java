package banduty.stoneycore.definitions;

import banduty.stoneycore.data.SCDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponDefinitionsStorage {
    protected static final Map<ResourceLocation, WeaponDefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    /**
     * The definition for this stack: the one named by its {@link SCDataComponents#WEAPON_DEFINITION} component if set
     * and loaded, otherwise its item's. Prefer this over {@link #getData(Item)} whenever the stack is available.
     */
    public static WeaponDefinitionData getData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return getDefaultData();
        }
        WeaponDefinitionData override = getOverride(stack);
        return override != null ? override : getData(stack.getItem());
    }

    public static WeaponDefinitionData getData(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return DEFINITIONS.getOrDefault(id, getDefaultData());
    }

    private static WeaponDefinitionData getOverride(ItemStack stack) {
        ResourceLocation id = stack.get(SCDataComponents.WEAPON_DEFINITION.get());
        return id == null ? null : DEFINITIONS.get(id);
    }

    public static boolean containsItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return getOverride(stack) != null || containsItem(stack.getItem());
    }

    public static boolean containsItem(Item item) {
        return DEFINITIONS.containsKey(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isMelee(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return isMelee(getData(stack));
    }

    public static boolean isRanged(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return isRanged(getData(stack));
    }

    public static boolean isAmmo(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return isAmmo(getData(stack));
    }

    public static boolean isMelee(Item item) {
        return isMelee(getData(item));
    }

    public static boolean isRanged(Item item) {
        return isRanged(getData(item));
    }

    public static boolean isAmmo(Item item) {
        return isAmmo(getData(item));
    }

    private static boolean isMelee(WeaponDefinitionData data) {
        return data.melee() != null && data.usage().contains(WeaponDefinitionData.Usage.MELEE);
    }

    private static boolean isRanged(WeaponDefinitionData data) {
        return data.ranged() != null && data.usage().contains(WeaponDefinitionData.Usage.RANGED);
    }

    private static boolean isAmmo(WeaponDefinitionData data) {
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

    public static Map<ResourceLocation, WeaponDefinitionData> getDefinitions() {
        return DEFINITIONS;
    }
}
