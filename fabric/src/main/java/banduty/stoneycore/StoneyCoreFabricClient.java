package banduty.stoneycore;

import banduty.stoneycore.block.CraftmanAnvilBlockRenderer;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.client.*;
import banduty.stoneycore.entity.SCEntities;
import banduty.stoneycore.event.AttackCancelHandler;
import banduty.stoneycore.event.ClientTickHandler;
import banduty.stoneycore.event.ItemTooltipHandler;
import banduty.stoneycore.event.KeyInputHandler;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.custom.armor.SCAccessoryItem;
import banduty.stoneycore.items.custom.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.tongs.Tongs;
import banduty.stoneycore.networking.SCPayloadsClient;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import banduty.stoneycore.particle.SCParticles;
import banduty.stoneycore.platform.*;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.SCScreenHandlers;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.render.FabricRenderTypeHelper;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class StoneyCoreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlatform.setClientPlaformHelper(new FabricClientPlatformHelper());
        ClientPlatform.setSCRenderTypeHelper(new FabricRenderTypeHelper());
        ClientPlatform.setHumanoidModelSetupAnimHelper(new FabricHumanoidModelSetupAnimHelper());
        ClientPlatform.setKeyInputHelper(new FabricKeyInputHelper());
        ClientPlatform.setRenderFirstPersonAccessoryArmorHelper(new FabricRenderFirstPersonAccessoryArmorHelper());
        SCPayloadsClient.registerPayloads();
        SCPayloadsClient.registerS2CReceivers();
        ClientPreAttackCallback.EVENT.register(new AttackCancelHandler());
        ItemTooltipCallback.EVENT.register(new ItemTooltipHandler());
        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickHandler());
        KeyInputHandler.register();
        EntityRendererRegistry.register(SCEntities.SC_BULLET.get(), SCBulletEntityRenderer::new);
        ClientOutlineRenderer.register();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof SCAccessoryItem || item instanceof SCUnderArmor) {

                ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex != 0) {
                        return -1;
                    }

                    if (item instanceof SCDyeableUnderArmor dyeable) {
                        return dyeable.getColor(stack);
                    }

                    if (item instanceof SCAccessoryItem accessory && accessory.isDyeable(stack)) {
                        return accessory.getColor(stack);
                    }

                    return -1;
                }, item);
            }
            if (item instanceof SCAccessoryItem) {
                AccessoriesRendererRegistry.registerRenderer(item, SCAccessoryItemRenderer::new);
            }
            if (item instanceof SCUnderArmor) {
                ArmorRenderer.register(new SCUnderArmourRenderer(), item);
            }

            ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath("","broken"),
                    (stack, world, entity, seed) ->
                            stack.is(SCTags.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f ? 1.0F : 0.0F);

            if (item instanceof HotIron hotIron) {
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath("","finished"),
                        (stack, world, entity, seed) ->
                                hotIron.isFinished(stack) ? 1.0F : 0.0F);
            }

            if (item instanceof Tongs) {
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath("","hotiron"),
                        (stack, world, entity, seed) ->
                                Tongs.getTargetStack(stack).getItem() instanceof HotIron ? 1.0F : 0.0F);
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath("","finished"),
                        (stack, world, entity, seed) ->
                                !(HotIron.getTargetStack(Tongs.getTargetStack(stack)).isEmpty()) ? 1.0F : 0.0F);
            }
        }
        ArmorRenderer.register(new CrownRenderer(), SCItems.CROWN.get());

        ParticleFactoryRegistry.getInstance().register(SCParticles.MUZZLES_SMOKE_PARTICLE.get(), MuzzlesSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SCParticles.MUZZLES_FLASH_PARTICLE.get(), MuzzlesFlashParticle.Factory::new);

        BlockEntityRenderers.register(SCBlocks.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(), CraftmanAnvilBlockRenderer::new);

        MenuScreens.register(SCScreenHandlers.BLUEPRINT_SCREEN_HANDLER.get(), BlueprintScreen::new);
    }
}
