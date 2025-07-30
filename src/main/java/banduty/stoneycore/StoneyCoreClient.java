package banduty.stoneycore;

import banduty.stoneycore.client.CrownRenderer;
import banduty.stoneycore.client.SCAccessoryItemRenderer;
import banduty.stoneycore.client.SCBulletEntityRenderer;
import banduty.stoneycore.client.SCUnderArmourRenderer;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.*;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.armor.ISCUnderArmor;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.OutlineClaimS2CPacket;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.ModScreenHandlers;
import banduty.stoneycore.util.render.LandTitleRenderer;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.platform.Platform;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

public class StoneyCoreClient implements ClientModInitializer {
	public static final LandTitleRenderer LAND_TITLE_RENDERER = new LandTitleRenderer();

	@Override
	public void onInitializeClient() {
		ModMessages.registerS2CPackets();
		ClientPreAttackCallback.EVENT.register(new AttackCancelHandler());
		ItemTooltipCallback.EVENT.register(new ItemTooltipHandler());
		ClientTickEvents.END_CLIENT_TICK.register(new ClientTickHandler());
		KeyInputHandler.register();

		if (Platform.isForge()) {
			ClientLifecycleEvent.CLIENT_SETUP.register(minecraftClient -> {
				registerClient();
			});
		} else {
			registerClient();
		}
	}

	private void registerClient() {
		EntityRendererRegistry.register(ModEntities.SC_BULLET.get(), SCBulletEntityRenderer::new);
		for (Item item : Registries.ITEM) {
			if (((item instanceof SCAccessoryItem || item instanceof ISCUnderArmor) && item instanceof DyeableItem dyeableItem)) {
				ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
						tintIndex > 0 ? -1 : dyeableItem.getColor(stack), item);
			}
			if (item instanceof SCAccessoryItem) {
				AccessoriesRendererRegistry.registerRenderer(item, SCAccessoryItemRenderer::new);
			}
			if (item instanceof ISCUnderArmor) {
				ArmorRenderer.register(new SCUnderArmourRenderer(), item);
			}
		}
		ArmorRenderer.register(new CrownRenderer(), SCItems.CROWN.get());

		ParticleFactoryRegistry.getInstance().register(ModParticles.MUZZLES_SMOKE_PARTICLE.get(), MuzzlesSmokeParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.MUZZLES_FLASH_PARTICLE.get(), MuzzlesFlashParticle.Factory::new);

		HandledScreens.register(ModScreenHandlers.BLUEPRINT_SCREEN_HANDLER, BlueprintScreen::new);

		OutlineClaimS2CPacket.registerRenderer();
	}
}