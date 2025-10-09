package banduty.stoneycore;

import banduty.stoneycore.combat.range.BowHandler;
import banduty.stoneycore.combat.range.CrossbowHandler;
import banduty.stoneycore.combat.range.MusketHandler;
import banduty.stoneycore.combat.range.RangedWeaponHandlers;
import banduty.stoneycore.commands.SCCommandsHandler;
import banduty.stoneycore.config.StoneyCoreConfig;
import banduty.stoneycore.datagen.ModItemTagProvider;
import banduty.stoneycore.datagen.ModModelProvider;
import banduty.stoneycore.datagen.ModRecipeProvider;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.*;
import banduty.stoneycore.event.custom.CraftingPreviewCallback;
import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.screen.ModScreenHandlers;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.*;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import banduty.stoneycore.util.data.playerdata.SCAttributes;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoneyCore implements ModInitializer, DataGeneratorEntrypoint {
	public static final String MOD_ID = "stoneycore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final StoneyCoreConfig CONFIG = StoneyCoreConfig.createAndLoad();

	public static final DeferredRegister<EntityAttribute> ATTRIBUTES = DeferredRegister.create(MOD_ID, RegistryKeys.ATTRIBUTE);

	public static final RegistrySupplier<EntityAttribute> HUNGER_DRAIN_MULTIPLIER = ATTRIBUTES.register("hunger_drain_multiplier", () -> SCAttributes.HUNGER_DRAIN_MULTIPLIER);
	public static final RegistrySupplier<EntityAttribute> STAMINA = ATTRIBUTES.register("stamina", () -> SCAttributes.STAMINA);
	public static final RegistrySupplier<EntityAttribute> MAX_STAMINA = ATTRIBUTES.register("max_stamina", () -> SCAttributes.MAX_STAMINA);


	@Override
	public void onInitialize() {
		ATTRIBUTES.register();
		ModSounds.registerSounds();
		SCItems.registerItems();
		ModEntities.registerEntities();
		ModMessages.registerC2SPackets();
		ModScreenHandlers.registerScreenHandlers();
		ServerTickEvents.START_SERVER_TICK.register(new StartTickHandler());
		PlayerBlockBreakEvents.AFTER.register(new PlayerBlockBreakAfterHandler());
		PlayerBlockBreakEvents.BEFORE.register(new PlayerBlockBreakBeforeHandler());
		UseBlockCallback.EVENT.register(new UseBlockHandler());
		BetterCombatClientEvents.ATTACK_HIT.register(new PlayerAttackHitHandler());
		AdjustAttributeModifierCallback.EVENT.register(new AdjustAttributeModifierEvent());
		UseEntityCallback.EVENT.register(new UseEntityHandler());
        PlayerNameTagEvents.EVENT.register(new PlayerNameTagHandler());
        CraftingPreviewCallback.EVENT.register(new CraftingPreviewHandler());
		ModParticles.registerParticles();
        CommandRegistrationCallback.EVENT.register(new SCCommandsHandler());

        RangedWeaponHandlers.register(new BowHandler());
        RangedWeaponHandlers.register(new CrossbowHandler());
        RangedWeaponHandlers.register(new MusketHandler());

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new WeaponDefinitionsLoader());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ArmorDefinitionsLoader());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AccessoriesDefinitionsLoader());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new LandDefinitionsLoader());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SiegeEngineDefinitionsLoader());

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (player != null) {
                double currentStamina = StaminaData.getStamina(player);
                StaminaData.saveStamina((IEntityDataSaver) player, currentStamina);
            }
        });

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			if (player != null) {
				if (NBTDataHelper.get((IEntityDataSaver) player, PDKeys.FIRST_JOIN, false)) {
                    StaminaData.loadStamina(player);
                    return;
				}
                
                player.sendMessage(Text.literal("""
                       §4StoneyCore §radds an overlay that makes a noise effect.
                       If you have §4epilepsy §rit is §lhighly recommended §rto §4disable Noise Effect.
                       """),
                        false);

                NBTDataHelper.set((IEntityDataSaver) player, PDKeys.FIRST_JOIN, true);

                StaminaData.setStamina(player, player.getAttributeValue(StoneyCore.MAX_STAMINA.get()));
			}
		});
	}

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModItemTagProvider::new);
	}

	public static StoneyCoreConfig getConfig() {
		return CONFIG;
	}
}