package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.SCItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, SCItems.SMITHING_HAMMER.get(), 1)
                .pattern("IIN")
                .pattern(" S ")
                .pattern(" S ")
                .input('S', Items.STICK)
                .input('I', Items.IRON_INGOT)
                .input('N', Items.IRON_NUGGET)
                .criterion(hasItem(Items.STICK), conditionsFromItem(Items.STICK))
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
                .offerTo(exporter, new Identifier(StoneyCore.MOD_ID, getRecipeName(SCItems.SMITHING_HAMMER.get())));

        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, SCItems.BLACK_POWDER.get(), 4)
                .input(Items.CHARCOAL)
                .input(Items.BONE_MEAL)
                .input(Items.MAGMA_BLOCK)
                .criterion(hasItem(Items.CHARCOAL), conditionsFromItem(Items.CHARCOAL))
                .criterion(hasItem(Items.BONE_MEAL), conditionsFromItem(Items.BONE_MEAL))
                .criterion(hasItem(Items.MAGMA_BLOCK), conditionsFromItem(Items.MAGMA_BLOCK))
                .offerTo(exporter, new Identifier(StoneyCore.MOD_ID, getRecipeName(SCItems.BLACK_POWDER.get())));
    }
}
