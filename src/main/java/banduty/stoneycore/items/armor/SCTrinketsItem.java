
package banduty.stoneycore.items.armor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

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

    default boolean hasOverlay() {
        return false;
    }

    default boolean hasCustomAngles() {
        return false;
    }
}
