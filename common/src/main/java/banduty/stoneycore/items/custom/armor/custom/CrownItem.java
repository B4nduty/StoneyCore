package banduty.stoneycore.items.custom.armor.custom;

import banduty.stoneycore.client.item.CrownAttachmentRenderer;
import banduty.stoneycore.client.render.ArmorAttachmentRenderProvider;
import banduty.stoneycore.client.render.ArmorAttachmentRenderer;
import banduty.stoneycore.items.ModArmorMaterials;
import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static banduty.stoneycore.util.SCInventoryItemFinder.findUnderArmor;

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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack target = findUnderArmor(player, type);
        if (!target.isEmpty() && target.getItem() instanceof SCUnderArmor) {
            return ArmorAttachment.super.use(level, player, hand, ArmorItem.Type.HELMET);
        } else return super.use(level, player, hand);
    }
}