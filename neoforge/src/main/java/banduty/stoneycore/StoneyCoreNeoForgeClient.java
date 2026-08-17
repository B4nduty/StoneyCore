package banduty.stoneycore;

import banduty.stoneycore.block.CraftmanAnvilBlockRenderer;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.client.SCBulletEntityRenderer;
import banduty.stoneycore.client.item.ClientUnderArmorTooltip;
import banduty.stoneycore.entity.SCEntities;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorTooltip;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import banduty.stoneycore.items.custom.tongs.Tongs;
import banduty.stoneycore.model.*;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import banduty.stoneycore.particle.SCParticles;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.platform.NeoForgeHumanoidModelSetupAnimHelper;
import banduty.stoneycore.platform.NeoForgeKeyInputHelper;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.SCScreenHandlers;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.data.itemdata.SCTags;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StoneyCoreNeoForgeClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientPlatform.setHumanoidModelSetupAnimHelper(new NeoForgeHumanoidModelSetupAnimHelper());
            ClientPlatform.setKeyInputHelper(new NeoForgeKeyInputHelper());

            BlockEntityRenderers.register(
                    SCBlocks.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(),
                    CraftmanAnvilBlockRenderer::new
            );

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
                    ItemProperties.register(
                            item,
                            ResourceLocation.fromNamespaceAndPath(
                                    StoneyCore.MOD_ID,
                                    "ignited"
                            ),
                            (stack, world, entity, seed) ->
                                    stack.getOrDefault(
                                            SCDataComponents.IGNITED.get(),
                                            false
                                    ) ? 1.0F : 0.0F
                    );
                }
            }
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                SCScreenHandlers.BLUEPRINT_SCREEN_HANDLER.get(),
                BlueprintScreen::new
        );
    }


    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(UnderArmourHelmetModel.LAYER_LOCATION, UnderArmourHelmetModel::getTexturedModelData);
        event.registerLayerDefinition(UnderArmourChestplateModel.LAYER_LOCATION, UnderArmourChestplateModel::getTexturedModelData);
        event.registerLayerDefinition(UnderArmourLeggingsModel.LAYER_LOCATION, UnderArmourLeggingsModel::getTexturedModelData);
        event.registerLayerDefinition(UnderArmourBootsModel.LAYER_LOCATION, UnderArmourBootsModel::getTexturedModelData);
        event.registerLayerDefinition(CrownModel.LAYER_LOCATION, CrownModel::getTexturedModelData);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SCEntities.SC_BULLET.get(), SCBulletEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SCParticles.MUZZLES_SMOKE_PARTICLE.get(), MuzzlesSmokeParticle.Factory::new);
        event.registerSpriteSet(SCParticles.MUZZLES_FLASH_PARTICLE.get(), MuzzlesFlashParticle.Factory::new);
    }

    @SubscribeEvent
    public static void onRegisterClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(UnderArmorTooltip.class, data -> {
            if (data instanceof UnderArmorTooltip(UnderArmorContents contents, ArmorItem.Type type)) {
                return new ClientUnderArmorTooltip(contents, type);
            }
            return null;
        });
    }
}