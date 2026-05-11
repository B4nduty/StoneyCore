package banduty.stoneycore.util.patterns;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.ArrayList;
import java.util.List;

public class PatternHelper {
    public static void setBannerPatterns(ItemStack stack, HolderLookup.Provider registries, List<Tuple<ResourceLocation, DyeColor>> patterns) {
        HolderLookup.RegistryLookup<BannerPattern> lookup = registries.lookupOrThrow(Registries.BANNER_PATTERN);
        BannerPatternLayers.Builder builder = new BannerPatternLayers.Builder();

        for (Tuple<ResourceLocation, DyeColor> pattern : patterns) {
            ResourceKey<BannerPattern> key = ResourceKey.create(Registries.BANNER_PATTERN, pattern.getA());

            lookup.get(key).ifPresent(holder -> builder.add(holder, pattern.getB()));
        }

        stack.set(DataComponents.BANNER_PATTERNS, builder.build());
    }

    public static List<Tuple<ResourceLocation, DyeColor>> getBannerPatterns(ItemStack stack) {
        List<Tuple<ResourceLocation, DyeColor>> patterns = new ArrayList<>();

        BannerPatternLayers component = stack.get(DataComponents.BANNER_PATTERNS);

        if (component != null && !component.layers().isEmpty()) {
            for (BannerPatternLayers.Layer layer : component.layers()) {

                ResourceLocation patternId = layer.pattern().unwrapKey()
                        .map(ResourceKey::location)
                        .orElse(ResourceLocation.fromNamespaceAndPath("minecraft", "base"));

                DyeColor dyeColor = layer.color();

                patterns.add(new Tuple<>(patternId, dyeColor));
            }
        }

        return patterns;
    }

    private static DyeColor findClosestDyeColor(float r, float g, float b) {
        DyeColor closestColor = DyeColor.WHITE;
        float minDistance = Float.MAX_VALUE;

        for (DyeColor color : DyeColor.values()) {
            int colorInt = color.getTextureDiffuseColor();
            float cr = (float) (colorInt >> 16 & 255) / 255.0F;
            float cg = (float) (colorInt >> 8 & 255) / 255.0F;
            float cb = (float) (colorInt & 255) / 255.0F;

            float distance = colorDistance(r, g, b, cr, cg, cb);
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
        stack.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(dyeColor.getTextureDiffuseColor(), true));
    }

    public static float[] getBannerDyeColor(ItemStack stack) {
        net.minecraft.world.item.component.DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);
        if (dyedColor == null) return new float[]{1.0f, 1.0f, 1.0f};

        int color = dyedColor.rgb();
        return new float[]{
                (float) (color >> 16 & 255) / 255.0F,
                (float) (color >> 8 & 255) / 255.0F,
                (float) (color & 255) / 255.0F
        };
    }

    public static boolean hasBannerPatterns(ItemStack stack) {
        BannerPatternLayers layers = stack.get(DataComponents.BANNER_PATTERNS);
        return layers != null && !layers.layers().isEmpty();
    }

    public static List<Tuple<ResourceLocation, DyeColor>> getBannerPatternsFromBanner(ItemStack bannerStack, Item armor) {
        List<Tuple<ResourceLocation, DyeColor>> patterns = new ArrayList<>();

        if (bannerStack.isEmpty() || !(bannerStack.getItem() instanceof BannerItem)) return patterns;

        BannerPatternLayers layers = bannerStack.get(DataComponents.BANNER_PATTERNS);
        if (layers == null || layers.layers().isEmpty()) return patterns;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(armor);

        for (BannerPatternLayers.Layer layer : layers.layers()) {
            String patternName = layer.pattern().unwrapKey()
                    .map(key -> key.location().getPath())
                    .orElse("base");

            DyeColor color = layer.color();

            ResourceLocation patternId = ResourceLocation.fromNamespaceAndPath(
                    itemId.getNamespace(),
                    "textures/banner_pattern/" + itemId.getPath() + "/" + patternName + ".png"
            );

            patterns.add(new Tuple<>(patternId, color));
        }
        return patterns;
    }
}