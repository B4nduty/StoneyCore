package banduty.stoneycore.compat.jei;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import banduty.stoneycore.recipes.StackIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class CraftmanAnvilCategoryJEI implements IRecipeCategory<CraftmanAnvilRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "craftman_anvil");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/gui/craftman_anvil_gui.png");

    public static final RecipeType<CraftmanAnvilRecipe> CRAFTMAN_ANVIL_TYPE =
            new RecipeType<>(UID, CraftmanAnvilRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CraftmanAnvilCategoryJEI(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 175, 82);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(SCBlocks.CRAFTMAN_ANVIL.get().asItem()));
    }

    @Override
    public @NotNull RecipeType<CraftmanAnvilRecipe> getRecipeType() {
        return CRAFTMAN_ANVIL_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Craftsman's Anvil");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CraftmanAnvilRecipe recipe, IFocusGroup focuses) {
        int[] inputSlotsX = {36, 54, 72, 36, 54, 72};
        int[] inputSlotsY = {11, 11, 11, 29, 29, 29};

        if (recipe.pattern().isPresent()) {
            List<Optional<StackIngredient>> slots = recipe.pattern().get().slots();

            for (int i = 0; i < slots.size(); i++) {
                Optional<StackIngredient> ingredient = slots.get(i);

                if (ingredient.isEmpty()) continue;

                builder.addSlot(RecipeIngredientRole.INPUT, inputSlotsX[i], inputSlotsY[i])
                        .addItemStacks(ingredient.get().asItemStacks());
            }
        } else {
            List<StackIngredient> ingredients = recipe.ingredients();
            int inputSize = Math.min(ingredients.size(), 6);

            for (int i = 0; i < inputSize; i++) {
                StackIngredient ing = ingredients.get(i);
                int addX = 0;
                int addY = 0;

                if (inputSize <= 3) addY = -9;
                if (inputSize == 1 || inputSize == 2) addX = 18;
                else if (inputSize == 5 && (i == 3 || i == 4)) addX = 9;

                builder.addSlot(RecipeIngredientRole.INPUT, inputSlotsX[i] + addX, inputSlotsY[i] + addY)
                        .addItemStacks(ing.asItemStacks());
            }
        }

        ItemStack output = recipe.output();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 20).addItemStack(output);
    }

    @Override
    public void draw(CraftmanAnvilRecipe recipe, IRecipeSlotsView slots, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.pose().pushPose();

        guiGraphics.drawString(Minecraft.getInstance().font, "Hits: " + recipe.hitTimes(), 10, 42, 0xFFFFFF, true);

        if (recipe.chance() < 1f) {
            guiGraphics.drawString(Minecraft.getInstance().font, String.format("Chance: %.1f%%", recipe.chance() * 100), 90, 42, 0xFFFFFF, true);
        }

        guiGraphics.pose().popPose();
    }
}
