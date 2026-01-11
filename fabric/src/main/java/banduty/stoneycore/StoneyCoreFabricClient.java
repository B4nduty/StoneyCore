package banduty.stoneycore;

import banduty.stoneycore.block.CraftmanAnvilBlockRenderer;
import banduty.stoneycore.block.ModBlockEntities;
import banduty.stoneycore.client.CrownRenderer;
import banduty.stoneycore.client.SCAccessoryItemRenderer;
import banduty.stoneycore.client.SCBulletEntityRenderer;
import banduty.stoneycore.client.SCUnderArmourRenderer;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.event.AttackCancelHandler;
import banduty.stoneycore.event.ClientTickHandler;
import banduty.stoneycore.event.ItemTooltipHandler;
import banduty.stoneycore.event.KeyInputHandler;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.armor.ISCUnderArmor;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.tongs.Tongs;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.OutlineClaimS2CPacket;
import banduty.stoneycore.platform.FabricHumanoidModelSetupAnimHelper;
import banduty.stoneycore.platform.FabricKeyInputHelper;
import banduty.stoneycore.platform.FabricRenderFirstPersonAccessoryArmorHelper;
import banduty.stoneycore.particle.ModParticles;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.platform.FabricClientPlatformHelper;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.ModScreenHandlers;
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
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;

public class StoneyCoreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlatform.setClientPlaformHelper(new FabricClientPlatformHelper());
        ClientPlatform.setSCRenderTypeHelper(new FabricRenderTypeHelper());
        ClientPlatform.setHumanoidModelSetupAnimHelper(new FabricHumanoidModelSetupAnimHelper());
        ClientPlatform.setKeyInputHelper(new FabricKeyInputHelper());
        ClientPlatform.setRenderFirstPersonAccessoryArmorHelper(new FabricRenderFirstPersonAccessoryArmorHelper());
        ModMessages.registerS2CPackets();
        ClientPreAttackCallback.EVENT.register(new AttackCancelHandler());
        ItemTooltipCallback.EVENT.register(new ItemTooltipHandler());
        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickHandler());
        KeyInputHandler.register();
        EntityRendererRegistry.register(ModEntities.SC_BULLET.get(), SCBulletEntityRenderer::new);
        for (Item item : BuiltInRegistries.ITEM) {
            if (((item instanceof SCAccessoryItem || item instanceof ISCUnderArmor) && item instanceof DyeableLeatherItem dyeableItem)) {
                ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
                        tintIndex > 0 ? -1 : dyeableItem.getColor(stack), item);
            }
            if (item instanceof SCAccessoryItem) {
                AccessoriesRendererRegistry.registerRenderer(item, SCAccessoryItemRenderer::new);
            }
            if (item instanceof ISCUnderArmor) {
                ArmorRenderer.register(new SCUnderArmourRenderer(), item);
            }

            ItemProperties.register(item, new ResourceLocation("broken"),
                    (stack, world, entity, seed) ->
                            stack.is(SCTags.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f ? 1.0F : 0.0F);

            if (item instanceof HotIron hotIron) {
                ItemProperties.register(item, new ResourceLocation("finished"),
                        (stack, world, entity, seed) ->
                                hotIron.isFinished(stack) ? 1.0F : 0.0F);
            }

            if (item instanceof Tongs) {
                ItemProperties.register(item, new ResourceLocation("hotiron"),
                        (stack, world, entity, seed) ->
                                Tongs.getTargetStack(stack).getItem() instanceof HotIron ? 1.0F : 0.0F);
                ItemProperties.register(item, new ResourceLocation("finished"),
                        (stack, world, entity, seed) ->
                                !(HotIron.getTargetStack(Tongs.getTargetStack(stack)).isEmpty()) ? 1.0F : 0.0F);
            }
        }
        ArmorRenderer.register(new CrownRenderer(), SCItems.CROWN.get());

        ParticleFactoryRegistry.getInstance().register(ModParticles.MUZZLES_SMOKE_PARTICLE.get(), MuzzlesSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.MUZZLES_FLASH_PARTICLE.get(), MuzzlesFlashParticle.Factory::new);

        BlockEntityRenderers.register(ModBlockEntities.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(), CraftmanAnvilBlockRenderer::new);

        MenuScreens.register(ModScreenHandlers.BLUEPRINT_SCREEN_HANDLER, BlueprintScreen::new);

        OutlineClaimS2CPacket.registerRenderer();
    }
}
