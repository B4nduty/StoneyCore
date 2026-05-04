package banduty.stoneycore.items;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.SmithingHammer;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.manuscript.Manuscript;
import banduty.stoneycore.items.custom.tongs.Tongs;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public interface SCItems {
    Item SMITHING_HAMMER = registerItem("smithing_hammer", new SmithingHammer(new Item.Properties().stacksTo(1).durability(163)));

    Item BLACK_POWDER = registerItem("black_powder", new Item(new Item.Properties()));

    Item CROWN = registerItem("crown", new ArmorItem(ModArmorMaterials.CROWN, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    Item MANUSCRIPT = registerItem("manuscript", new Manuscript(new Item.Properties().stacksTo(1)));

    Item TONGS = registerItem("tongs", new Tongs(new Item.Properties().stacksTo(1)));

    Item HOT_IRON = registerItem("hot_iron", new HotIron(new Item.Properties()));

    private static Item registerItem(String name, Item item) {
        return Services.PLATFORM.register(BuiltInRegistries.ITEM, name, () -> item).get();
    }

    static void register() {
        StoneyCore.LOG.info("Registering Mod Items for " + StoneyCore.MOD_ID);
    }
}