package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.itemdata.SCTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SCModelRegistration {

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

            // 1. "_icon" flat models for 2D geo items
            if (item.builtInRegistryHolder().is(SCTags.GEO_2D_ITEMS.getTag())) {
                event.register(ModelResourceLocation.inventory(
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_icon")
                ));
            }

            // 2. "_3d" models for 3D weapon items
            if (item.builtInRegistryHolder().is(SCTags.WEAPONS_3D.getTag())) {
                event.register(ModelResourceLocation.inventory(
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_3d")
                ));
            }

            // 3. Per-pattern composite models: "<item_path>/<pattern_shortname>"
            if (item.builtInRegistryHolder().is(SCTags.BANNER_COMPATIBLE.getTag())) {
                String folder = "models/item/" + itemId.getPath() + "/";
                Map<ResourceLocation, Resource> found =
                        resourceManager.listResources(folder, path -> path.getPath().endsWith(".json"));

                for (ResourceLocation resId : found.keySet()) {
                    String fileName = resId.getPath();
                    fileName = fileName.substring(fileName.lastIndexOf('/') + 1,
                            fileName.length() - ".json".length());

                    event.register(ModelResourceLocation.inventory(
                            ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "/" + fileName)));
                }
            }
        }
    }
}