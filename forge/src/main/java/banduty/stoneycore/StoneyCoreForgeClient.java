package banduty.stoneycore;

import banduty.stoneycore.block.CraftmanAnvilBlockRenderer;
import banduty.stoneycore.block.ModBlockEntities;
import banduty.stoneycore.client.SCAccessoryItemRenderer;
import banduty.stoneycore.client.SCBulletEntityRenderer;
import banduty.stoneycore.entity.ModEntities;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.model.UnderArmourBootsModel;
import banduty.stoneycore.model.UnderArmourChestplateModel;
import banduty.stoneycore.model.UnderArmourHelmetModel;
import banduty.stoneycore.model.UnderArmourLeggingsModel;
import banduty.stoneycore.platform.ForgeHumanoidModelSetupAnimHelper;
import banduty.stoneycore.platform.ForgeKeyInputHelper;
import banduty.stoneycore.platform.ForgeRenderFirstPersonAccessoryArmorHelper;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.platform.ForgeClientPlatformHelper;
import banduty.stoneycore.util.render.ForgeRenderTypeHelper;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StoneyCoreForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientPlatform.setClientPlaformHelper(new ForgeClientPlatformHelper());
            ClientPlatform.setSCRenderTypeHelper(new ForgeRenderTypeHelper());
            ClientPlatform.setHumanoidModelSetupAnimHelper(new ForgeHumanoidModelSetupAnimHelper());
            ClientPlatform.setKeyInputHelper(new ForgeKeyInputHelper());
            ClientPlatform.setRenderFirstPersonAccessoryArmorHelper(new ForgeRenderFirstPersonAccessoryArmorHelper());
            BlockEntityRenderers.register(
                    ModBlockEntities.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(),
                    CraftmanAnvilBlockRenderer::new
            );
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof SCAccessoryItem)
                    .forEach(item -> AccessoriesRendererRegistry.registerRenderer(item, SCAccessoryItemRenderer::new));
        });
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
        event.registerEntityRenderer(ModEntities.SC_BULLET.get(), SCBulletEntityRenderer::new);
    }
}