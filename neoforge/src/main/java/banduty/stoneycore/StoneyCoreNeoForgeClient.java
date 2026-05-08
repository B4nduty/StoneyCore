package banduty.stoneycore;

import banduty.stoneycore.block.CraftmanAnvilBlockRenderer;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.client.SCAccessoryItemRenderer;
import banduty.stoneycore.client.SCBulletEntityRenderer;
import banduty.stoneycore.entity.SCEntities;
import banduty.stoneycore.items.custom.armor.SCAccessoryItem;
import banduty.stoneycore.items.custom.armor.underarmor.SCDyeableUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.tongs.Tongs;
import banduty.stoneycore.model.UnderArmourBootsModel;
import banduty.stoneycore.model.UnderArmourChestplateModel;
import banduty.stoneycore.model.UnderArmourHelmetModel;
import banduty.stoneycore.model.UnderArmourLeggingsModel;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import banduty.stoneycore.particle.SCParticles;
import banduty.stoneycore.platform.*;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.SCScreenHandlers;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.render.ForgeRenderTypeHelper;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StoneyCoreNeoForgeClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientPlatform.setClientPlaformHelper(new NeoForgeClientPlatformHelper());
            ClientPlatform.setSCRenderTypeHelper(new ForgeRenderTypeHelper());
            ClientPlatform.setHumanoidModelSetupAnimHelper(new NeoForgeHumanoidModelSetupAnimHelper());
            ClientPlatform.setKeyInputHelper(new NeoForgeKeyInputHelper());
            ClientPlatform.setRenderFirstPersonAccessoryArmorHelper(new NeoForgeRenderFirstPersonAccessoryArmorHelper());

            BlockEntityRenderers.register(
                    SCBlocks.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(),
                    CraftmanAnvilBlockRenderer::new
            );

            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof SCAccessoryItem)
                    .forEach(item -> AccessoriesRendererRegistry.registerRenderer(item, SCAccessoryItemRenderer::new));

            for (Item item : BuiltInRegistries.ITEM) {
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "broken"),
                        (stack, world, entity, seed) ->
                                stack.is(SCTags.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f ? 1.0F : 0.0F);

                if (item instanceof HotIron hotIron) {
                    ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "finished"),
                            (stack, world, entity, seed) -> hotIron.isFinished(stack) ? 1.0F : 0.0F);
                }

                if (item instanceof Tongs) {
                    ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "hotiron"),
                            (stack, world, entity, seed) ->
                                    Tongs.getTargetStack(stack).getItem() instanceof HotIron ? 1.0F : 0.0F);
                    ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "finished"),
                            (stack, world, entity, seed) ->
                                    !(HotIron.getTargetStack(Tongs.getTargetStack(stack)).isEmpty()) ? 1.0F : 0.0F);
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
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SCEntities.SC_BULLET.get(), SCBulletEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof SCAccessoryItem || item instanceof SCUnderArmor) {
                event.register((stack, tintIndex) -> {
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
        }
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SCParticles.MUZZLES_SMOKE_PARTICLE.get(), MuzzlesSmokeParticle.Factory::new);
        event.registerSpriteSet(SCParticles.MUZZLES_FLASH_PARTICLE.get(), MuzzlesFlashParticle.Factory::new);
    }
}