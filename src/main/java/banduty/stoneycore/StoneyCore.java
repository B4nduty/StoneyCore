package banduty.stoneycore;

import banduty.stoneycore.config.StoneyCoreConfig;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.*;
import banduty.stoneycore.event.custom.LivingEntityDamageEvents;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.definitionsloader.SCAccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCRangedWeaponDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.playerdata.SCAttributes;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoneyCore implements ModInitializer {
	public static final String MOD_ID = "stoneycore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final StoneyCoreConfig CONFIG = StoneyCoreConfig.createAndLoad();
	private static final String FIRST_JOIN_TAG = MOD_ID + ":first_join";

	@Override
	public void onInitialize() {
		Registry.register(Registries.ATTRIBUTE, new Identifier(MOD_ID, "hunger_drain_multiplier"), SCAttributes.HUNGER_DRAIN_MULTIPLIER);
		Registry.register(Registries.ATTRIBUTE, new Identifier(MOD_ID, "stamina"), SCAttributes.STAMINA);
		Registry.register(Registries.ATTRIBUTE, new Identifier(MOD_ID, "max_stamina"), SCAttributes.MAX_STAMINA);

		ModEntities.registerEntities();
		ModSounds.registerSounds();
		ModMessages.registerC2SPackets();
		LivingEntityDamageEvents.EVENT.register(new EntityDamageHandler());
		ServerTickEvents.START_SERVER_TICK.register(new StartTickHandler());
		PlayerBlockBreakEvents.AFTER.register(new PlayerBlockBreakHandler());
		BetterCombatClientEvents.ATTACK_HIT.register(new PlayerAttackHitHandler());
		AdjustAttributeModifierCallback.EVENT.register(new AdjustAttributeModifierEvent());
		ModParticles.registerParticles();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SCMeleeWeaponDefinitionsLoader());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SCRangedWeaponDefinitionsLoader());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SCArmorDefinitionsLoader());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SCAccessoriesDefinitionsLoader());

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			if (player != null) {
				NbtCompound playerData = ((IEntityDataSaver) player).stoneycore$getPersistentData();
				if (!playerData.getBoolean(FIRST_JOIN_TAG)) {
					player.sendMessage(Text.literal("""
                       §4StoneyCore §radds an overlay that makes a noise effect.
                       If you have §4epilepsy §rit is §lhighly recommended §rto §4disable Noise Effect.
                       """),
							false);

					playerData.putBoolean(FIRST_JOIN_TAG, true);
				}
			}
		});
	}

	public static StoneyCoreConfig getConfig() {
		return CONFIG;
	}
}