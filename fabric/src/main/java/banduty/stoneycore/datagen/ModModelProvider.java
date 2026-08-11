package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.items.SCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import java.util.concurrent.CompletableFuture;

public class ModModelProvider extends FabricModelProviderPlus {
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;
    public ModModelProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output);
        this.registriesFuture = registriesFuture;
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
        HolderLookup.Provider registries = registriesFuture.join();

        itemModelGenerator.generateFlatItem(SCItems.SMITHING_HAMMER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.BLACK_POWDER.get(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.CROWN.get(), ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(SCItems.HOT_IRON.get(), ModelTemplates.FLAT_ITEM);

        registerItemWConditions(SCItems.TONGS.get(), itemModelGenerator, registries, false, false,
                new OverrideCondition(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "hotiron"), 1),
                new OverrideCondition(ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "finished"), 1));
    }
}
