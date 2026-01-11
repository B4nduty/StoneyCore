package banduty.stoneycore.items;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.item.FabricManuscript;
import banduty.stoneycore.items.item.FabricSmithingHammer;
import banduty.stoneycore.items.item.FabricTongs;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public interface SCItems {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(StoneyCore.MOD_ID, Registries.ITEM);

    RegistrySupplier<Item> SMITHING_HAMMER = ITEMS.register("smithing_hammer", () ->
            new FabricSmithingHammer(new Item.Properties().stacksTo(1).defaultDurability(163)));

    RegistrySupplier<Item> BLACK_POWDER = ITEMS.register("black_powder", () ->
            new Item(new Item.Properties()));

    RegistrySupplier<Item> CROWN = ITEMS.register("crown", () ->
            new ArmorItem(ModArmorMaterials.CROWN, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    RegistrySupplier<Item> MANUSCRIPT = ITEMS.register("manuscript", () ->
            new FabricManuscript(new Item.Properties().stacksTo(1)));

    RegistrySupplier<Item> TONGS = ITEMS.register("tongs", () ->
            new FabricTongs(new Item.Properties().stacksTo(1)));

    RegistrySupplier<Item> HOT_IRON = ITEMS.register("hot_iron", () ->
            new HotIron(new Item.Properties()));

    private static void addItemsToToolsItemGroup(FabricItemGroupEntries entries) {
        entries.accept(SMITHING_HAMMER.get());
        entries.accept(TONGS.get());
    }

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.accept(BLACK_POWDER.get());
        entries.accept(HOT_IRON.get());
        entries.accept(CROWN.get());
    }

    static void registerItems() {
        ITEMS.register();
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(SCItems::addItemsToIngredientItemGroup);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(SCItems::addItemsToToolsItemGroup);
        StoneyCore.LOG.info("Registering Mod Items for " + StoneyCore.MOD_ID);
    }
}
