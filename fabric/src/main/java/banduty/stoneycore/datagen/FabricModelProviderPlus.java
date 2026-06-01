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
import net.minecraft.world.item.Item;

import java.util.*;

public abstract class FabricModelProviderPlus extends FabricModelProvider {
    public FabricModelProviderPlus(FabricDataOutput output) {
        super(output);
    }

    protected void registerItemWConditions(Item item, ItemModelGenerators itemModelGenerators, OverrideCondition... conditions) {
        registerItemWConditions(item, itemModelGenerators, true, false, conditions);
    }

    protected void registerItemWConditions(Item item, ItemModelGenerators itemModelGenerators, boolean joinConditions, boolean overlay, OverrideCondition... conditions) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String namespace = itemId.getNamespace();
        String path = itemId.getPath();

        Set<String> generatedModels = new HashSet<>();
        JsonArray overrides = new JsonArray();

        // Generate individual condition models and overrides
        for (OverrideCondition condition : conditions) {
            String modelName = condition.getModelName(path);
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
                        modelNames.add(condition.getModelName(path));
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

        ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(namespace, "item/" + path);

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

        ModelTemplate finalModel1 = finalModel;
        finalModel.create(
                modelId,
                textures,
                itemModelGenerators.output,
                (id, textureMap) -> {
                    JsonObject json = finalModel1.createBaseTemplate(id, textureMap);
                    if (!overrides.isEmpty()) {
                        json.add("overrides", overrides);
                    }
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

    protected void generateBannerPatternModels(Item item, ModelTemplate model, ItemModelGenerators itemModelGenerator) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String[] bannerPatternNames = {
                "border", "bricks", "circle", "creeper", "cross", "curly_border", "diagonal_left", "diagonal_right",
                "diagonal_up_left", "diagonal_up_right", "flow", "flower", "globe", "gradient", "gradient_up", "guster", "half_horizontal",
                "half_horizontal_bottom", "half_vertical", "half_vertical_right", "mojang", "piglin", "rhombus", "skull",
                "small_stripes", "square_bottom_left", "square_bottom_right", "square_top_left", "square_top_right",
                "straight_cross", "stripe_bottom", "stripe_center", "stripe_downleft", "stripe_downright", "stripe_left",
                "stripe_middle", "stripe_right", "stripe_top", "triangle_bottom", "triangle_top", "triangles_bottom",
                "triangles_top"
        };

        for (String pattern : bannerPatternNames) {
            ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath() + "/" + pattern);
            TextureMapping textures = TextureMapping.layer0(ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath() + "/" + pattern));
            model.create(modelId, textures, itemModelGenerator.output);
        }
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
            return basePath + "_" + predicateKey.getPath();
        }
    }
}
