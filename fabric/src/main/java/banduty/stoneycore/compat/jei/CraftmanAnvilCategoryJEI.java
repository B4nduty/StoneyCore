package banduty.stoneycore.compat.jei;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.manuscript.Manuscript;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.smithing.AnvilRecipe;
import banduty.stoneycore.smithing.StackIngredient;
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
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CraftmanAnvilCategoryJEI implements IRecipeCategory<AnvilRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(StoneyCore.MOD_ID, "craftman_anvil");
    public static final ResourceLocation TEXTURE = new ResourceLocation(StoneyCore.MOD_ID, "textures/gui/craftman_anvil_gui.png");

    public static final RecipeType<AnvilRecipe> CRAFTMAN_ANVIL_TYPE =
            new RecipeType<>(UID, AnvilRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CraftmanAnvilCategoryJEI(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 175, 82);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(Services.PLATFORM.getCraftmanAnvil().asItem()));
    }

    @Override
    public @NotNull RecipeType<AnvilRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, AnvilRecipe recipe, IFocusGroup focuses) {
        var ingredients = recipe.ingredients();
        int inputSize = Math.min(ingredients.size(), 6);

        int[] inputSlotsX = {54, 36, 72, 54, 36, 72};
        int[] inputSlotsY = {29, 29, 29, 11, 11, 11};

        for (int i = 0; i < inputSize; i++) {
            StackIngredient ing = ingredients.get(i);
            int addX = 0;
            int addY = 0;

            if (inputSize <= 3) addY = -9;
            if (inputSize == 1 || inputSize == 2) addX = 18;
            else if (inputSize == 5 && (i == 3 || i == 4)) addX = 9;

            if (ing.tag() != null) {
                Ingredient tagIngredient = Ingredient.of(ing.tag());
                List<ItemStack> stacks = Arrays.stream(tagIngredient.getItems())
                        .map(ItemStack::copy)
                        .collect(Collectors.toList());

                builder.addSlot(RecipeIngredientRole.INPUT, inputSlotsX[i] + addX, inputSlotsY[i] + addY)
                        .addItemStacks(stacks);
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, inputSlotsX[i] + addX, inputSlotsY[i] + addY)
                        .addIngredients(VanillaTypes.ITEM_STACK, ing.asItemStacks());
            }
        }
        
        ItemStack output = recipe.output();

        ItemStack target = HotIron.getTargetStack(output);
        if (target.isEmpty()) target = Manuscript.getTargetStack(output);
        if (target.isEmpty()) target = output;

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(target);
    }

    @Override
    public void draw(AnvilRecipe recipe, IRecipeSlotsView slots, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.pose().pushPose();

        // Hit times
        guiGraphics.drawString(Minecraft.getInstance().font, "Hits: " + recipe.hitTimes(), 10, 42, 0xFFFFFF, true);

        // Chance
        if (recipe.chance() < 1f) {
            guiGraphics.drawString(Minecraft.getInstance().font, String.format("Chance: %.1f%%", recipe.chance() * 100), 90, 42, 0xFFFFFF, true);
        }

        ItemStack output = recipe.output();

        ItemStack target = HotIron.getTargetStack(output);
        if (target.isEmpty()) target = Manuscript.getTargetStack(output);

        if (!target.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(128, 28, 0);
            float scale = 0.5f;
            guiGraphics.pose().scale(scale, scale, scale);
            guiGraphics.renderItem(target, 0, 0);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, target, 0, 0);
            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(120, 20, 0);
        guiGraphics.renderItem(output, 0, 0);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, output, 0, 0);
        guiGraphics.pose().popPose();

        guiGraphics.pose().popPose();
    }
}
