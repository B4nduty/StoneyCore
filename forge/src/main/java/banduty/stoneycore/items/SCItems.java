package banduty.stoneycore.items;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.armor.CrownItem;
import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.item.ForgeManuscript;
import banduty.stoneycore.items.item.ForgeSmithingHammer;
import banduty.stoneycore.items.item.ForgeTongs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface SCItems {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, StoneyCore.MOD_ID);

    RegistryObject<Item> SMITHING_HAMMER = ITEMS.register("smithing_hammer", () ->
            new ForgeSmithingHammer(new Item.Properties().stacksTo(1).defaultDurability(163)));

    RegistryObject<Item> BLACK_POWDER = ITEMS.register("black_powder", () ->
            new Item(new Item.Properties()));

    RegistryObject<Item> CROWN = ITEMS.register("crown", () ->
            new CrownItem(ModArmorMaterials.CROWN, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    RegistryObject<Item> MANUSCRIPT = ITEMS.register("manuscript", () ->
            new ForgeManuscript(new Item.Properties().stacksTo(1)));

    RegistryObject<Item> TONGS = ITEMS.register("tongs", () ->
            new ForgeTongs(new Item.Properties().stacksTo(1)));

    RegistryObject<Item> HOT_IRON = ITEMS.register("hot_iron", () ->
            new HotIron(new Item.Properties()));

    static void registerItems(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.addListener(SCItems::addItemsToCreativeTabs);
        StoneyCore.LOG.info("Registering Mod Items for " + StoneyCore.MOD_ID);
    }

    private static void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(BLACK_POWDER.get());
            event.accept(HOT_IRON.get());
            event.accept(CROWN.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(SMITHING_HAMMER.get());
            event.accept(TONGS.get());
        }
    }
}