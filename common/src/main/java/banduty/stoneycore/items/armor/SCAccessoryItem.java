package banduty.stoneycore.items.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface SCAccessoryItem {

    // ---- CLIENT SIDE ----
    default ModelBundle getModels(ItemStack stack) {
        return ModelBundle.EMPTY;
    }

    ResourceLocation getTexturePath(ItemStack stack);

    default Optional<ResourceLocation> getEmissiveTexturePath(ItemStack stack) {
        return Optional.empty();
    }

    default RenderSettings getRenderSettings(ItemStack stack) {
        return RenderSettings.DEFAULT;
    }

    // ---- SERVER/LOGIC ----
    default boolean hasOpenVisor(ItemStack stack) {
        return false;
    }

    // ---- SUPPORTING TYPES ----
    record ModelBundle(
            Optional<HumanoidModel<LivingEntity>> base,
            Optional<HumanoidModel<LivingEntity>> visorOpen,
            Optional<HumanoidModel<LivingEntity>> firstPerson
    ) {
        public static final ModelBundle EMPTY = new ModelBundle(Optional.empty(), Optional.empty(), Optional.empty());

        public static ModelBundle ofBase(HumanoidModel<LivingEntity> base) {
            return new ModelBundle(Optional.of(base), Optional.empty(), Optional.empty());
        }

        public static ModelBundle ofBaseAndVisor(HumanoidModel<LivingEntity> base, HumanoidModel<LivingEntity> visor) {
            return new ModelBundle(Optional.of(base), Optional.of(visor), Optional.empty());
        }

        public static ModelBundle ofBaseAndFirstPerson(HumanoidModel<LivingEntity> base, HumanoidModel<LivingEntity> firstPerson) {
            return new ModelBundle(Optional.of(base), Optional.empty(), Optional.of(firstPerson));
        }
    }

    record RenderSettings(boolean overlay, boolean customAngles, boolean hideHeadInFirstPerson) {
        public static final RenderSettings DEFAULT = new RenderSettings(false, false, false);
        public static final RenderSettings OVERLAY_ONLY = new RenderSettings(true, false, false);
        public static final RenderSettings CUSTOM_ANGLES_ONLY = new RenderSettings(false, true, false);
        public static final RenderSettings HIDE_HEAD_ONLY = new RenderSettings(false, false, true);
        public static final RenderSettings OVERLAY_AND_HIDE_HEAD = new RenderSettings(true, false, true);
        public static final RenderSettings OVERLAY_AND_CUSTOM_ANGLES = new RenderSettings(true, true, false);
        public static final RenderSettings HIDE_HEAD_AND_CUSTOM_ANGLES = new RenderSettings(false, true, true);
        public static final RenderSettings FULL = new RenderSettings(true, true, true);
    }
}
