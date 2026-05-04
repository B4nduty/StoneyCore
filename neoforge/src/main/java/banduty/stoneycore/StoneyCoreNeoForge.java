package banduty.stoneycore;

import banduty.stoneycore.config.SCConfigs;
import banduty.stoneycore.config.SCVisualConfigs;
import banduty.stoneycore.event.AdjustAttributeModifierEvent;
import banduty.stoneycore.platform.NeoForgePlatformHelper;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(StoneyCore.MOD_ID)
public class StoneyCoreNeoForge {

    public StoneyCoreNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, SCConfigs.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, SCVisualConfigs.SPEC);

        NeoForgePlatformHelper.registerRegistries(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        
        if (ModList.get().isLoaded("accessories")) {
            AdjustAttributeModifierCallback.EVENT.register(new AdjustAttributeModifierEvent());
        }

        StoneyCore.LOG.info("Hello Neo Forge world!");
        StoneyCore.init();

    }
}