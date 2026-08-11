package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.client.SC3DRendererProvider;
import banduty.stoneycore.items.client.SCBannersRendererProvider;
import banduty.stoneycore.items.client.SCIconRendererProvider;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.*;

public abstract class FabricModelProviderPlus extends FabricModelProvider {
    public FabricModelProviderPlus(FabricDataOutput output) {
        super(output);
    }

    protected void registerItemWConditions(Item item, ItemModelGenerators itemModelGenerators, HolderLookup.Provider registries, OverrideCondition... conditions) {
        registerItemWConditions(item, itemModelGenerators, registries, true, false, conditions);
    }

    protected void registerItemWConditions(Item item, ItemModelGenerators itemModelGenerators, HolderLookup.Provider registries, boolean joinConditions, boolean overlay, OverrideCondition... conditions) {
        List<OverrideCondition> conditionList = new ArrayList<>(Arrays.asList(conditions));
        if (item instanceof QuenchItem quenchItem && !quenchItem.destroysOnQuench()) {
            conditionList.add(new OverrideCondition(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "ignited"), 1));
        }
        conditions = conditionList.toArray(new OverrideCondition[0]);

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String namespace = itemId.getNamespace();
        String path = itemId.getPath();

        boolean is3DProvider = item instanceof SC3DRendererProvider;
        boolean isIconProvider = item instanceof SCIconRendererProvider;
        boolean isBannerProvider = item instanceof SCBannersRendererProvider;

        boolean isCustomRenderer = is3DProvider || isIconProvider || isBannerProvider;

        // 3D items get "_gui", Icon items get "_icon", Banner items use "_base", standard items use base path
        String customSuffix = is3DProvider ? "_gui" : (isIconProvider ? "_icon" : (isBannerProvider ? "_base" : ""));
        String iconPath = isCustomRenderer ? path + customSuffix : path;

        Set<String> generatedModels = new HashSet<>();
        JsonArray overrides = new JsonArray();

        // Generate individual condition models and overrides
        for (OverrideCondition condition : conditions) {
            String modelName = condition.getModelName(iconPath);
            generateOverrideModel(item, ModelTemplates.FLAT_ITEM, modelName, itemModelGenerators, overlay);
            generatedModels.add(modelName);

            addOverride(overrides, namespace, condition.predicateKey, condition.predicateValue, modelName);
        }

        // Generate combined condition model and override if multiple conditions exist
        if (joinConditions && conditions.length > 1) {
            List<List<OverrideCondition>> allCombinations = generateAllCombinations(conditions);

            for (List<OverrideCondition> combination : allCombinations) {
                if (combination.size() > 1) {
                    JsonObject combinedPredicate = new JsonObject();
                    List<String> modelNames = new ArrayList<>();

                    for (OverrideCondition condition : combination) {
                        combinedPredicate.addProperty(condition.predicateKey.toString(), condition.predicateValue);
                        modelNames.add(condition.getModelName(iconPath));
                    }

                    String combinedModelName = combineMultipleModelNames(modelNames);

                    if (!generatedModels.contains(combinedModelName)) {
                        generateOverrideModel(item, ModelTemplates.FLAT_ITEM, combinedModelName, itemModelGenerators, overlay);
                        generatedModels.add(combinedModelName);
                    }

                    addOverride(overrides, namespace, combinedPredicate, combinedModelName);
                }
            }
        }

        TextureMapping textures;
        ModelTemplate finalModel;

        if (overlay) {
            textures = new TextureMapping()
                    .put(TextureSlot.LAYER0, ResourceLocation.fromNamespaceAndPath(namespace, "item/" + path))
                    .put(TextureSlot.LAYER1, ResourceLocation.fromNamespaceAndPath(namespace, "item/" + path + "_overlay"));
            finalModel = ModelTemplates.TWO_LAYERED_ITEM;
        } else {
            textures = TextureMapping.layer0(ResourceLocation.fromNamespaceAndPath(namespace, "item/" + path));
            finalModel = ModelTemplates.FLAT_ITEM;
        }

        // Generate flat 2D base model (models/item/surcoat_base.json)
        ResourceLocation iconModelId = ResourceLocation.fromNamespaceAndPath(namespace, "item/" + iconPath);
        ModelTemplate finalModelTemplate = finalModel;

        finalModelTemplate.create(
                iconModelId,
                textures,
                itemModelGenerators.output,
                (id, textureMap) -> {
                    JsonObject json = finalModelTemplate.createBaseTemplate(id, textureMap);
                    if (!overrides.isEmpty()) {
                        json.add("overrides", overrides);
                    }
                    return json;
                }
        );

