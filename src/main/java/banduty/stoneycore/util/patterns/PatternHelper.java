package banduty.stoneycore.util.patterns;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PatternHelper {
    public static void setBannerPatterns(ItemStack stack, List<Pair<Identifier, DyeColor>> patterns) {
        NbtCompound nbt = stack.getOrCreateNbt();

        NbtList patternList = new NbtList();
        for (Pair<Identifier, DyeColor> pattern : patterns) {
            patternList.add(NbtString.of(pattern.getLeft().toString()));
        }
        nbt.put("BannerPatterns", patternList);

        NbtList colorList = new NbtList();
        for (Pair<Identifier, DyeColor> pattern : patterns) {
            float[] components = pattern.getRight().getColorComponents();
            NbtCompound colorTag = new NbtCompound();
            colorTag.putFloat("R", components[0]);
            colorTag.putFloat("G", components[1]);
            colorTag.putFloat("B", components[2]);
            colorList.add(colorTag);
        }
        nbt.put("BannerColors", colorList);
    }

    public static List<Pair<Identifier, DyeColor>> getBannerPatterns(ItemStack stack) {
        List<Pair<Identifier, DyeColor>> patterns = new ArrayList<>();
        if (stack.getNbt() != null && stack.getNbt().contains("BannerPatterns")) {
            NbtList patternList = stack.getNbt().getList("BannerPatterns", NbtString.STRING_TYPE);
            NbtList colorList = stack.getNbt().getList("BannerColors", NbtCompound.COMPOUND_TYPE);

            for (int i = 0; i < patternList.size(); i++) {
                Identifier patternId = Identifier.tryParse(patternList.getString(i));

                NbtCompound colorTag = colorList.getCompound(i);
                float r = colorTag.getFloat("R");
                float g = colorTag.getFloat("G");
                float b = colorTag.getFloat("B");

                DyeColor dyeColor = findClosestDyeColor(r, g, b);

                patterns.add(new Pair<>(patternId, dyeColor));
            }
        }
        return patterns;
    }

    private static DyeColor findClosestDyeColor(float r, float g, float b) {
        DyeColor closestColor = DyeColor.WHITE;
        float minDistance = Float.MAX_VALUE;

        for (DyeColor color : DyeColor.values()) {
            float[] components = color.getColorComponents();
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
        NbtCompound nbt = stack.getOrCreateNbt();
        float[] components = dyeColor.getColorComponents();
        nbt.putFloat("dyeColorR", components[0]);
        nbt.putFloat("dyeColorG", components[1]);
        nbt.putFloat("dyeColorB", components[2]);
    }

    public static float[] getBannerDyeColor(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("dyeColorR")) return new float[]{1, 1, 1};
        return new float[] {
                nbt.getFloat("dyeColorR"),
                nbt.getFloat("dyeColorG"),
                nbt.getFloat("dyeColorB")
        };
    }
}