package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.items.SCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class ModModelProvider extends FabricModelProviderPlus {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {

        ResourceLocation model = new ResourceLocation("stoneycore", "block/craftman_anvil");

        generators.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(ModBlocks.CRAFTMAN_ANVIL)
                        .with(
                                PropertyDispatch.property(HorizontalDirectionalBlock.FACING)

                                        .select(Direction.NORTH,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, model)
                                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0)
                                        )

                                        .select(Direction.EAST,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, model)
                                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                        )

                                        .select(Direction.SOUTH,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, model)
                                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                        )

                                        .select(Direction.WEST,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, model)
                                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                        )
                        )
        );
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
