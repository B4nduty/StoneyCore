package banduty.stoneycore.items.custom.armor.custom;

import banduty.stoneycore.client.item.CrownAttachmentRenderer;
import banduty.stoneycore.client.render.ArmorAttachmentRenderProvider;
import banduty.stoneycore.client.render.ArmorAttachmentRenderer;
import banduty.stoneycore.items.ModArmorMaterials;
import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import net.minecraft.world.item.ArmorItem;

public class CrownItem extends ArmorItem implements ArmorAttachment, ArmorAttachmentRenderProvider {
    public CrownItem(Properties properties) {
        super(ModArmorMaterials.CROWN, ArmorItem.Type.HELMET, properties);
    }

    private ArmorAttachmentRenderer cachedRenderer;

    @Override
    public ArmorAttachmentRenderer getRenderer() {
        if (this.cachedRenderer == null) {
            this.cachedRenderer = new CrownAttachmentRenderer();
        }
        return this.cachedRenderer;
    }


}