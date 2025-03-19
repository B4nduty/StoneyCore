package banduty.stoneycore;

import banduty.stoneycore.client.SCBulletEntityRenderer;
import banduty.stoneycore.client.SCTrinketsItemRenderer;
import banduty.stoneycore.client.SCUnderArmourRenderer;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.AttackCancelHandler;
import banduty.stoneycore.event.ItemTooltipHandler;
import banduty.stoneycore.event.KeyInputHandler;
import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.items.armor.SCUnderArmorItem;
import banduty.stoneycore.items.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import banduty.stoneycore.util.DyeUtil;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
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
			if ((item instanceof SCTrinketsItem scTrinketsItem && scTrinketsItem.isDyeable()) || item instanceof SCDyeableUnderArmor) {
				ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
						tintIndex > 0 ? -1 : DyeUtil.getColor(stack), item);
			}
			if (item instanceof SCTrinketsItem) {
				TrinketRendererRegistry.registerRenderer(item, new SCTrinketsItemRenderer());
			}
			if (item instanceof SCUnderArmorItem) {
				ArmorRenderer.register(new SCUnderArmourRenderer(), item);
			}
		}
	}
}