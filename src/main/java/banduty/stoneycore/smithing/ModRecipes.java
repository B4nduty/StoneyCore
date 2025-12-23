package banduty.stoneycore.smithing;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {
    public static void registerRecipes() {
        StoneyCore.LOGGER.info("Registering ModRecipes for " + StoneyCore.MOD_ID);
    }

    public static final RecipeType<AnvilRecipe> ANVIL_RECIPE_TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            new ResourceLocation(StoneyCore.MOD_ID, "craftman_anvil_crafting"),
            new RecipeType<AnvilRecipe>() {
                public String toString() {
                    return "craftman_anvil_crafting";
                }
            }
    );

    public static final RecipeSerializer<AnvilRecipe> ANVIL_RECIPE_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            new ResourceLocation(StoneyCore.MOD_ID, "craftman_anvil_crafting"),
            AnvilRecipeSerializer.INSTANCE
    );
}
