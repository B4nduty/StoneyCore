package banduty.stoneycore.smithing;

import banduty.stoneycore.StoneyCore;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface ModRecipes {

    DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, StoneyCore.MOD_ID);
    DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, StoneyCore.MOD_ID);

    RegistryObject<RecipeType<AnvilRecipe>> ANVIL_RECIPE_TYPE =
            RECIPE_TYPES.register("craftman_anvil_crafting", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "craftman_anvil_crafting";
                }
            });

    RegistryObject<RecipeSerializer<AnvilRecipe>> ANVIL_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("craftman_anvil_crafting", () -> AnvilRecipeSerializer.INSTANCE);

    static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        StoneyCore.LOG.info("Registered ModRecipes for " + StoneyCore.MOD_ID);
    }
}
