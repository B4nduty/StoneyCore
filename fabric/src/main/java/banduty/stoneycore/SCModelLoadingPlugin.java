package banduty.stoneycore;

import banduty.stoneycore.util.data.itemdata.SCTags;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SCModelLoadingPlugin implements ModelLoadingPlugin {

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        List<ResourceLocation> toRegister = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

            if (item.builtInRegistryHolder().is(SCTags.GEO_2D_ITEMS.getTag())) {
                ResourceLocation iconPath = ResourceLocation.fromNamespaceAndPath(
                        itemId.getNamespace(), itemId.getPath() + "_icon");
                if (modelExists(resourceManager, iconPath)) {
                    toRegister.add(ResourceLocation.fromNamespaceAndPath(
                            itemId.getNamespace(), "item/" + itemId.getPath() + "_icon"));
                }
            }

            if (item.builtInRegistryHolder().is(SCTags.WEAPONS_3D.getTag())) {
                ResourceLocation weaponPath = ResourceLocation.fromNamespaceAndPath(
                        itemId.getNamespace(), itemId.getPath() + "_3d");
                if (modelExists(resourceManager, weaponPath)) {
                    toRegister.add(ResourceLocation.fromNamespaceAndPath(
                            itemId.getNamespace(), "item/" + itemId.getPath() + "_3d"));
                }
            }

            if (item.builtInRegistryHolder().is(SCTags.BANNER_COMPATIBLE.getTag())) {
                String folder = "models/item/" + itemId.getPath() + "/";
                Map<ResourceLocation, Resource> found =
                        resourceManager.listResources(folder, path -> path.getPath().endsWith(".json"));

                for (ResourceLocation resId : found.keySet()) {
                    String fileName = resId.getPath();
                    fileName = fileName.substring(fileName.lastIndexOf('/') + 1,
                            fileName.length() - ".json".length());

                    toRegister.add(ResourceLocation.fromNamespaceAndPath(
                            itemId.getNamespace(), "item/" + itemId.getPath() + "/" + fileName));
                }
            }
        }

        pluginContext.addModels(toRegister);
    }

    private static boolean modelExists(ResourceManager manager, ResourceLocation modelId) {
        ResourceLocation jsonPath = ResourceLocation.fromNamespaceAndPath(
                modelId.getNamespace(), "models/item/" + modelId.getPath() + ".json");
        return manager.getResource(jsonPath).isPresent();
    }
}