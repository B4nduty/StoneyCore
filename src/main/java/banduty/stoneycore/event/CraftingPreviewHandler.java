package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.CraftingPreviewCallback;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
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

        inventory.getInputStacks().stream()
                .filter(stack -> stack.getItem() instanceof BannerItem)
                .findFirst()
                .ifPresent(banner -> {
                    List<Pair<Identifier, DyeColor>> patterns = getBannerPatterns(banner, original.getItem());
                    PatternHelper.setBannerPatterns(original, patterns);
                });

        return original;
    }

    public static List<Pair<Identifier, DyeColor>> getBannerPatterns(ItemStack banner, Item armor) {
        NbtCompound nbt = banner.getNbt();
        if (banner.isEmpty() || !(banner.getItem() instanceof BannerItem) || nbt == null) return List.of();

        NbtCompound blockEntityTag = nbt.getCompound(INBTKeys.BLOCK_ENTITY_TAG);
        if (!blockEntityTag.contains(INBTKeys.PATTERNS)) return List.of();

        Identifier armorId = Registries.ITEM.getId(armor);
        NbtList patternList = blockEntityTag.getList(INBTKeys.PATTERNS, NbtCompound.COMPOUND_TYPE);

        List<Pair<Identifier, DyeColor>> patterns = new ArrayList<>();
        for (int i = 0; i < patternList.size(); i++) {
            NbtCompound patternTag = patternList.getCompound(i);
            String pattern = NBTDataHelper.get(patternTag, INBTKeys.PATTERN, "");
            int colorId = NBTDataHelper.get(patternTag, INBTKeys.COLOR, 0);

            Identifier patternId = new Identifier(
                    armorId.getNamespace(),
                    "textures/banner_pattern/" + armorId.getPath() + "/" + pattern + ".png"
            );
            patterns.add(new Pair<>(patternId, DyeColor.byId(colorId)));
        }
        return patterns;
    }
}