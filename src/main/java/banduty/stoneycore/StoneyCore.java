package banduty.stoneycore;

import banduty.stoneycore.config.StoneyCoreConfig;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.*;
import banduty.stoneycore.event.custom.LivingEntityDamageEvents;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCRangedWeaponDefinitionsLoader;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoneyCore implements ModInitializer {
	public static final String MOD_ID = "stoneycore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final StoneyCoreConfig CONFIG = StoneyCoreConfig.createAndLoad();

	@Override
	public void onInitialize() {
		ModEntities.registerEntities();
		ModSounds.registerSounds();
		ModMessages.registerC2SPackets();
		LivingEntityDamageEvents.EVENT.register(new EntityDamageHandler());
		ServerTickEvents.START_SERVER_TICK.register(new StartTickHandler());
		ServerTickEvents.START_SERVER_TICK.register(new ReloadTickHandler());
		PlayerBlockBreakEvents.AFTER.register(new PlayerBlockBreakHandler());
		BetterCombatClientEvents.ATTACK_HIT.register(new PlayerAttackHit());
		ModParticles.registerParticles();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SCMeleeWeaponDefinitionsLoader());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SCRangedWeaponDefinitionsLoader());
	}

	public static StoneyCoreConfig getConfig() {
		return CONFIG;
	}
}