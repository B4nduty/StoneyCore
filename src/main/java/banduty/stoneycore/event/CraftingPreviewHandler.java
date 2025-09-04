package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.CraftingPreviewCallback;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.patterns.PatternHelper;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class CraftingPreviewHandler implements CraftingPreviewCallback {
    @Override
    public ItemStack modifyResult(ServerPlayerEntity player, RecipeInputInventory inventory, ItemStack original) {
        if (!original.isIn(SCTags.BANNER_COMPATIBLE.getTag())) return original;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack ingredient = inventory.getStack(i);
            if (ingredient.getItem() instanceof BannerItem) {
                List<Pair<Identifier, DyeColor>> bannerPatterns = getBannerPatterns(ingredient, original.getItem());
                PatternHelper.setBannerPatterns(original, bannerPatterns);
                break;
            }
        }
        return original;
    }


    private static List<Pair<Identifier, DyeColor>> getBannerPatterns(ItemStack bannerStack, Item armor) {
        List<Pair<Identifier, DyeColor>> patterns = new ArrayList<>();

        if (!bannerStack.isEmpty() && bannerStack.getItem() instanceof BannerItem) {
            NbtCompound nbt = bannerStack.getNbt();
            if (nbt != null && nbt.contains("BlockEntityTag")) {
                NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
                if (blockEntityTag.contains("Patterns")) {
                    NbtList patternList = blockEntityTag.getList("Patterns", NbtCompound.COMPOUND_TYPE);
                    for (int i = 0; i < patternList.size(); i++) {
                        NbtCompound patternTag = patternList.getCompound(i);
                        String pattern = patternTag.getString("Pattern");
                        int colorId = patternTag.getInt("Color");
                        DyeColor color = DyeColor.byId(colorId);

                        Identifier itemId = Registries.ITEM.getId(armor);
                        Identifier patternId = new Identifier(
                                itemId.getNamespace(),
                                "textures/banner_pattern/" + itemId.getPath() + "/" + pattern + ".png"
                        );

                        patterns.add(new Pair<>(patternId, color));
                    }
                }
            }
        }
        return patterns;
    }

}
