package banduty.stoneycore.items;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.item.SmithingHammer;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKeys;

public class SCItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(StoneyCore.MOD_ID, RegistryKeys.ITEM);

    public static final RegistrySupplier<Item> SMITHING_HAMMER = ITEMS.register("smithing_hammer", () ->
            new SmithingHammer(new Item.Settings().maxCount(1).maxDamage(20)));

    public static final RegistrySupplier<Item> BLACK_POWDER = ITEMS.register("black_powder", () ->
            new Item(new Item.Settings()));

    public static final RegistrySupplier<Item> CROWN = ITEMS.register("crown", () ->
            new ArmorItem(ModArmorMaterials.CROWN, ArmorItem.Type.HELMET, new Item.Settings().maxCount(1)));

    private static void addItemsToToolsItemGroup(FabricItemGroupEntries entries) {
        entries.add(SMITHING_HAMMER.get());
    }

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.add(BLACK_POWDER.get());
        entries.add(CROWN.get());
    }

    public static void registerItems() {
        ITEMS.register();
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(SCItems::addItemsToIngredientItemGroup);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(SCItems::addItemsToToolsItemGroup);
        StoneyCore.LOGGER.info("Registering Mod Items for " + StoneyCore.MOD_ID);
    }
}
