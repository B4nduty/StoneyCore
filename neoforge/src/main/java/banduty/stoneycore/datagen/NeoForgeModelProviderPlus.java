package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.client.SC3DRendererProvider;
import banduty.stoneycore.items.client.SCBannersRendererProvider;
import banduty.stoneycore.items.client.SCIconRendererProvider;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.*;

public abstract class NeoForgeModelProviderPlus extends ItemModelProvider {
    public NeoForgeModelProviderPlus(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    protected void registerItemWConditions(Item item, OverrideCondition... conditions) {
        registerItemWConditions(item, true, false, conditions);
    }

    protected void registerItemWConditions(Item item, boolean joinConditions, boolean overlay, OverrideCondition... conditions) {
        List<OverrideCondition> conditionList = new ArrayList<>(Arrays.asList(conditions));
        if (item instanceof QuenchItem quenchItem && !quenchItem.destroysOnQuench()) {
            conditionList.add(new OverrideCondition(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "ignited"), 1));
        }
        conditions = conditionList.toArray(new OverrideCondition[0]);

        String path = BuiltInRegistries.ITEM.getKey(item).getPath();

        boolean is3DProvider = item instanceof SC3DRendererProvider;
        boolean isIconProvider = item instanceof SCIconRendererProvider;
        boolean isBannerProvider = item instanceof SCBannersRendererProvider;

        boolean isCustomRenderer = is3DProvider || isIconProvider || isBannerProvider;

        // 3D items get "_gui", Icon items get "_icon", Banner items use "_base", standard items use base path
        String customSuffix = is3DProvider ? "_gui" : (isIconProvider ? "_icon" : (isBannerProvider ? "_base" : ""));
        String iconPath = isCustomRenderer ? path + customSuffix : path;

        // Create the base (visible/icon) model
        ItemModelBuilder baseBuilder = overlay
                ? withExistingParent(iconPath, "item/handheld")
                .texture("layer0", modLoc("item/" + path))
                .texture("layer1", modLoc("item/" + path + "_overlay"))
                : withExistingParent(iconPath, "item/generated")
                .texture("layer0", modLoc("item/" + path));

        Set<String> generatedModels = new HashSet<>();

        // 1. Individual condition models and overrides
        for (OverrideCondition condition : conditions) {
            String modelName = condition.getModelName(iconPath);
            generateOverrideModel(item, modelName, overlay);
            generatedModels.add(modelName);

            baseBuilder.override()
                    .predicate(condition.predicateKey, condition.predicateValue.floatValue())
                    .model(getExistingFile(modLoc("item/" + modelName)))
                    .end();
        }

        // 2. Combined condition models if multiple conditions exist
        if (joinConditions && conditions.length > 1) {
            List<List<OverrideCondition>> allCombinations = generateAllCombinations(conditions);

            for (List<OverrideCondition> combination : allCombinations) {
                if (combination.size() > 1) {
                    List<String> modelNames = new ArrayList<>();
                    var overrideBuilder = baseBuilder.override();

                    for (OverrideCondition condition : combination) {
                        overrideBuilder.predicate(condition.predicateKey, condition.predicateValue.floatValue());
                        modelNames.add(condition.getModelName(iconPath));
                    }

                    String combinedModelName = combineMultipleModelNames(modelNames);
                    if (!generatedModels.contains(combinedModelName)) {
                        generateOverrideModel(item, combinedModelName, overlay);
                        generatedModels.add(combinedModelName);
                    }

                    overrideBuilder.model(getExistingFile(modLoc("item/" + combinedModelName))).end();
                }
            }
        }

        if (isCustomRenderer) {
            getBuilder(path).parent(new ModelFile.UncheckedModelFile("builtin/entity"));
        }
    }

    private void generateOverrideModel(Item item, String modelName) {
        this.generateOverrideModel(item, modelName, false);
    }
    private void generateOverrideModel(Item item, String modelName, boolean overlay) {
        if (overlay) {
            withExistingParent(modelName, "item/handheld")
                    .texture("layer0", modLoc("item/" + modelName))
                    .texture("layer1", modLoc("item/" + modelName + "_overlay"));
        } else {
            withExistingParent(modelName, "item/generated")
                    .texture("layer0", modLoc("item/" + modelName));
        }
    }

    private List<List<OverrideCondition>> generateAllCombinations(OverrideCondition[] conditions) {
        List<List<OverrideCondition>> allCombinations = new ArrayList<>();
        int n = conditions.length;
        for (int i = 1; i < (1 << n); i++) {
            List<OverrideCondition> combination = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) combination.add(conditions[j]);
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
            if (allStartWith) baseName = potentialBase;
            else break;
        }

        Set<String> conditions = new HashSet<>();
        for (String modelName : modelNames) {
            String conditionPart = modelName.substring(baseName.length());
            if (conditionPart.startsWith("_")) conditionPart = conditionPart.substring(1);
            if (!conditionPart.isEmpty()) conditions.add(conditionPart);
        }

        List<String> sortedConditions = new ArrayList<>(conditions);
        Collections.sort(sortedConditions);
        return baseName + "_" + String.join("_", sortedConditions);
    }

    protected void registerWCustomName(Item item, String parent, String modelName, ResourceLocation texturePath) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String path = itemId.getPath();

        String finalModelName = (modelName == null || modelName.isEmpty()) ? path : modelName;

        ResourceLocation texture = texturePath != null ? texturePath : modLoc("item/" + path);

        if ("builtin/entity".equals(parent)) {
            getBuilder(finalModelName).parent(new ModelFile.UncheckedModelFile(parent));
        } else {
            withExistingParent(finalModelName, parent)
                    .texture("layer0", texture);
        }
    }

    public record OverrideCondition(ResourceLocation predicateKey, Number predicateValue) {
        String getModelName(String basePath) {
            String cleanPath = basePath.replaceAll("(_gui|_icon|_base)$", "");
            return cleanPath + "_" + predicateKey.getPath();
        }
    }
}