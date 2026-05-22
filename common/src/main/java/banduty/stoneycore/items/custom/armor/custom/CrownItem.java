package banduty.stoneycore.items.custom.armor.custom;

import banduty.stoneycore.client.item.CrownAccessoryRenderer;
import banduty.stoneycore.client.render.AccessoryRenderProvider;
import banduty.stoneycore.client.render.AccessoryRenderer;
import banduty.stoneycore.items.ModArmorMaterials;
import banduty.stoneycore.items.custom.armor.SCAccessory;
import net.minecraft.world.item.ArmorItem;
import org.jetbrains.annotations.NotNull;

public class CrownItem extends ArmorItem implements SCAccessory, AccessoryRenderProvider {
    public CrownItem(Properties properties) {
        super(ModArmorMaterials.CROWN, ArmorItem.Type.HELMET, properties);
    }

    private AccessoryRenderer cachedRenderer;

    @Override
    public AccessoryRenderer getRenderer() {
        if (this.cachedRenderer == null) {
            this.cachedRenderer = new CrownAccessoryRenderer();
        }
        return this.cachedRenderer;
    }

    @Override
    public @NotNull Type getArmorSlot() {
        return Type.HELMET;
    }

    @Override
    public int numberSlot() {
        return 1;
    }
}