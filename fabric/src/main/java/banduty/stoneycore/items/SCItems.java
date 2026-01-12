package banduty.stoneycore.items;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.item.FabricManuscript;
import banduty.stoneycore.items.item.FabricSmithingHammer;
import banduty.stoneycore.items.item.FabricTongs;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public interface SCItems {
    Item SMITHING_HAMMER = registerItem("smithing_hammer", new FabricSmithingHammer(new Item.Properties().stacksTo(1).defaultDurability(163)));

    Item BLACK_POWDER = registerItem("black_powder", new Item(new Item.Properties()));

    Item CROWN = registerItem("crown", new ArmorItem(ModArmorMaterials.CROWN, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    Item MANUSCRIPT = registerItem("manuscript", new FabricManuscript(new Item.Properties().stacksTo(1)));

    Item TONGS = registerItem("tongs", new FabricTongs(new Item.Properties().stacksTo(1)));

    Item HOT_IRON = registerItem("hot_iron", new HotIron(new Item.Properties()));

    private static void addItemsToToolsItemGroup(FabricItemGroupEntries entries) {
        entries.accept(SMITHING_HAMMER);
        entries.accept(TONGS);
    }

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.accept(BLACK_POWDER);
        entries.accept(HOT_IRON);
        entries.accept(CROWN);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(StoneyCore.MOD_ID, name), item);
    }

    static void registerItems() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(SCItems::addItemsToIngredientItemGroup);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(SCItems::addItemsToToolsItemGroup);
        StoneyCore.LOG.info("Registering Mod Items for " + StoneyCore.MOD_ID);
    }
}
