package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.SCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;

public class ModModelProvider extends FabricModelProviderPlus {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(SCItems.SMITHING_HAMMER, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.BLACK_POWDER, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.CROWN, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.MANUSCRIPT, ModelTemplates.FLAT_ITEM);

        registerItemWConditions(SCItems.TONGS, ModelTemplates.FLAT_ITEM, itemModelGenerator, false,
                new OverrideCondition(new ResourceLocation(StoneyCore.MOD_ID, "hotiron"), 1),
                new OverrideCondition(new ResourceLocation(StoneyCore.MOD_ID,"finished"), 1));

        registerItemWConditions(SCItems.HOT_IRON, ModelTemplates.FLAT_ITEM, itemModelGenerator,
                new OverrideCondition(new ResourceLocation(StoneyCore.MOD_ID,"finished"), 1));
    }
}
