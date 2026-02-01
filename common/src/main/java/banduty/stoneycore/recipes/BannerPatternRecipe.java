package banduty.stoneycore.recipes;

import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.patterns.PatternHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BannerPatternRecipe extends CustomRecipe {
    public BannerPatternRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack banner = ItemStack.EMPTY;
        ItemStack itemInput = ItemStack.EMPTY;
        int count = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            count++;

            if (stack.getItem() instanceof BannerItem) banner = stack;
            else itemInput = stack;
        }

        if (count != 2 || banner.isEmpty() || itemInput.isEmpty()) return false;

        final ItemStack finalItemInput = itemInput;

        return level.getRecipeManager()
                .getAllRecipesFor(Services.PLATFORM.getBannerRecipeType())
                .stream()
                .anyMatch(recipe -> {
                    for (Ingredient ingredient : recipe.getIngredients()) {
                        if (ingredient.test(finalItemInput)) return true;
                    }
                    return false;
                });
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registry) {
        ItemStack banner = ItemStack.EMPTY;
        ItemStack armor = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof BannerItem) banner = stack;
            else armor = stack;
        }

        ItemStack result = armor.copy();
        List<Tuple<ResourceLocation, DyeColor>> patterns = getBannerPatterns(banner, result.getItem());
        PatternHelper.setBannerPatterns(result, patterns);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Services.PLATFORM.getBannerRecipeSerializer();
    }

    public static List<Tuple<ResourceLocation, DyeColor>> getBannerPatterns(ItemStack banner, Item armor) {
        CompoundTag nbt = banner.getTag();
        if (banner.isEmpty() || !(banner.getItem() instanceof BannerItem) || nbt == null) return List.of();

        CompoundTag blockEntityTag = nbt.getCompound(INBTKeys.BLOCK_ENTITY_TAG);
        if (!blockEntityTag.contains(INBTKeys.PATTERNS)) return List.of();

        ResourceLocation armorId = BuiltInRegistries.ITEM.getKey(armor);
        ListTag patternList = blockEntityTag.getList(INBTKeys.PATTERNS, CompoundTag.TAG_COMPOUND);

        List<Tuple<ResourceLocation, DyeColor>> patterns = new ArrayList<>();
        for (int i = 0; i < patternList.size(); i++) {
            CompoundTag patternTag = patternList.getCompound(i);
            String pattern = NBTDataHelper.get(patternTag, INBTKeys.PATTERN, "");
            int colorId = NBTDataHelper.get(patternTag, INBTKeys.COLOR, 0);

            ResourceLocation patternId = new ResourceLocation(
                    armorId.getNamespace(),
                    "textures/banner_pattern/" + armorId.getPath() + "/" + pattern + ".png"
            );
            patterns.add(new Tuple<>(patternId, DyeColor.byId(colorId)));
        }
        return patterns;
    }
}