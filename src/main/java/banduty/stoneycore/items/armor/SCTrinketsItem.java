
package banduty.stoneycore.items.armor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public interface SCTrinketsItem {
    double armor();

    double toughness();

    double hungerDrainAddition();

    @Environment(EnvType.CLIENT)
    BipedEntityModel<LivingEntity> getModel();

    @Environment(EnvType.CLIENT)
    default BipedEntityModel<LivingEntity> getFirstPersonModel() {
        return null;
    }

    Identifier getTexturePath();

    default boolean isDyeable() {
        return false;
    }

    default boolean isDyeableWithOverlay() {
        return false;
    }

    default int getDefaultColor() {
        return 0;
    }

    default boolean unrenderCapeFeature() {
        return false;
    }

    default void setBannerPatterns(ItemStack stack, List<Identifier> patterns) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList patternList = new NbtList();
        for (Identifier pattern : patterns) {
            patternList.add(NbtString.of(pattern.toString()));
        }
        nbt.put("BannerPatterns", patternList);
    }

    default List<Identifier> getBannerPatterns(ItemStack stack) {
        List<Identifier> patterns = new ArrayList<>();
        if (stack.hasNbt() && stack.getNbt().contains("BannerPatterns")) {
            NbtList patternList = stack.getNbt().getList("BannerPatterns", NbtString.STRING_TYPE);
            for (int i = 0; i < patternList.size(); i++) {
                patterns.add(Identifier.tryParse(patternList.getString(i)));
            }
        }
        return patterns;
    }
}
