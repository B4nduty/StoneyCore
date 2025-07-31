
package banduty.stoneycore.items.armor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface SCAccessoryItem {
    @Environment(EnvType.CLIENT)
    BipedEntityModel<LivingEntity> getModel(ItemStack itemStack);

    @Environment(EnvType.CLIENT)
    default BipedEntityModel<LivingEntity> getFirstPersonModel(ItemStack itemStack) {
        return null;
    }

    Identifier getTexturePath(ItemStack itemStack);

    default boolean hasOverlay() {
        return false;
    }

    default boolean hasCustomAngles(ItemStack stack) {
        return false;
    }

    default boolean shouldNotRenderOnHeadInFirstPerson(){return false;};
}
