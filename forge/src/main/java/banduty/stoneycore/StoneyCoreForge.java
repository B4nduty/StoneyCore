package banduty.stoneycore;

import banduty.stoneycore.block.ModBlockEntities;
import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.config.SCConfigs;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.AdjustAttributeModifierEvent;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.screen.ModScreenHandlers;
import banduty.stoneycore.smithing.ModRecipes;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.data.playerdata.SCAttributes;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(StoneyCore.MOD_ID)
public class StoneyCoreForge {
    public static SCConfigs CONFIG;

    public StoneyCoreForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AutoConfig.register(SCConfigs.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        CONFIG = AutoConfig.getConfigHolder(SCConfigs.class).getConfig();

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

        AdjustAttributeModifierCallback.EVENT.register(new AdjustAttributeModifierEvent());

        StoneyCore.LOG.info("Hello Forge world!");
        StoneyCore.init();
        
    }
}