        // Generate base item model (models/item/surcoat.json) pointing to builtin/entity
        if (isCustomRenderer) {
            ResourceLocation baseModelId = ResourceLocation.fromNamespaceAndPath(namespace, "item/" + path);
            itemModelGenerators.output.accept(baseModelId, () -> {
                JsonObject json = new JsonObject();
                json.addProperty("parent", "builtin/entity");
                return json;
            });
        }
    }

    private List<List<OverrideCondition>> generateAllCombinations(OverrideCondition[] conditions) {
        List<List<OverrideCondition>> allCombinations = new ArrayList<>();
        int n = conditions.length;

        for (int i = 1; i < (1 << n); i++) {
            List<OverrideCondition> combination = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    combination.add(conditions[j]);
                }
            }
            allCombinations.add(combination);
        }

        return allCombinations;
    }

    private String combineMultipleModelNames(List<String> modelNames) {
        if (modelNames.isEmpty()) return "";
        if (modelNames.size() == 1) return modelNames.get(0);

        String[] firstParts = modelNames.get(0).split("_");
        String baseName = firstParts[0];

        for (int i = 1; i < firstParts.length; i++) {
            String potentialBase = baseName + "_" + firstParts[i];
            boolean allStartWith = true;

            for (String modelName : modelNames) {
                if (!modelName.startsWith(potentialBase + "_")) {
                    allStartWith = false;
                    break;
                }
            }

            if (allStartWith) {
                baseName = potentialBase;
            } else {
                break;
            }
        }

        Set<String> conditions = new HashSet<>();
        for (String modelName : modelNames) {
            String conditionPart = modelName.substring(baseName.length());
            if (conditionPart.startsWith("_")) {
                conditionPart = conditionPart.substring(1);
            }
            if (!conditionPart.isEmpty()) {
                conditions.add(conditionPart);
            }
        }

        List<String> sortedConditions = new ArrayList<>(conditions);
        sortedConditions.sort(String::compareTo);
        return baseName + "_" + String.join("_", sortedConditions);
    }

    private void generateOverrideModel(Item item, ModelTemplate model, String modelName, ItemModelGenerators itemModelGenerators) {
        this.generateOverrideModel(item, model, modelName, itemModelGenerators, false);
    }

    private void generateOverrideModel(Item item, ModelTemplate model, String modelName, ItemModelGenerators itemModelGenerators, boolean overlay) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String namespace = itemId.getNamespace();
        ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(namespace, "item/" + modelName);

        TextureMapping textures;
        if (overlay) {
            textures = new TextureMapping()
                    .put(TextureSlot.LAYER0, ResourceLocation.fromNamespaceAndPath(namespace, "item/" + modelName))
                    .put(TextureSlot.LAYER1, ResourceLocation.fromNamespaceAndPath(namespace, "item/" + modelName + "_overlay"));

            ModelTemplates.TWO_LAYERED_ITEM.create(modelId, textures, itemModelGenerators.output);
        } else {
            textures = TextureMapping.layer0(ResourceLocation.fromNamespaceAndPath(namespace, "item/" + modelName));
            model.create(modelId, textures, itemModelGenerators.output);
        }
    }

    private void addOverride(JsonArray overrides, String namespace,
                             ResourceLocation predicateKey, Number predicateValue, String modelName) {
        JsonObject predicate = new JsonObject();
        predicate.addProperty(predicateKey.toString(), predicateValue);
        addOverride(overrides, namespace, predicate, modelName);
    }

    private void addOverride(JsonArray overrides, String namespace,
                             JsonObject predicate, String modelName) {
        JsonObject override = new JsonObject();
        override.add("predicate", predicate);
        override.addProperty("model", namespace + ":item/" + modelName);
        overrides.add(override);
    }

    protected void registerWCustomName(Item item, ModelTemplate model, ItemModelGenerators itemModelGenerator, String modelName, ResourceLocation texturePath) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String path = itemId.getPath();

        ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + path);
        if (!modelName.isEmpty()) modelId = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + modelName);

        TextureMapping texture = TextureMapping.layer0(ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + path));
        if (texturePath != null) texture = TextureMapping.layer0(texturePath);

        model.create(modelId, texture, itemModelGenerator.output);
    }

    public record OverrideCondition(ResourceLocation predicateKey, Number predicateValue) {
        String getModelName(String basePath) {
            String cleanPath = basePath.replaceAll("(_gui|_icon|_base)$", "");
            return cleanPath + "_" + predicateKey.getPath();
        }
    }
}