package banduty.stoneycore;

import banduty.stoneycore.client.SCBulletEntityRenderer;
import banduty.stoneycore.client.SCAccessoryItemRenderer;
import banduty.stoneycore.client.SCUnderArmourRenderer;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.*;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.items.armor.ISCUnderArmor;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

public class StoneyCoreClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModMessages.registerS2CPackets();
		ClientPreAttackCallback.EVENT.register(new AttackCancelHandler());
		KeyInputHandler.register();

		EntityRendererRegistry.register(ModEntities.SC_BULLET, SCBulletEntityRenderer::new);

		ItemTooltipCallback.EVENT.register(new ItemTooltipHandler());

		ParticleFactoryRegistry.getInstance().register(ModParticles.MUZZLES_SMOKE_PARTICLE, MuzzlesSmokeParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.MUZZLES_FLASH_PARTICLE, MuzzlesFlashParticle.Factory::new);

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
	}
}