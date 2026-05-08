package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.SCBlocks;
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

        ResourceLocation model = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "block/craftman_anvil");

        generators.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(SCBlocks.CRAFTMAN_ANVIL.get())
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
        itemModelGenerator.generateFlatItem(SCItems.SMITHING_HAMMER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.BLACK_POWDER.get(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.CROWN.get(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.MANUSCRIPT.get(), ModelTemplates.FLAT_ITEM);

        registerItemWConditions(SCItems.TONGS.get(), ModelTemplates.FLAT_ITEM, itemModelGenerator, false,
                new OverrideCondition(ResourceLocation.fromNamespaceAndPath("", "hotiron"), 1),
                new OverrideCondition(ResourceLocation.fromNamespaceAndPath("", "finished"), 1));

        registerItemWConditions(SCItems.HOT_IRON.get(), ModelTemplates.FLAT_ITEM, itemModelGenerator,
                new OverrideCondition(ResourceLocation.fromNamespaceAndPath("", "finished"), 1));
    }
}
