package banduty.stoneycore.datagen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.SCItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SCItems.TONGS.get(), 1)
                .pattern("N N")
                .pattern(" N ")
                .pattern("I I")
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .unlockedBy(getHasName(Items.IRON_NUGGET), has(Items.IRON_NUGGET))
                .save(consumer, new ResourceLocation(StoneyCore.MOD_ID, getSimpleRecipeName(SCItems.TONGS.get())));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SCItems.SMITHING_HAMMER.get(), 1)
                .pattern("IIN")
                .pattern(" S ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .unlockedBy(getHasName(Items.IRON_NUGGET), has(Items.IRON_NUGGET))
                .save(consumer, new ResourceLocation(StoneyCore.MOD_ID, getSimpleRecipeName(SCItems.SMITHING_HAMMER.get())));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SCItems.BLACK_POWDER.get(), 4)
                .requires(Items.CHARCOAL)
                .requires(Items.BONE_MEAL)
                .requires(Items.MAGMA_BLOCK)
                .unlockedBy(getHasName(Items.CHARCOAL), has(Items.CHARCOAL))
                .unlockedBy(getHasName(Items.BONE_MEAL), has(Items.BONE_MEAL))
                .unlockedBy(getHasName(Items.MAGMA_BLOCK), has(Items.MAGMA_BLOCK))
                .save(consumer, new ResourceLocation(StoneyCore.MOD_ID, getSimpleRecipeName(SCItems.BLACK_POWDER.get())));

        SimpleCookingRecipeBuilder.campfireCooking(
                        Ingredient.of(Items.IRON_INGOT),
                        RecipeCategory.MISC,
                        SCItems.HOT_IRON.get(),
                        0.7f,
                        900
                ).unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(consumer, new ResourceLocation(StoneyCore.MOD_ID, "iron_ingot_from_campfire"));
    }
}