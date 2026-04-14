package banduty.stoneycore;

import banduty.stoneycore.block.ModBlockEntities;
import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.config.SCConfigs;
import banduty.stoneycore.config.SCVisualConfigs;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.AdjustAttributeModifierEvent;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.recipes.ModRecipes;
import banduty.stoneycore.screen.ModScreenHandlers;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.data.playerdata.SCAttributes;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(StoneyCore.MOD_ID)
public class StoneyCoreForge {

    public StoneyCoreForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        SCConfigs.loadConfig(SCConfigs.SPEC, FMLPaths.CONFIGDIR.get().resolve(StoneyCore.MOD_ID + "-common.toml"));
        SCVisualConfigs.loadConfig(SCVisualConfigs.SPEC, FMLPaths.CONFIGDIR.get().resolve(StoneyCore.MOD_ID + "-client.toml"));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SCConfigs.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SCVisualConfigs.SPEC);

        SCAttributes.register(modEventBus);
        ModSounds.registerSounds(modEventBus);
        ModRecipes.register(modEventBus);
        SCItems.registerItems(modEventBus);
        ModEntities.registerEntities(modEventBus);
        ModMessages.register();
        ModScreenHandlers.register(modEventBus);
        ModBlocks.registerBlocks(modEventBus);
        ModBlockEntities.registerBlockEntities(modEventBus);
        ModParticles.registerParticles(modEventBus);

        if (ModList.get().isLoaded("accessories")) {
            AdjustAttributeModifierCallback.EVENT.register(new AdjustAttributeModifierEvent());
        }

        StoneyCore.LOG.info("Hello Forge world!");
        StoneyCore.init();

    }
}