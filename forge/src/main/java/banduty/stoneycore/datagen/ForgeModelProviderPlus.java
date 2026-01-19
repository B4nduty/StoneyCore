package banduty.stoneycore.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;

public abstract class ForgeModelProviderPlus extends ItemModelProvider {

    public ForgeModelProviderPlus(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    protected void registerItemWConditions(Item item, OverrideCondition... conditions) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();

        // Create the base model
        ItemModelBuilder builder = withExistingParent(path, "item/generated")
                .texture("layer0", modLoc("item/" + path));

        // Generate and add overrides
        for (OverrideCondition condition : conditions) {
            String modelName = condition.getModelName(path);

            // Create the sub-model for the override
            withExistingParent(modelName, "item/generated")
                    .texture("layer0", modLoc("item/" + modelName));

            // Add the override logic to the base model
            builder.override()
                    .predicate(new ResourceLocation(condition.predicateKey), condition.predicateValue.floatValue())
                    .model(getExistingFile(modLoc("item/" + modelName)))
                    .end();
        }
    }

    protected void generateBannerPatternModels(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        String[] patterns = {"bl", "bo", "br", "bri", "bs", "bt", "bts", "cbo", "cr", "cre", "cs", "dls", "drs", "flo", "glb", "gra", "gru", "hh", "hhb", "ld", "ls", "lud", "mc", "moj", "mr", "ms", "pig", "rd", "rs", "rud", "sc", "sku", "ss", "tl", "tr", "ts", "tt", "tts", "vh", "vhr"};

        for (String pattern : patterns) {
            withExistingParent(path + "/" + pattern, "item/generated")
                    .texture("layer0", modLoc("item/" + path + "/" + pattern));
        }
    }

    public static record OverrideCondition(String predicateKey, Number predicateValue) {
        String getModelName(String basePath) {
            return basePath + "_" + this.predicateKey;
        }
    }
}