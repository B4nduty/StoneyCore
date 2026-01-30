package banduty.stoneycore;

import banduty.stoneycore.block.ModBlockEntities;
import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.commands.FabricSCCommandsHandler;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.*;
import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.screen.ModScreenHandlers;
import banduty.stoneycore.recipes.ModRecipes;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.BetterCombatEvents;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import banduty.stoneycore.util.data.playerdata.SCAttributes;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.*;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;

public class StoneyCoreFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        SCAttributes.registerAttributes();
        ModSounds.registerSounds();
        ModRecipes.registerRecipes();
        SCItems.registerItems();
        ModEntities.registerEntities();
        ModMessages.registerC2SPackets();
        ModScreenHandlers.registerMenu();
        ModBlocks.registerBlocks();
        ModBlockEntities.registerBlockEntities();
        ModParticles.registerParticles();

        ServerTickEvents.START_SERVER_TICK.register(new StartTickHandler());
        PlayerBlockBreakEvents.AFTER.register(new PlayerBlockBreakAfterHandler());
        PlayerBlockBreakEvents.BEFORE.register(new PlayerBlockBreakBeforeHandler());
        UseBlockCallback.EVENT.register(new UseBlockHandler());
        if (FabricLoader.getInstance().isModLoaded("bettercombat")) BetterCombatEvents.registerEvents();
        AdjustAttributeModifierCallback.EVENT.register(new AdjustAttributeModifierEvent());
        UseEntityCallback.EVENT.register(new UseEntityHandler());
        PlayerNameTagEvents.EVENT.register(new PlayerNameTagHandler());
        CommandRegistrationCallback.EVENT.register(new FabricSCCommandsHandler());

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new WeaponDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ArmorDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new AccessoriesDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new LandDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SiegeEngineDefinitionsLoader());

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            double currentStamina = StaminaData.getStamina(player);
            StaminaData.saveStamina((IEntityDataSaver) player, currentStamina);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (NBTDataHelper.get((IEntityDataSaver) player, PDKeys.FIRST_JOIN, false)) {
                StaminaData.loadStamina(player);
                return;
            }

            player.displayClientMessage(Component.literal("""
                   §4StoneyCore §radds an overlay that makes a noise effect.
                   If you have §4epilepsy §rit is §lhighly recommended §rto §4disable Noise Effect.
                   """),
                    false);

            NBTDataHelper.set((IEntityDataSaver) player, PDKeys.FIRST_JOIN, true);

            StaminaData.setStamina(player, player.getAttributeValue(SCAttributes.MAX_STAMINA));
        });

        StoneyCore.LOG.info("Hello Fabric world!");
        StoneyCore.init();
    }
}
