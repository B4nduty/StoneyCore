package banduty.stoneycore;

import banduty.stoneycore.block.CraftmanAnvilBlockRenderer;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.client.ClientOutlineRenderer;
import banduty.stoneycore.client.CrownRenderer;
import banduty.stoneycore.client.SCBulletEntityRenderer;
import banduty.stoneycore.client.item.ClientUnderArmorTooltip;
import banduty.stoneycore.entity.SCEntities;
import banduty.stoneycore.event.AttackCancelHandler;
import banduty.stoneycore.event.ClientTickHandler;
import banduty.stoneycore.event.ItemTooltipHandler;
import banduty.stoneycore.event.KeyInputHandler;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorTooltip;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import banduty.stoneycore.items.custom.tongs.Tongs;
import banduty.stoneycore.model.*;
import banduty.stoneycore.networking.SCS2CNetworking;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import banduty.stoneycore.particle.SCParticles;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.platform.FabricHumanoidModelSetupAnimHelper;
import banduty.stoneycore.platform.FabricKeyInputHelper;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.SCScreenHandlers;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.data.itemdata.SCTags;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StoneyCoreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlatform.setHumanoidModelSetupAnimHelper(new FabricHumanoidModelSetupAnimHelper());
        ClientPlatform.setKeyInputHelper(new FabricKeyInputHelper());
        SCS2CNetworking.registerS2CNetworking();
        ClientPreAttackCallback.EVENT.register(new AttackCancelHandler());
        ItemTooltipCallback.EVENT.register(new ItemTooltipHandler());
        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickHandler());
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof UnderArmorTooltip(UnderArmorContents contents, ArmorItem.Type armorType)) {
                return new ClientUnderArmorTooltip(contents, armorType);
            }
            return null;
        });

        KeyInputHandler.register();
        EntityRendererRegistry.register(SCEntities.SC_BULLET.get(), SCBulletEntityRenderer::new);
        ClientOutlineRenderer.register();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "broken"),
                    (stack, world, entity, seed) ->
                            stack.is(SCTags.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f ? 1.0F : 0.0F);

            if (item instanceof Tongs tongs) {
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "hotiron"),
                        (stack, world, entity, seed) -> {
                            ItemStack itemStack = tongs.getCapturedItem(stack);
                            if (!(itemStack.getItem() instanceof QuenchItem quenchItem)) return 0.0F;
                            return itemStack.getItem() == SCItems.HOT_IRON.get() || !quenchItem.isFinished(stack) ? 1.0F : 0.0F;
                        });
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "finished"),
                        (stack, world, entity, seed) -> {
                            ItemStack itemStack = tongs.getCapturedItem(stack);
                            if (!(itemStack.getItem() instanceof QuenchItem quenchItem)) return 0.0F;
                            return quenchItem.isFinished(itemStack) ? 1.0F : 0.0F;
                        });
            }

            if (item instanceof QuenchItem && item != SCItems.HOT_IRON.get()) {
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "ignited"),
                        (stack, world, entity, seed) ->
                                !stack.getOrDefault(SCDataComponents.FINISHED.get(), false) ? 1.0F : 0.0F);
            }
        }
        ArmorRenderer.register(new CrownRenderer(), SCItems.CROWN.get());

        ParticleFactoryRegistry.getInstance().register(SCParticles.MUZZLES_SMOKE_PARTICLE.get(), MuzzlesSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SCParticles.MUZZLES_FLASH_PARTICLE.get(), MuzzlesFlashParticle.Factory::new);

        BlockEntityRenderers.register(SCBlocks.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(), CraftmanAnvilBlockRenderer::new);

        MenuScreens.register(SCScreenHandlers.BLUEPRINT_SCREEN_HANDLER.get(), BlueprintScreen::new);

        EntityModelLayerRegistry.registerModelLayer(UnderArmourHelmetModel.LAYER_LOCATION, UnderArmourHelmetModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(UnderArmourChestplateModel.LAYER_LOCATION, UnderArmourChestplateModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(UnderArmourLeggingsModel.LAYER_LOCATION, UnderArmourLeggingsModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(UnderArmourBootsModel.LAYER_LOCATION, UnderArmourBootsModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(CrownModel.LAYER_LOCATION, CrownModel::getTexturedModelData);

        ModelLoadingPlugin.register(new SCModelLoadingPlugin());
    }
}
