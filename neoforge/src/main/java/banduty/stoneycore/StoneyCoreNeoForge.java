package banduty.stoneycore;

import banduty.stoneycore.config.SCConfigs;
import banduty.stoneycore.config.SCVisualConfigs;
import banduty.stoneycore.platform.NeoForgePlatformHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(StoneyCore.MOD_ID)
public class StoneyCoreNeoForge {

    public StoneyCoreNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        StoneyCore.init();
        modContainer.registerConfig(ModConfig.Type.COMMON, SCConfigs.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, SCVisualConfigs.SPEC);

        NeoForgePlatformHelper.registerRegistries(modEventBus);

        StoneyCore.LOG.info("Hello Neo Forge world!");

    }
}