package banduty.stoneycore.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;

import java.util.*;

public abstract class FabricModelProviderPlus extends FabricModelProvider {
    public FabricModelProviderPlus(FabricDataOutput output) {
        super(output);
    }

    protected void registerItemWConditions(Item item, ModelTemplate model, ItemModelGenerators itemModelGenerators, OverrideCondition... conditions) {
        registerItemWConditions(item, model, itemModelGenerators, true, conditions);
    }

    protected void registerItemWConditions(Item item, ModelTemplate model, ItemModelGenerators itemModelGenerators, boolean joinConditions, OverrideCondition... conditions) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String namespace = itemId.getNamespace();
        String path = itemId.getPath();

        Set<String> generatedModels = new HashSet<>();
        JsonArray overrides = new JsonArray();

        // Generate individual condition models and overrides
        for (OverrideCondition condition : conditions) {
            String modelName = condition.getModelName(path);
            generateOverrideModel(item, ModelTemplates.FLAT_ITEM, modelName, itemModelGenerators);
            generatedModels.add(modelName);

            addOverride(overrides, namespace, condition.predicateKey, condition.predicateValue, modelName);
        }

        // Generate combined condition model and override if multiple conditions exist
        if (joinConditions && conditions.length > 1) {
            List<List<OverrideCondition>> allCombinations = generateAllCombinations(conditions);

            for (List<OverrideCondition> combination : allCombinations) {
                if (combination.size() > 1) { // Skip single conditions (already handled above)
                    JsonObject combinedPredicate = new JsonObject();
                    List<String> modelNames = new ArrayList<>();

                    for (OverrideCondition condition : combination) {
                        combinedPredicate.addProperty(condition.predicateKey.toString(), condition.predicateValue);
                        modelNames.add(condition.getModelName(path));
                    }

                    String combinedModelName = combineMultipleModelNames(modelNames);

                    // Only generate combined model if it doesn't exist yet
                    if (!generatedModels.contains(combinedModelName)) {
                        generateOverrideModel(item, ModelTemplates.FLAT_ITEM, combinedModelName, itemModelGenerators);
                        generatedModels.add(combinedModelName);
                    }

                    addOverride(overrides, namespace, combinedPredicate, combinedModelName);
                }
            }
        }

        // Register the main model with overrides
        ResourceLocation modelId = new ResourceLocation(namespace, "item/" + path);

        TextureMapping textures;
        if (item instanceof DyeableLeatherItem) {
            textures = TextureMapping.layered(
                    new ResourceLocation(namespace, "item/" + path),
                    new ResourceLocation(namespace, "item/" + path + "_overlay")
            );
        } else {
            // For non-dyeable items, use the standard approach
            textures = TextureMapping.layer0(new ResourceLocation(namespace, "item/" + path));
        }
        model.create(
                modelId,
                textures,
                itemModelGenerators.output,
                (id, textureMap) -> {
                    JsonObject json = model.createBaseTemplate(id, textureMap);
                    json.add("overrides", overrides);
                    return json;
                }
        );
    }

    private List<List<OverrideCondition>> generateAllCombinations(OverrideCondition[] conditions) {
        List<List<OverrideCondition>> allCombinations = new ArrayList<>();
        int n = conditions.length;

        // Generate all subsets (2^n - 1, excluding empty set)
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

        // Keep adding parts until we find where models diverge
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

        // Extract all unique conditions
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

    private void generateOverrideModel(Item item, ModelTemplate model, String modelName, ItemModelGenerators itemModelGenerator) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String namespace = itemId.getNamespace();
        ResourceLocation modelId = new ResourceLocation(namespace, "item/" + modelName);

        if (item instanceof DyeableLeatherItem) {
            TextureMapping textures = TextureMapping.layered(
                    new ResourceLocation(namespace, "item/" + modelName),
                    new ResourceLocation(namespace, "item/" + modelName + "_overlay")
            );

            ModelTemplate customModel = new ModelTemplate(Optional.of(new ResourceLocation("item/handheld")), Optional.empty(), TextureSlot.LAYER0, TextureSlot.LAYER1);
            customModel.create(modelId, textures, itemModelGenerator.output, (id, textureMap) -> {
                JsonObject json = new JsonObject();
                json.addProperty("parent", "item/handheld");

                JsonObject texturesJson = new JsonObject();
                texturesJson.addProperty("layer0", textureMap.get(TextureSlot.LAYER0).toString());
                texturesJson.addProperty("layer1", textureMap.get(TextureSlot.LAYER1).toString());
                json.add("textures", texturesJson);

                return json;
            });
        } else {
            TextureMapping textures = TextureMapping.layer0(new ResourceLocation(namespace, "item/" + modelName));
            model.create(modelId, textures, itemModelGenerator.output);
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

    protected void generateBannerPatternModels(Item item, ModelTemplate model, ItemModelGenerators itemModelGenerator) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        String[] bannerPatternNames = {
                "bl", "bo", "br", "bri", "bs", "bt", "bts", "cbo", "cr", "cre",
                "cs", "dls", "drs", "flo", "glb", "gra", "gru", "hh", "hhb", "ld",
                "ls", "lud", "mc", "moj", "mr", "ms", "pig", "rd", "rs", "rud",
                "sc", "sku", "ss", "tl", "tr", "ts", "tt", "tts", "vh", "vhr"
        };

        for (String pattern : bannerPatternNames) {
            ResourceLocation modelId = new ResourceLocation(itemId.getNamespace(), "item/" + itemId.getPath() + "/" + pattern);

            TextureMapping textures = TextureMapping.layer0(
                    new ResourceLocation(itemId.getNamespace(), "item/" + itemId.getPath() + "/" + pattern)
            );

            model.create(modelId, textures, itemModelGenerator.output);
        }
    }

    protected void registerWCustomName(Item item, ModelTemplate model, ItemModelGenerators itemModelGenerator, String modelName, ResourceLocation texturePath) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        ResourceLocation modelId = new ResourceLocation(itemId.getNamespace(), "item/" + item);
        if (!modelName.isEmpty()) modelId = new ResourceLocation(itemId.getNamespace(), "item/" + modelName);

        TextureMapping texture = TextureMapping.layer0(new ResourceLocation(itemId.getNamespace(), "item/" + item));
        if (texturePath != null) texture = TextureMapping.layer0(texturePath);

        model.create(modelId, texture, itemModelGenerator.output);
    }

    public record OverrideCondition(ResourceLocation predicateKey, Number predicateValue) {
        String getModelName(String basePath) {
            return basePath + "_" + predicateKey.getPath();
        }
    }
}
