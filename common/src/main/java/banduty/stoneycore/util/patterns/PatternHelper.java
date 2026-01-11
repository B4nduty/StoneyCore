package banduty.stoneycore.util.patterns;

import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PatternHelper {
    public static void setBannerPatterns(ItemStack stack, List<Tuple<ResourceLocation, DyeColor>> patterns) {
        CompoundTag tag = stack.getOrCreateTag();

        ListTag patternList = new ListTag();
        for (Tuple<ResourceLocation, DyeColor> pattern : patterns) {
            patternList.add(StringTag.valueOf(pattern.getA().toString()));
        }
        tag.put("BannerPatterns", patternList);

        ListTag colorList = new ListTag();
        for (Tuple<ResourceLocation, DyeColor> pattern : patterns) {
            float[] components = pattern.getB().getTextureDiffuseColors();
            CompoundTag colorTag = new CompoundTag();
            colorTag.putFloat("R", components[0]);
            colorTag.putFloat("G", components[1]);
            colorTag.putFloat("B", components[2]);
            colorList.add(colorTag);
        }
        tag.put("BannerColors", colorList);
    }

    public static List<Tuple<ResourceLocation, DyeColor>> getBannerPatterns(ItemStack stack) {
        List<Tuple<ResourceLocation, DyeColor>> patterns = new ArrayList<>();
        if (stack.getTag() != null && stack.getTag().contains("BannerPatterns")) {
            ListTag patternList = stack.getTag().getList("BannerPatterns", Tag.TAG_STRING);
            ListTag colorList = stack.getTag().getList("BannerColors", Tag.TAG_COMPOUND);

            for (int i = 0; i < patternList.size(); i++) {
                ResourceLocation patternId = ResourceLocation.tryParse(patternList.getString(i));

                CompoundTag colorTag = colorList.getCompound(i);
                float r = colorTag.getFloat("R");
                float g = colorTag.getFloat("G");
                float b = colorTag.getFloat("B");

                DyeColor dyeColor = findClosestDyeColor(r, g, b);

                if (patternId == null) continue;

                patterns.add(new Tuple<>(patternId, dyeColor));
            }
        }
        return patterns;
    }

    private static DyeColor findClosestDyeColor(float r, float g, float b) {
        DyeColor closestColor = DyeColor.WHITE;
        float minDistance = Float.MAX_VALUE;

        for (DyeColor color : DyeColor.values()) {
            float[] components = color.getTextureDiffuseColors();
            float distance = colorDistance(r, g, b, components[0], components[1], components[2]);
            if (distance < minDistance) {
                minDistance = distance;
                closestColor = color;
            }
        }

        return closestColor;
    }

    private static float colorDistance(float r1, float g1, float b1, float r2, float g2, float b2) {
        return (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);
    }

    public static void setBannerDyeColor(ItemStack stack, DyeColor dyeColor) {
        float[] components = dyeColor.getTextureDiffuseColors();
        NBTDataHelper.set(stack, INBTKeys.DYE_COLOR_R, components[0]);
        NBTDataHelper.set(stack, INBTKeys.DYE_COLOR_G, components[1]);
        NBTDataHelper.set(stack, INBTKeys.DYE_COLOR_B, components[2]);
    }

    public static float[] getBannerDyeColor(ItemStack stack) {
        return new float[] {
                NBTDataHelper.get(stack, INBTKeys.DYE_COLOR_R, 1.0f),
                NBTDataHelper.get(stack, INBTKeys.DYE_COLOR_G, 1.0f),
                NBTDataHelper.get(stack, INBTKeys.DYE_COLOR_B, 1.0f)
        };
    }
}