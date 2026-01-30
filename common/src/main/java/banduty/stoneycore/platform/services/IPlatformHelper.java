package banduty.stoneycore.platform.services;

import banduty.stoneycore.config.ConfigImpl;
import banduty.stoneycore.lands.util.ClaimWorker;
import banduty.stoneycore.recipes.BannerPatternRecipe;
import banduty.stoneycore.recipes.ManuscriptCraftingRecipe;
import banduty.stoneycore.recipes.AnvilRecipe;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Queue;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    ItemStack getCraftingRemainingItem(ItemStack stack);

    void sendTitle(ServerPlayer player, Component mainTitle);

    BakedModel getBakedModel(ModelManager modelManager, ResourceLocation iModelPath);

    Queue<ClaimWorker> getClaimTasks();

    int comboCount(Player player);

    ItemStack getWeaponStack(Entity attacker, ItemStack defaultStack);

    Attribute getAttackRange();
    Attribute getReach();

    Block getCraftmanAnvil();
    RecipeType<AnvilRecipe> getCraftmanAnvilRecipe();
    RecipeSerializer<AnvilRecipe> getCraftmanAnvilRecipeSerializer();

    RecipeSerializer<ManuscriptCraftingRecipe> getManuscriptRecipeSerializer();
    RecipeSerializer<BannerPatternRecipe> getBannerRecipeSerializer();

    ConfigImpl getConfig();

    List<ItemStack> getEquippedAccessories(LivingEntity livingEntity);
}