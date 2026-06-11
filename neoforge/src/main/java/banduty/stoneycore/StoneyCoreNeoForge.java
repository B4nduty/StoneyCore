package banduty.stoneycore;

import banduty.stoneycore.config.SCConfigs;
import banduty.stoneycore.config.SCVisualConfigs;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.platform.NeoForgePlatformHelper;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(StoneyCore.MOD_ID)
public class StoneyCoreNeoForge {

    public StoneyCoreNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        StoneyCore.init();
        modContainer.registerConfig(ModConfig.Type.COMMON, SCConfigs.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, SCVisualConfigs.SPEC);

        NeoForgePlatformHelper.registerRegistries(modEventBus);
        modEventBus.addListener(StoneyCoreNeoForge::addItemsToCreativeTabs);

        StoneyCore.LOG.info("Hello Neo Forge world!");

    }

    private static void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(SCItems.BLACK_POWDER.get());
            event.accept(SCItems.HOT_IRON.get());
            event.accept(SCItems.CROWN.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(SCItems.SMITHING_HAMMER.get());
            event.accept(SCItems.TONGS.get());
        }
    }
}