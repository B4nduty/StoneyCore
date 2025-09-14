package banduty.stoneycore.items.armor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface SCAccessoryItem {

    // ---- CLIENT SIDE ----
    @Environment(EnvType.CLIENT)
    default ModelBundle getModels(ItemStack stack) {
        return ModelBundle.EMPTY;
    }

    @Environment(EnvType.CLIENT)
    Identifier getTexturePath(ItemStack stack);

    @Environment(EnvType.CLIENT)
    default RenderSettings getRenderSettings(ItemStack stack) {
        return RenderSettings.DEFAULT;
    }

    // ---- SERVER/LOGIC ----
    default boolean hasOpenVisor(ItemStack stack) {
        return false;
    }

    // ---- SUPPORTING TYPES ----
    record ModelBundle(
            Optional<BipedEntityModel<LivingEntity>> base,
            Optional<BipedEntityModel<LivingEntity>> visorOpen,
            Optional<BipedEntityModel<LivingEntity>> firstPerson
    ) {
        public static final ModelBundle EMPTY = new ModelBundle(Optional.empty(), Optional.empty(), Optional.empty());

        public static ModelBundle ofBase(BipedEntityModel<LivingEntity> base) {
            return new ModelBundle(Optional.of(base), Optional.empty(), Optional.empty());
        }

        public static ModelBundle ofBaseAndVisor(BipedEntityModel<LivingEntity> base, BipedEntityModel<LivingEntity> visor) {
            return new ModelBundle(Optional.of(base), Optional.of(visor), Optional.empty());
        }

        public static ModelBundle ofBaseAndFirstPerson(BipedEntityModel<LivingEntity> base, BipedEntityModel<LivingEntity> firstPerson) {
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
