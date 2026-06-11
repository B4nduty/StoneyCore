package banduty.stoneycore;

import banduty.stoneycore.commands.FabricSCCommandsHandler;
import banduty.stoneycore.event.*;
import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.networking.SCC2SNetworking;
import banduty.stoneycore.networking.SCPayloads;
import banduty.stoneycore.networking.payload.SyncDefinitionsPacket;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import banduty.stoneycore.util.definitionsloader.*;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTabs;

public class StoneyCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Services.PLATFORM.getConfig();
        SCPayloads.registerPayloads();
        SCC2SNetworking.registerC2SNetworking();
        HotIronCoolingHandler.init();
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(StoneyCoreFabric::addItemsToIngredientItemGroup);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(StoneyCoreFabric::addItemsToToolsItemGroup);

        ServerTickEvents.START_SERVER_TICK.register(new StartTickHandler());
        PlayerBlockBreakEvents.AFTER.register(new PlayerBlockBreakAfterHandler());
        PlayerBlockBreakEvents.BEFORE.register(new PlayerBlockBreakBeforeHandler());
        UseBlockCallback.EVENT.register(new UseBlockHandler());
        UseBlockCallback.EVENT.register(new VisitorUseBlock());
        ServerLivingEntityEvents.AFTER_DEATH.register(new VisitorDeath());
        if (FabricLoader.getInstance().isModLoaded("bettercombat")) {
            BetterCombatClientEvents.ATTACK_HIT.register(new PlayerAttackHitHandler());
        }

        UseEntityCallback.EVENT.register(new UseEntityHandler());
        PlayerNameTagEvents.EVENT.register(new PlayerNameTagHandler());
        CommandRegistrationCallback.EVENT.register(new FabricSCCommandsHandler());
        TongsPickupHandler.register();

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new WeaponDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ArmorDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ArmorAttachmentDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ArmorAttachmentSlotDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new LandDefinitionsLoader());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SiegeEngineDefinitionsLoader());
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {

            if (!(oldPlayer instanceof IEntityDataSaver oldSaver)) return;
            if (!(newPlayer instanceof IEntityDataSaver newSaver)) return;

            CompoundTag oldData = oldSaver.stoneycore$getPersistentData();
            CompoundTag newData = newSaver.stoneycore$getPersistentData();

            newData.merge(oldData); // Fabric-friendly copy
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();

            IEntityDataSaver saver = (IEntityDataSaver) player;
            double currentStamina = StaminaData.getStamina(player);
            StaminaData.saveStamina(saver, currentStamina);

        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.execute(() -> {
                ServerPlayer player = handler.getPlayer();

                sendSyncDefinitions(player);

                if (((IEntityDataSaver) player).stoneycore$getPersistentData().getBoolean("firstJoin")) {
                    StaminaData.loadStamina(player);
                    return;
                }

                player.displayClientMessage(Component.literal("""
                                §4StoneyCore §radds an overlay that makes a noise effect.
                                If you have §4epilepsy §rit is §lhighly recommended §rto §4disable Noise Effect.
                                """),
                        false);

                ((IEntityDataSaver) player).stoneycore$getPersistentData().putBoolean("firstJoin", true);

                StaminaData.setStamina(player, player.getAttributeValue(SCAttributes.MAX_STAMINA));
            });
        });
        StoneyCore.LOG.info("Hello Fabric world!");
        StoneyCore.init();
    }

    public static void sendSyncDefinitions(ServerPlayer player) {
        ServerPlayNetworking.send(player, new SyncDefinitionsPacket(ArmorDefinitionsStorage.getDefinitions(),
                ArmorAttachmentDefinitionsStorage.getDefinitions(), ArmorAttachmentSlotDefinitionsStorage.getDefinitions(),
                LandDefinitionsStorage.getDefinitions(), SiegeEngineDefinitionsStorage.getDefinitions(),
                WeaponDefinitionsStorage.getDefinitions()));
    }

    private static void addItemsToToolsItemGroup(FabricItemGroupEntries entries) {
        entries.accept(SCItems.SMITHING_HAMMER.get());
        entries.accept(SCItems.TONGS.get());
        entries.accept(SCItems.MANUSCRIPT.get());
    }

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.accept(SCItems.BLACK_POWDER.get());
        entries.accept(SCItems.HOT_IRON.get());
        entries.accept(SCItems.CROWN.get());
    }
}
