package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.CraftingPreviewCallback;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.patterns.PatternHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CraftingPreviewHandler implements CraftingPreviewCallback {
    @Override
    public ItemStack modifyResult(ServerPlayer player, CraftingContainer inventory, ItemStack original) {
        if (!original.is(SCTags.BANNER_COMPATIBLE.getTag())) return original;

        inventory.getItems().stream()
                .filter(stack -> stack.getItem() instanceof BannerItem)
                .findFirst()
                .ifPresent(banner -> {
                    List<Tuple<ResourceLocation, DyeColor>> patterns = getBannerPatterns(banner, original.getItem());
                    PatternHelper.setBannerPatterns(original, patterns);
                });

        return original;
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