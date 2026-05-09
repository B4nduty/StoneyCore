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

import java.util.function.Supplier;

public interface SCItems {
    Supplier<Item> SMITHING_HAMMER = registerItem("smithing_hammer", () -> new SmithingHammer(new Item.Properties().stacksTo(1).durability(163)));

    Supplier<Item> BLACK_POWDER = registerItem("black_powder", () -> new Item(new Item.Properties()));

    Supplier<Item> CROWN = registerItem("crown", () -> new ArmorItem(ModArmorMaterials.CROWN, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(256)));

    Supplier<Item> MANUSCRIPT = registerItem("manuscript", () -> new Manuscript(new Item.Properties().stacksTo(1)));

    Supplier<Item> TONGS = registerItem("tongs", () -> new Tongs(new Item.Properties().stacksTo(1)));

    Supplier<Item> HOT_IRON = registerItem("hot_iron", () -> new HotIron(new Item.Properties()));

    private static Supplier<Item> registerItem(String name, Supplier<Item> itemSupplier) {
        return Services.PLATFORM.register(BuiltInRegistries.ITEM, name, itemSupplier);
    }

    static void register() {
        StoneyCore.LOG.info("Registering Mod Items for " + StoneyCore.MOD_ID);
    }
}